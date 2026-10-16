package io.github.forsigh.waze

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.intOption
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Keep Waze alive on Android Auto (opt-in, default off).
 *
 * The Android Auto projection counterpart of `keepAlivePatch` (which drives the Android Automotive
 * path). On a projection session, `WazeCarAppService.onDestroy` reads two gates whose logs name
 * them: `onDestroy(appType=..., keepAlive=..., deferShutdown=...)`.
 *
 *  - `CONFIG_VALUE_AAP_SHUTDOWN_DEFERRING_ENABLED` is the `deferShutdown` gate
 *    (`com.waze.car_lib.bo`, lambda `h`). When true, the service destroy goes through
 *    `com.waze.car_lib.bh.f()` instead of calling `shutDown()` immediately.
 *  - `CONFIG_VALUE_AAP_SHUTDOWN_DELAY_ON_MAIN_SERVICE_DESTROY_MS` is the delay that `bh.f()`
 *    schedules before actually shutting down (`aapShutdownDelayOnMainServiceDestroyMs config=`
 *    in the log). A new service create within that window cancels the pending shutdown
 *    (`onServiceCreated ... jobActive=`), so reconnecting to the car resumes instantly.
 *
 * Both are server-controlled; without them a projection disconnect shuts Waze down at once
 * (the cold start every time you re-enter the car). The pair only applies to the projection
 * app type (`com.waze.aa.a`); Automotive installs and plain phone use are untouched.
 */
@Suppress("unused")
val keepAliveAndroidAutoPatch = bytecodePatch(
    name = "Keep Waze alive on Android Auto",
    description = "Keeps Waze running after you disconnect from Android Auto.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)
    category("Android Auto")

    dependsOn(configOverridesPatch)

    val minutes by intOption(
        key = "minutes", default = 10,
        title = "Minutes alive after disconnect",
        description = "Waze shuts down this long after you leave the car.",
    )

    execute {
        val m = minutes ?: 10
        recordBoolean("kaa", true, "CONFIG_VALUE_AAP_SHUTDOWN_DEFERRING_ENABLED")
        recordLong("kaa", "CONFIG_VALUE_AAP_SHUTDOWN_DELAY_ON_MAIN_SERVICE_DESTROY_MS" to m * 60_000L)
        println("keepAliveAndroidAutoPatch: deferring on, delay = $m minute(s)")
    }
}
