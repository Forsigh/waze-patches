package io.github.forsigh.waze

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE
import kotlin.math.roundToInt

/**
 * OLED dark mode: the night map's background becomes true black and the roads get slightly brighter, so
 * an OLED screen's dark pixels are actually off. A plain, patch-time resource edit - select it in Morphe
 * and the skin descriptions inside the APK change; leave it unticked and they stay stock. No injected
 * code, no runtime switch, no companion app.
 *
 * MEASURED ON 5.25.0.1: the night map is drawn from `assets/res/skins/default/skin_values.night.lua`
 * (with `editor.night` and `experiment/` siblings). Two details matter:
 *   - the map canvas colour is the `Palette.map_background` entry (`rgb(0x272E3A)` as shipped), and
 *   - every road type is its own `Palette` entry (`freeways`, `primary`, `secondary`, `highways`,
 *     `street`, `private`, `trails4x4`, `alleys`, `ramps`) which the lower half of the file reads back
 *     through `color.lighten`/`color.darken`, so raising a palette value lifts the whole road family
 *     consistently.
 * Only the `night` files are touched; day mode, labels, traffic and route colours stay as Waze ships them.
 *
 * WHY THE VERSION CODE IS BUMPED: Waze draws skins it has already extracted and only re-extracts them
 * when the app's versionCode rises - the same reason `Map theme` depends on `SkinRefreshPatch`, which this
 * patch pulls in too.
 *
 * AND IF `Map theme` IS ALSO SELECTED: this patch steps aside for it. The theme rewrites every entry in
 * the same files, so stacking would only fight over the result; the files carry the theme's marker
 * comment and this patch leaves marked files untouched. Pick one of the two.
 */
private const val SKIN_DIR = "assets/res/skins/default"

/** Both the plain and the `experiment/` skin copies ship in the APK; Waze loads one of them. */
private val SKIN_VARIANTS = listOf("", "experiment/")

/** The night descriptions: the driving map and the map editor, each with its own schema. */
private val NIGHT_MODES = listOf("night", "editor.night")

/** True black, for the OLED background. */
private const val OLED_BLACK = "000000"

/**
 * How much brighter the roads get: each channel is moved this fraction of the way toward white,
 * matching the `color.lighten(0.1, ...)` step Waze itself uses inside the same file. "A bit", not a
 * redesign - this single number is the tuning knob.
 */
private const val ROAD_LIFT = 0.10

/** The road palette entries, in the order the file itself groups them. */
private val ROAD_KEYS = listOf(
    "freeways", "primary", "secondary", "highways", "street", "private", "trails4x4", "alleys", "ramps",
)

/** The marker `Map theme` leaves behind; when present, the theme owns this file. */
private const val THEME_MARKER = "Forsigh/waze-patches: map theme applied"

/** One `Palette` entry: leading whitespace, the key, `=`, an `rgb(0xRRGGBB)` value. */
private fun entry(key: String) = Regex(
    "(?m)(^\\h*${Regex.escape(key)}\\h*=\\h*)rgba?\\(\\h*0x([0-9A-Fa-f]{6})\\h*\\)",
)

/** Move each channel of a hex colour `ROAD_LIFT` of the way toward white (Waze's lighten semantics). */
private fun lift(hex: String): String {
    val value = hex.toInt(16)
    val channels = intArrayOf((value shr 16) and 0xFF, (value shr 8) and 0xFF, value and 0xFF)
    val lifted = channels.map { channel -> (channel + ROAD_LIFT * (255 - channel)).roundToInt() }
    return "%06X".format((lifted[0] shl 16) or (lifted[1] shl 8) or lifted[2])
}

@Suppress("unused")
val oledDarkModePatch = rawResourcePatch(
    name = "OLED dark mode",
    description = "Makes the night map truly dark for OLED screens: the map background becomes pure " +
        "black (#000000) and the road colours are lifted slightly (one Waze lighten step) so they stay " +
        "readable against it. Only the background and the road palette change - day mode, labels, " +
        "traffic and routes are left as Waze ships them. Applied at patch time: no runtime switch and " +
        "nothing injected into the app. Steps aside if Map theme is also selected.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)
    // The skin rewrite and the versionCode refresh are two halves of one feature and two different patch
    // kinds (raw file writes vs the decoded manifest), so the refresh is an unnamed dependency - which
    // also keeps it out of selections that do not want it.
    dependsOn(skinRefreshPatch)

    execute {
        var files = 0
        var lifted = 0
        var skipped = 0
        for (variant in SKIN_VARIANTS) {
            for (mode in NIGHT_MODES) {
                val path = "$SKIN_DIR/${variant}skin_values.$mode.lua"
                val file = get(path)
                if (!file.exists()) {
                    // The experiment/ copies are not guaranteed to be there; the plain ones are.
                    if (variant.isEmpty()) {
                        throw PatchException(
                            "oledDarkModePatch: Waze has no $path - refusing to report a change that was not applied",
                        )
                    }
                    continue
                }
                val source = file.readText()
                if (source.contains(THEME_MARKER)) {
                    println("oledDarkModePatch: $path already carries the Map theme; stepping aside")
                    skipped++
                    continue
                }

                val background = entry("map_background")
                if (background.findAll(source).count() != 1) {
                    throw PatchException("oledDarkModePatch: expected exactly one map_background entry in $path")
                }
                var out = background.replace(source) { "${it.groupValues[1]}rgb(0x$OLED_BLACK)" }

                var changed = 0
                for (key in ROAD_KEYS) {
                    val matches = entry(key).findAll(out).toList()
                    if (matches.isEmpty()) {
                        println("oledDarkModePatch: no '$key' palette entry in $path (skipped)")
                        continue
                    }
                    if (matches.size != 1) {
                        throw PatchException("oledDarkModePatch: expected one '$key' entry in $path, found ${matches.size}")
                    }
                    val match = matches.single()
                    out = out.replaceRange(match.range, "${match.groupValues[1]}rgb(0x${lift(match.groupValues[2])})")
                    changed++
                }
                if (changed < ROAD_KEYS.size - 1) {
                    throw PatchException(
                        "oledDarkModePatch: only $changed of ${ROAD_KEYS.size} road entries found in $path - Waze may have changed",
                    )
                }

                file.writeText(out)
                files++
                lifted += changed
                println("oledDarkModePatch: $path - background #000000, $changed road colour(s) lifted")
            }
        }
        if (files == 0) {
            if (skipped > 0) {
                println("oledDarkModePatch: nothing to do - Map theme owns every night skin")
            } else {
                throw PatchException("oledDarkModePatch: no night skin file was rewritten")
            }
        } else {
            println(
                "oledDarkModePatch: $files night skin file(s) rewritten, $lifted road colour(s) lifted; " +
                    "versionCode refreshed for re-extraction",
            )
        }
    }
}