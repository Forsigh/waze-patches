package io.github.forsigh.waze

import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * No Google Assistant (opt-in, default off).
 *
 * `CONFIG_VALUE_GOOGLE_ASSISTANT_WAZE_FEATURE_ENABLED` (read in com.waze.google_assistant.c)
 * keeps the Assistant button, onboarding and settings entry away whatever the server enables.
 */
@Suppress("unused")
val noGoogleAssistantPatch = bytecodePatch(
    name = "No Google Assistant",
    description = "Keeps Google Assistant out of Waze.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    dependsOn(configOverridesPatch)

    execute {
        recordBoolean("noassist", false, "CONFIG_VALUE_GOOGLE_ASSISTANT_WAZE_FEATURE_ENABLED")
        println("noGoogleAssistantPatch: Google Assistant forced off")
    }
}
