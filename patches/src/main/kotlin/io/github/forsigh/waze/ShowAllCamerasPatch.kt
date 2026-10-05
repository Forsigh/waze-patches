package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

@Suppress("unused")
val showAllCamerasPatch = bytecodePatch(
    name = "Show all cameras & road hazards",
    description = "Shows speed cameras, red-light cameras and speed bumps on the map and enables their notifications.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    execute {
        BooleanConfigGetterFingerprint.method.addInstructions(
            0,
            forceBoolean(
                "cam", true,
                "CONFIG_VALUE_MAP_SHOW_SPEED_CAMS",
                "CONFIG_VALUE_MAP_SHOW_SPEED_AND_RED_LIGHT_CAMERAS",
                "CONFIG_VALUE_MAP_SHOW_SPEED_BUMPS_PERMANENT_HAZARD",
                "CONFIG_VALUE_NOTIFICATIONS_ON_ROUTE_SPEED_CAMERAS",
                "CONFIG_VALUE_NOTIFICATIONS_ON_ROUTE_SPEED_CAMERAS_SOUND_ENABLED",
            ),
        )
    }
}
