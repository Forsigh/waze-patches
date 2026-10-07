extension {
    name = "extensions/waze/forsigh-settings.mpe"
}

android {
    namespace = "io.github.forsigh.waze.extension"

    // Matches the SDK the local toolchain has installed (and the app targets).
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Android API stubs come from the android block above; nothing else is needed yet.
}
