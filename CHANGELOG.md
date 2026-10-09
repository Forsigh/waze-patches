## [1.7.0](https://github.com/Forsigh/waze-patches/compare/v1.6.0...v1.7.0) (2026-10-09)

### ✨ New Features

* wire a patched YouTube Music into Waze's audio player ([126cc2e](https://github.com/Forsigh/waze-patches/commit/126cc2e0b82171bed7770c880ad45910df5ab3ee))

## [1.6.0](https://github.com/Forsigh/waze-patches/compare/v1.5.0...v1.6.0) (2026-10-08)

### ✨ New Features

* support Waze 5.25.0.1 alongside 5.24.90.901 ([fbb41a2](https://github.com/Forsigh/waze-patches/commit/fbb41a28e1005ec7d1ddd569981c828157060688))

## [1.5.0](https://github.com/Forsigh/waze-patches/compare/v1.4.0...v1.5.0) (2026-10-08)

### 🐛 Bug Fixes

* apply config overrides as one block per getter ([40a39f5](https://github.com/Forsigh/waze-patches/commit/40a39f536c6c0b63d3f60e4660643c1331623e64))

### ✨ New Features

* add the Forsigh Settings row to Waze's settings list ([2250284](https://github.com/Forsigh/waze-patches/commit/2250284f9403c1c14a731d33adc0e63ddfbada7d))
* restart-to-apply prompt in the extension ([d32019e](https://github.com/Forsigh/waze-patches/commit/d32019ead450149f711c468dbb030ca937fa8eee))

## [1.4.0](https://github.com/Forsigh/waze-patches/compare/v1.3.0...v1.4.0) (2026-10-07)

### ✨ New Features

* Forsigh Settings extension groundwork ([1614651](https://github.com/Forsigh/waze-patches/commit/16146515a8a2e3465ee0269413acfaa67595cd92))

## [1.3.0](https://github.com/Forsigh/waze-patches/compare/v1.2.2...v1.3.0) (2026-10-07)

### ✨ New Features

* auto zoom - pin CONFIG_VALUE_ROUTING_AUTO_ZOOM to no/yes/speed ([ca0cef6](https://github.com/Forsigh/waze-patches/commit/ca0cef62bf16198d398690f0427caa8ea7acbda1))

## [1.2.2](https://github.com/Forsigh/waze-patches/compare/v1.2.1...v1.2.2) (2026-10-07)

### 🐛 Bug Fixes

* declare the production target (5.24.90.901), not the beta MOD's versionName ([9784954](https://github.com/Forsigh/waze-patches/commit/9784954c2d3e261eb36b95314395be8c1efc9114))

## [1.2.1](https://github.com/Forsigh/waze-patches/compare/v1.2.0...v1.2.1) (2026-10-05)

### 🐛 Bug Fixes

* drop spoof install source patch (inert for Waze; AA visibility is install-time) ([a85a81e](https://github.com/Forsigh/waze-patches/commit/a85a81e03d90db9499d6a77e59ea6bfc04e9f71c))

## [1.2.0](https://github.com/Forsigh/waze-patches/compare/v1.1.1...v1.2.0) (2026-10-05)

### ✨ New Features

* add universal spoof install source patch ([c184c9e](https://github.com/Forsigh/waze-patches/commit/c184c9e13889ce4a01a322a3a4abca10b633abaa))

## [1.2.0](https://github.com/Forsigh/waze-patches/compare/v1.1.1...v1.2.0) (2026-10-05)

### ✨ New Features

* add universal spoof install source patch ([c184c9e](https://github.com/Forsigh/waze-patches/commit/c184c9e13889ce4a01a322a3a4abca10b633abaa))

## [1.1.1](https://github.com/Forsigh/waze-patches/compare/v1.1.0...v1.1.1) (2026-10-05)

### 🐛 Bug Fixes

* grant issues and pull-requests write so the semantic-release success step stops failing ([0cf3977](https://github.com/Forsigh/waze-patches/commit/0cf39777d2117a8e91478db94fa904054afe9ecf))

## [1.1.0](https://github.com/Forsigh/waze-patches/compare/v1.0.0...v1.1.0) (2026-10-05)

### ✨ New Features

* add 8 Waze config patches; fix Boolean vs Long getter typing ([6013230](https://github.com/Forsigh/waze-patches/commit/6013230bbfe1b12d509d8ca9263a84ecefaa4609))

## 1.0.0 (2026-10-05)

### 🐛 Bug Fixes

* mark gradlew executable so the Linux CI runner can invoke it ([bc0b9fe](https://github.com/Forsigh/waze-patches/commit/bc0b9fe51f49eff89f1f4fc6339af039b6fd4060))

### ✨ New Features

* initial Waze patches (remove ads, extended police/hazard alert distances) ([5027682](https://github.com/Forsigh/waze-patches/commit/5027682d2d750b1375625f41a18e6d3ddad2c9f7))
