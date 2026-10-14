package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Alert distances — one row, three levels, chosen in the patch options.
 *
 * Stock (default) writes Waze's own distances back, i.e. changes nothing; Moderate keeps the
 * enlarged incident distances within 2x of stock; Maximum is the community (CGE) table the row
 * originally carried. Every value is meters; the stock numbers come from the mod's published
 * official -> mod delta list.
 */
@Suppress("unused")
val policeAlertDistancePatch = bytecodePatch(
    name = "Extended police & hazard alert distances",
    description = "Pick how early police and hazard alerts fire.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    // The emitter patch is what actually writes the keys recorded below. Depending on it means this
    // patch can be ticked alone and still produce a build where the override exists — without the
    // dependency the key is collected and never written, which looks exactly like a working patch.
    dependsOn(configOverridesPatch)

    val level by stringOption(
        "level", "Stock",
        mapOf(
            "Stock (default)" to "Stock",
            "Moderate" to "Moderate",
            "Maximum" to "Maximum",
        ),
        title = "Alert distance level",
        description = "Stock is Waze's own; the others warn earlier.",
    )

    execute {
        // All Long (com/waze/config/c) keys, so must hook the Long getter. Values are meters.
        // Stock = Waze's official distances; Maximum = the community table's original numbers.
        val tables = mapOf(
            "Stock" to listOf(500L, 1000L, 2000L, 600L, 600L),
            "Moderate" to listOf(700L, 900L, 1200L, 1200L, 1200L),
            "Maximum" to listOf(700L, 900L, 1200L, 2000L, 3000L),
        )
        val keys = listOf(
            "CONFIG_VALUE_ALERTS_ENFORCEMENT_ALERTS_DISTANCE_STREETS",
            "CONFIG_VALUE_ALERTS_ENFORCEMENT_ALERTS_DISTANCE_HIGHWAY",
            "CONFIG_VALUE_ALERTS_ENFORCEMENT_ALERTS_DISTANCE_FREEWAY",
            "CONFIG_VALUE_ALERTS_ACCIDENT_ALERT_DISTANCE_METERS",
            "CONFIG_VALUE_ALERTS_HEAVY_TRAFFIC_ALERT_DISTANCE_METERS",
        )
        val chosen = level ?: "Stock"
        val values = tables[chosen]
            ?: throw PatchException(
                "policeAlertDistancePatch: unknown level '$chosen' - pick Stock, Moderate or Maximum",
            )
        recordLong("dst", *keys.zip(values).toTypedArray())
    }
}
