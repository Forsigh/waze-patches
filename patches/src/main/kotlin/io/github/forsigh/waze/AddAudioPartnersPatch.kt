package io.github.forsigh.waze

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE

/**
 * Add your own media apps as Waze audio partners.
 *
 * Waze has no media-app discovery. Proved on 5.25.0.1: `getInstalledPackages`,
 * `getInstalledApplications` and `getAllServices` appear in ZERO files of the APK, so nothing in Waze
 * ever asks the OS what you have installed. The audio-partner list is a table compiled into the app -
 *
 *   `SdkConfiguration.appConfigs`, a `Map<package, com.waze.sdk.au>`, filled in `<clinit>` from literal
 *   descriptors. The complete set of package literals in the entire APK is `com.spotify.music`
 *   (+ `.debug` / `.canary`) and `com.waze.sdk.audio.sample` ("Audio Demo").
 *
 * and the settings list you see with Install buttons is a *different* list: the NavConnect roster, pushed
 * from native code as protobuf titles/icons (`NavConnectNativeManagerBase.onStateChangeJNI([B)` ->
 * `navigate/el` -> `nav_connect/a/e`), composed by Waze's servers and not extensible from the client. This
 * patch targets the table that actually drives audio integration.
 *
 * WHERE THE TABLE COMES FROM (what the first version of this patch got wrong):
 *
 *   `SdkConfiguration.updateAppList()` asks native for a roster
 *   (`NativeManager.configGetSdkConfigurationNTV()`) as parallel arrays - `PackageNames`, `AppNames`,
 *   `Scopes`, `InstallationUrls` - builds a FRESH `HashMap` into `appConfigs` from them, then calls
 *   `savePackageScopesToSharedPref()`, which iterates that map and writes `au.c` (the feature int) per
 *   package into the SharedPreferences file `com.waze.sdk.scopes`.
 *
 * Two consequences, both measured in the stock 5.25.0.1 smali:
 *
 *   1. `updateAppList()` REPLACES `appConfigs` wholesale. Entries added anywhere else - for example in
 *      `getPartnerAudioApps()`, which merely reads the map and filters it - are wiped the next time the
 *      roster refreshes. That is why a build with "successfully injected entries" can still show nothing.
 *   2. The per-package scope that `getCachedPackageScope(Context, String)` later reads back (the SDK
 *      service's `dispatchTransaction` in `com/waze/sdk/aj` is one reader) is the SAME int as `au.c`. A
 *      package absent from `appConfigs` when the save runs has no persisted scope at all.
 *
 * So this patch injects into `updateAppList()`, immediately BEFORE the scope save: only the success path
 * reaches that point, the map has just been built and is non-null, and the method's own save then persists
 * our entries' scope too - listed and allowlisted, and it survives a roster refresh instead of being
 * dropped. The roster is rebuilt whenever `appConfigs` is null, so the entries come back on every rebuild.
 *
 * Injection point and shape, measured - both were got wrong first:
 *
 *   * mid-method insertion, and tail insertion, of a block whose labels branch forward, produced branches
 *     pointing backwards into the native roster build (code unit 0x30) - a VerifyError-class defect. The
 *     block is therefore straight-line, with no labels at all.
 *   * the tail is reachable from the method's "arrays not initialised" bail-outs, where `appConfigs` is
 *     null, so a plain `put` there would throw. The save site is not reachable that way.
 *
 * `au` construction: entries go through `au(String, String, int)`, injected into `SdkConfiguration` itself
 * so the package-private constructor is callable (same package) with no access flag violated. The
 * five-argument overload used by the upstream Loupric patch (`au(String, String, int, String, byte[])`) is
 * a Kotlin default-argument bridge that ignores the byte array and forwards the rest, so nothing is gained
 * by it - confirmed in the stock smali, not assumed.
 *
 * `isAudioSupported(version, package)` is a feature-bitmask test:
 *
 *     0x200000    FEATURE_AUDIO                 -> required, else unsupported
 *     0x10000000  IS_AUDIO_PARTNER_INTERNAL_TESTING -> if set, the package must equal the
 *                                                    partner-testing config value or it is rejected
 *
 * We set exactly `0x200000`: it passes unconditionally and claims nothing about another app. The
 * internal-testing bit is Waze's own narrow partner-under-test path and is not used. The low bit the
 * upstream patch carries (0x200001) is not read by `isAudioSupported` and its meaning elsewhere is
 * unproven, so it is not carried over.
 *
 * Technique credit: Loupric/morphe-waze-music-patch (GPLv3) reached the same seam from the other side -
 * hooking `updateAppList` and treating the package-scope check as the thing to satisfy. See
 * `upstream-port/CREDITS.txt`.
 *
 * What Waze does with an entry: `com/waze/sdk/a/j->a(pkg)` probes
 * `PackageManager.queryIntentServices(Intent(pkg).setAction("android.media.browse.MediaBrowserService"))`
 * and, on a hit, builds a browser client - the generic `sdk/a/i`, or `sdk/a/k` for Spotify. That probe
 * is a package-visibility query, which is why this patch pairs with "Make Waze see all media apps": the
 * OS will not answer it for a package Waze's manifest does not declare. Both patches are needed for a
 * non-Spotify app to be both listed and reachable.
 *
 * Honest limits: an app only ever appears if it exposes an `android.media.browse.MediaBrowserService`,
 * and playback control is whatever that browser offers. Waze's deeper Audio Kit integration
 * (`com.waze.sdk.audio.ACTION_INIT`, `com.waze.sdk.ISdkService`) is implemented by the *player*; without
 * it Waze gets a media-browser link, not a kit partner. Nothing here can create that half.
 */

