package io.github.forsigh.waze

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import app.morphe.patcher.util.smali.ExternalLabel
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE
import io.github.forsigh.waze.WazeConstants.MORPHE_YOUTUBE_MUSIC
import io.github.forsigh.waze.WazeConstants.OFFICIAL_YOUTUBE_MUSIC

/**
 * Point Waze's audio player at a Morphe YouTube Music build, in place of the official one.
 *
 * Waze drives audio partners through its "Audiokit"/NavConnect integration and ships no YouTube Music
 * package string of its own: the partner roster (app id, title, icon, privacy URL, consent config) is
 * delivered as protobuf by Waze's servers, and the only audio packages hardcoded in the app are
 * Spotify's. A Morphe YouTube Music build therefore has to be installed as one of the packages Waze
 * will look for - it asks `PackageManager` for a launch intent by package name, and a package it does
 * not know is treated as "partner not installed".
 *
 * This patch does not fight the roster. Every entry the UI shows - the picker, the settings row, and
 * the row model behind the in-app music button - is built in ONE method,
 * `com/waze/navigate/el;->J(Lcom/waze/jni/protos/nav_connect/NavConnectPartnerAppInfo;)Lcom/waze/nav_connect/a/e;`,
 * from exactly four reads of the roster message:
 *
 *     getPartnerAppId()   -> the package Waze will launch
 *     getTitle()          -> the label the user reads
 *     getIconData()       -> the icon
 *     getPrivacyPolicyUrl()
 *
 * `J` is called from three list-building paths in `el` (the available partners, the connected partner,
 * and the ISV/consent variants), so rewriting the package there reaches every surface at once while
 * leaving the genuine icon, privacy URL and consent config in place. The label is rewritten too, keyed
 * on the package actually being used, so the row reads as the Morphe build rather than claiming to be
 * the official one.
 *
 * Two launch sites are still patched directly, as a fallback for any path that hands a package
 * straight to `PackageManager` without going through the roster model:
 *  - `com/waze/sdk/ap;->c(Ljava/lang/String;)V` - SDK launch path.
 *  - `com/waze/main_screen/b/c/e;->b(...)` - the main-screen music button, which resolves the launch
 *    intent from `com/waze/nav_connect/b/h;->c()` (the row's package).
 *
 * Limits, stated plainly: this makes Waze resolve and launch your build, and makes the row say so.
 * Whether Waze's Audiokit then completes its handshake with a re-signed, possibly renamed build is
 * not settled by static analysis - that needs the device, as does the shape of the runtime roster.
 */
@Suppress("unused")
val audioPartnerRemapPatch = bytecodePatch(
    name = "Wire Morphe YouTube Music into Waze",
    description = "Replaces the official YouTube Music entry with your Morphe YouTube Music: the " +
        "package Waze launches and the row label, at the single point every audio-partner list entry " +
        "is built from - plus both launch paths as a fallback.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    val officialPackage by stringOption(
        "officialPackage", OFFICIAL_YOUTUBE_MUSIC,
        title = "Official YouTube Music package",
        description = "The package Waze asks for. Requests matching this prefix are rewritten.",
    )

    val targetPackage by stringOption(
        "targetPackage", MORPHE_YOUTUBE_MUSIC,
        title = "Your Morphe YouTube Music package",
        description = "The package your Morphe YouTube Music is installed as. " +
            "Morphe Manager shows it on the app's patch screen.",
    )

    val rowTitle by stringOption(
        "rowTitle", "Morphe YouTube Music",
        title = "Row label in Waze's list",
        description = "What the list entry reads once it points at your build. Set it back to " +
            "\"YouTube Music\" to leave the label as Waze ships it.",
    )

    execute {
        val official = officialPackage ?: OFFICIAL_YOUTUBE_MUSIC
        val target = targetPackage ?: MORPHE_YOUTUBE_MUSIC
        val title = rowTitle ?: "Morphe YouTube Music"

        var sites = 0

        // 1. The roster entry itself: the choke point every picker/settings/list row is built from.
        if (PartnerEntryFingerprint.matchOrNull() != null) {
            val method = PartnerEntryFingerprint.method
            val instructions = method.implementation!!.instructions.toList()
            val idResult = resultIndexAfter(instructions, "getPartnerAppId")
            val titleResult = resultIndexAfter(instructions, "getTitle")

            if (idResult != null && titleResult != null) {
                // Insert the LATER site first: adding instructions shifts every index after it, and the
                // external label binds to an instruction object rather than an index, so the earlier
                // site's index and target both stay correct.
                val titleAt = titleResult + 1
                method.addInstructionsWithLabels(
                    titleAt,
                    relabelBlock("apr_roster_title", "v5", "v6", target, title),
                    ExternalLabel("apr_roster_title", instructions[titleAt]),
                )
                val idAt = idResult + 1
                method.addInstructionsWithLabels(
                    idAt,
                    remapBlock("apr_roster_id", "v5", official, target),
                    ExternalLabel("apr_roster_id", instructions[idAt]),
                )
                sites++
            } else {
                println("audioPartnerRemapPatch: roster entry matched but its reads did not - skipped")
            }
        } else {
            println("audioPartnerRemapPatch: no roster entry build site in this build - skipped")
        }

        // 2. SDK launch path: rewrite the parameter before it reaches PackageManager.
        if (AudioPartnerLaunchFingerprint.matchOrNull() != null) {
            val method = AudioPartnerLaunchFingerprint.method
            // The label is bound to the instruction that currently sits at the injection point, so
            // it lands after the injected block no matter how the insert shifts things.
            val after = method.implementation!!.instructions.first()
            method.addInstructionsWithLabels(
                0,
                remapBlock("apr_audio", "p1", official, target),
                ExternalLabel("apr_audio", after),
            )
            sites++
        }

        // 3. Music button path: the package sits in p3 by the time the PackageManager is consulted;
        // injecting in front of that call keeps us past the app's own `if-eqz p3` null check.
        if (MusicButtonLaunchFingerprint.matchOrNull() != null) {
            val method = MusicButtonLaunchFingerprint.method
            val insertAt = MusicButtonLaunchFingerprint.instructionMatches[1].index
            val after = method.implementation!!.instructions.elementAt(insertAt)
            method.addInstructionsWithLabels(
                insertAt,
                remapBlock("apr_button", "p3", official, target),
                ExternalLabel("apr_button", after),
            )
            sites++
        }

        println("audioPartnerRemapPatch: patched $sites site(s) - $official -> $target (row: $title)")
    }
}

