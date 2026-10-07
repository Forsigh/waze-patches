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
    // Waze's settings classes, compile-only: the extension is compiled against these signatures but
    // they are never packaged, so at runtime the bytecode binds to Waze's own classes.
    compileOnly(project(":stubs"))
}
