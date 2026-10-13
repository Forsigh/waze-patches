package io.github.forsigh.waze

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE
import org.w3c.dom.Element

/**
 * Morphe YouTube Music as a Waze audio partner - Loupric's Waze-side pair, ported.
 *
 * THE TWO EDITS (both upstream's, kept exactly):
 *
 *  1. `<queries>` declaration. Waze targets a recent SDK and carries no QUERY_ALL_PACKAGES, so Android's
 *     package visibility hides ordinary media apps from it - a lookup for
 *     `app.morphe.android.apps.youtube.music` answers "no such package" no matter what the code does.
 *     Declaring the package in the manifest's `<queries>` is what makes it visible.
 *  2. `SdkConfiguration.updateAppList()` injection. That method rebuilds `appConfigs`
 *     (`Map<package, com.waze.sdk.au>`) from Waze's native roster and then persists each partner's scope
 *     via `savePackageScopesToSharedPref`. Upstream's block sits right after those calls - where the map
 *     has just been rebuilt and is non-null - checks `containsKey`, and, when the Morphe Music package is
 *     absent, constructs `au("app.morphe.android.apps.youtube.music", "Morphe Music", 0x200001)` through
 *     the five-argument constructor and puts it in. The guard makes the edit idempotent across roster
 *     refreshes.
 *
 * PROVENANCE: ported from Loupric/morphe-waze-music-patch (GPLv3), release v1.0.2, file
 * `AddMorpheToWaze.kt` - see `upstream-port/CREDITS.txt`. The seam (`updateAppList`, marked by its call
 * to `savePackageScopesToSharedPref`), the five-argument constructor, the feature bits `0x200001` and the
 * insertion point are upstream's; this port pins the fingerprint by class name and signature where theirs
 * matched by method name plus a custom predicate, and wraps the same edits in fail-loudly guards.
 *
 * WHY THIS REPLACED OUR OWN ATTEMPT (0.22.0-0.27.0): those builds injected the entry before the scope
 * save with feature bits 0x200000, forced the audio-SDK config flags, edited the `au.g` availability
 * check and offered extra-package options. On-device testing did not produce a working partner, so the
 * machinery was retired and the bundle now ships upstream's exact pair instead - no options, on by
 * default, exactly as upstream lists it. Ported at the owner's explicit direction.
 */

/** The Morphe YouTube Music package name. */
private const val MORPHE_MUSIC_PACKAGE = "app.morphe.android.apps.youtube.music"

/** The label Waze shows for the partner row (upstream's choice, kept). */
private const val MORPHE_MUSIC_LABEL = "Morphe Music"

/**
 * `SdkConfiguration.updateAppList()` - the roster refresh that rebuilds `appConfigs` and persists each
 * partner's scope. Pinned by its own call to `savePackageScopesToSharedPref`, which is what makes it this
 * method and not a namesake.
 */
internal val AppListRefreshFingerprint = Fingerprint(
    definingClass = "Lcom/waze/sdk/SdkConfiguration;",
    name = "updateAppList",
    parameters = listOf(),
    returnType = "V",
    filters = listOf(
        methodCall(
            definingClass = "Lcom/waze/sdk/SdkConfiguration;",
            name = "savePackageScopesToSharedPref",
            returnType = "V",
        ),
    ),
)

/**
 * The manifest half (upstream's `wazeMorpheMusicResourcePatch`): declares the Morphe Music package in
 * Waze's `<queries>`. Unnamed - it is the other half of one feature and is applied as a dependency of
 * [morpheMusicPartnerPatch], so the whole feature is one row.
 */
@Suppress("unused")
val morpheMusicSupportPatch = resourcePatch(
    name = null,
    description = "Declares Morphe YouTube Music (app.morphe.android.apps.youtube.music) in Waze's " +
        "manifest <queries>, so Android's package visibility lets Waze find it at all. Applied as part " +
        "of \"Morphe YouTube Music as a Waze audio partner\".",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    execute {
        document("AndroidManifest.xml").use { dom ->
            val queries = dom.getElementsByTagName("queries").item(0) as? Element
                ?: throw PatchException(
                    "morpheMusicSupportPatch: Waze's manifest has no <queries> node - refusing to " +
                        "report a declaration that was not applied"
                )
            val packages = queries.getElementsByTagName("package")
            val declared = (0 until packages.length).mapNotNull { index ->
                (packages.item(index) as? Element)?.getAttribute("android:name")?.takeIf(String::isNotEmpty)
            }
            if (MORPHE_MUSIC_PACKAGE in declared) {
                println("morpheMusicSupportPatch: $MORPHE_MUSIC_PACKAGE already declared in <queries>")
            } else {
                val element = dom.createElement("package")
                element.setAttribute("android:name", MORPHE_MUSIC_PACKAGE)
                queries.appendChild(element)
                println("morpheMusicSupportPatch: declared $MORPHE_MUSIC_PACKAGE in <queries>")
            }
        }
    }
}

@Suppress("unused")
val morpheMusicPartnerPatch = bytecodePatch(
    name = "Morphe YouTube Music as a Waze audio partner",
    description = "Makes Morphe YouTube Music (app.morphe.android.apps.youtube.music) usable as Waze's " +
        "audio partner: the package is declared in Waze's manifest so Android's package visibility " +
        "lets Waze see it, and it is added to the audio-partner table (SdkConfiguration.appConfigs) " +
        "whenever Waze rebuilds that roster. Ported from Loupric/morphe-waze-music-patch (GPLv3).",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)
    dependsOn(morpheMusicSupportPatch)

    execute {
        val method = AppListRefreshFingerprint.method
        val matches = AppListRefreshFingerprint.instructionMatches
        if (matches.isEmpty()) {
            throw PatchException(
                "morpheMusicPartnerPatch: updateAppList no longer calls savePackageScopesToSharedPref - " +
                    "refusing to inject into a roster refresh that has moved"
            )
        }

        // Upstream's insertion point: right AFTER the last scope-save call, where the rebuilt map is in
        // hand and non-null. Registers v4-v10 are free there; v1 (the monitor) is not touched.
        val insertIndex = matches.last().index + 1
        method.addInstructionsWithLabels(
            insertIndex,
            """
            sget-object v4, Lcom/waze/sdk/SdkConfiguration;->appConfigs:Ljava/util/Map;
            const-string v6, "$MORPHE_MUSIC_PACKAGE"
            invoke-interface {v4, v6}, Ljava/util/Map;->containsKey(Ljava/lang/Object;)Z
            move-result v7
            if-nez v7, :cond_skip_morphe
            new-instance v5, Lcom/waze/sdk/au;
            const-string v7, "$MORPHE_MUSIC_LABEL"
            const v8, 0x200001
            const/4 v9, 0x0
            const/4 v10, 0x0
            invoke-direct/range {v5 .. v10}, Lcom/waze/sdk/au;-><init>(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;[B)V
            invoke-interface {v4, v6, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            :cond_skip_morphe
            nop
            """.trimIndent(),
        )
        println("morpheMusicPartnerPatch: Morphe Music entry injected after the scope save in updateAppList")
    }
}
