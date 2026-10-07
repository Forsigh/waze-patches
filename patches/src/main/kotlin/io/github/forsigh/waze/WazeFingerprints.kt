package io.github.forsigh.waze

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

/**
 * Waze configuration values are typed wrappers. Every `CONFIG_VALUE_*` field in
 * `com.waze.config.ConfigValues` is one of:
 *
 *   - `com/waze/config/c` -> Long    getter `a()Ljava/lang/Long;`    delegates to `h.a(c)J`
 *   - `com/waze/config/b` -> Boolean getter `a()Ljava/lang/Boolean;` delegates to `h.k(b)Z`
 *   - `com/waze/config/d` -> String  getter `a()Ljava/lang/String;`  delegates to `h.c(d)String`
 *
 * Forcing a value means patching the matching typed getter: when the receiver config value is
 * one of ours, return our constant instead of the stored value. Hooking the READ path (rather
 * than writing a preference) means the override survives Waze re-syncing config from the server.
 *
 * The key's declared type in ConfigValues.smali MUST match the getter used, or the injected
 * smali is invalid. Verified against Waze 5.24.4.900 (1030731).
 */

/** `com.waze.config.c.a()` - Long config getter. */
object LongConfigGetterFingerprint : Fingerprint(
    definingClass = "Lcom/waze/config/c;",
    name = "a",
    returnType = "Ljava/lang/Long;",
    filters = listOf(
        methodCall(definingClass = "Lcom/waze/config/h;", name = "a", returnType = "J"),
    ),
)

/** `com.waze.config.b.a()` - Boolean config getter. */
object BooleanConfigGetterFingerprint : Fingerprint(
    definingClass = "Lcom/waze/config/b;",
    name = "a",
    returnType = "Ljava/lang/Boolean;",
    filters = listOf(
        methodCall(definingClass = "Lcom/waze/config/h;", name = "k", returnType = "Z"),
    ),
)

/** `com.waze.config.d.a()` - String config getter. */
object StringConfigGetterFingerprint : Fingerprint(
    definingClass = "Lcom/waze/config/d;",
    name = "a",
    returnType = "Ljava/lang/String;",
    filters = listOf(
        methodCall(definingClass = "Lcom/waze/config/h;", name = "c", returnType = "Ljava/lang/String;"),
    ),
)

/**
 * `com.waze.mobile.WazeMobileApplication.onCreate()` - the earliest point in Waze's own startup.
 *
 * Used to hand a Context to the extension: the config getters every patch hooks are static and
 * contextless, so a SharedPreferences-backed setting cannot be read without one being captured here.
 * `p0` is the Application itself, which *is* a Context, so no register juggling is needed.
 */
object ApplicationOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/waze/mobile/WazeMobileApplication;",
    name = "onCreate",
    parameters = listOf(),
    returnType = "V",
)

/**
 * `com.waze.settings.gp.<init>()` - the settings ViewModel, which is where the settings screen's
 * top-level section list is assembled: it creates an ArrayList and appends each section to it.
 *
 * Identified by its call to `gj.l()`, one of the five section builders it collects. The patch appends
 * our own row to that list after its last append, which is what makes the entry show up as a normal
 * settings row.
 */
object SettingsSectionListFingerprint : Fingerprint(
    definingClass = "Lcom/waze/settings/gp;",
    name = "<init>",
    returnType = "V",
    filters = listOf(
        methodCall(
            definingClass = "Lcom/waze/settings/gj;",
            name = "l",
            returnType = "Lcom/waze/settings/tree/f;",
        ),
    ),
)
