// Compile-time only stubs of the Waze classes our extension touches.
//
// The extension is compiled against these signatures but they are NEVER packaged: the real classes
// already exist in the target APK and the extension's bytecode resolves to those at runtime. Shipping
// them would put a second definition of com.waze.* classes into the merged dex.
// An Android library (not a plain java-library) because the stubbed signatures reference android.*
// types such as android.view.View, which only android.jar provides. The android plugin is already on
// the classpath from the root build, so no version is declared here.
plugins {
    id("com.android.library")
}

android {
    namespace = "io.github.forsigh.waze.stubs"

    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
