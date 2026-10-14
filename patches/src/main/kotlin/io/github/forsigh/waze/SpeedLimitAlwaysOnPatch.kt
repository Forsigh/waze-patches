package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

@Suppress("unused")
val speedLimitAlwaysOnPatch = bytecodePatch(
    name = "Speed limit sign always shown",
    description = "Always shows the speed limit sign.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    // The emitter patch is what actually writes the keys recorded below. Depending on it means this
    // patch can be ticked alone and still produce a build where the override exists — without the
    // dependency the key is collected and never written, which looks exactly like a working patch.
    dependsOn(configOverridesPatch)

    execute {
        recordBoolean(
                "sls", true,
                "CONFIG_VALUE_MAP_SPEEDOMETER_SPEED_LIMIT_ENABLED",
                "CONFIG_VALUE_MAP_SPEEDOMETER_SPEED_LIMIT_OVERRIDE_ENABLED",
            )
    }
}
