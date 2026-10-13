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

    // The emitter patch is what actually writes the keys recorded below. Depending on it means this
    // patch can be ticked alone and still produce a build where the override exists — without the
    // dependency the key is collected and never written, which looks exactly like a working patch.
    dependsOn(configOverridesPatch)

    execute {
        recordBoolean(
                "avg", true,
                "CONFIG_VALUE_AVERAGE_SPEED_CAMERA_FEATURE_ENABLED",
                "CONFIG_VALUE_AVERAGE_SPEED_CAMERA_RECOMMENDED_SPEED_ENABLED",
            )
    }
}
