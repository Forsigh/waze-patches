# 🧩 Forsigh Waze Patches

Unofficial [Morphe](https://morphe.software) patches for **Waze** (`com.waze`).

These patches were derived by reverse-engineering a "Magical Unicorn" Waze MOD build against the
matching stock APK, then re-expressing the changes as Morphe patches that hook Waze's numeric
config getter (`com.waze.config.ConfigValues`).

## ➕ Add to Morphe

**One tap** (open on the phone that runs Morphe Manager):
[**Add to Morphe**](https://morphe.software/add-source?github=Forsigh/waze-patches)

**Manually:** Morphe Manager → patch sources **+** → **Remote** → paste
`https://github.com/Forsigh/waze-patches`

Then select Waze in the patcher, enable the patches you want, and patch the stock APK. These are dex-only
patches: no root, no PC, no custom keystore. The patched build is re-signed, so uninstall any existing
Waze (or install it as a separate copy) before installing.

> Unofficial community bundle, GPLv3, not affiliated with Waze or Morphe. Targets the Waze versions in
> the patch list below — a build outside them is skipped rather than half-patched, so check
> `Applying N patches` in the log. Four patches are **off by default**: *Allow high refresh rate*, which
> costs battery and heat; *Map theme* and *OLED dark mode*, which are visual choices; and *Add your own
> media apps as Waze audio partners*, whose handshake has not yet been confirmed on a device.

### Implemented patches

Each patch below is selected on its own, the usual Morphe way. They all hook Waze's typed config
getters (`com.waze.config.ConfigValues`): Boolean keys use the Boolean getter (`com/waze/config/b`), Long
keys the Long getter (`com/waze/config/c`), String keys the String getter (`com/waze/config/d`). Hooking
the read path means an override survives Waze re-syncing config from the server.

Every one of them records what it wants forced and lets the config writer apply it — exactly one block
per getter, because two blocks in one method leave the first block's branch targets pointing at the wrong
instruction and the runtime verifier then rejects the class. The writer is not a row of its own: it has no
name, is applied automatically, and every patch that records a key declares it as a dependency, so ticking
one patch alone still applies it and a key can never be collected and never written. The media patch works
the same way with the manifest fix, which is why the media feature is one row rather than three.

- **Speedometer** — visible at any speed, including while stopped (minimum speed → 0).
- **Speed limit sign** — forces the sign on and enables its manual overrides.
- **Speed-limit warnings** — warns when the speed limit drops, with an extended pre-alert distance.
- **Cameras & road hazards** — speed/red-light cameras and speed bumps, with their notifications.
- **Alert distances** — wider pre-alert distances for police enforcement, accidents and heavy traffic.
- **Police & enforcement alerts** — turns on Waze's police/enforcement alert system.
- **Radar sound at any speed** — plays speed-camera sound alerts even below the limit.
- **Average-speed cameras** — average-speed camera alerts plus recommended-speed guidance.
- **Brief voice guidance** — enables brief voice guidance.
- **Lane guidance** — enables lane guidance, including continue-straight hints.
- **Auto zoom** — pins Waze's own auto-zoom setting (no / yes / speed) across server config sync.
- **Remove ads** — forces Waze's advertising config values off.

### Installing, and Android Auto visibility

Installing the patched APK needs no special tool — any installer works. You only have to uninstall
the Play Store Waze first, because the patched build is signed with a different key
(`INSTALL_FAILED_UPDATE_INCOMPATIBLE` otherwise). KingInstaller does not bypass that either.

**Android Auto visibility** is the part that historically needed KingInstaller, and it is not
patchable. Android Auto reads the system's `installerPackageName` record, which the installer writes
at install time, so no bytecode patch inside Waze can reach it — Waze's only
`getInstallerPackageName` call sites are in Firebase Crashlytics, and it never uses
`InstallSourceInfo` at all.

Set it at install time instead. In Morphe Manager, enable **Install as Play Store** for the install
method you use (*Play Store install* / *Root Play Store install* / *Shizuku Play Store install*) —
it records Google Play Store as the install source, the same mechanism KingInstaller's non-root path
uses. Morphe's own warning applies: it helps Android Auto recognize the app but is not guaranteed on
every app or device, and the Play Store may later offer an update that overwrites the patched APK
(turn updates off for Waze).

Fallback only, if your device's installer ignores it: **KingInstaller** (Shizuku, else root;
Xiaomi/HyperOS effectively needs root). Its rule of thumb: the app's "Installed by" must be the
Play Store.

### Planned (not yet implemented)

- Bigger speed-limit box and speedometer (resource patch on `res/layout/speedometer.xml`).
- Extend **Cameras & road hazards** to every hazard class Waze defines: stop-sign, seatbelt,
  safe-distance, bus/HOV/carpool-lane and mobile-phone cameras, police, animals, road construction.
- A **quiet dialogs** switch: stop the beacons promo, the background-location dialog, the navigation
  settings popup, the Assistant onboarding and the NavConnect tooltip from reappearing.
- **Engine-read keys** we do not force yet — ghost mode (`REALTIME_INVISIBLE_MODE`), keep-display-on and
  default routing avoidances — the only class where an override provably reaches the engine, which looks
  configuration up by numeric ID.

> ⚠️ Best-effort. Every injected config key is verified to exist and to match its getter's type, but
> whether forcing it changes behaviour is unverified until a patched APK is tested on a device. Ten keys
> behind the alerting switches (alert distances, enforcement alerts, radar sound, average-speed cameras,
> brief guidance) have no reader anywhere in Waze's own Java code: the engine reads configuration by
> numeric ID, so a static check cannot tell whether it acts on them. See `PATCH-AUDIT.md`.

### How to use these patches

Add this repository as a Morphe patch source, or download the `.mpp` from Releases and add it as a
local source: https://morphe.software/add-source?github=Forsigh/waze-patches

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v0.26.0](https://github.com/Forsigh/waze-patches/releases/tag/v0.26.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;17 patches total
<details open>
<summary>📦 Waze&nbsp;&nbsp;•&nbsp;&nbsp;17 patches</summary>
<br>

**🎯 Supported versions:**

| 5.24.90.901 | 5.25.0.1 |
| :---: | :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Allow high refresh rate](#allow-high-refresh-rate) | Waze rejects any display mode above 60 Hz when it picks the mode for its surface, so a 120 Hz screen is served 60 Hz. This raises that ceiling to the requested rate (120 Hz by default). Costs battery and heat - off by default. | • Target refresh rate (Hz) |
| [Auto zoom](#auto-zoom) | Pins Waze's own auto-zoom setting (no / yes / speed) and keeps it across server config sync. Waze ships the row itself; this makes the choice stick. | • Auto-zoom mode |
| [Average-speed camera alerts](#average-speed-camera-alerts) | Enables average-speed camera alerts and recommended-speed guidance. |  |
| [Brief voice guidance](#brief-voice-guidance) | Enables Waze's brief voice-guidance mode. |  |
| [Enable police & enforcement alerts](#enable-police-enforcement-alerts) | Turns on Waze's police/enforcement alert system. |  |
| [Extended police & hazard alert distances](#extended-police-hazard-alert-distances) | Increases the pre-alert distances for police enforcement, accidents and heavy traffic. |  |
| [Lane guidance](#lane-guidance) | Enables lane guidance, including continue-straight hints. |  |
| [Map theme](#map-theme) | Recolours Waze's own map skin - light and dark palettes sampled from Google Maps, or an OLED-black palette for both modes - by rewriting the compiled skin descriptions in the APK and raising the app's versionCode so Waze re-extracts its skins instead of drawing the cached originals. Applied at patch time: change the theme by re-patching. No injected code. Off by default. | • Theme |
| [Radar sound at any speed](#radar-sound-at-any-speed) | Plays speed-camera sound alerts even when not above the limit. |  |
| [Remove ads](#remove-ads) | Forces Waze's advertising config values off. |  |
| [Show all cameras & road hazards](#show-all-cameras-road-hazards) | Shows speed cameras, red-light cameras and speed bumps on the map and enables their notifications. |  |
| [Speed limit sign always shown](#speed-limit-sign-always-shown) | Forces the speed-limit sign on and enables the manual overrides. |  |
| [Speed-limit decrease warnings](#speed-limit-decrease-warnings) | Enables warnings when the speed limit drops, with an extended pre-alert distance. |  |
| [Speedometer always on](#speedometer-always-on) | Keeps the speedometer visible at any speed, including while stopped. |  |
| [Unlock driver moods](#unlock-driver-moods) | Waze's mood picker is gated three ways: an eligibility check that accepts only one mood name (and reads the beta-moods flag), a baby-account gate that hides moods, and a filtered native catalogue call. This opens all three, so moods already in the local catalogue become selectable, special and hidden entries included. Artwork Waze has never downloaded is not invented. |  |
| [Add your own media apps as Waze audio partners](#add-your-own-media-apps-as-waze-audio-partners) | Waze's audio-partner list is a table compiled into the app - it contains Spotify and an internal demo, and Waze never scans installed apps. This adds your own entries to that table, and declares the media browser service in Waze's manifest, because Android hides other apps from Waze until it asks. One patch for the whole media feature: the manifest half and the config writer are applied with it and are not listed separately. Waze's own availability check is opened as well: any installed app now qualifies as a partner, not only apps that implement Waze's SDK. Waze then connects to the app through its media-browser service, the same generic client Spotify's integration extends. | • Partners to add (package=Label)<br>• Turn Waze's audio-partner feature on<br>• Extra packages to make visible (one per line)<br>• Service action to declare<br>• Let Waze see every installed app |
| [OLED dark mode](#oled-dark-mode) | Turns the dark map into an OLED map: the night background becomes pure black (#000000) and the road colours are lifted one Waze lighten step so they stay readable against it. Day mode, labels, traffic and routes are untouched. Off by default; if Map theme is also selected, this one steps aside for it. |  |

</details>

<!-- PATCHES_END -->

### 🛠️ Building locally

- Run `./gradlew buildAndroid`
- The built patches .mpp file is found in `patches/build/libs/patches-*.mpp`
- Patch the mpp file using [Morphe-Desktop](https://github.com/MorpheApp/morphe-desktop)
  like any other patch bundle.

See the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation) for more information.

## 📜 License

Forsigh Waze Patches are licensed under the [GNU General Public License v3.0](LICENSE).

## 🙏 Credits

* **Map theme** and its palette data are ported from [aaayd/waze-custom-patches](https://github.com/aaayd/waze-custom-patches)
  (release `waze-5.25.0.1-1030736-patches-1.15`, GPLv3): the palette substitution over Waze's compiled skin
  descriptions, the renderer colour overrides, the versionCode refresh that makes Waze re-extract its skins,
  and the `.properties` palettes themselves (sampled from Google Maps captures by their generator script),
  which ship byte-for-byte.
* The audio-partner technique (injecting into `SdkConfiguration.updateAppList` ahead of the scope save) was
  reverse-engineered from [Loupric/morphe-waze-music-patch](https://github.com/Loupric/morphe-waze-music-patch) (GPLv3).
* **Unlock driver moods** is ported from aaayd/waze-custom-patches (GPLv3, `DriverIconsPatch.kt`), re-anchored
  against this build: their finder matched a `(Context, String) -> Z` method by three mood-name strings, which
  does not exist here - the same method compares against a string resource instead - so the anchors are pinned
  by class, name and signature.

## Support

Like my work?

<a href="https://buymeacoffee.com/forsigh"><img src="store/buy-me-a-coffee.png" alt="Buy me a coffee" width="200"></a>
