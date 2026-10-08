package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

@Suppress("unused")
val policeAlertDistancePatch = bytecodePatch(
    name = "Extended police & hazard alert distances",
    description = "Increases the pre-alert distances for police enforcement, accidents and heavy traffic.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    execute {
        // All Long (com/waze/config/c) keys, so must hook the Long getter.
        recordLong(
                "dst",
                "CONFIG_VALUE_ALERTS_ENFORCEMENT_ALERTS_DISTANCE_STREETS" to 700L,
                "CONFIG_VALUE_ALERTS_ENFORCEMENT_ALERTS_DISTANCE_HIGHWAY" to 900L,
                "CONFIG_VALUE_ALERTS_ENFORCEMENT_ALERTS_DISTANCE_FREEWAY" to 1200L,
                "CONFIG_VALUE_ALERTS_ACCIDENT_ALERT_DISTANCE_METERS" to 2000L,
                "CONFIG_VALUE_ALERTS_HEAVY_TRAFFIC_ALERT_DISTANCE_METERS" to 3000L,
            )
    }
}
