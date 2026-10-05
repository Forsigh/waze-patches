package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

@Suppress("unused")
val enableEnforcementAlertsPatch = bytecodePatch(
    name = "Enable police & enforcement alerts",
    description = "Turns on Waze's police/enforcement alert system.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    execute {
        BooleanConfigGetterFingerprint.method.addInstructions(
            0,
            forceBoolean(
                "enf", true,
                "CONFIG_VALUE_ALERTS_ENABLE_ENFORCEMENT_ALERTS",
            ),
        )
    }
}
