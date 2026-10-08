package com.waze.settings.tree.c;

import android.view.View;

/**
 * Stub of Waze's settings row handler interface - see the module README in build.gradle.kts.
 *
 * Real implementation in the APK: `com.waze.settings.tree.c.b`, with exactly Q()Z, R(View,Z)V and
 * S(View,Z)Z. Rows built from `com.waze.settings.tree.d.t` take an implementation of this to react to
 * taps and to bind state.
 */
public interface b {
    boolean Q();

    void R(View view, boolean z);

    boolean S(View view, boolean z);
}
