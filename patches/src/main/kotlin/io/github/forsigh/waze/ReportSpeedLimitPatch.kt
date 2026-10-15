package io.github.forsigh.waze

import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Report speed limit (opt-in, default off).
 *
 * `CONFIG_VALUE_MAP_SPEEDOMETER_SPEED_LIMIT_OVERRIDE_ENABLED` turns on Waze's own switch for
 * correcting the speed limit from the speedometer. Waze's server keeps it off by default.
 */
@Suppress("unused")
val reportSpeedLimitPatch = bytecodePatch(
    name = "Report speed limit",
    description = "Turns on fixing the speed limit from the speedometer.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    dependsOn(configOverridesPatch)

    execute {
        recordBoolean("rsl", true, "CONFIG_VALUE_MAP_SPEEDOMETER_SPEED_LIMIT_OVERRIDE_ENABLED")
        println("reportSpeedLimitPatch: speed-limit correction switch forced on")
    }
}
