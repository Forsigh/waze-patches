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
> `Applying N patches` in the log. Two patches are **off by default**: *Allow high refresh rate* (costs
> battery and heat) and *Disable Play Store updates* (locks the store out of updating Waze).

### Implemented patches

Each patch below is selected on its own, the usual Morphe way. Most of them hook Waze's typed config
getters (`com.waze.config.ConfigValues`): Boolean keys use the Boolean getter (`com/waze/config/b`), Long
keys the Long getter (`com/waze/config/c`), String keys the String getter (`com/waze/config/d`). Hooking
the read path means an override survives Waze re-syncing config from the server.

Every one of them records what it wants forced and lets the config writer apply it — exactly one block
per getter, because two blocks in one method leave the first block's branch targets pointing at the wrong
instruction and the runtime verifier then rejects the class. The writer is not a row of its own: it has no
name, is applied automatically, and every patch that records a key declares it as a dependency, so ticking
one patch alone still applies it and a key can never be collected and never written. The Morphe Music
patch works the same way with its manifest half, which is why that feature is one row rather than two.

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
- **Disable Play Store updates** — the Play Store always sees your installed copy as newer, so it never offers or applies an update; the lock value advances on every re-patch and Waze's own version accessor still reports the pre-lock code (off by default).

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
> **[v0.22.5](https://github.com/Forsigh/waze-patches/releases/tag/v0.22.5)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;16 patches total
<details open>
<summary>📦 Waze&nbsp;&nbsp;•&nbsp;&nbsp;16 patches</summary>
<br>

**🎯 Supported versions:**

| 5.24.90.901 | 5.25.0.1 | 5.25.4.900 |
| :---: | :---: | :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Allow high refresh rate](#allow-high-refresh-rate) | Waze rejects any display mode above 60 Hz when it picks the mode for its surface, so a 120 Hz screen is served 60 Hz. This raises that ceiling to the requested rate (120 Hz by default). Costs battery and heat - off by default. | • Target refresh rate (Hz) |
| [Auto zoom](#auto-zoom) | Keeps the auto-zoom mode you pick (no / yes / speed) from being overwritten when Waze syncs its server config. The setting comes from Waze itself; this makes it stick. | • Auto-zoom mode |
| [Average-speed camera alerts](#average-speed-camera-alerts) | Turns on average-speed camera alerts and the recommended speed for the zone. |  |
| [Brief voice guidance](#brief-voice-guidance) | Enables Waze's brief voice-guidance mode, which trims the spoken directions down. |  |
| [Disable Play Store updates](#disable-play-store-updates) | Stops the Play Store from offering or applying updates to Waze: the manifest version code is locked above Play's maximum (2,100,000,000 + minutes), so the store always sees the installed copy as newer than its listing. The value advances on every re-patch, so Waze still refreshes its extracted resources, and Waze's own package-version accessor keeps reporting the pre-lock code. Ported from Morphe's universal "Disable Play Store updates" (GPLv3). |  |
| [Enable police & enforcement alerts](#enable-police-enforcement-alerts) | Turns on Waze's police and enforcement alerts. |  |
| [Extended police & hazard alert distances](#extended-police-hazard-alert-distances) | Alerts for police, accidents and heavy traffic fire earlier, with their pre-alert distances extended. |  |
| [Lane guidance](#lane-guidance) | Turns on lane guidance, including the continue-straight hints. |  |
| [Morphe YouTube Music as a Waze audio partner](#morphe-youtube-music-as-a-waze-audio-partner) | Makes Morphe YouTube Music (app.morphe.android.apps.youtube.music) usable as Waze's audio partner: the package is declared in Waze's manifest so Android's package visibility lets Waze see it, and it is added to the audio-partner table (SdkConfiguration.appConfigs) whenever Waze rebuilds that roster. Ported from Loupric/morphe-waze-music-patch (GPLv3). |  |
| [Radar sound at any speed](#radar-sound-at-any-speed) | Plays the speed-camera alert sound even when you are not over the limit. Waze normally only sounds it when you are. |  |
| [Remove ads](#remove-ads) | Forces Waze's advertising settings off, so the ad slots that respect those values stay empty. |  |
| [Show all cameras & road hazards](#show-all-cameras-road-hazards) | Draws speed cameras, red-light cameras and speed bumps on the map, with their on-route notifications enabled. |  |
| [Speed limit sign always shown](#speed-limit-sign-always-shown) | The speed-limit sign stays on the map, and its manual overrides become available. |  |
| [Speed-limit decrease warnings](#speed-limit-decrease-warnings) | Warns when the posted speed limit drops, with a longer pre-alert distance than stock. |  |
| [Speedometer always on](#speedometer-always-on) | The speedometer stays on screen at any speed, including when stopped. Waze hides it below its own minimum speed otherwise. |  |
| [Unlock driver moods](#unlock-driver-moods) | Waze's mood picker is gated three ways: an eligibility check that accepts only one mood name (and reads the beta-moods flag), a baby-account gate that hides moods, and a filtered native catalogue call. This opens all three, so moods already in the local catalogue - including special and hidden entries - become selectable. Artwork Waze has never downloaded is not invented. |  |

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

* **Morphe YouTube Music as a Waze audio partner** is ported from
  [Loupric/morphe-waze-music-patch](https://github.com/Loupric/morphe-waze-music-patch) (GPLv3, release
  v1.0.2): the manifest `<queries>` declaration, the injection into `SdkConfiguration.updateAppList`, the
  five-argument `au` constructor and the feature bits are their work, credited again in the source.
* **Unlock driver moods** and the resource-refresh half (a raised versionCode so Waze re-extracts its
  bundled assets) are ported from [aaayd/waze-custom-patches](https://github.com/aaayd/waze-custom-patches)
  (GPLv3): moods from `DriverIconsPatch.kt`, re-anchored against this build - their finder matched a
  `(Context, String) -> Z` method by three mood-name strings, which does not exist here, where the same
  method compares against a string resource instead - so the anchors are pinned by class, name and
  signature. The refresh is re-expressed as a manifest-DOM edit so it composes with the other manifest
  patches.
* **Disable Play Store updates** is adapted from
  [MorpheApp/morphe-patches](https://github.com/MorpheApp/morphe-patches) (GPLv3, PR #2470): the
  versionCode lock and the execute/finalize write split are their work, under their preserved notice.
  The Waze adaptation changes the locked value (it advances on every re-patch instead of sitting at the
  maximum, so the skin re-extraction keeps working) and narrows the read preservation to Waze's own
  version accessor.

## Support

Like my work?

<a href="https://buymeacoffee.com/forsigh"><img src="store/buy-me-a-coffee.png" alt="Buy me a coffee" width="200"></a>
