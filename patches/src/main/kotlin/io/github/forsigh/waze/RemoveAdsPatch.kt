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

    // The emitter patch is what actually writes the keys recorded below. Depending on it means this
    // patch can be ticked alone and still produce a build where the override exists — without the
    // dependency the key is collected and never written, which looks exactly like a working patch.
    dependsOn(configOverridesPatch)

    execute {
        // All Boolean (com/waze/config/b) keys, so must hook the Boolean getter.
        recordBoolean(
                "ads", false,
                "CONFIG_VALUE_AD_EVENTS_FEATURE_ENABLED",
                "CONFIG_VALUE_ADS_AI_BADGE_ENABLED",
                "CONFIG_VALUE_ADS_INVENTORY_PREDICTION_ENABLED",
                "CONFIG_VALUE_ADS_ALLOW_PROFILE_TARGETING",
                "CONFIG_VALUE_ADS_ALLOW_PROFILE_TARGETING_DEFAULT",
            )
    }
}
