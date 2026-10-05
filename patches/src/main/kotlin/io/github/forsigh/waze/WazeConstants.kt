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
            // Confirmed target.
            AppTarget(version = "5.24.4.900"),
            // Experimental: attempt on any version / any architecture.
            AppTarget(version = null, isExperimental = true),
        ),
    )
}
