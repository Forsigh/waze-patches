package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/** Fully qualified extension entry point, merged into the APK from the .mpe below. */
private const val EXTENSION_CLASS = "Lio/github/forsigh/waze/extension/settings/ForsighSettings;"

/**
 * Forsigh Settings.
 *
 * The in-app menu this will grow into needs the override mechanism to read a preference at runtime,
 * because a value pinned when the APK is patched cannot be changed from a settings screen. This patch
 * is the groundwork: it merges the extension and hands it a Context at the earliest point Waze runs,
 * which is what makes any runtime setting readable at all.
 *
 * The menu screen itself plugs into Waze's settings tree and lands here next.
 */
@Suppress("unused")
val forsighSettingsPatch = bytecodePatch(
    name = "Forsigh Settings",
    description = "Merges the Forsigh Settings extension and gives it a Context at startup, so the " +
        "other patches can be controlled by a runtime setting instead of only at patch time.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    extendWith("extensions/waze/forsigh-settings.mpe")

    execute {
        ApplicationOnCreateFingerprint.method.addInstructions(
            0,
            "invoke-static {p0}, $EXTENSION_CLASS->init(Landroid/content/Context;)V",
        )
    }
}
