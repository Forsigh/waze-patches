package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

@Suppress("unused")
val speedLimitAlwaysOnPatch = bytecodePatch(
    name = "Speed limit sign always shown",
    description = "Forces the speed-limit sign on and enables the manual overrides.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    execute {
        recordBoolean(
                "sls", true,
                "CONFIG_VALUE_MAP_SPEEDOMETER_SPEED_LIMIT_ENABLED",
                "CONFIG_VALUE_MAP_SPEEDOMETER_SPEED_LIMIT_OVERRIDE_ENABLED",
            )
    }
}
