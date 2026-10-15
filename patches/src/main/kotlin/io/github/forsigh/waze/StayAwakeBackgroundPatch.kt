package io.github.forsigh.waze

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.intOption
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Stay awake in the background (opt-in, default off).
 *
 * `CONFIG_VALUE_POWER_BACKGROUND_TIMEOUT_MS` lets Waze keep running in the background without a
 * route for the chosen time, instead of closing itself; Waze's own cap is 10 minutes. Waze still
 * closes itself when you close it.
 */
@Suppress("unused")
val stayAwakeBackgroundPatch = bytecodePatch(
    name = "Stay awake in the background",
    description = "Keeps Waze running in the background without a route.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)
    category("Experimental")

    dependsOn(configOverridesPatch)

    val minutes by intOption(
        key = "minutes", default = 10,
        title = "Minutes in the background",
        description = "Waze's own cap is 10.",
    )

    execute {
        val m = minutes ?: 10
        recordLong("sab", "CONFIG_VALUE_POWER_BACKGROUND_TIMEOUT_MS" to m * 60_000L)
        println("stayAwakeBackgroundPatch: background timeout = $m minutes")
    }
}
