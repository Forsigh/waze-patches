package io.github.forsigh.waze

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE
import kotlin.math.roundToInt

/**
 * AMOLED dark mode: Waze's own night map becomes an AMOLED map - the canvas goes true black and the
 * road colours are lifted so they keep their contrast against it. A patch-time resource edit on the
 * skin descriptions inside the APK: select it in Morphe and the stock night skins change; leave it
 * unticked and they stay as Waze ships them. No runtime switch, no companion app, nothing injected.
 *
 * WHAT IT CHANGES, MEASURED (assets/res/skins/default/, all four night files: night, editor.night and
 * each `experiment/` sibling):
 *   - `Palette.map_background` `rgb(0x272E3A)` -> `rgb(0x000000)` - the map canvas.
 *   - the nine road palette entries (freeways, primary, secondary, highways, street, private, trails4x4,
 *     alleys, ramps) lifted 15% of the way to white; each channel moves as `c + f * (255 - c)`, the
 *     same semantics as the `color.lighten(...)` calls the file itself uses.
 *
 * Two facts from the earlier OLED attempt (removed in 0.28.0, rebuilt here) shape this design:
 *   - `map_background` is not only the canvas: the render tree references it ~20 times as the
 *     light/medium/strong stroke of Private, Parking, Alleys, Pedestrian, Trails and Walkway (and
 *     Street's strong stroke). Setting it black deliberately blackens those strokes with it - they are
 *     the "eraser" outlines that must match the canvas, so they follow the same value instead of being
 *     overridden. That cascade is expected, not a bug.
 *   - the night palette is dark-on-dark by design; against a pure-black canvas the stock road values
 *     (street etc. `#3F5871`) lose most of their contrast, which is why the lift is 15% rather than the
 *     10% the first attempt used - the same idea with enough headroom to stay readable.
 * Only the night files are touched; day mode, labels, traffic and route colours stay stock (the day
 * files are asserted byte-identical by this project's verifier).
 *
 * WHY THE VERSION CODE IS BUMPED: Waze draws skins it has already extracted and only re-extracts them
 * when the app's versionCode rises, so the rewrite rides with `SkinRefreshPatch` (an unnamed dependency).
 */
private const val SKIN_DIR = "assets/res/skins/default"

/** Both the plain and the `experiment/` skin copies ship in the APK; Waze loads one of them. */
private val SKIN_VARIANTS = listOf("", "experiment/")

/** The night descriptions: the driving map and the map editor, each with its own schema. */
private val NIGHT_MODES = listOf("night", "editor.night")

/** True black, for the AMOLED canvas. */
private const val AMOLED_BLACK = "000000"

/**
 * How much brighter the roads get: each channel is moved this fraction of the way toward white,
 * matching the `color.lighten(...)` semantics used inside the same file. "A bit", not a redesign -
 * this single number is the tuning knob.
 */
private const val ROAD_LIFT = 0.15

/** The road palette entries, in the order the file itself groups them. */
private val ROAD_KEYS = listOf(
    "freeways", "primary", "secondary", "highways", "street", "private", "trails4x4", "alleys", "ramps",
)

/** One `Palette` entry: leading whitespace, the key, `=`, an `rgb(0xRRGGBB)` value. */
private fun entry(key: String) = Regex(
    "(?m)(^\\h*${Regex.escape(key)}\\h*=\\h*)rgba?\\(\\h*0x([0-9A-Fa-f]{6})\\h*\\)",
)

/** Move each channel of a hex colour [ROAD_LIFT] of the way toward white (Waze's lighten semantics). */
private fun lift(hex: String): String {
    val value = hex.toInt(16)
    val channels = intArrayOf((value shr 16) and 0xFF, (value shr 8) and 0xFF, value and 0xFF)
    val lifted = channels.map { channel -> (channel + ROAD_LIFT * (255 - channel)).roundToInt() }
    return "%06X".format((lifted[0] shl 16) or (lifted[1] shl 8) or lifted[2])
}

@Suppress("unused")
val amoledDarkModePatch = rawResourcePatch(
    name = "AMOLED dark mode",
    description = "Replaces Waze's stock night map with an AMOLED one: the map canvas becomes true " +
        "black (#000000) and the nine road families are lifted 15% toward white so they keep their " +
        "contrast against it. Only the night skins change - day mode, labels, traffic and route " +
        "colours stay as Waze ships them. Applied at patch time: no runtime switch, nothing injected.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)
    // The skin rewrite and the versionCode refresh are two halves of one feature in two patch kinds
    // (raw asset writes vs the decoded manifest), so the refresh is an unnamed dependency - which also
    // keeps it out of selections that do not need it.
    dependsOn(skinRefreshPatch)

    execute {
        var files = 0
        var lifted = 0
        for (variant in SKIN_VARIANTS) {
            for (mode in NIGHT_MODES) {
                val path = "$SKIN_DIR/${variant}skin_values.$mode.lua"
                val file = get(path)
                if (!file.exists()) {
                    // The experiment/ copies are not guaranteed to be there; the plain ones are.
                    if (variant.isEmpty()) {
                        throw PatchException(
                            "amoledDarkModePatch: Waze has no $path - refusing to report a change that was not applied",
                        )
                    }
                    continue
                }
                val source = file.readText()

                val background = entry("map_background")
                if (background.findAll(source).count() != 1) {
                    throw PatchException("amoledDarkModePatch: expected exactly one map_background entry in $path")
                }
                var out = background.replace(source) { "${it.groupValues[1]}rgb(0x$AMOLED_BLACK)" }

                for (key in ROAD_KEYS) {
                    val matches = entry(key).findAll(out).toList()
                    if (matches.size != 1) {
                        throw PatchException(
                            "amoledDarkModePatch: expected one '$key' palette entry in $path, found ${matches.size}",
                        )
                    }
                    val match = matches.single()
                    out = out.replaceRange(match.range, "${match.groupValues[1]}rgb(0x${lift(match.groupValues[2])})")
                    lifted++
                }

                file.writeText(out)
                files++
                println("amoledDarkModePatch: $path - canvas #000000, ${ROAD_KEYS.size} road colour(s) lifted")
            }
        }
        if (files == 0) {
            throw PatchException("amoledDarkModePatch: no night skin file was rewritten")
        }
        println("amoledDarkModePatch: $files night skin file(s) rewritten, $lifted road colour(s) lifted")
    }
}
