package io.github.forsigh.waze

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

/**
 * App compatibility declarations for Waze (com.waze).
 *
 * The confirmed target is the exact build these patches were developed against,
 * plus an experimental "any version" target so the patches can still be attempted on
 * newer builds (fingerprints may or may not survive an app update).
 */
object WazeConstants {
    val COMPATIBILITY_WAZE = Compatibility(
        name = "Waze",
        packageName = "com.waze",
        // Waze is distributed on APKMirror as an APKM bundle. Kept in sync so Morphe Manager
        // redirects the user to the correct download page.
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x33CCFF,
        targets = listOf(
            // Confirmed target: the production build every config key was verified against
            // (versionCode 1030734). Waze uses a different versionName scheme for beta vs
            // production builds: the beta MOD this project was reverse-engineered from reports
            // versionName 5.24.4.900 with versionCode 1030731, i.e. the two are adjacent
            // builds - not a different release. Declaring the beta name made Morphe skip
            // every patch on the production APK, so the production name is the confirmed one.
            AppTarget(version = "5.24.90.901"),
            // Second confirmed target (versionCode 1030736). Verified the same way as the first: the
            // bundle applies WITHOUT -f, every hooked getter comes out structurally identical
            // (57/33/7 instructions, no invalid branches), and all 30 forced CONFIG_VALUE_* keys
            // still exist in its dex. Without this entry Morphe skips all 13 patches silently there.
            AppTarget(version = "5.25.0.1"),
            // Experimental: attempt on any version / any architecture.
            AppTarget(version = null, isExperimental = true),
        ),
    )

    /**
     * Audio-partner remap. Waze resolves an audio partner by asking PackageManager for the roster's
     * package; Morphe's YouTube Music build is installed under its own package name, so Waze never
     * sees it. These are the two ends of that rewrite (the target is configurable at patch time).
     */
    const val OFFICIAL_YOUTUBE_MUSIC = "com.google.android.apps.youtube.music"
    const val MORPHE_YOUTUBE_MUSIC = "app.morphe.android.apps.youtube.music"
}
