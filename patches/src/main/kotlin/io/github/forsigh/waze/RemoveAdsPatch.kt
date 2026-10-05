package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Config keys that gate Waze's advertising. Forcing each to 0 disables the corresponding feature.
 * These mirror the keys the "Magical Unicorn" Waze MOD sets to 0 in its preferences payload.
 *
 * ASSUMPTION: these entries are read through the numeric (Long) config accessor. An entry exposed
 * through a different accessor (e.g. a boolean or string getter) is simply not matched here.
 * Confirm on device.
 */
private val AD_CONFIG_KEYS = listOf(
    "CONFIG_VALUE_AD_EVENTS_FEATURE_ENABLED",
    "CONFIG_VALUE_ADS_INVENTORY_PREDICTION_ENABLED",
    "CONFIG_VALUE_ADS_ALLOW_PROFILE_TARGETING",
    "CONFIG_VALUE_ADS_ALLOW_PROFILE_TARGETING_DEFAULT",
)

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Forces Waze's advertising config values off.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    execute {
        // Prepended to the numeric config getter: if the requested ConfigValue is one of the ad
        // keys, return 0 instead of reading the stored value. Label prefix "ads_" keeps the labels
        // unique from other patches that inject into this same method.
        val smali = buildString {
            AD_CONFIG_KEYS.forEachIndexed { i, key ->
                append("sget-object v0, Lcom/waze/config/ConfigValues;->$key:Lcom/waze/config/c;\n")
                append("if-eq p0, v0, :ads_force$i\n")
            }
            append("goto :ads_skip\n")
            AD_CONFIG_KEYS.forEachIndexed { i, _ ->
                append(":ads_force$i\n")
                append("const-wide/16 v0, 0x0\n")
                append("invoke-static {v0, v1}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;\n")
                append("move-result-object v0\n")
                append("return-object v0\n")
            }
            append(":ads_skip\n")
        }
        NumericConfigGetterFingerprint.method.addInstructions(0, smali)
    }
}
