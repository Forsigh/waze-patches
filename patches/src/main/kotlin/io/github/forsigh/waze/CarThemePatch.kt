package io.github.forsigh.waze

import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Car screen follows the phone's theme (opt-in, default off).
 *
 * `ANDROID_AUTO_USE_MOBILE_THEME_MODE_ON_CAR` (read in `com.waze.nightmode.e`) makes the car
 * experience track the phone's day/night mode instead of the car host's, and
 * `CAR_LIB_CAR_APP_THEME_SOURCE_ENABLED` (read in `com.waze.car_lib.bo`) lets the car app take
 * its theme from the phone. Together the car screen matches the phone's look - including the
 * day/night pairing of the map themes.
 */
@Suppress("unused")
val carThemePatch = bytecodePatch(
    name = "Car screen follows the phone theme",
    description = "Uses the phone's day and night look on the car screen.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)
    category("Android Auto")

    dependsOn(configOverridesPatch)

    execute {
        recordBoolean(
            "cartheme", true,
            "CONFIG_VALUE_ANDROID_AUTO_USE_MOBILE_THEME_MODE_ON_CAR",
            "CONFIG_VALUE_CAR_LIB_CAR_APP_THEME_SOURCE_ENABLED",
        )
        println("carThemePatch: car theme mode follows the phone")
    }
}
