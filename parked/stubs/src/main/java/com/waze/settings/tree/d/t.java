package com.waze.settings.tree.d;

import com.waze.settings.tree.c.b;

/**
 * Stub of Waze's plain clickable settings row.
 *
 * The constructor modelled here is the real one at
 * `com.waze.settings.tree.d.t.<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/waze/settings/tree/c/b;)V`
 * - it wraps the second argument in `com.waze.ui.ab.b.d(String)`, i.e. the label is a RAW STRING and
 * never goes through Waze's localisation. That is what keeps "Forsigh Settings" English in every
 * locale; the other constructor overloads take a label resource id instead and would be translated.
 *
 * @param key      settings key for the row
 * @param label    the literal row text (untranslatable by design)
 * @param titleKey analytics/title key
 * @param handler  tap + bind handler
 */
public class t extends com.waze.settings.tree.f {
    public t(String key, String label, String titleKey, b handler) {
    }
}