/**
 * `SdkConfiguration.updateAppList()` - the roster refresh that rebuilds `appConfigs` and persists each
 * partner's scope. Pinned by its own call to `savePackageScopesToSharedPref`, which is what makes it this
 * method and not a namesake.
 */
object AppListRefreshFingerprint : Fingerprint(
    definingClass = "Lcom/waze/sdk/SdkConfiguration;",
    name = "updateAppList",
    parameters = listOf(),
    returnType = "V",
    filters = listOf(
        methodCall(
            definingClass = "Lcom/waze/sdk/SdkConfiguration;",
            name = "savePackageScopesToSharedPref",
            returnType = "V",
        ),
    ),
)

/** `FEATURE_AUDIO` - the only bit `isAudioSupported` requires for a plain, unconditionally-valid entry. */
private const val FEATURE_AUDIO = 0x200000

/**
 * The manifest dependency's options.
 *
 * Declared here - BEFORE the patch that registers them, because Kotlin initialises top-level properties in
 * file order and a null receiver would fail the build - and registered on `addAudioPartnersPatch`, because
 * `mediaAppVisibilityPatch` has no name and is not listed: the media feature is configured from one row.
 * The dependency reads these same objects at patch time (`Option.value`), so what the user sets here is
 * what its manifest edit uses. Option KEYS are unchanged from when they lived on the manifest patch, so
 * saved selections keep their meaning.
 */
internal val alsoAllowPackageOption = stringOption(
    "alsoAllowPackage", "",
    title = "Extra packages to make visible (one per line)",
    description = "Package names to declare individually, for an app that does not expose a media " +
        "browser service. Leave empty for none.",
)

internal val mediaBrowserActionOption = stringOption(
    "mediaBrowserAction", "android.media.browse.MediaBrowserService",
    title = "Service action to declare",
    description = "The action Waze is allowed to query for. The default is the standard Android " +
        "media browser service, which every player capable of Android Auto support implements.",
)

internal val allowAllPackagesOption = booleanOption(
    "allowAllPackages", false,
    title = "Let Waze see every installed app",
    description = "Adds the QUERY_ALL_PACKAGES permission instead of a targeted declaration. The " +
        "blunt version: use it only if the targeted declaration is not enough, and expect Waze to " +
        "learn about apps it has no use for.",
)

/**
 * smali block putting one entry per configured partner into `appConfigs`.
 *
 * Injected into `SdkConfiguration` itself, so the package-private `au` constructor is callable (same
 * package) and no access flags are violated at runtime.
 *
 * Deliberately STRAIGHT-LINE: no labels, no branches. Measured on 5.25.0.1, a block whose labels branched
 * forward inside this method resolved into branches pointing backwards into the native roster build (code
 * unit 0x30) at both the mid-method and the tail position - a VerifyError-class defect. With no labels
 * there is nothing to mis-resolve.
 *
 * Registers: `updateAppList` declares `.locals 11`, but v1 holds the method's monitor object across the
 * whole body (`monitor-exit v1`), so the block uses v0 (map), v2 (package), v3 (entry), v4 (title) and
 * v5 (features) and leaves v1 untouched. At the injection point - immediately before the scope save,
 * which only the success path reaches - those registers are dead.
 *
 * No `containsKey` guard is needed: the map was created empty a few instructions earlier, so a plain
 * `put` is exactly right. A configured package that Waze also ships (Spotify) is simply replaced by an
 * equivalent entry.
 */
private fun partnerEntries(partners: List<Pair<String, String>>): String = buildString {
    partners.forEach { (pkg, title) ->
        append("sget-object v0, Lcom/waze/sdk/SdkConfiguration;->appConfigs:Ljava/util/Map;\n")
        append("const-string v2, \"$pkg\"\n")
        append("new-instance v3, Lcom/waze/sdk/au;\n")
        append("const-string v4, \"$title\"\n")
        append("const/high16 v5, $FEATURE_AUDIO\n")
        append("invoke-direct {v3, v2, v4, v5}, Lcom/waze/sdk/au;-><init>(Ljava/lang/String;Ljava/lang/String;I)V\n")
        append("invoke-interface {v0, v2, v3}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n")
    }
}

