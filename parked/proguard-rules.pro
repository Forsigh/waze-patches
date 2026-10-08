# Kept deliberately minimal: extensions are merged into the target APK as a DEX before patching,
# so anything stripped here changes the classes the injected smali calls into.
-dontwarn android.**
-dontwarn androidx.**

# Our published entry points are called from injected smali, not from Java, so nothing
# in the extension is referenced from within the extension itself.
-keep class io.github.forsigh.waze.extension.** { *; }
