package io.github.forsigh.waze

import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Traffic meter on the car screen (opt-in, default off).
 *
 * `CONFIG_VALUE_CAR_LIB_TRAFFIC_METER_FEATURE_ENABLED` gates the car-side traffic meter (read via
 * the traffic-meter config repository `com.waze.car_lib.i.c.g`, consumed in `com.waze.car_lib.j.z`).
 * Forcing it on shows the meter where the server had it off.
 */
@Suppress("unused")
val trafficMeterPatch = bytecodePatch(
    name = "Traffic meter",
    description = "Shows the traffic meter on the car screen.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)
    category("Android Auto")

    dependsOn(configOverridesPatch)

    execute {
        recordBoolean("tm", true, "CONFIG_VALUE_CAR_LIB_TRAFFIC_METER_FEATURE_ENABLED")
        println("trafficMeterPatch: car traffic meter forced on")
    }
}
