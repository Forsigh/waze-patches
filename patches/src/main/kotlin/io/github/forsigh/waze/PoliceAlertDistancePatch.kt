package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.intOption
import app.morphe.patcher.patch.stringOption
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Alert distances — one row, one gear: a level for the whole family of alert distances, or set
 * each distance yourself under Custom. All nine keys are Long (com/waze/config/c), values in
 * meters. Standard writes Waze's own numbers back, i.e. changes nothing; Moderate and Heavier
 * use the enlarged tables.
 */
@Suppress("unused")
val policeAlertDistancePatch = bytecodePatch(
    name = "Alert distances",
    description = "Choose how early alerts fire: Standard, Moderate, Heavier or Custom.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    // The emitter patch is what actually writes the keys recorded below. Depending on it means this
    // patch can be ticked alone and still produce a build where the override exists — without the
    // dependency the key is collected and never written, which looks exactly like a working patch.
    dependsOn(configOverridesPatch)

    val level by stringOption(
        "level", "Standard",
        mapOf(
            "Standard (Waze's own)" to "Standard",
            "Moderate" to "Moderate",
            "Heavier" to "Heavier",
            "Custom (set each below)" to "Custom",
        ),
        title = "Alert distance level",
        description = "Standard keeps Waze's own; Moderate and Heavier warn earlier.",
    )

    // Only used when the level above is Custom. Defaults are Waze's own numbers.
    val police by intOption(key = "police", default = 600, title = "Police", description = "Waze: 600 m.")
    val hazard by intOption(key = "hazard", default = 600, title = "Hazards", description = "Waze: 600 m.")
    val accident by intOption(key = "accident", default = 600, title = "Crashes", description = "Waze: 600 m.")
    val traffic by intOption(key = "traffic", default = 600, title = "Traffic jams", description = "Waze: 600 m.")
    val streets by intOption(key = "streets", default = 500, title = "Speed cams - streets", description = "Waze: 500 m.")
    val highways by intOption(key = "highways", default = 1000, title = "Speed cams - highways", description = "Waze: 1000 m.")
    val freeways by intOption(key = "freeways", default = 2000, title = "Speed cams - freeways", description = "Waze: 2000 m.")
    val blocked by intOption(key = "blocked", default = 400, title = "Blocked lanes", description = "Waze: 400 m.")
    val between by intOption(key = "between", default = 300, title = "Gap between alerts", description = "Waze: 300 m.")

    execute {
        val keys = listOf(
            "CONFIG_VALUE_ALERTS_POLICE_ALERT_DISTANCE_METERS",
            "CONFIG_VALUE_ALERTS_HAZARD_ALERT_DISTANCE_METERS",
            "CONFIG_VALUE_ALERTS_ACCIDENT_ALERT_DISTANCE_METERS",
            "CONFIG_VALUE_ALERTS_HEAVY_TRAFFIC_ALERT_DISTANCE_METERS",
            "CONFIG_VALUE_ALERTS_ENFORCEMENT_ALERTS_DISTANCE_STREETS",
            "CONFIG_VALUE_ALERTS_ENFORCEMENT_ALERTS_DISTANCE_HIGHWAY",
            "CONFIG_VALUE_ALERTS_ENFORCEMENT_ALERTS_DISTANCE_FREEWAY",
            "CONFIG_VALUE_ALERTS_BLOCKED_LANE_ALERT_DISTANCE",
            "CONFIG_VALUE_ALERTS_DISTANCE_BETWEEN_ALERTS",
        )
        val tables = mapOf(
            "Standard" to listOf(600L, 600L, 600L, 600L, 500L, 1000L, 2000L, 400L, 300L),
            "Moderate" to listOf(900L, 900L, 1200L, 1200L, 700L, 900L, 1200L, 700L, 300L),
            "Heavier" to listOf(1000L, 900L, 2000L, 3000L, 700L, 900L, 1200L, 1000L, 200L),
        )
        val chosen = level ?: "Standard"
        val values = if (chosen == "Custom") {
            val stock = tables.getValue("Standard")
            listOf(police, hazard, accident, traffic, streets, highways, freeways, blocked, between)
                .mapIndexed { i, v -> (v ?: stock[i].toInt()).toLong() }
        } else {
            tables[chosen]
                ?: throw PatchException(
                    "policeAlertDistancePatch: unknown level '$chosen' - pick Standard, Moderate, Heavier or Custom",
                )
        }
        // All Long (com/waze/config/c) keys, so must hook the Long getter. Values are meters.
        recordLong("dst", *keys.zip(values).toTypedArray())
        println("policeAlertDistancePatch: alert distances = $chosen")
    }
}
