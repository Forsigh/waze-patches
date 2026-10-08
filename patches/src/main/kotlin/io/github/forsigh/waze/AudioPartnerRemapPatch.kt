package io.github.forsigh.waze

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import app.morphe.patcher.util.smali.ExternalLabel
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE
import io.github.forsigh.waze.WazeConstants.MORPHE_YOUTUBE_MUSIC
import io.github.forsigh.waze.WazeConstants.OFFICIAL_YOUTUBE_MUSIC

/**
 * Wire a patched YouTube Music into Waze's in-app audio player.
 *
 * Waze drives audio partners through its "Audiokit"/NavConnect integration. It ships no YouTube
 * Music package string of its own: the partner roster (app id, title, icon, launch package) is
 * delivered as protobuf by Waze's servers, and the only audio packages hardcoded in the app are
 * Spotify's (`com/b/a/a/b/j`). A patched YouTube Music therefore has to be installed as one of the
 * packages Waze will look for - Waze asks `PackageManager` for a launch intent by package name, and
 * a package it does not know is treated as "partner not installed".
 *
 * This patch does not fight the roster. It changes the package Waze *asks for*: at every site that
 * resolves an audio partner's launch intent, a request naming the official YouTube Music package is
 * rewritten to the package you configure. Waze then finds the patched build, treats the partner as
 * installed and launches it.
 *
 * Anchors (verified against Waze 5.24.90.901 / 1030734 and 5.25.0.1 / 1030736):
 *  - `com/waze/sdk/ap;->c(Ljava/lang/String;)V` - SDK launch path; builds the launch intent and
 *    launches the partner app.
 *  - `com/waze/main_screen/b/c/e;->b(...)` - the main-screen music button; resolves the launch
 *    intent from `com/waze/nav_connect/b/h;->c()` (the partner's package) and either launches it or
 *    reports the failure back through `com/waze/nav_connect/b/k;->d`.
 *
 * Limits, stated plainly: this makes Waze find and launch the patched build. Whether Waze's Audiokit
 * then completes its handshake with a re-signed, possibly renamed build is not settled by static
 * analysis - that needs the device.
 */
@Suppress("unused")
val audioPartnerRemapPatch = bytecodePatch(
    name = "Wire patched YouTube Music into Waze",
    description = "Makes Waze's audio player look for your patched YouTube Music instead of the " +
        "official package, so the patched build is detected and launched.",
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
        title = "Your patched YouTube Music package",
        description = "The package your patched YouTube Music is actually installed as. " +
            "Morphe Manager shows it on the app's patch screen.",
    )

    execute {
        val official = officialPackage ?: OFFICIAL_YOUTUBE_MUSIC
        val target = targetPackage ?: MORPHE_YOUTUBE_MUSIC

        var sites = 0

        // SDK launch path: rewrite the parameter before it reaches PackageManager.
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

        // Music button path: the package sits in p3 by the time the PackageManager is consulted;
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

        println("audioPartnerRemapPatch: patched $sites site(s) - $official -> $target")
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
