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

<!-- Do not modify this section by hand. The patch list is generated when release.yml creates a new release.
     
     If you wish for the patches list to be collapsed, then remove the word 'EXPANDED' from the comment tag above.

     If you wish to manually keep this list updated then remove the PATCHES_START and PATCHES_END 
     comment blocks entirely. -->

#### A list of your patches will automatically be shown here after your first patches release is created.

&nbsp;

## 🚀 Getting development started

To start using this template, follow these steps:

1. [Setup](https://github.com/MorpheApp/morphe-documentation/blob/main/docs/morphe-development/README.md) your development environment including adding a GitHub PAT as described [here](https://github.com/MorpheApp/morphe-patcher/blob/main/docs/2_1_setup.md#-prepare-the-environment).
2. [Create a new repository using this template](https://github.com/new?template_name=morphe-patches-template&template_owner=MorpheApp). Select create a new repository, and **enable 'Include all branches'** 
3. Enable "Allow GitHub Actions to create and approve pull requests" in your repo Settings > Actions > General > Workflow permissions
4. Update the [build.gradle.kts](patches/build.gradle.kts) file (Specifically, the 
   [group of the project](patches/build.gradle.kts#L1), and the [About](patches/build.gradle.kts#L6-L11))
5. Update the [README.md](README.md) file to be specific of your repo, and update the links in the [issue templates](.github/ISSUE_TEMPLATE).
6. Choose a name for your patches project. Keep in mind you must use a name that does not 
   imply authorship by the Morphe open source project. If unsure, then simply name these
   patches after yourself ("UserXYZ Morphe patches"). See the [NOTICE](NOTICE) for details. 
7. (Optional): Add `patches-bundle.png` to the project if you want a custom icon to show in
   Morphe Manager instead of your GitHub profile avatar.

🎉 You are now ready to start creating patches!

## 🧑‍💻 Dev usage

To develop and release your Patches using this template:

- **Make all changes to the `dev` branch.**
- For local development work build your patches using the gradle task `./gradlew buildAndroid` to generate the mpp file found in `patches/build/libs/patches-*.mpp`. Apply your patches locally using Morphe Desktop tool like any other patch bundle.
- Always use [Semantic commit](https://kapeli.com/cheat_sheets/Semantic_Commits.docset/Contents/Resources/Documents/index) messages for commits. To keep it simple use only 3 commit message types: 
  - `feat: Added a new feature`
  - `fix: Some problem now fixed`
  - `chore: Random change you do not want in the user facing changelog`
- Commits of `fix:` and `feat:` will automatically generate new pre-releases and `chore:` will not create a new release.
- Users can apply your dev branch releases by enabling `pre-release` in Morphe Manager patch sources.
- When your dev branch is ready, and you want a stable release, merge dev branch to main (do not squash, and only merge).
- **Always use semantic release (release.yml)**. Do not manually upload or create releases by hand
  because many files must be updated and release.yml handles everything.

## 🤓 Tips
- See the [patcher documentation](https://github.com/MorpheApp/morphe-patcher/blob/main/docs/1_patcher_intro.md) for more examples of creating patches and fingerprints.
- Do not use AI to create new release scripts. The `release.yml` here already handles everything.
  If you need omething custom with your releases then modify the existing `release.yml`
  and `.releaserc` instead of writing everything new from scratch.
- Do not manually edit or manually commit any generated files such as: `patches-list.json`,
  `patches-bundle.json`, `CHANGELOG.md`.  These files will be automatically updated by `release.yml`.
- Do not force push any semantic release commits as that will break all future releases.
  If you need to fix a broken release, it's always easiest to create a new release instead of 
  fixing an existing release.


<!-- The patches end tag is intentionally placed here so the first release will clean up 
     this readme of all developer instructions above. -->
<!-- PATCHES_END -->

### 🛠️ Building locally

- Run `./gradlew buildAndroid`
- The built patches .mpp file is found in `patches/build/libs/patches-*.mpp`
- Patch the mpp file using [Morphe-Desktop](https://github.com/MorpheApp/morphe-desktop)
  like any other patch bundle.

See the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation) for more information.

## 📜 License

UserXYZ Patches are licensed under the [GNU General Public License v3.0](LICENSE)
