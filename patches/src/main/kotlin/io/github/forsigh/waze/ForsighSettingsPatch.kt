package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/** Static holder the injected startup hook calls into (merged from the .mpe below). */
private const val EXTENSION_CLASS = "Lio/github/forsigh/waze/extension/settings/ForsighSettings;"

/** The settings row that opens the Forsigh Settings menu. */
private const val EXTENSION_ENTRY = "Lio/github/forsigh/waze/extension/settings/ForsighSettingsEntry;"

/** The settings screen fills its section list by appending each section to a `List`. */
private const val LIST_ADD = "Ljava/util/List;->add(Ljava/lang/Object;)Z"

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

        // Add the "Forsigh Settings" row to the settings screen's section list.
        val method = SettingsSectionListFingerprint.method
        val instructions = (method.implementation ?: error("gp.<init> has no implementation"))
            .instructions.toList()

        // The list register is reused for several different sections, so anchor on the LAST append -
        // everything before it is a different list being filled.
        val lastAdd = instructions.indexOfLast { instruction ->
            instruction.opcode == Opcode.INVOKE_INTERFACE &&
                (instruction as? ReferenceInstruction)?.reference?.toString() == LIST_ADD
        }
        check(lastAdd >= 0) { "Could not find the settings section list append in gp.<init>" }

        // {vA, vB} - for INVOKE_INTERFACE the first register is the receiver, i.e. the list itself.
        val listRegister = (instructions[lastAdd] as FiveRegisterInstruction).registerC
        method.addInstructions(
            lastAdd + 1,
            "invoke-static {v$listRegister}, $EXTENSION_ENTRY->appendTo(Ljava/util/List;)V",
        )
    }
}
