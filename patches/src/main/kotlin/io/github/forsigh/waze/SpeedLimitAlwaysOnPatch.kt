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
            )
        // The Boolean above only turns the feature on. WHEN the sign is drawn comes from this
        // String key: "no", "yes" (only when over the limit) or "always" - com/waze/bw/d reads it
        // with equals("always") plus a ("always", "yes", "1") membership test. Without it the sign
        // still disappears whenever you are under the limit, which is not "always shown".
        recordString(
                "sls",
                "CONFIG_VALUE_MAP_SPEEDOMETER_SPEED_LIMIT_USER_ENABLED" to "always",
            )
    }
}
