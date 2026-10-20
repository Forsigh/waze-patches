# 🧩 Forsigh Waze Patches

Morphe patches for **Waze** (`com.waze`).

## ➕ Add to Morphe

**One tap** (open on the phone that runs Morphe Manager):
[**Add to Morphe**](https://morphe.software/add-source?github=Forsigh/waze-patches)

**Manually:** Morphe Manager → patch sources **+** → **Remote** → paste
`https://github.com/Forsigh/waze-patches`

Uninstall the Play Store version of Waze first. The patched build is signed with a different key.
For Android Auto visibility, install with Morphe Manager's **Install as Play Store** option (or
KingInstaller as a fallback).

> GPLv3 · Not affiliated with Waze. Targets the Waze versions in the patch list below.
> Fifteen rows are **off by default** - *Allow high refresh rate*, *Disable Play Store updates*,
> *Skip sign-in*, *Theme*, *AMOLED Dark Mode For UI*, the four **Android Auto** rows (incl.
> *Keep Waze alive on Android Auto*), the four **Experimental** rows, and *Report speed limit* and *Speed-limit sign style*.

## 🩹 Patches

<!-- PATCHES_START EXPANDED -->
> **[v0.22.9](https://github.com/Forsigh/waze-patches/releases/tag/v0.22.9)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;30 patches total
<details open>
<summary>📦 Waze&nbsp;&nbsp;•&nbsp;&nbsp;30 patches</summary>
<br>

**🎯 Supported versions:**

| 5.24.90.901 | 5.25.0.1 | 5.25.4.900 | 5.25.90.900 |
| :---: | :---: | :---: | :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [AMOLED Dark Mode For UI](#amoled-dark-mode-for-ui) | Dark-mode backgrounds go pure black. |  |
| [Alert distances](#alert-distances) | Choose how early alerts fire: Standard, Moderate, Heavier or Custom. | • Alert distance level<br>• Police<br>• Hazards<br>• Crashes<br>• Traffic jams<br>• Speed cams - streets<br>• Speed cams - highways<br>• Speed cams - freeways<br>• Blocked lanes<br>• Gap between alerts |
| [Allow high refresh rate](#allow-high-refresh-rate) | Runs Waze above 60 Hz, up to your screen's refresh rate. | • Target refresh rate (Hz) |
| [Auto zoom](#auto-zoom) | Keeps your chosen auto-zoom mode from being reset. | • Auto-zoom mode |
| [Average-speed camera alerts](#average-speed-camera-alerts) | Average-speed camera alerts, with the zone's recommended speed. |  |
| [Bigger speedometer & limit sign](#bigger-speedometer-limit-sign) | Scales the speedometer and the speed-limit sign. | • Speedometer size<br>• Speed limit sign size |
| [Brief voice guidance](#brief-voice-guidance) | Shorter spoken directions. |  |
| [Car alerts as notifications](#car-alerts-as-notifications) | Delivers police and hazard alerts as car notifications. |  |
| [Disable Play Store updates](#disable-play-store-updates) | Stops the Play Store from updating Waze. |  |
| [Enable police & enforcement alerts](#enable-police-enforcement-alerts) | Turns on police and enforcement alerts. |  |
| [Instrument cluster support](#instrument-cluster-support) | Shows the map, alerts and ETA on the car's digital cluster, where it has one. |  |
| [Keep Waze alive on Android Auto](#keep-waze-alive-on-android-auto) | Keeps Waze running after you disconnect from Android Auto. | • Minutes alive after disconnect |
| [Keep Waze alive on car](#keep-waze-alive-on-car) | Keeps Waze connected through car service restarts. |  |
| [Lane guidance](#lane-guidance) | Lane guidance, including continue-straight hints. |  |
| [Morphe YouTube Music as a Waze audio partner](#morphe-youtube-music-as-a-waze-audio-partner) | Lets Morphe YouTube Music work as Waze's audio partner. |  |
| [Radar sound at any speed](#radar-sound-at-any-speed) | Plays the speed-camera sound even below the limit. |  |
| [Remove ads](#remove-ads) | Removes ads from Waze. |  |
| [Report speed limit](#report-speed-limit) | Turns on fixing the speed limit from the speedometer. |  |
| [Show FPS counter](#show-fps-counter) | Shows Waze's frames-per-second counter on the map. |  |
| [Show all cameras & road hazards](#show-all-cameras-road-hazards) | Shows cameras and road hazards on the map, with alerts. |  |
| [Skip sign-in](#skip-sign-in) | Boots straight into the map as a guest. |  |
| [Smoother Android Auto map](#smoother-android-auto-map) | Smoothes the car map's movement and heading on Android Auto. |  |
| [Speed limit sign always shown](#speed-limit-sign-always-shown) | Always shows the speed limit sign. |  |
| [Speed-limit decrease warnings](#speed-limit-decrease-warnings) | Warns when the speed limit drops. |  |
| [Speed-limit sign style](#speed-limit-sign-style) | Picks the sign beside the speedometer: US or World. | • Sign style |
| [Speedometer always on](#speedometer-always-on) | Visible at any speed. |  |
| [Stay awake in the background](#stay-awake-in-the-background) | Keeps Waze running in the background without a route. | • Minutes in the background |
| [Theme](#theme) | Pick any theme for the map - day and night. | • Day theme<br>• Night theme |
| [Traffic meter](#traffic-meter) | Shows the traffic meter on the car screen. |  |
| [Unlock driver moods](#unlock-driver-moods) | Unlocks all moods already on the device. |  |

</details>

<!-- PATCHES_END -->

## 🛠️ Building locally

- `./gradlew buildAndroid` → the bundle lands in `patches/build/libs/patches-*.mpp`
- Patch that file with [Morphe Desktop](https://github.com/MorpheApp/morphe-desktop) like any other bundle.

## 📜 License

[GNU General Public License v3.0](LICENSE).

## 🙏 Credits

- **Morphe YouTube Music as a Waze audio partner**: ported from [Loupric/morphe-waze-music-patch](https://github.com/Loupric/morphe-waze-music-patch) (GPLv3), extended here to force Waze's audio-extension switches, so the partner does not depend on Waze's server flags.
- **Unlock driver moods** and the resource refresh: ported from [aaayd/waze-custom-patches](https://github.com/aaayd/waze-custom-patches) (GPLv3).
- **Disable Play Store updates**: adapted from [MorpheApp/morphe-patches](https://github.com/MorpheApp/morphe-patches) (GPLv3).
- **Speed-limit sign style**, **Report speed limit**, **Stay awake in the background**, and the extra keys in **Remove ads**, **Enable police & enforcement alerts** and **Alert distances**: ported from [bearinmindcat/morphe-patches](https://github.com/bearinmindcat/morphe-patches) (GPLv3).

More detail in [CREDITS.md](CREDITS.md).

## Support

Like my work?

<a href="https://buymeacoffee.com/forsigh"><img src="store/buy-me-a-coffee.png" alt="Buy me a coffee" width="200"></a>
