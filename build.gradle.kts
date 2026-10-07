// Root build configuration for morphe-patches-template
plugins {
    // Already on the classpath via the Morphe settings plugin, so no version here:
    // declaring one fails with "plugin is already on the classpath with an unknown version".
    id("com.android.library") apply false
}
