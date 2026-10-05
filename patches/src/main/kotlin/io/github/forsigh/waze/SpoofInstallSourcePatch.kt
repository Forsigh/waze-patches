/*
 * Ported from xob0t/morphe-patches — xob0t/privacy/patches/installsource/SpoofInstallSourcePatch.kt
 * (GPLv3; the same file is reused by kiraio-moe/Lain-Patches). Credit: xob0t.
 *
 * Universal patch: rewrites the app's OWN reads of the install source so they report a
 * chosen package (default Google Play). It cannot change Android Auto visibility, because
 * that is the system's installerPackageName record, set by the installer at install time —
 * not something app bytecode can influence.
 */
package io.github.forsigh.waze

import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val PLAY_STORE_PACKAGE = "com.android.vending"
private const val PACKAGE_MANAGER = "Landroid/content/pm/PackageManager;"
private const val INSTALL_SOURCE_INFO = "Landroid/content/pm/InstallSourceInfo;"

private fun Instruction.methodReferenceOrNull(): MethodReference? =
    (this as? ReferenceInstruction)?.reference as? MethodReference

private fun Instruction.isInstallSourcePatchTarget(): Boolean {
    if (opcode !in setOf(Opcode.INVOKE_VIRTUAL, Opcode.INVOKE_VIRTUAL_RANGE)) return false
    val reference = methodReferenceOrNull() ?: return false
    return reference.isPackageManagerGetInstallerPackageName() || reference.isInstallSourceInfoPackageGetter()
}

private fun MethodReference.isPackageManagerGetInstallerPackageName() =
    definingClass == PACKAGE_MANAGER &&
        name == "getInstallerPackageName" &&
        parameterTypes.size == 1 &&
        parameterTypes[0].toString() == "Ljava/lang/String;" &&
        returnType == "Ljava/lang/String;"

private fun MethodReference.isInstallSourceInfoPackageGetter() =
    definingClass == INSTALL_SOURCE_INFO &&
        name in setOf(
            "getInitiatingPackageName",
            "getInstallingPackageName",
            "getOriginatingPackageName",
            "getUpdateOwnerPackageName",
        ) &&
        parameterTypes.isEmpty() &&
        returnType == "Ljava/lang/String;"

private fun Method.hasInstallSourcePatchTarget(): Boolean =
    instructionsOrNull?.any { it.isInstallSourcePatchTarget() } == true

@Suppress("unused")
val spoofInstallSourcePatch = bytecodePatch(
    name = "Spoof install source",
    description = "Spoofs the app's own package-installer checks to report the configured package name " +
        "(default Google Play). Universal. Does not change Android Auto visibility.",
    default = false,
) {
    val installerPackageName by stringOption(
        "packageName",
        PLAY_STORE_PACKAGE,
        mapOf(PLAY_STORE_PACKAGE to PLAY_STORE_PACKAGE),
        title = "Installer package name",
        description = "The package name to report as the installation source.",
    )

    execute {
        var patchedInstallerPackageNameReads = 0

        classDefForEach { classDef ->
            if (classDef.methods.none { it.hasInstallSourcePatchTarget() }) return@classDefForEach

            mutableClassDefBy(classDef).methods.forEach { method ->
                if (!method.hasInstallSourcePatchTarget()) return@forEach

                val instructionList = method.instructionsOrNull?.toList() ?: return@forEach

                instructionList.forEachIndexed { index, instruction ->
                    if (!instruction.isInstallSourcePatchTarget()) return@forEachIndexed

                    val moveResult = instructionList.getOrNull(index + 1) as? OneRegisterInstruction
                        ?: return@forEachIndexed
                    if (moveResult.opcode != Opcode.MOVE_RESULT_OBJECT) return@forEachIndexed

                    method.replaceInstruction(
                        index + 1,
                        "const-string v${moveResult.registerA}, \"$installerPackageName\"",
                    )
                    patchedInstallerPackageNameReads++
                }
            }
        }

        println("Spoof install source: patched $patchedInstallerPackageNameReads installer package reads.")
    }
}
