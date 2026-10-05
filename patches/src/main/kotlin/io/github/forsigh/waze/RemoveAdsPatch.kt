package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Forces Waze's advertising config values off.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    execute {
        // All Boolean (com/waze/config/b) keys, so must hook the Boolean getter.
        BooleanConfigGetterFingerprint.method.addInstructions(
            0,
            forceBoolean(
                "ads", false,
                "CONFIG_VALUE_AD_EVENTS_FEATURE_ENABLED",
                "CONFIG_VALUE_ADS_AI_BADGE_ENABLED",
                "CONFIG_VALUE_ADS_INVENTORY_PREDICTION_ENABLED",
                "CONFIG_VALUE_ADS_ALLOW_PROFILE_TARGETING",
                "CONFIG_VALUE_ADS_ALLOW_PROFILE_TARGETING_DEFAULT",
            ),
        )
    }
}
