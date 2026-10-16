package io.github.forsigh.waze

import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Smoother Android Auto map (opt-in, default off).
 *
 * The car map is fed by the car's GPS through the projection link, and three of Waze's own
 * switches govern how that feed is handled:
 *
 *  - `CAR_LIB_CAR_GPS_FILTERING_ENABLED` gates the car-GPS filtering stage (read in
 *    `com.waze.location.g`, applied in `com.waze.location.k`),
 *  - `AAP_USE_NORMAL_CAR_GPS_UPDATE_RATE` selects the update-rate policy the projection
 *    (AAP) path uses (wired through the `com.waze.modules` chain),
 *  - `CAR_LIB_LISTEN_TO_CAR_COMPASS` makes the car map take heading from the car's compass
 *    (read in `com.waze.car_lib.h.c`).
 *
 * All three are server-controlled; which side of each switch a given device gets is why one
 * head unit can feel smooth and another choppy. This row forces them on (read-path, like every
 * other override) as the one app-side lever for the car-map feel.
 *
 * 5.25.90.900 dropped `AAP_USE_NORMAL_CAR_GPS_UPDATE_RATE` from the config set; on builds that
 * no longer declare it the emitter skips that one key automatically and the other two still
 * apply - the row never references a key the build lacks.
 */
@Suppress("unused")
val aaSmootherMapPatch = bytecodePatch(
    name = "Smoother Android Auto map",
    description = "Smoothes the car map's movement and heading on Android Auto.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)
    category("Android Auto")

    // The emitter patch is what actually writes the keys recorded below; ticking this row alone
    // must still produce a build carrying the overrides.
    dependsOn(configOverridesPatch)

    execute {
        recordBoolean(
            "aas", true,
            "CONFIG_VALUE_CAR_LIB_CAR_GPS_FILTERING_ENABLED",
            "CONFIG_VALUE_AAP_USE_NORMAL_CAR_GPS_UPDATE_RATE",
            "CONFIG_VALUE_CAR_LIB_LISTEN_TO_CAR_COMPASS",
        )
        println("aaSmootherMapPatch: forcing car-GPS filtering, normal car-GPS rate and car compass")
    }
}
