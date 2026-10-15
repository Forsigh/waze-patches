package io.github.forsigh.waze

import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Keep Waze alive through car service restarts (opt-in, default off).
 *
 * `CONFIG_VALUE_AAOS_KEEP_APP_ALIVE_ON_MAIN_SERVICE_DESTROY` (read in `com.waze.car_lib.bo` and
 * `com.waze.bw.a.a.b`) makes the car build survive its main service being destroyed instead of
 * tearing down - aimed at Android Automotive / car-unit installs where the system restarts
 * services in the background. On a standard projection setup it is harmless.
 */
@Suppress("unused")
val keepAlivePatch = bytecodePatch(
    name = "Keep Waze alive on car",
    description = "Keeps Waze connected through car service restarts.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)
    category("Experimental")

    dependsOn(configOverridesPatch)

    execute {
        recordBoolean("ka", true, "CONFIG_VALUE_AAOS_KEEP_APP_ALIVE_ON_MAIN_SERVICE_DESTROY")
        println("keepAlivePatch: keep-alive through service restarts forced on")
    }
}