@Suppress("unused")
val addAudioPartnersPatch = bytecodePatch(
    name = "Add your own media apps as Waze audio partners",
    description = "Waze's audio-partner list is a table compiled into the app - it contains Spotify and " +
        "an internal demo, and Waze never scans installed apps (getInstalledPackages has zero references " +
        "in the APK). One patch for the whole media feature: it adds your own entries to that table at the " +
        "point where Waze persists each partner's scope, and declares the media browser service in Waze's " +
        "manifest, without which Android hides your players from Waze entirely. Waze's own " +
        "availability check is opened as well: any installed app qualifies as a partner, and Waze " +
        "reaches it through the generic media-browser client instead of requiring Waze's own SDK.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)

    // The three patches this one cannot work without. Declaring them means the patcher applies them
    // automatically: the manifest declaration makes Waze's MediaBrowserService probe resolve at all,
    // and the emitter is what writes the audio-SDK keys recorded below. Ticking this patch alone used
    // to produce a build where the table had entries and nothing could see them.
    dependsOn(mediaAppVisibilityPatch, configOverridesPatch, mediaPartnerAvailabilityPatch)

    // The manifest half of this feature is a dependency, not a row of its own, so its three options are
    // registered here (declared above as alsoAllowPackageOption, mediaBrowserActionOption and
    // allowAllPackagesOption; mediaAppVisibilityPatch reads their values). Registered first so the option
    // order the listing reports matches the order they appear in this file, which the verifier checks.
    alsoAllowPackageOption()
    mediaBrowserActionOption()
    allowAllPackagesOption()

    val partners by stringOption(
        "partners",
        "app.morphe.android.apps.youtube.music=YouTube Music Morphe, " +
            "ak.alizandro.smartaudiobookplayer=Smart Audiobook Player",
        title = "Partners to add (package=Label)",
        description = "One entry per partner, comma separated, written package=Label. The label defaults " +
            "to the package when omitted. Each app must expose an " +
            "android.media.browse.MediaBrowserService for Waze to be able to reach it.",
    )
    val enableAudioSdk by booleanOption(
        "enableAudioSdk", true,
        title = "Turn Waze's audio-partner feature on",
        description = "Forces CONFIG_VALUE_AUDIO_EXTENSION_SDK_FEATURE_ENABLED and " +
            "CONFIG_VALUE_AUDIO_EXTENSION_SDK_ON to true. Without them Waze builds an empty partner " +
            "list and the entries above would never be reached.",
    )

    // The manifest half of this feature is a dependency, not a row of its own, so its three options are
    // registered above, next to dependsOn.
    execute {
        // "package=Label" pairs, comma separated. A stray blank or a malformed pair must not cost the
        // user the rest of the list, so each is parsed independently and blanks are skipped.
        val entries = partners.orEmpty().split(',')
            .mapNotNull { pair ->
                val packageName = pair.substringBefore('=').trim()
                if (packageName.isEmpty()) return@mapNotNull null
                val label = pair.substringAfter('=', "").trim().ifEmpty { packageName }
                packageName to label
            }

        if (enableAudioSdk == true) {
            recordBoolean(
                "aap",
                true,
                "CONFIG_VALUE_AUDIO_EXTENSION_SDK_FEATURE_ENABLED",
                "CONFIG_VALUE_AUDIO_EXTENSION_SDK_ON",
            )
        }

        if (entries.isEmpty()) {
            println("addAudioPartnersPatch: no partner packages configured, nothing injected")
            return@execute
        }

        val method = AppListRefreshFingerprint.method
        val implementation = method.implementation
            ?: error(
                "addAudioPartnersPatch: updateAppList was found but has no implementation; " +
                    "refusing to report success for a patch that would change nothing",
            )

        // Inject immediately BEFORE the scope save - NOT mid-method, and NOT at the tail.
        //
        // Measured on 5.25.0.1: a labelled block inserted mid-method resolved into branches pointing
        // backwards into the native roster build; the same happened at the tail, and the tail is also
        // reachable from the "arrays not initialised" bail-outs, where appConfigs is null and a plain put
        // would throw. The save site is reached only on the success path, with the map built and non-null,
        // and the block that goes there is straight-line, so there are no labels to mis-resolve.
        //
        // The method's own save then persists our entries' scope, which is what getCachedPackageScope
        // reads back and the SDK service checks - no second save call needed.
        val instructions = implementation.instructions.toList()
        val scopeSave = instructions.indexOfFirst { instruction ->
            instruction.opcode == Opcode.INVOKE_STATIC &&
                instruction is ReferenceInstruction &&
                instruction.reference.toString().contains("savePackageScopesToSharedPref")
        }
        if (scopeSave < 0) {
            error(
                "addAudioPartnersPatch: updateAppList no longer calls savePackageScopesToSharedPref - " +
                    "this is not the roster refresh any more; refusing to inject",
            )
        }

        method.addInstructions(scopeSave, partnerEntries(entries))
        entries.forEach { (pkg, label) ->
            println("addAudioPartnersPatch: added $pkg (\"$label\") to the partner table before the scope save")
        }
    }
}
