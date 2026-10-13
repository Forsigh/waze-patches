package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

@Suppress("unused")
val speedometerAlwaysOnPatch = bytecodePatch(
    name = "Speedometer always on",
    description = "The speedometer stays on screen at any speed, including when stopped. " +
        "Waze hides it below its own minimum speed otherwise.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    // The emitter patch is what actually writes the keys recorded below. Depending on it means this
    // patch can be ticked alone and still produce a build where the override exists — without the
    // dependency the key is collected and never written, which looks exactly like a working patch.
    dependsOn(configOverridesPatch)

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
