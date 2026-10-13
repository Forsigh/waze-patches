package io.github.forsigh.waze

import app.morphe.patcher.patch.resourcePatch
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE
import org.w3c.dom.Element

/**
 * Makes Waze able to see media apps it is not a partner of.
 *
 * This is not a Waze bug and not something the bytecode can work around: since Android 11 (API 30) the
 * system answers `PackageManager` queries only for packages the caller is allowed to see, and what a
 * caller is allowed to see comes from `<queries>` in its manifest. Waze (targetSdk 36) declares:
 *
 *     <queries>
 *       <provider .../>                                  androidx.car.app connection
 *       <package android:name="com.google.android.play..."/>   Google quick search
 *       <package android:name="com.spotify.music"/>            Spotify, hardcoded
 *       <intent><action android:name="com.waze.sdk.audio.ACTION_INIT"/></intent>   its own Audio Kit
 *       ... TTS, recognition, SEND, IMAGE_CAPTURE ...
 *     </queries>
 *
 * with no `QUERY_ALL_PACKAGES`. So Waze can see Spotify and apps that implement the Audio Kit, and
 * nothing else - YouTube Music and every ordinary media app are invisible to it. That is why every code
 * patch aimed at the partner picker resolved to nothing: Waze asked "is this package a usable music
 * partner?" and the system answered "no such package", no matter which package the code asked about.
 *
 * Declaring the standard `android.media.browse.MediaBrowserService` action - the same action Android Auto
 * uses to find media apps - makes every app that ships a media browser service visible to Waze. The
 * option to allow all packages outright is the blunt instrument for anything that still does not show up.
 */
@Suppress("unused")
val mediaAppVisibilityPatch = resourcePatch(
    // Not listed in the patch list: this is the manifest half of "Add your own media apps as Waze audio
    // partners" and is pulled in as its dependency. Its three options are registered on that patch, so the
    // whole media feature is configured from one row instead of three.
    name = null,
    description = "Android hides other apps from Waze unless Waze's manifest asks for them, so its audio " +
        "player can only ever offer Spotify and its Audio Kit partners. This declares the standard media " +
        "browser service in Waze's manifest, making ordinary media apps visible to it - YouTube Music, " +
        "audiobook and podcast players included.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    execute {
        val action = mediaBrowserActionOption.value?.trim().orEmpty()
        val extras = alsoAllowPackageOption.value.orEmpty()
            .split('\n', ',')
            .map(String::trim)
            .filter { it.isNotEmpty() }


        document("AndroidManifest.xml").use { dom ->
            val manifest = dom.documentElement
            val queries = (dom.getElementsByTagName("queries").item(0) as? Element)
                ?: dom.createElement("queries").also { manifest.appendChild(it) }

            // Comparing against what is already declared keeps the patch idempotent: applying it twice
            // must not leave two identical <intent> blocks in the manifest.
            fun declaredActions(): Set<String> {
                val actions = mutableSetOf<String>()
                val intents = queries.getElementsByTagName("intent")
                for (i in 0 until intents.length) {
                    val intent = intents.item(i) as? Element ?: continue
                    val inner = intent.getElementsByTagName("action")
                    for (j in 0 until inner.length) {
                        (inner.item(j) as? Element)
                            ?.getAttribute("android:name")
                            ?.takeIf(String::isNotEmpty)
                            ?.let(actions::add)
                    }
                }
                return actions
            }

            fun declaredPackages(): Set<String> {
                val packages = mutableSetOf<String>()
                val nodes = queries.getElementsByTagName("package")
                for (i in 0 until nodes.length) {
                    (nodes.item(i) as? Element)
                        ?.getAttribute("android:name")
                        ?.takeIf(String::isNotEmpty)
                        ?.let(packages::add)
                }
                return packages
            }

            if (allowAllPackagesOption.value == true) {
                val permissions = dom.getElementsByTagName("uses-permission")
                var already = false
                for (i in 0 until permissions.length) {
                    val name = (permissions.item(i) as? Element)?.getAttribute("android:name")
                    if (name == "android.permission.QUERY_ALL_PACKAGES") already = true
                }
                if (!already) {
                    val permission = dom.createElement("uses-permission")
                    permission.setAttribute("android:name", "android.permission.QUERY_ALL_PACKAGES")
                    // straight after the last existing permission, where a manifest would put it
                    val last = permissions.item(permissions.length - 1) as? Element
                    if (last != null) manifest.insertBefore(permission, last.nextSibling)
                    else manifest.insertBefore(permission, manifest.firstChild)
                    println("mediaAppVisibilityPatch: added QUERY_ALL_PACKAGES")
                } else {
                    println("mediaAppVisibilityPatch: QUERY_ALL_PACKAGES already declared")
                }
            }

            if (action.isNotEmpty() && action !in declaredActions()) {
                val intent = dom.createElement("intent")
                val actionElement = dom.createElement("action")
                actionElement.setAttribute("android:name", action)
                intent.appendChild(actionElement)
                queries.appendChild(intent)
                println("mediaAppVisibilityPatch: declared $action in <queries>")
            }

            val known = declaredPackages()
            extras.filterNot(known::contains).forEach { packageName ->
                val element = dom.createElement("package")
                element.setAttribute("android:name", packageName)
                queries.appendChild(element)
                println("mediaAppVisibilityPatch: declared package $packageName")
            }

            println("mediaAppVisibilityPatch: <queries> now declares ${declaredActions().size} action(s) " +
                "and ${declaredPackages().size} package(s)")
        }
    }
}
