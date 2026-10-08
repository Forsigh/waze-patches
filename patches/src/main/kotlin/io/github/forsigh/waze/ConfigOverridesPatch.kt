package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Applies every config override the other patches registered, as exactly one block per typed getter.
 *
 * This patch injects nothing itself until [finalize], which runs after all patches have executed -
 * that is what lets it see the complete [Overrides] registry regardless of patch order, and why the
 * individual patches need no dependencies on each other.
 */
@Suppress("unused")
val configOverridesPatch = bytecodePatch(
    name = "Config overrides",
    description = "Applies the configuration changes requested by the other patches. Applied " +
        "automatically; there is nothing to configure.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    finalize {
        if (Overrides.booleans.isNotEmpty()) {
            BooleanConfigGetterFingerprint.method.addInstructions(
                0,
                forceBoolean("ovb", Overrides.booleans),
            )
        }
        if (Overrides.longs.isNotEmpty()) {
            LongConfigGetterFingerprint.method.addInstructions(
                0,
                forceLong("ovl", Overrides.longs),
            )
        }
        if (Overrides.strings.isNotEmpty()) {
            StringConfigGetterFingerprint.method.addInstructions(
                0,
                forceString("ovs", Overrides.strings),
            )
        }
    }
}
