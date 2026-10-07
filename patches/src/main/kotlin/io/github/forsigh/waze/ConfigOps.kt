package io.github.forsigh.waze

/**
 * Builders for the smali blocks injected into Waze's typed config getters.
 *
 * Each block is prepended to a getter; `p0` is the receiver ConfigValue. If it equals one of the
 * listed `CONFIG_VALUE_*` fields the block returns our forced value, otherwise it falls through
 * to the original body. Because several patches inject into the same getter, `prefix` MUST be
 * unique per patch or the assembler rejects duplicate labels.
 */

/** smali block force-returning [value] for each Boolean [keys]. Injected into the Boolean getter. */
internal fun forceBoolean(prefix: String, value: Boolean, vararg keys: String): String {
    val boxed = if (value) "Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;"
                else "Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;"
    return buildString {
        keys.forEachIndexed { i, key ->
            append("sget-object v0, Lcom/waze/config/ConfigValues;->$key:Lcom/waze/config/b;\n")
            append("if-eq p0, v0, :${prefix}_b$i\n")
        }
        append("goto :${prefix}_be\n")
        keys.forEachIndexed { i, _ ->
            append(":${prefix}_b$i\n")
            append("sget-object v0, $boxed\n")
            append("return-object v0\n")
        }
        append(":${prefix}_be\n")
    }
}

/** smali block force-returning a Long for each key. Injected into the Long getter. */
internal fun forceLong(prefix: String, vararg entries: Pair<String, Long>): String = buildString {
    entries.forEachIndexed { i, (key, _) ->
        append("sget-object v0, Lcom/waze/config/ConfigValues;->$key:Lcom/waze/config/c;\n")
        append("if-eq p0, v0, :${prefix}_l$i\n")
    }
    append("goto :${prefix}_le\n")
    entries.forEachIndexed { i, (_, v) ->
        append(":${prefix}_l$i\n")
        append("const-wide/16 v0, $v\n")
        append("invoke-static {v0, v1}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;\n")
        append("move-result-object v0\n")
        append("return-object v0\n")
    }
    append(":${prefix}_le\n")
}

/** smali block force-returning a String for each key. Injected into the String getter. */
internal fun forceString(prefix: String, vararg entries: Pair<String, String>): String = buildString {
    entries.forEachIndexed { i, (key, _) ->
        append("sget-object v0, Lcom/waze/config/ConfigValues;->$key:Lcom/waze/config/d;\n")
        append("if-eq p0, v0, :${prefix}_s$i\n")
    }
    append("goto :${prefix}_se\n")
    entries.forEachIndexed { i, (_, v) ->
        append(":${prefix}_s$i\n")
        append("const-string v0, \"$v\"\n")
        append("return-object v0\n")
    }
    append(":${prefix}_se\n")
}
