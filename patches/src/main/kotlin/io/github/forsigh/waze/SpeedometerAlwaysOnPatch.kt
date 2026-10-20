package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

@Suppress("unused")
val speedometerAlwaysOnPatch = bytecodePatch(
    name = "Speedometer always on",
    description = "Visible at any speed.",
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
        // com/waze/bw/f hides the speedometer outright while SHOW_SPEEDOMETER_AFTER_NUM_SESSIONS
        // is below GENERAL_SESSION_NUMBER (it forces min speed to -1, so the zero above is never
        // reached). A value above any real session count keeps that gate open on a fresh install.
        recordLong(
                "spd",
                "CONFIG_VALUE_MAP_SHOW_SPEEDOMETER_AFTER_NUM_SESSIONS" to 2_147_483_647L,
            )
    }
}
