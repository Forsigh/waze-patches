package io.github.forsigh.waze.extension.settings;

import android.view.View;

import java.util.List;

import com.waze.settings.tree.c.b;
import com.waze.settings.tree.d.t;

/**
 * The "Forsigh Settings" entry row in Waze's settings list.
 *
 * Built from Waze's own row type (`com.waze.settings.tree.d.t`) rather than a custom view, so it is
 * rendered, themed and laid out by Waze exactly like every other row, and it is appended to the same
 * list the settings screen already consumes.
 *
 * The label is passed as a raw String, which Waze turns into a text source via
 * `com.waze.ui.ab.b.d(String)`. Nothing in that path consults the app's locale, so the row reads
 * "Forsigh Settings" in every language - including Polish.
 *
 * Not yet implemented: `R` (the tap action). It lands here next, opening the sub-page that hosts the
 * per-patch toggles.
 */
public final class ForsighSettingsEntry implements b {

    public static final String ROW_KEY = "forsigh_settings";
    public static final String ROW_LABEL = "Forsigh Settings";
    private static final String ROW_TITLE_KEY = "FORSIGH_SETTINGS";

    /**
     * Injection point. Appends the entry to the settings section list the screen builds, so it shows
     * up as a normal row in Waze's settings.
     */
    @SuppressWarnings("unchecked")
    public static void appendTo(List list) {
        if (list == null) {
            return;
        }
        list.add(new t(ROW_KEY, ROW_LABEL, ROW_TITLE_KEY, new ForsighSettingsEntry()));
    }

    /** Whether the row is currently enabled/visible. */
    @Override
    public boolean Q() {
        return true;
    }

    /** Tap handling. TODO: open the Forsigh Settings sub-page. */
    @Override
    public void R(View view, boolean z) {
    }

    /** State binding for the row. */
    @Override
    public boolean S(View view, boolean z) {
        return false;
    }
}
