package io.github.forsigh.waze

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patcher.patch.stringOption
import io.github.forsigh.waze.WazeConstants.COMPATIBILITY_WAZE
import java.util.Properties

/**
 * Map theme: recolour Waze's own map skin, at patch time, with no runtime code.
 *
 * HOW WAZE SKINS WORK (measured on 5.25.0.1): the map is drawn from compiled Lua skin descriptions in
 * the base APK - `assets/res/skins/default/skin_values.<mode>.lua` for `day` and `night`, plus an
 * `editor` pair for the map editor, and an `experiment/` copy of each. Two things control what you see:
 *
 *   1. the `Palette` table, whose entries are `name = rgb(0xRRGGBB)` (or rgba), and
 *   2. the `Colors.<Group>.<key>` tables, which are read AFTER the palette and override it.
 *
 * So a theme is a colour substitution over those files: replace the palette entries and append explicit
 * colour overrides. Colours are then applied through Waze's own renderer - nothing is drawn by this patch,
 * and no code is injected, which is why it cannot half-apply or crash at runtime.
 *
 * WHY THE VERSION CODE IS BUMPED: Waze extracts skins to its data directory and only re-extracts them when
 * the app's versionCode increases. Editing the assets alone therefore does nothing on a device that has
 * already run Waze - which is why this row pulls in `SkinRefreshPatch`, an unnamed companion that raises
 * the versionCode through the decoded manifest and is documented there.
 *
 * WHAT THIS DOES NOT DO: Waze's in-app theme switcher. That needs a settings row and a runtime component
 * compiled into the APK (the upstream bundle ships one as an extension dex). Choosing the theme when
 * patching needs neither, cannot fail to apply, and changing the theme is a re-patch. If an in-app
 * switcher is ever wanted, it goes on top of this - the recolouring and the refresh are the same either way.
 *
 * Palette data and the recollection/versionCode logic are ported from aaayd/waze-custom-patches (release
 * waze-5.25.0.1-1030736-patches-1.15, commit b6811e5), GPLv3 - see `upstream-port/CREDITS.txt`. Their
 * palette generator samples RGB from Google Maps reference captures; the properties files are carried
 * byte-for-byte under `patches/src/main/resources/themes/` so the colours stay reproducible from source.
 */
private const val SKIN_DIR = "assets/res/skins/default"

/** Both the plain and the `experiment/` skin copies ship in the APK; Waze loads one of them. */
private val SKIN_VARIANTS = listOf("", "experiment/")

/** The modes that have their own skin description. */
private val SKIN_MODES = listOf("day", "night")

/** Classpath handle for the bundled palettes under `themes/` (the `.properties` files). */
private object ThemeResources

private fun themeProperties(name: String): Map<String, String> {
    val properties = Properties()
    val input = ThemeResources::class.java.getResourceAsStream("/themes/$name.properties")
        ?: throw PatchException("mapThemePatch: palette '$name' is missing from the bundle resources")
    input.use(properties::load)
    return properties.stringPropertyNames().associateWith(properties::getProperty)
}

/**
 * Find a Lua table's extent, without depending on indentation or where its closing brace sits.
 * Ported from aaayd/waze-custom-patches (GPLv3).
 */
private fun luaTable(source: String, name: String, required: Boolean = true): IntRange? {
    val declarations = Regex("(?m)(?:^|[,{])\\h*(?:local\\h+)?${Regex.escape(name)}\\h*=").findAll(source).toList()
    if (declarations.isEmpty() && !required) return null
    if (declarations.size != 1) throw PatchException("mapThemePatch: expected one Lua table named $name")
    var index = declarations.single().range.last + 1
    while (index < source.length && source[index].isWhitespace()) index++
    if (index >= source.length || source[index] != '{') throw PatchException("mapThemePatch: expected a Lua table value for $name")
    val start = index
    var depth = 0
    while (index < source.length) {
        when {
            source.startsWith("--[[", index) -> {
                val end = source.indexOf("]]", index + 4)
                if (end < 0) throw PatchException("mapThemePatch: unclosed Lua comment in $name")
                index = end + 2
            }
            source.startsWith("--", index) -> index = source.indexOf('\n', index).let { if (it < 0) source.length else it + 1 }
            source[index] == '\'' || source[index] == '"' -> {
                val quote = source[index++]
                while (index < source.length && source[index] != quote) index += if (source[index] == '\\') 2 else 1
                index++
            }
            source[index] == '{' -> { depth++; index++ }
            source[index] == '}' -> { if (--depth == 0) return start..index; index++ }
            else -> index++
        }
    }
    throw PatchException("mapThemePatch: unclosed Lua table $name")
}

/**
 * Recolour one skin description: replace the recognised `Palette` entries, then append explicit renderer
 * colour overrides. Ported from aaayd/waze-custom-patches (GPLv3), with the same fail-loudly rules - an
 * entry that appears twice, or a required colour that is missing, refuses the patch rather than shipping a
 * half-themed map.
 */
