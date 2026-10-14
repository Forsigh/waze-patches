package io.github.forsigh.waze

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Unlock the driver-mood picker.
 *
 * WHAT WAZE GATES (measured on 5.25.0.1, `com/waze/MoodManager`):
 *
 *  1. `canSetMood(Context, String)Z` decides whether a mood may be set. It checks `isBaby()`, then
 *     compares the requested mood name against one string resource, and - for the beta list - reads
 *     `ConfigValues.CONFIG_VALUE_MOODS_BETA_ENABLED`. That config read happens *only here* in this class,
 *     so returning true makes the eligibility, beta and name checks all moot in one edit.
 *  2. `isBaby()Z` returns a field; the mood screen uses it to hide moods from "baby" (new-driver) accounts.
 *  3. `refreshMoodsList()V` asks the native catalogue twice:
 *         const/4 v0, 0x1 / const/4 v1, 0x0
 *         invoke-direct {p0, v1, v0}, getDefaultMoodListNTV(ZZ)
 *         invoke-direct {p0, v1, v1}, getCustomMoodListNTV(ZZ)
 *     Upstream's reading, kept here: the first flag of each call selects whether special/hidden entries
 *     are included, the second controls sorting. Forcing v1 = 1 asks for the unfiltered list.
 *
 * This patch unlocks what is *already in the app*: moods in the local catalogue, including the ones the
 * picker hides. It does not invent artwork - a mood whose images Waze has never downloaded renders blank -
 * and it does not grant editor permissions or fetch campaigns.
 *
 * Mood images come from the skin assets (`CONFIG_VALUE_USER_MOOD_ASSET_TIMESTAMP` tracks them), so this
 * depends on [skinRefreshPatch]: without the versionCode refresh, a device that has already run Waze would
 * keep drawing its extracted copies.
 *
 * Ported from aaayd/waze-custom-patches (GPLv3, `DriverIconsPatch.kt`) - see `upstream-port/CREDITS.txt`.
 * Re-anchored against this build: their finder matched a `(Context, String) -> Z` method by three mood-name
 * strings, which does not exist here (the same method compares against a string *resource* instead), so the
 * anchors are pinned by class, name and signature and the registers were read off this smali.
 */
private const val MOOD_MANAGER = "Lcom/waze/MoodManager;"

internal val MoodEligibilityFingerprint = Fingerprint(
    definingClass = MOOD_MANAGER,
    name = "canSetMood",
    parameters = listOf("Landroid/content/Context;", "Ljava/lang/String;"),
    returnType = "Z",
)

internal val BabyGateFingerprint = Fingerprint(
    definingClass = MOOD_MANAGER,
    name = "isBaby",
    parameters = listOf(),
    returnType = "Z",
)

internal val MoodCatalogueFingerprint = Fingerprint(
    definingClass = MOOD_MANAGER,
    name = "refreshMoodsList",
    parameters = listOf(),
    returnType = "V",
)

@Suppress("unused")
val unlockMoodsPatch = bytecodePatch(
    name = "Unlock driver moods",
    description = "Unlocks all moods already on the device.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WAZE)
    dependsOn(skinRefreshPatch)
    execute {
        val canSet = MoodEligibilityFingerprint.method
        canSet.addInstructions(0, "const/4 v0, 0x1\n\nreturn v0\n")

        val baby = BabyGateFingerprint.method
        baby.addInstructions(0, "const/4 p0, 0x0\n\nreturn p0\n")

        val refresh = MoodCatalogueFingerprint.method
        val instructions = refresh.implementation?.instructions?.toList()
            ?: throw PatchException("unlockMoodsPatch: refreshMoodsList() has no implementation")
        val sites = instructions.withIndex().filter { (_, instruction) ->
            instruction.opcode == Opcode.INVOKE_DIRECT &&
                instruction is ReferenceInstruction &&
                (instruction.reference as? MethodReference)?.let { ref ->
                    ref.definingClass == MOOD_MANAGER &&
                        ref.name in setOf("getDefaultMoodListNTV", "getCustomMoodListNTV") &&
                        ref.parameterTypes.map(CharSequence::toString) == listOf("Z", "Z")
                } == true
        }.map { it.index }
        if (sites.size != 2) {
            throw PatchException(
                "unlockMoodsPatch: expected the two native mood-catalogue calls, found ${sites.size} - " +
                    "refusing to report the picker as unlocked"
            )
        }
        // Descending: inserting at a later site first keeps the earlier indexes valid.
        for (site in sites.sortedDescending()) {
            refresh.addInstructions(site, "const/4 v1, 0x1\n")
        }
        println("unlockMoodsPatch: mood eligibility, baby gate and ${sites.size} catalogue filters opened")
    }
}