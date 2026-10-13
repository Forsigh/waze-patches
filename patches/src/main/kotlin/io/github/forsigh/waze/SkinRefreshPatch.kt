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
 * DOM, the same way the media-visibility patch declares `<queries>`), while the skin rewrite next to it is a
 * `rawResourcePatch` working on APK files. A patch is typed to exactly one context, so this half cannot live
 * inside that one. It has no name, so it is not a row: `Map theme` declares it as a dependency, and it is
 * applied whenever that row is ticked. Nothing else depends on it, so a selection without `Map theme` never
 * bumps the version.
 *
 * THE NUMBER: a fixed target above every build we support, advanced in steps - not "current + 1". That way
 * the bump is idempotent across selections and re-patching *without* `Map theme` also lands higher than the
 * previously patched build, so Waze re-extracts and the stock skins come back: a theme can be removed.
 * The build's own number is never lowered.
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
            val next = maxOf(REFRESHED_VERSION_CODE, current + REFRESHED_STEP)
            manifest.setAttribute("android:versionCode", next.toString())
            bumped = next - current
        }
        println("skinRefreshPatch: versionCode advanced by $bumped so Waze re-extracts its skins")
    }
}
