package io.github.forsigh.waze

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

/**
 * Waze numeric config value getter: com.waze.config.c.a()Ljava/lang/Long;
 *
 * Reads the numeric value of a config entry: the receiver is a `com.waze.config.c` ConfigValue
 * object, which is compared against the named constants in `com.waze.config.ConfigValues`.
 * Patching this single accessor lets a patch force the returned value of any numeric config key.
 *
 * The pattern is the one confirmed working in dowjames' Waze patch (github.com/dowjames/morphe-patches).
 * The names `c` and `h` are obfuscated and may change between app targets.
 */
object NumericConfigGetterFingerprint : Fingerprint(
    definingClass = "Lcom/waze/config/c;",
    name = "a",
    returnType = "Ljava/lang/Long;",
    filters = listOf(
        methodCall(
            definingClass = "Lcom/waze/config/h;",
            name = "a",
        ),
    ),
)
