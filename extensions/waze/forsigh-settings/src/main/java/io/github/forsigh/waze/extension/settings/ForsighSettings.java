package io.github.forsigh.waze.extension.settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Runtime configuration for the Forsigh Waze patches.
 *
 * <p>This class exists because an in-app switch cannot be baked into the dex: the patch-time
 * {@code force*} blocks in the patch bundle return a constant, and a constant cannot be changed from
 * Waze's own settings screen. Instead the injected smali calls into this class, which reads the value
 * the user actually chose, and falls back to the patch default when nothing was stored.
 *
 * <p>Lives in the extension, so it is merged into the target APK as a DEX before the bytecode patches
 * run - which is what lets the injected smali resolve these methods.
 */
public final class ForsighSettings {

    /** Shared preferences file the Forsigh Settings screen writes to. */
    public static final String PREFS_NAME = "ForsighSettings";

    private static volatile SharedPreferences preferences;

    private ForsighSettings() {
    }

    /**
     * Injection point. Called once, early, from Waze's application class, which is the only way this
     * class gets a usable Context - the config getters the patches hook are static and contextless.
     */
    public static void init(Context context) {
        if (preferences == null && context != null) {
            preferences = context.getApplicationContext()
                    .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    /** True when the user has actually stored a value for [key]. */
    public static boolean has(String key) {
        final SharedPreferences current = preferences;
        return current != null && current.contains(key);
    }

    /** The stored String, or [fallback] when unset (or before [init] has run). */
    public static String getString(String key, String fallback) {
        final SharedPreferences current = preferences;
        return current == null ? fallback : current.getString(key, fallback);
    }

    /** The stored boolean, or [fallback] when unset (or before [init] has run). */
    public static boolean getBoolean(String key, boolean fallback) {
        final SharedPreferences current = preferences;
        return current == null ? fallback : current.getBoolean(key, fallback);
    }

    /** The stored long, or [fallback] when unset (or before [init] has run). */
    public static long getLong(String key, long fallback) {
        final SharedPreferences current = preferences;
        return current == null ? fallback : current.getLong(key, fallback);
    }
}
