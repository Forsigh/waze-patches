package io.github.forsigh.waze

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Boot straight into the map in guest mode.
 *
 * Waze's first run is a state machine (com/waze/uid/state_impl). Each screen is a state whose h()
 * method decides whether it runs: return false and the machine skips it silently. The welcome
 * carousel, the AADC age request and the two consent screens (ND4C and the privacy flow select)
 * are gated off that way, and the "default result" state - which normally reads whatever the
 * install-services controller hands it - is pinned to GUEST so the machine routes into
 * GuestLoginState without ever showing the account options.
 *
 * What stays intact: the machine itself, so every value the guest branch normally sets is still
 * set, and the settings entry point that opens this flow on demand for signing in later.
 */

private const val INSTALL_PARAMETERS = "Lcom/waze/uid/state_impl/util/InstallParameters;"
private const val INSTALL_RESULT = "Lcom/waze/uid/state_impl/util/InstallParameters\$Result;"
private const val STATE_EVENT = "Lcom/waze/uid/e/e;"
private const val TIMESTAMP = "Lcom/waze/shared_infra/q/i;"
private const val AGE_MODE = "Lcom/waze/sharedui/profile/AgeRestrictionMode;"
private const val AGE_STATE = "Lcom/waze/b/a;"

/** 01/01/2000 00:00:00 UTC in milliseconds - what the age gate will believe. */
private const val BIRTH_DATE_MILLIS = 946_684_800_000L
private const val TRUE_SNIPPET = "const/4 p0, 0x1\n\nreturn p0\n"
private const val FALSE_SNIPPET = "const/4 p0, 0x0\n\nreturn p0\n"

/** WelcomeState.h - return false and the welcome carousel never shows. */
internal val WelcomeStateFingerprint = Fingerprint(
    definingClass = "Lcom/waze/uid/state_impl/g/w;",
    name = "h",
    parameters = listOf(STATE_EVENT),
    returnType = "Z",
)

/** RequestUserAgeState.h - return false and the age request is skipped. */
internal val RequestUserAgeFingerprint = Fingerprint(
    definingClass = "Lcom/waze/uid/state_impl/a/e;",
    name = "h",
    parameters = listOf(STATE_EVENT),
    returnType = "Z",
)

/** ND4CConsent.h - return false and the ND4C consent screen is skipped. */
internal val Nd4cConsentFingerprint = Fingerprint(
    definingClass = "Lcom/waze/uid/state_impl/g/m;",
    name = "h",
    parameters = listOf(STATE_EVENT),
    returnType = "Z",
)

/** StatePrivacyFlowSelect's child state (g/q).h - return false and the privacy select is skipped. */
internal val PrivacyFlowSelectFingerprint = Fingerprint(
    definingClass = "Lcom/waze/uid/state_impl/g/q;",
    name = "h",
    parameters = listOf(STATE_EVENT),
    returnType = "Z",
)

/** CheckDefaultResultState.f - pins InstallParameters.result to GUEST right after the default write. */
internal val CheckDefaultResultFingerprint = Fingerprint(
    definingClass = "Lcom/waze/uid/state_impl/d/a;",
    name = "f",
    parameters = listOf(STATE_EVENT),
    returnType = "V",
)

/** b/k.g - the age-restriction state factory; forced to a known adult birth date. */
internal val AadcStateFactoryFingerprint = Fingerprint(
    definingClass = "Lcom/waze/b/k;",
    name = "g",
    parameters = listOf("Z", "Z", "Lcom/waze/sharedui/profile/UserProfile;", "Lcom/waze/a/ag;"),
    returnType = "Lcom/waze/b/e;",
)

/** bg/b (privacy consent impl): i() = consent already approved. */
internal val ConsentApprovedFingerprint = Fingerprint(
    definingClass = "Lcom/waze/bg/b;",
    name = "i",
    parameters = listOf(),
    returnType = "Z",
)

/** bg/b (privacy consent impl): j() = consent bump (policy update) applies. */
internal val ConsentBumpFingerprint = Fingerprint(
    definingClass = "Lcom/waze/bg/b;",
    name = "j",
    parameters = listOf(),
    returnType = "Z",
)

/** az/a (ND4C consent impl): i() = feature enabled. */
internal val Nd4cEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/waze/az/a;",
    name = "i",
    parameters = listOf(),
    returnType = "Z",
)

/** az/a (ND4C consent impl): j() = approval still needed. */
internal val Nd4cNeedsApprovalFingerprint = Fingerprint(
    definingClass = "Lcom/waze/az/a;",
    name = "j",
    parameters = listOf(),
    returnType = "Z",
)

