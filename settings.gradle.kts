rootProject.name = "waze-patches"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/MorpheApp/registry")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
        maven { url = uri("https://jitpack.io") }
    }
}

plugins {
    id("app.morphe.patches") version "1.3.4"
}

settings {
    extensions {
        defaultNamespace = "io.github.forsigh.waze.extension"

        // Must resolve to an absolute path, otherwise extensions in subfolders
        // fail to find the proguard config.
        proguardFiles(rootProject.projectDir.resolve("extensions/proguard-rules.pro").toString())
    }
}

// Compile-time stubs of the Waze classes the extension calls into. Never packaged - the real
// classes are already in the APK, and shipping these would duplicate com.waze.* in the merged dex.
include(":stubs")
