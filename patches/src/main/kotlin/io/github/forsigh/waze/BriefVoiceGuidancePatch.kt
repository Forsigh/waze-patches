package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

@Suppress("unused")
val briefVoiceGuidancePatch = bytecodePatch(
    name = "Brief voice guidance",
    description = "Enables Waze's brief voice-guidance mode.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    execute {
        recordBoolean(
                "brv", true,
                "CONFIG_VALUE_BRIEF_VOICE_GUIDANCE_MODE_ENABLED",
                "CONFIG_VALUE_BRIEF_VOICE_GUIDANCE_MODE_FEATURE_ENABLED",
            )
    }
}
