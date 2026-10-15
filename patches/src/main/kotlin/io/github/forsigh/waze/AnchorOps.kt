package io.github.forsigh.waze

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod

/**
 * Multi-version anchor resolution.
 *
 * Waze re-letters obfuscated classes between releases. 5.25.90.900 moved the welcome state from
 * g/w to g/v, the privacy state from g/q to g/p, the privacy consent impl from bg/b to bi/b, the
 * ND4C consent impl from az/a to ba/a, the age-state factory from b/k to b/l, and renamed
 * MoodManager.canSetMood to t() and refreshMoodsList to r() (dropping the Java isBaby() entirely -
 * the native is now read inline in p()).
 *
 * Each affected patch therefore declares one fingerprint per known shape and asks for the method
 * through [resolveAnchor], which returns the first candidate that binds.
 *
 * Order the candidates oldest-first. The anchor that is precise on the older builds must be tried
 * first: a stray namesake on a newer build (g/v exists on every build as *some* state) must never
 * shadow an intended match. On a build where the old shape no longer exists its match throws, the
 * throw is swallowed here, and the next candidate is tried.
 */

/** The first candidate fingerprint that binds on this build, or null when none do. */
internal fun BytecodePatchContext.boundMethodOrNull(vararg candidates: Fingerprint): MutableMethod? =
    candidates.firstNotNullOfOrNull { fingerprint ->
        try {
            fingerprint.method
        } catch (_: PatchException) {
            null
        }
    }

/** Like [boundMethodOrNull], but fails the patch with [label] when no candidate binds. */
internal fun BytecodePatchContext.resolveAnchor(
    label: String,
    vararg candidates: Fingerprint,
): MutableMethod = boundMethodOrNull(*candidates)
    ?: throw PatchException("$label: none of the ${candidates.size} known anchor shapes matched this build")
