# 🧩 Forsigh Waze Patches

Unofficial [Morphe](https://morphe.software) patches for **Waze** (`com.waze`). Personal use.

These patches were derived by reverse-engineering a "Magical Unicorn" Waze MOD build against the
matching stock APK, then re-expressing the changes as Morphe patches that hook Waze's numeric
config getter (`com.waze.config.ConfigValues`).

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

### Planned (not yet implemented)

- Bigger speed-limit box and speedometer (resource patch on `res/layout/speedometer.xml`).

> ⚠️ Best-effort. Every injected config key is verified to exist and to match its getter's type, but
> whether forcing it changes behaviour is unverified until a patched APK is tested on a device.

### How to use these patches

Add this repository as a Morphe patch source, or download the `.mpp` from Releases and add it as a
local source: https://morphe.software/add-source?github=Forsigh/waze-patches

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.1.1](https://github.com/Forsigh/waze-patches/releases/tag/v1.1.1)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;11 patches total
<details open>
<summary>📦 Waze&nbsp;&nbsp;•&nbsp;&nbsp;11 patches</summary>
<br>

**🎯 Supported versions:**

| 5.24.4.900 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Average-speed camera alerts](#average-speed-camera-alerts) | Enables average-speed camera alerts and recommended-speed guidance. |  |
| [Brief voice guidance](#brief-voice-guidance) | Enables Waze's brief voice-guidance mode. |  |
| [Enable police & enforcement alerts](#enable-police-enforcement-alerts) | Turns on Waze's police/enforcement alert system. |  |
| [Extended police & hazard alert distances](#extended-police-hazard-alert-distances) | Increases the pre-alert distances for police enforcement, accidents and heavy traffic. |  |
| [Lane guidance](#lane-guidance) | Enables lane guidance, including continue-straight hints. |  |
| [Radar sound at any speed](#radar-sound-at-any-speed) | Plays speed-camera sound alerts even when not above the limit. |  |
| [Remove ads](#remove-ads) | Forces Waze's advertising config values off. |  |
| [Show all cameras & road hazards](#show-all-cameras-road-hazards) | Shows speed cameras, red-light cameras and speed bumps on the map and enables their notifications. |  |
| [Speed limit sign always shown](#speed-limit-sign-always-shown) | Forces the speed-limit sign on and enables the manual overrides. |  |
| [Speed-limit decrease warnings](#speed-limit-decrease-warnings) | Enables warnings when the speed limit drops, with an extended pre-alert distance. |  |
| [Speedometer always on](#speedometer-always-on) | Keeps the speedometer visible at any speed, including while stopped. |  |

</details>

<!-- PATCHES_END -->

### 🛠️ Building locally

- Run `./gradlew buildAndroid`
- The built patches .mpp file is found in `patches/build/libs/patches-*.mpp`
- Patch the mpp file using [Morphe-Desktop](https://github.com/MorpheApp/morphe-desktop)
  like any other patch bundle.

See the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation) for more information.

## 📜 License

UserXYZ Patches are licensed under the [GNU General Public License v3.0](LICENSE)
