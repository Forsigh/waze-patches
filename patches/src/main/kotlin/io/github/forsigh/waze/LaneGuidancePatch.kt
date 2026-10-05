package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

@Suppress("unused")
val laneGuidancePatch = bytecodePatch(
    name = "Lane guidance",
    description = "Enables lane guidance, including continue-straight hints.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    execute {
        BooleanConfigGetterFingerprint.method.addInstructions(
            0,
            forceBoolean(
                "lan", true,
                "CONFIG_VALUE_LANE_GUIDANCE_ENABLED",
                "CONFIG_VALUE_LANE_GUIDANCE_CONTINUE_STRAIGHT_ENABLED",
            ),
        )
    }
}
