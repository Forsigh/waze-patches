package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

@Suppress("unused")
val averageSpeedCameraPatch = bytecodePatch(
    name = "Average-speed camera alerts",
    description = "Enables average-speed camera alerts and recommended-speed guidance.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    execute {
        BooleanConfigGetterFingerprint.method.addInstructions(
            0,
            forceBoolean(
                "avg", true,
                "CONFIG_VALUE_AVERAGE_SPEED_CAMERA_FEATURE_ENABLED",
                "CONFIG_VALUE_AVERAGE_SPEED_CAMERA_RECOMMENDED_SPEED_ENABLED",
            ),
        )
    }
}
