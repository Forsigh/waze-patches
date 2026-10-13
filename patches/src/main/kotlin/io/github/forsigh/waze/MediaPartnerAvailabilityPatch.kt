package io.github.forsigh.waze

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Let Waze treat any installed partner as available, not only apps that implement Waze's SDK.
 *
 * WHY THIS EXISTS (measured on 5.25.0.1): `com/waze/sdk/au;->g(Context)` is the availability check
 * behind every partner row and behind `SdkConfiguration`'s partner list. It has two paths:
 *
 *     if (package == "com.spotify.music")            // Spotify: is the app installed?
 *         return getPackageInfo(package) != null
 *     return ap.d(package)                           // everyone else: does the app have a receiver
 *                                                    // for the com.waze.sdk.audio.ACTION_INIT broadcast?
 *
 * `ap.d` builds that broadcast Intent and calls `queryBroadcastReceivers`. The consequence: an app that
 * implements Waze's SDK (Morphe YouTube Music) passes, and every ordinary media app fails - it is listed
 * as a partner but never available, so Waze never connects.
 *
 * Waze already contains the door those apps need: `com/waze/sdk/a/j` resolves a package's
 * `android.media.browse.MediaBrowserService` and builds `com/waze/sdk/a/i` for it - the generic
 * support-media-browser client (metadata, queue, playback state, session tokens) that the Spotify
 * client `a/k` itself extends. Only the availability check stands in the way.
 *
 * THE EDIT: one instruction. After `String.equals` decides whether the package is Spotify, the result
 * lands in a register and the branch below sends non-Spotify packages to the receiver check. Forcing
 * that register to true sends every partner down the Spotify path instead, i.e. "available = installed".
 * For apps that do implement the SDK nothing changes (they are installed, so the answer stays true); for
 * apps that do not, availability stops being impossible. The branch itself is untouched, and an
 * uninstalled package still fails: `getPackageInfo` throws and the `NameNotFoundException` path leaves
 * the cached answer false.
 *
 * This is an unnamed helper applied with the media feature: `AddAudioPartnersPatch` depends on it, so
 * ticking that row is enough. Whether a given player then answers the media-browser handshake is a
 * device question - this patch removes Waze's refusal, it does not make the partner cooperate.
 */
private val PartnerAvailabilityFingerprint = Fingerprint(
    definingClass = "Lcom/waze/sdk/au;",
    name = "g",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "Z",
)

private const val SPOTIFY = "com.spotify.music"

@Suppress("unused")
val mediaPartnerAvailabilityPatch = bytecodePatch(
    name = null,
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)
    execute {
        val method = PartnerAvailabilityFingerprint.method
        val instructions = method.implementation?.instructions?.toList()
            ?: throw PatchException("mediaPartnerAvailabilityPatch: au.g(Context) has no implementation")

        val literal = instructions.withIndex().filter { (_, instruction) ->
            instruction.opcode == Opcode.CONST_STRING &&
                (instruction as? ReferenceInstruction)?.reference?.let { (it as? StringReference)?.string } == SPOTIFY
        }.map { it.index }
        if (literal.size != 1) {
            throw PatchException(
                "mediaPartnerAvailabilityPatch: expected one \"$SPOTIFY\" literal in au.g, found ${literal.size}"
            )
        }
        val result = instructions.withIndex().firstOrNull { (index, instruction) ->
            index > literal.single() && instruction.opcode == Opcode.MOVE_RESULT
        }?.index ?: throw PatchException("mediaPartnerAvailabilityPatch: no move-result after the Spotify check")
        val branch = instructions.getOrNull(result + 1)
        if (branch?.opcode != Opcode.IF_EQZ) {
            throw PatchException(
                "mediaPartnerAvailabilityPatch: the Spotify check is not followed by a branch - " +
                    "refusing to edit an availability gate this build no longer has"
            )
        }

        // Force the comparison's result: every partner is now checked for being installed, like Spotify.
        method.replaceInstruction(result, "const/4 v1, 0x1")
        println("mediaPartnerAvailabilityPatch: partner availability now accepts any installed app")
    }
}