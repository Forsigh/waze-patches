package io.github.forsigh.waze

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Black UI background - repaints Waze's dark app chrome to pure #000000.
 *
 * The app's screens (bottom sheet, menus, dialogs, bars) take their dark surfaces from a small
 * set of named colours in `res/values/colors.xml`. On the live app the dominant one measures
 * #202124 - exactly `Grey900`, which is the leaf behind Waze's own `background_default`
 * (night), `background_default_negative`, `always_dark_background_default` and the night ETA
 * bar drawable; the rest are the AppCompat / Material `background`/`surface` sets that paint
 * floating windows and dialogs. This patch rewrites those leaves to #000000, so every surface
 * that aliases them lands on true black in one step.
 *
 * The edit runs through the patcher's resource pipeline: with this patch selected the patcher
 * decodes resources, this row edits the decoded `res/values/colors.xml` document, and the
 * incremental encoder writes only the changed entries back into the resource table - the rest
 * of the table is reused byte-for-byte. (No asset files are touched, so the build stays in the
 * same pack-safety class as the code-only patches.)
 *
 * Scope: dark-mode backgrounds only - the light theme stays as Waze built it; text, icons,
 * buttons and the map keep their colours. Day/night map palettes are the Theme row's job, not
 * this one's.
 */
private val BLACK_UI_COLORS = mapOf(
    // Waze's own dark surface family (the measured #202124 sheet + ETA bar + dark defaults).
    "Grey900" to "#000000",
    // AppCompat dark themes: window background and floating windows (dialogs/popups).
    "material_grey_850" to "#000000",
    "material_grey_800" to "#000000",
    "material_grey_900" to "#000000",
    // Google design library dark background/surface (#121212).
    "design_dark_default_color_background" to "#000000",
    "design_dark_default_color_surface" to "#000000",
    // Material 3 dark background/surface/dim - one shared leaf.
    "m3_ref_palette_neutral6" to "#000000",
)

@Suppress("unused")
val blackUiPatch = resourcePatch(
    name = "UI AMOLED dark mode",
    description = "Dark-mode backgrounds go pure black.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    execute {
        var edited = 0
        var checked = 0
        for (path in listOf("res/values/colors.xml", "res/values-night/colors.xml")) {
            val doc = try {
                document(path)
            } catch (e: Exception) {
                println("blackUiPatch: $path not present in this build - skipping")
                continue
            }
            checked++
            doc.use { d ->
                val nodes = d.getElementsByTagName("color")
                for (i in 0 until nodes.length) {
                    val element = nodes.item(i) ?: continue
                    val name = element.attributes?.getNamedItem("name")?.nodeValue ?: continue
                    val target = BLACK_UI_COLORS[name] ?: continue
                    if (element.textContent.trim() != target) {
                        element.textContent = target
                        println("blackUiPatch: $path: $name -> $target")
                        edited++
                    }
                }
            }
        }
        if (edited == 0) {
            throw PatchException(
                "blackUiPatch: none of the expected UI background colours were found in $checked decoded file(s) - refusing to install an edit that would do nothing",
            )
        }
        println("blackUiPatch: $edited colour value(s) repainted to #000000")
    }
}
