package io.github.forsigh.waze.extension.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

/**
 * The "restart Waze to apply" flow, the same shape Morphe's own extensions ship.
 *
 * Some settings are read once while the app starts, so changing them can only take effect on a fresh
 * process. Instead of silently doing nothing, the app asks. The restart itself is deliberately the
 * exact sequence Morphe uses in its shared extension library: build a restart task for our own launch
 * component, then kill the process.
 *
 * Note this is a *restart* prompt, not a re-patch prompt. Anything baked in at patch time - map skins,
 * for instance, which live in the APK's assets - cannot be changed by restarting and has no business
 * showing this dialog.
 *
 * All user-visible text is a literal, never a resource: it must read English in every locale.
 */
public final class ForsighUi {

    private static final String RESTART_TITLE = "Restart Waze";
    private static final String RESTART_CONFIRM = "Restart";
    private static final String RESTART_LATER = "Later";

    private ForsighUi() {
    }

    /**
     * Relaunch the app and quit this process.
     *
     * `setPackage` is not optional: from API 34 the restart task needs an explicit package or the
     * launch throws. `System.exit(0)` after starting the task is what makes it a restart rather than
     * a second copy of the app in the task stack.
     */
    public static void restartApp(Context context) {
        if (context == null) {
            return;
        }
        final String packageName = context.getPackageName();
        final Intent launch = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launch == null || launch.getComponent() == null) {
            return;
        }
        final Intent mainIntent = Intent.makeRestartActivityTask(launch.getComponent());
        mainIntent.setPackage(packageName);
        context.startActivity(mainIntent);
        System.exit(0);
    }

    /**
     * Ask before restarting.
     *
     * Needs a live Activity, not an application context - a dialog built on the application context has
     * no window token and throws.
     */
    public static void showRestartDialog(final Activity activity, String message) {
        showRestartDialog(activity, message, null);
    }

    /**
     * Ask before restarting, with custom work to run instead of the plain restart (used when something
     * has to be persisted first).
     */
    public static void showRestartDialog(final Activity activity, String message,
                                         final Runnable onRestart) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        new AlertDialog.Builder(activity)
                .setTitle(RESTART_TITLE)
                .setMessage(message)
                .setPositiveButton(RESTART_CONFIRM, (dialog, which) -> {
                    if (onRestart != null) {
                        onRestart.run();
                    } else {
                        restartApp(activity);
                    }
                })
                .setNegativeButton(RESTART_LATER, null)
                .show();
    }
}
