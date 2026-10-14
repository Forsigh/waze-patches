# 🧩 Forsigh Waze Patches

Morphe patches for **Waze** (`com.waze`).

## ➕ Add to Morphe

**One tap** (open on the phone that runs Morphe Manager):
[**Add to Morphe**](https://morphe.software/add-source?github=Forsigh/waze-patches)

**Manually:** Morphe Manager → patch sources **+** → **Remote** → paste
`https://github.com/Forsigh/waze-patches`

Uninstall the Play Store version of Waze first — the patched build is signed with a different key.
For Android Auto visibility, install with Morphe Manager's **Install as Play Store** option (or
KingInstaller as a fallback).

> GPLv3 · Not affiliated with Waze. Targets the Waze versions in the patch list below.
> Five rows are **off by default**: *Allow high refresh rate*, *Disable Play Store updates*,
> *Skip sign-in*, *Theme* and *UI AMOLED dark mode*.

## 🩹 Patches

<!-- PATCHES_START EXPANDED -->
> **[v0.22.6](https://github.com/Forsigh/waze-patches/releases/tag/v0.22.6)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;19 patches total
<details open>
<summary>📦 Waze&nbsp;&nbsp;•&nbsp;&nbsp;19 patches</summary>
<br>

**🎯 Supported versions:**

| 5.24.90.901 | 5.25.0.1 | 5.25.4.900 |
| :---: | :---: | :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Allow high refresh rate](#allow-high-refresh-rate) | Runs Waze above 60 Hz, up to your screen's refresh rate. | • Target refresh rate (Hz) |
| [Auto zoom](#auto-zoom) | Keeps your chosen auto-zoom mode from being reset. | • Auto-zoom mode |
| [Average-speed camera alerts](#average-speed-camera-alerts) | Average-speed camera alerts, with the zone's recommended speed. |  |
| [Brief voice guidance](#brief-voice-guidance) | Shorter spoken directions. |  |
| [Disable Play Store updates](#disable-play-store-updates) | Stops the Play Store from updating Waze. |  |
| [Enable police & enforcement alerts](#enable-police-enforcement-alerts) | Turns on police and enforcement alerts. |  |
| [Extended police & hazard alert distances](#extended-police-hazard-alert-distances) | Pick how early police and hazard alerts fire. | • Alert distance level |
| [Lane guidance](#lane-guidance) | Lane guidance, including continue-straight hints. |  |
| [Morphe YouTube Music as a Waze audio partner](#morphe-youtube-music-as-a-waze-audio-partner) | Lets Morphe YouTube Music work as Waze's audio partner. |  |
| [Radar sound at any speed](#radar-sound-at-any-speed) | Plays the speed-camera sound even below the limit. |  |
| [Remove ads](#remove-ads) | Removes ads from Waze. |  |
| [Show all cameras & road hazards](#show-all-cameras-road-hazards) | Shows cameras and road hazards on the map, with alerts. |  |
| [Skip sign-in](#skip-sign-in) | Boots straight into the map as a guest. |  |
| [Speed limit sign always shown](#speed-limit-sign-always-shown) | Always shows the speed limit sign. |  |
| [Speed-limit decrease warnings](#speed-limit-decrease-warnings) | Warns when the speed limit drops. |  |
| [Speedometer always on](#speedometer-always-on) | Visible at any speed. |  |
| [Theme](#theme) | Pick any theme for the map - day and night. | • Day theme<br>• Night theme |
| [UI AMOLED dark mode](#ui-amoled-dark-mode) | Dark-mode backgrounds go pure black. |  |
| [Unlock driver moods](#unlock-driver-moods) | Unlocks all moods already on the device. |  |

</details>

<!-- PATCHES_END -->

## 🛠️ Building locally

- `./gradlew buildAndroid` → the bundle lands in `patches/build/libs/patches-*.mpp`
- Patch that file with [Morphe Desktop](https://github.com/MorpheApp/morphe-desktop) like any other bundle.

## 📜 License

[GNU General Public License v3.0](LICENSE).

## 🙏 Credits

- **Morphe YouTube Music as a Waze audio partner** — ported from [Loupric/morphe-waze-music-patch](https://github.com/Loupric/morphe-waze-music-patch) (GPLv3).
- **Unlock driver moods** and the resource refresh — ported from [aaayd/waze-custom-patches](https://github.com/aaayd/waze-custom-patches) (GPLv3).
- **Disable Play Store updates** — adapted from [MorpheApp/morphe-patches](https://github.com/MorpheApp/morphe-patches) (GPLv3).

## Support

Like my work?

<a href="https://buymeacoffee.com/forsigh"><img src="store/buy-me-a-coffee.png" alt="Buy me a coffee" width="200"></a>