private fun recolorSkin(source: String, palette: Map<String, String>, colors: Map<String, String>): String {
    val table = luaTable(source, "Palette")
        ?: throw PatchException("mapThemePatch: skin has no Palette table")
    val start = table.first
    val end = table.last + 1
    var block = source.substring(start, end)
    val missing = mutableListOf<String>()
    val required = setOf("map_background", "labels", "sea", "parks")

    for ((key, value) in palette) {
        if (!value.matches(Regex("[0-9A-F]{6}([0-9A-F]{2})?"))) {
            throw PatchException("mapThemePatch: invalid colour for palette.$key: $value")
        }
        val declaration = Regex("(?m)(?:^|[,{])\\h*${Regex.escape(key)}\\h*=")
        if (!declaration.containsMatchIn(block) && key !in required) {
            missing.add("Palette.$key")
            continue
        }
        val entry = Regex("(?m)((?:^|[,{])\\h*${Regex.escape(key)}\\h*=\\h*)rgba?\\h*\\(\\h*0x[0-9a-fA-F]+\\h*\\)")
        if (entry.findAll(block).count() != 1) {
            throw PatchException("mapThemePatch: expected exactly one Waze palette entry for $key")
        }
        val function = if (value.length == 8) "rgba" else "rgb"
        block = entry.replace(block) { "${it.groupValues[1]}$function(0x$value)" }
    }

    val overrides = buildString {
        append("\n-- Forsigh/waze-patches: map theme applied; sampled colours bypass Waze's palette transforms.\n")
        val colorSource = source.substring(luaTable(source, "Colors")!!)
        for ((path, value) in colors.toSortedMap()) {
            if (!path.matches(Regex("[A-Za-z][A-Za-z0-9]*\\.[A-Za-z][A-Za-z0-9_]*")) ||
                !value.matches(Regex("[0-9A-F]{6}([0-9A-F]{2})?"))
            ) {
                throw PatchException("mapThemePatch: invalid renderer colour $path=$value")
            }
            val (group, key) = path.split('.')
            val groupRange = luaTable(colorSource, group, required = false)
            val groupSource = groupRange?.let { colorSource.substring(it) }
            if (groupSource == null || !Regex("(?m)(?:^|[,{])\\h*${Regex.escape(key)}\\h*=").containsMatchIn(groupSource)) {
                if (path == "General.map_background") throw PatchException("mapThemePatch: missing renderer colour $path")
                missing.add("Colors.$path")
                continue
            }
            val function = if (value.length == 8) "rgba" else "rgb"
            append("Colors.$path = $function(0x$value)\n")
        }
    }
    if (missing.isNotEmpty()) {
        println("mapThemePatch: this Waze build has no optional skin entries: ${missing.joinToString()}")
    }
    return source.substring(0, start) + block + source.substring(end) + overrides
}

/** The palette pair a theme name maps to: which mode file provides the palette, and which provides colors. */
private fun themeSources(choice: String): Map<String, Pair<String, String>> = when (choice) {
    "google" -> mapOf("day" to ("day" to "day.colors"), "night" to ("night" to "night.colors"))
    "oled" -> mapOf("day" to ("night" to "oled.colors"), "night" to ("night" to "oled.colors"))
    else -> throw PatchException("mapThemePatch: unknown theme '$choice' - use google or oled")
}

@Suppress("unused")
val mapThemePatch = rawResourcePatch(
    name = "Map theme",
    description = "Recolours Waze's own map skin (Google Maps-like light/dark, or OLED black) by rewriting " +
        "the compiled skin descriptions in the APK, and raises the app's versionCode so Waze re-extracts " +
        "its skins instead of serving the cached stock ones. Applied at patch time: change the theme by " +
        "re-patching. No injected code, nothing to render at runtime.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_WAZE)
    // The skin rewrite and the versionCode refresh are two halves of one feature, and they are different
    // patch kinds (raw file writes vs the decoded manifest), so they are two patches: this row and one
    // unnamed dependency. The dependency also keeps the refresh out of a selection that does not want it.
    dependsOn(skinRefreshPatch)

    val theme by stringOption(
        "theme",
        "google",
        title = "Theme",
        description = "google = light and dark palettes sampled from Google Maps. oled = pure-black " +
            "night palette for both modes. Anything else fails the patch run.",
    )

    execute {
        val choice = theme.orEmpty().trim().lowercase().ifEmpty { "google" }
        val sources = themeSources(choice)
        val palettes = sources.mapValues { (_, names) -> themeProperties(names.first) }
        val colors = sources.mapValues { (_, names) -> themeProperties(names.second) }

        var themed = 0
        var optional = 0
        for (variant in SKIN_VARIANTS) {
            for (mode in SKIN_MODES) {
                val path = "$SKIN_DIR/${variant}skin_values.$mode.lua"
                val file = get(path)
                if (!file.exists()) {
                    // The experiment/ copies and the editor schemas are not guaranteed to be there.
                    if (variant.isEmpty()) {
                        throw PatchException("mapThemePatch: Waze has no $path - refusing to report a theme that was not applied")
                    }
                    optional++
                    continue
                }
                val source = file.readText()
                file.writeText(recolorSkin(source, palettes.getValue(mode), colors.getValue(mode)))
                themed++
                println("mapThemePatch: themed $path ($choice)")
            }

            // The map editor has its own complete schema; only the OLED palette restyles it.
            if (choice == "oled") {
                for (mode in SKIN_MODES) {
                    val path = "$SKIN_DIR/${variant}skin_values.editor.$mode.lua"
                    val file = get(path)
                    if (!file.exists()) {
                        optional++
                        continue
                    }
                    file.writeText(recolorSkin(file.readText(), themeProperties("night"), colors.getValue(mode)))
                    themed++
                    println("mapThemePatch: themed $path ($choice)")
                }
            }
        }

        if (themed == 0) {
            throw PatchException("mapThemePatch: no skin file was rewritten - Waze may have changed")
        }

        // The versionCode bump that makes Waze re-extract these skins is skinRefreshPatch, declared above.
        println("mapThemePatch: $choice applied to $themed skin file(s); versionCode refreshed for re-extraction")

        if (optional > 0) println("mapThemePatch: $optional optional skin file(s) not present in this build")
    }
}