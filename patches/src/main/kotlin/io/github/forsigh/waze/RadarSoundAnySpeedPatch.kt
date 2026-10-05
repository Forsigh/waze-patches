package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

@Suppress("unused")
val radarSoundAnySpeedPatch = bytecodePatch(
    name = "Radar sound at any speed",
    description = "Plays speed-camera sound alerts even when not above the limit.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    execute {
        BooleanConfigGetterFingerprint.method.addInstructions(
            0,
            forceBoolean(
                "rsd", true,
                "CONFIG_VALUE_ALERTS_PLAY_SPEED_CAMERA_SOUND_BELOW_SPEED_LIMIT",
            ),
        )
    }
}
