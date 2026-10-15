package io.github.forsigh.waze

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.intOption
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Android Auto heads-up distances (opt-in, default off).
 *
 * How far ahead the car screen warns of what is coming, and how long a jam must last before the
 * traffic bar shows it: `CONFIG_VALUE_ANDROID_AUTO_HEADS_UP_DISTANCE_NORMAL`,
 * `..._FREEWAY` and `CONFIG_VALUE_TRAFFIC_BAR_MIN_TIME_IN_TRAFFIC_SECONDS`. The defaults are the
 * old Waze mods' table; Waze's own numbers are lower and shown on each option.
 */
@Suppress("unused")
val aaHeadsUpDistancesPatch = bytecodePatch(
    name = "Android Auto heads-up distances",
    description = "How early the car screen warns and when the traffic bar shows a jam.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)
    category("Android Auto")

    dependsOn(configOverridesPatch)

    val headsUp by intOption(
        key = "headsUp", default = 1200,
        title = "Heads-up, normal roads (m)",
        description = "Waze: 200 m.",
    )
    val headsUpFreeway by intOption(
        key = "headsUpFreeway", default = 1300,
        title = "Heads-up, freeways (m)",
        description = "Waze: 500 m.",
    )
    val trafficBar by intOption(
        key = "trafficBar", default = 180,
        title = "Traffic bar: time in traffic (s)",
        description = "Waze: 360 s.",
    )

    execute {
        recordLong(
            "aah",
            "CONFIG_VALUE_ANDROID_AUTO_HEADS_UP_DISTANCE_NORMAL" to (headsUp ?: 1200).toLong(),
            "CONFIG_VALUE_ANDROID_AUTO_HEADS_UP_DISTANCE_FREEWAY" to (headsUpFreeway ?: 1300).toLong(),
            "CONFIG_VALUE_TRAFFIC_BAR_MIN_TIME_IN_TRAFFIC_SECONDS" to (trafficBar ?: 180).toLong(),
        )
        println("aaHeadsUpDistancesPatch: car heads-up distances forced")
    }
}