/**
 * Smali that rewrites [packageRegister] when it names the official package, skipping out to the
 * external label [label] when it does not (or when the package is null).
 *
 * The label is NOT defined in this string: it is passed as an `ExternalLabel` bound to the
 * instruction that follows the injection point. Inlining the label here was tried first and the
 * patcher bound it to the block's *first* instruction instead of its last, so a non-matching
 * package branched backwards into its own block - an infinite loop on every other audio partner.
 */
private fun remapBlock(label: String, packageRegister: String, official: String, target: String): String =
    "    if-eqz $packageRegister, :$label\n" +
        "    const-string v0, \"$official\"\n" +
        "    invoke-virtual { $packageRegister, v0 }, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z\n" +
        "    move-result v0\n" +
        "    if-eqz v0, :$label\n" +
        "    const-string $packageRegister, \"$target\"\n"

/**
 * Index of the `move-result-object` that consumes the call to `NavConnectPartnerAppInfo;->[callName]()`
 * in [instructions], or null when either the call or its result is absent.
 */
private fun resultIndexAfter(instructions: List<Instruction>, callName: String): Int? {
    val call = instructions.indexOfFirst { instruction ->
        val reference = (instruction as? ReferenceInstruction)?.reference
        reference is MethodReference &&
            reference.definingClass == "Lcom/waze/jni/protos/nav_connect/NavConnectPartnerAppInfo;" &&
            reference.name == callName
    }
    if (call < 0) return null
    val result = instructions.drop(call).indexOfFirst { it.opcode == Opcode.MOVE_RESULT_OBJECT }
    return if (result < 0) null else call + result
}

/**
 * Smali that gives [titleRegister] the label [newTitle] when [packageRegister] holds [target] - i.e.
 * only for the entry this patch has just pointed at the Morphe build, so every other partner keeps its
 * own name. Same external-label contract as [remapBlock].
 */
private fun relabelBlock(
    label: String,
    packageRegister: String,
    titleRegister: String,
    target: String,
    newTitle: String,
): String =
    "    if-eqz $packageRegister, :$label\n" +
        "    if-eqz $titleRegister, :$label\n" +
        "    const-string v0, \"$target\"\n" +
        "    invoke-virtual { $packageRegister, v0 }, Ljava/lang/String;->equals(Ljava/lang/Object;)Z\n" +
        "    move-result v0\n" +
        "    if-eqz v0, :$label\n" +
        "    const-string $titleRegister, \"$newTitle\"\n"

/**
 * `com/waze/navigate/el;->J(...)` - builds the UI entry model from one roster message. Matched on its
 * shape rather than its obfuscated name: class + parameter + return type + the two reads it rewrites.
 */
private object PartnerEntryFingerprint : Fingerprint(
    definingClass = "Lcom/waze/navigate/el;",
    parameters = listOf("Lcom/waze/jni/protos/nav_connect/NavConnectPartnerAppInfo;"),
    returnType = "Lcom/waze/nav_connect/a/e;",
    filters = listOf(
        methodCall(
            definingClass = "Lcom/waze/jni/protos/nav_connect/NavConnectPartnerAppInfo;",
            name = "getPartnerAppId",
            returnType = "Ljava/lang/String;",
        ),
        methodCall(
            definingClass = "Lcom/waze/jni/protos/nav_connect/NavConnectPartnerAppInfo;",
            name = "getTitle",
            returnType = "Ljava/lang/String;",
        ),
    ),
)

/** `com/waze/sdk/ap;->c(Ljava/lang/String;)V` - launch path that hands a package to PackageManager. */
private object AudioPartnerLaunchFingerprint : Fingerprint(
    definingClass = "Lcom/waze/sdk/ap;",
    name = "c",
    parameters = listOf("Ljava/lang/String;"),
    returnType = "V",
    filters = listOf(
        methodCall(
            definingClass = "Landroid/content/pm/PackageManager;",
            name = "getLaunchIntentForPackage",
            returnType = "Landroid/content/Intent;",
        ),
    ),
)

/** `com/waze/main_screen/b/c/e;->b(...)` - the music button's resolve-and-launch lambda. */
private object MusicButtonLaunchFingerprint : Fingerprint(
    definingClass = "Lcom/waze/main_screen/b/c/e;",
    name = "b",
    returnType = "Lh/ab;",
    filters = listOf(
        methodCall(
            definingClass = "Lcom/waze/nav_connect/b/h;",
            name = "c",
            returnType = "Ljava/lang/String;",
        ),
        // The app's own null check on the package sits between these two calls; the injections go
        // in front of the PackageManager lookup, i.e. past that check.
        methodCall(
            definingClass = "Landroid/content/Context;",
            name = "getPackageManager",
            returnType = "Landroid/content/pm/PackageManager;",
        ),
        methodCall(
            definingClass = "Landroid/content/pm/PackageManager;",
            name = "getLaunchIntentForPackage",
            returnType = "Landroid/content/Intent;",
        ),
    ),
)
