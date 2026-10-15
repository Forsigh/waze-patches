package io.github.forsigh.waze

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Speed-limit sign style (opt-in, default off).
 *
 * `CONFIG_VALUE_MAP_SPEEDOMETER_STYLE` (read in com.waze.bv.f, com.waze.bv.h and
 * com.waze.settings.gj) decides which sign is drawn beside the speedometer. Waze picks one by
 * country; this forces it: "us" = white rectangle, "world" = red circle.
 */
@Suppress("unused")
val speedLimitSignPatch = bytecodePatch(
    name = "Speed-limit sign style",
    description = "Picks the sign beside the speedometer: US or World.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    dependsOn(configOverridesPatch)

    val style by stringOption(
        "style", "us",
        mapOf(
            "US (white rectangle)" to "us",
            "World (red circle)" to "world",
        ),
        title = "Sign style",
        description = "Waze picks one by country; this forces it.",
    )

    execute {
        val chosen = style ?: "us"
        recordString("sls", "CONFIG_VALUE_MAP_SPEEDOMETER_STYLE" to chosen)
        println("speedLimitSignPatch: sign style = $chosen")
    }
}
