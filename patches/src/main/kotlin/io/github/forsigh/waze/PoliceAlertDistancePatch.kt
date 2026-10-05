package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Extends the pre-alert distances Waze uses for enforcement, accident and traffic reports (metres).
 * Values mirror the distances the "Magical Unicorn" Waze MOD applies.
 *
 * ASSUMPTION: these entries are read through the numeric (Long) config accessor. Confirm on device.
 */
private val ALERT_DISTANCE_METRES = linkedMapOf(
    "CONFIG_VALUE_ALERTS_ENFORCEMENT_ALERTS_DISTANCE_STREETS" to 700L,
    "CONFIG_VALUE_ALERTS_ENFORCEMENT_ALERTS_DISTANCE_HIGHWAY" to 900L,
    "CONFIG_VALUE_ALERTS_ENFORCEMENT_ALERTS_DISTANCE_FREEWAY" to 1200L,
    "CONFIG_VALUE_ALERTS_ACCIDENT_ALERT_DISTANCE_METERS" to 2000L,
    "CONFIG_VALUE_ALERTS_HEAVY_TRAFFIC_ALERT_DISTANCE_METERS" to 3000L,
)

@Suppress("unused")
val policeAlertDistancePatch = bytecodePatch(
    name = "Extended police & hazard alert distances",
    description = "Increases the pre-alert distances for police enforcement, accidents and heavy traffic.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    execute {
        // Prepended to the numeric config getter (see RemoveAdsPatch). Label prefix "dst_" keeps
        // labels unique from the other patch injection.
        val smali = buildString {
            ALERT_DISTANCE_METRES.keys.forEachIndexed { i, key ->
                append("sget-object v0, Lcom/waze/config/ConfigValues;->$key:Lcom/waze/config/c;\n")
                append("if-eq p0, v0, :dst_force$i\n")
            }
            append("goto :dst_skip\n")
            ALERT_DISTANCE_METRES.values.forEachIndexed { i, value ->
                append(":dst_force$i\n")
                append("const-wide/16 v0, ${value}\n")
                append("invoke-static {v0, v1}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;\n")
                append("move-result-object v0\n")
                append("return-object v0\n")
            }
            append(":dst_skip\n")
        }
        NumericConfigGetterFingerprint.method.addInstructions(0, smali)
    }
}
