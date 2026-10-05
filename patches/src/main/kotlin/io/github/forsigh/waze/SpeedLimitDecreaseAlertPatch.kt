package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

@Suppress("unused")
val speedLimitDecreaseAlertPatch = bytecodePatch(
    name = "Speed-limit decrease warnings",
    description = "Enables warnings when the speed limit drops, with an extended pre-alert distance.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    execute {
        BooleanConfigGetterFingerprint.method.addInstructions(
            0,
            forceBoolean(
                "dec", true,
                "CONFIG_VALUE_NOTIFICATIONS_ON_ROUTE_SPEED_LIMIT_DECREASE",
            ),
        )
        LongConfigGetterFingerprint.method.addInstructions(
            0,
            forceLong(
                "dec",
                "CONFIG_VALUE_ALERTS_SPEED_LIMIT_DECREASE_ALERT_DISTANCE_METERS" to 1000L,
            ),
        )
    }
}
