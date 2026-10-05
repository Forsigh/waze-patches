# 🧩 Forsigh Waze Patches

Unofficial [Morphe](https://morphe.software) patches for **Waze** (`com.waze`). Personal use.

These patches were derived by reverse-engineering a "Magical Unicorn" Waze MOD build against the
matching stock APK, then re-expressing the changes as Morphe patches that hook Waze's numeric
config getter (`com.waze.config.ConfigValues`).

### Implemented patches

- **Remove ads** — forces Waze's advertising config values off.
- **Extended police & hazard alert distances** — widens the pre-alert distances.

### Planned (not yet implemented)

- Bigger speed-limit box and speedometer (resource patch on `res/layout/speedometer.xml`).

> ⚠️ Best-effort. Which forced config key actually changes behaviour is unverified until a patched
> APK is tested on a device. Entries read through a non-numeric accessor are not affected.

### How to use these patches

Add this repository as a Morphe patch source, or download the `.mpp` from Releases and add it as a
local source: https://morphe.software/add-source?github=Forsigh/waze-patches

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0](https://github.com/Forsigh/waze-patches/releases/tag/v1.0.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;2 patches total
<details open>
<summary>📦 Waze&nbsp;&nbsp;•&nbsp;&nbsp;2 patches</summary>
<br>

**🎯 Supported versions:**

| 5.24.4.900 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Extended police & hazard alert distances](#extended-police-hazard-alert-distances) | Increases the pre-alert distances for police enforcement, accidents and heavy traffic. |  |
| [Remove ads](#remove-ads) | Forces Waze's advertising config values off. |  |

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
