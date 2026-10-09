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
> `Applying N patches` in the log. "Wire Morphe YouTube Music into Waze" is **experimental and off by
> default**: it rewrites the package Waze asks for so a renamed Morphe build of YouTube Music is seen as
> the audio partner, and that handshake has not yet been confirmed on a device.

### Implemented patches

Every patch hooks Waze's typed config getters (`com.waze.config.ConfigValues`): Boolean keys use the
Boolean getter (`com/waze/config/b`), Long keys the Long getter (`com/waze/config/c`). Hooking the
read path means the override survives Waze re-syncing config from the server.

- **Remove ads** — forces Waze's advertising config values off.
- **Extended police & hazard alert distances** — widens the pre-alert distances.
- **Enable police & enforcement alerts** — turns on the police/enforcement alert system.
- **Speedometer always on** — keeps the speedometer visible at any speed (minimum speed → 0).
- **Speed limit sign always shown** — forces the speed-limit sign and its override on.
- **Radar sound at any speed** — plays speed-camera sound alerts even below the limit.
- **Show all cameras & road hazards** — speed/red-light cameras and speed bumps, with notifications.
- **Average-speed camera alerts** — average-speed cameras plus recommended-speed guidance.
- **Speed-limit decrease warnings** — warns when the speed limit drops.
- **Brief voice guidance** — enables brief voice guidance.
- **Lane guidance** — enables lane guidance.

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

> ⚠️ Best-effort. Every injected config key is verified to exist and to match its getter's type, but
> whether forcing it changes behaviour is unverified until a patched APK is tested on a device.

### How to use these patches

Add this repository as a Morphe patch source, or download the `.mpp` from Releases and add it as a
local source: https://morphe.software/add-source?github=Forsigh/waze-patches

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v0.20.2](https://github.com/Forsigh/waze-patches/releases/tag/v0.20.2)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;14 patches total
<details open>
<summary>📦 Waze&nbsp;&nbsp;•&nbsp;&nbsp;14 patches</summary>
<br>

**🎯 Supported versions:**

| 5.24.90.901 | 5.25.0.1 |
| :---: | :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Auto zoom](#auto-zoom) | Pins Waze's own auto-zoom setting (no / yes / speed) and keeps it across server config sync. Waze ships the row itself; this makes the choice stick. | • Auto-zoom mode |
| [Average-speed camera alerts](#average-speed-camera-alerts) | Enables average-speed camera alerts and recommended-speed guidance. |  |
| [Brief voice guidance](#brief-voice-guidance) | Enables Waze's brief voice-guidance mode. |  |
| [Config overrides](#config-overrides) | Applies the configuration changes requested by the other patches. Applied automatically; there is nothing to configure. |  |
| [Enable police & enforcement alerts](#enable-police-enforcement-alerts) | Turns on Waze's police/enforcement alert system. |  |
| [Extended police & hazard alert distances](#extended-police-hazard-alert-distances) | Increases the pre-alert distances for police enforcement, accidents and heavy traffic. |  |
| [Lane guidance](#lane-guidance) | Enables lane guidance, including continue-straight hints. |  |
| [Radar sound at any speed](#radar-sound-at-any-speed) | Plays speed-camera sound alerts even when not above the limit. |  |
| [Remove ads](#remove-ads) | Forces Waze's advertising config values off. |  |
| [Show all cameras & road hazards](#show-all-cameras-road-hazards) | Shows speed cameras, red-light cameras and speed bumps on the map and enables their notifications. |  |
| [Speed limit sign always shown](#speed-limit-sign-always-shown) | Forces the speed-limit sign on and enables the manual overrides. |  |
| [Speed-limit decrease warnings](#speed-limit-decrease-warnings) | Enables warnings when the speed limit drops, with an extended pre-alert distance. |  |
| [Speedometer always on](#speedometer-always-on) | Keeps the speedometer visible at any speed, including while stopped. |  |
| [Wire Morphe YouTube Music into Waze](#wire-morphe-youtube-music-into-waze) | Makes Waze's audio player look for your Morphe YouTube Music instead of the official package, so the Morphe build is detected and launched. | • Official YouTube Music package<br>• Your Morphe YouTube Music package |

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
