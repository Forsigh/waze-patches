package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

@Suppress("unused")
val speedometerAlwaysOnPatch = bytecodePatch(
    name = "Speedometer always on",
    description = "Keeps the speedometer visible at any speed, including while stopped.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    execute {
        // Show the speedometer at all (Boolean) ...
        recordBoolean(
                "spd", true,
                "CONFIG_VALUE_MAP_SHOW_SPEEDOMETER",
            )
        // ... and drop the minimum speed gate to zero (Long).
        recordLong(
                "spd",
                "CONFIG_VALUE_MAP_SPEEDOMETER_MIN_SPEED_KPH" to 0L,
            )
    }
}
