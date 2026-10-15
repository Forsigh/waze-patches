package io.github.forsigh.waze

import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * No AI reporting (opt-in, default off).
 *
 * The conversational (Gemini) reporting stack: its switches, dialog, tips, education hints and
 * the in-car bits, each read across com.waze.reports_v2, com.waze.bm.b.c and com.waze.car_lib.
 */
@Suppress("unused")
val noAiReportingPatch = bytecodePatch(
    name = "No AI reporting",
    description = "Turns off Gemini voice reporting, prompts and tips.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    dependsOn(configOverridesPatch)

    execute {
        // All Boolean (com/waze/config/b) keys, so must hook the Boolean getter.
        recordBoolean(
                "noai", false,
                "CONFIG_VALUE_REPORTING_CONVERSATIONAL_REPORTING_ENABLED",
                "CONFIG_VALUE_REPORTING_CONVERSATIONAL_REPORTING_IN_CAR_ENABLED",
                "CONFIG_VALUE_REPORTING_CONVERSATIONAL_REPORTING_AGENT_ENABLED",
                "CONFIG_VALUE_REPORTING_CONVERSATIONAL_REPORTING_ACTIVATION_DIALOG_ENABLED",
                "CONFIG_VALUE_REPORTING_CONVERSATIONAL_REPORTING_FTE_TOOLTIP_ENABLED",
                "CONFIG_VALUE_REPORTING_CONVERSATIONAL_REPORTING_BUTTON_EDUCATION_ENABLED",
                "CONFIG_VALUE_REPORTING_CONVERSATIONAL_REPORTING_REPORT_MENU_ONBOARDING_ENABLED",
                "CONFIG_VALUE_REPORTING_CONVERSATIONAL_REPORTING_NON_FTE_HINT_ENABLED",
                "CONFIG_VALUE_REPORTING_CONVERSATIONAL_REPORTING_REPORT_BUTTON_FTE_HINT_ENABLED",
                "CONFIG_VALUE_REPORTING_CONVERSATIONAL_REPORTING_REPORT_MENU_FTE_HINT_ENABLED",
                "CONFIG_VALUE_REPORTING_CONVERSATIONAL_REPORTING_AGENT_FTE_HINT_ENABLED",
            )
        println("noAiReportingPatch: Gemini reporting stack forced off")
    }
}
