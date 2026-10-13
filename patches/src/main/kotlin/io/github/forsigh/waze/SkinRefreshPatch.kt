package io.github.forsigh.waze

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Raise Waze's versionCode so it re-extracts its bundled resources.
 *
 * WHY THIS EXISTS: Waze unpacks its skins into its data directory on first run and keeps drawing those
 * copies. Editing skin files inside the APK therefore changes nothing on a device that has already run
 * Waze; the extracted copies have to be invalidated, and Waze only does that when the app's versionCode
 * increases. This patch is that increment.
 *
 * WHY IT IS A SEPARATE, UNNAMED PATCH: it is a `resourcePatch` (the manifest is edited through the decoded
 * DOM, the same way the Morphe Music declaration adds its `<queries>` entry), so it cannot live inside any
 * single raw- or bytecode feature patch. It has no name, so it is not a row: `Unlock driver moods` declares
 * it as a dependency - mood artwork ships with the skin assets - so the refresh rides along with that
 * default-on row.
 *
 * THE NUMBER: a fixed target above every build we support, advanced in steps - not "current + 1". That way
 * the bump is idempotent across selections, and a re-patch that no longer edits the extracted resources
 * also lands higher than the previously patched build, so Waze re-extracts and the stock resources come
 * back: an edit can be undone. The build's own number is never lowered.
 *
 * COMPOSITION WITH THE PLAY-LOCK: when `Disable Play Store updates` is selected, its `finalize` block
 * writes the versionCode last (after this patch's execute), so the lock always wins regardless of
 * selection order. The guard below keeps a re-patch of an already-locked build from stepping the lock
 * value: the lock's own value advances on every re-patch anyway, which is what keeps the re-extraction
 * working there.
 *
 * Ported from aaayd/waze-custom-patches (GPLv3, `ManifestResources.kt`) - see `upstream-port/CREDITS.txt`.
 * Upstream wrote the bytes of the binary manifest directly, which only works while that file is staged and
 * is not guaranteed once another patch edits the manifest; going through the DOM is the same edit without
 * the ordering dependency.
 */
internal const val REFRESHED_VERSION_CODE = 1030752

/** The manifest is advanced in steps so the number itself is not a claim about a specific Waze build. */
internal const val REFRESHED_STEP = 20

@Suppress("unused")
val skinRefreshPatch = resourcePatch(
    name = null,
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)
    execute {
        var bumped = 0
        document("AndroidManifest.xml").use { dom ->
            val manifest = dom.documentElement
            val current = manifest.getAttribute("android:versionCode").toIntOrNull()
                ?: throw PatchException(
                    "skinRefreshPatch: Waze's manifest carries no numeric android:versionCode, so its " +
                        "extracted skins cannot be refreshed and a theme would never appear"
                )
            if (current >= PLAY_LOCK_BASE) {
                println(
                    "skinRefreshPatch: a Play-lock value is already in place ($current); " +
                        "skins re-extract from the lock's own value change - no bump needed"
                )
            } else {
                val next = maxOf(REFRESHED_VERSION_CODE, current + REFRESHED_STEP)
                manifest.setAttribute("android:versionCode", next.toString())
                bumped = next - current
            }
        }
        if (bumped > 0) {
            println("skinRefreshPatch: versionCode advanced by $bumped so Waze re-extracts its skins")
        }
    }
}