/** ProfileStatus.isAgeConfirmed - the age-block controller consults it; treat as confirmed. */
internal val AgeConfirmedFingerprint = Fingerprint(
    definingClass = "Lcom/waze/sharedui/profile/ProfileStatus;",
    name = "isAgeConfirmed",
    parameters = listOf(),
    returnType = "Z",
)

@Suppress("unused")
val skipSignInPatch = bytecodePatch(
    name = "Skip sign-in",
    description = "Boots straight into the map as a guest.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    execute {
        val screensToSkip = listOf(
            "welcome carousel" to WelcomeStateFingerprint.method,
            "age request" to RequestUserAgeFingerprint.method,
            "ND4C consent" to Nd4cConsentFingerprint.method,
            "privacy flow select" to PrivacyFlowSelectFingerprint.method,
        )
        for ((label, method) in screensToSkip) {
            method.addInstructions(0, "const/4 p0, 0x0\n\nreturn p0\n")
        }
        println("skipSignInPatch: gated off ${screensToSkip.size} screens")

        val checkDefault = CheckDefaultResultFingerprint.method
        val instructions = checkDefault.implementation?.instructions?.toList()
            ?: throw PatchException("skipSignInPatch: CheckDefaultResultState.f() has no implementation")
        val sites = instructions.withIndex().filter { (_, instruction) ->
            instruction.opcode == Opcode.IPUT_OBJECT &&
                instruction is ReferenceInstruction &&
                (instruction.reference as? FieldReference)?.let { ref ->
                    ref.definingClass == INSTALL_PARAMETERS && ref.name == "result"
                } == true
        }.map { it.index }
        if (sites.size != 1) {
            throw PatchException(
                "skipSignInPatch: expected one write of InstallParameters.result in " +
                    "CheckDefaultResultState.f(), found ${sites.size} - refusing to report the " +
                    "flow as bypassed"
            )
        }
        // Descending order not needed (single site); insert right after the original write so the
        // default value is overwritten before k() advances the machine.
        checkDefault.addInstructions(
            sites[0] + 1,
            "sget-object v0, $INSTALL_RESULT->GUEST:$INSTALL_RESULT\n\n" +
                "iput-object v0, p1, $INSTALL_PARAMETERS->result:$INSTALL_RESULT\n",
        )
        println("skipSignInPatch: default result pinned to GUEST")

        // The age gate: every consumer reads the restriction state built by b/k.g(). Pin that
        // state to a known adult birth date (01/01/2000) with no restriction, so neither the
        // ask screen nor the age-block controller ever has a reason to appear.
        val stateFactory = AadcStateFactoryFingerprint.method
        val factoryInstructions = stateFactory.implementation?.instructions?.toList()
            ?: throw PatchException("skipSignInPatch: age-state factory has no implementation")
        val profileRead = factoryInstructions.withIndex().firstOrNull { (_, instruction) ->
            instruction.opcode == Opcode.INVOKE_VIRTUAL &&
                instruction is ReferenceInstruction &&
                (instruction.reference as? MethodReference)?.let { ref ->
                    ref.definingClass == "Lcom/waze/sharedui/profile/UserProfile;" &&
                        ref.name == "getBasicInfo"
                } == true
        }?.index
            ?: throw PatchException("skipSignInPatch: age-state factory shape changed - refusing to guess")
        stateFactory.addInstructions(
            profileRead,
            "const-wide v0, 0x${BIRTH_DATE_MILLIS.toString(16).uppercase()}L\n\n" +
                "new-instance v2, $TIMESTAMP\n\n" +
                "invoke-direct {v2, v0, v1}, $TIMESTAMP-><init>(J)V\n\n" +
                "sget-object v0, $AGE_MODE->UNRESTRICTED:$AGE_MODE\n\n" +
                "new-instance v1, $AGE_STATE\n\n" +
                "invoke-direct {v1, v2, v0}, $AGE_STATE-><init>($TIMESTAMP$AGE_MODE)V\n\n" +
                "return-object v1\n",
        )

        // Consents: report approved, and never a pending bump or screen.
        ConsentApprovedFingerprint.method.addInstructions(0, TRUE_SNIPPET)
        ConsentBumpFingerprint.method.addInstructions(0, FALSE_SNIPPET)
        Nd4cEnabledFingerprint.method.addInstructions(0, FALSE_SNIPPET)
        Nd4cNeedsApprovalFingerprint.method.addInstructions(0, FALSE_SNIPPET)
        AgeConfirmedFingerprint.method.addInstructions(0, TRUE_SNIPPET)
        println("skipSignInPatch: birth date pinned to 01/01/2000, consents auto-accepted")
    }
}
