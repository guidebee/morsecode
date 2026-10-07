package au.com.guidebee.morsetoolkit.ads;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.ads.MobileAds;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import java.util.ArrayList;
import java.util.List;

import au.com.guidebee.morsetoolkit.activity.BuildConfig;

/** Consent precedes SDK initialization and every production ad request. */
public final class AdMobManager {
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final List<Runnable> pending = new ArrayList<>();
    private static boolean initializing;
    private static boolean initialized;

    private AdMobManager() {}

    public static void prepare(Activity activity, Runnable onReady) {
        prepare(activity, onReady, () -> {});
    }

    public static void prepare(Activity activity, Runnable onReady, Runnable onConsentUpdated) {
        if (!BuildConfig.ADMOB_CONFIGURED) {
            Log.w("AdMob", "Set the admobAppId Gradle property to enable the production banner.");
            return;
        }
        if (BuildConfig.ADMOB_TEST_ADS) {
            onConsentUpdated.run();
            initialize(activity, onReady);
            return;
        }
        ConsentInformation consent = UserMessagingPlatform.getConsentInformation(activity);
        Runnable finish = () -> {
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                onConsentUpdated.run();
                if (consent.canRequestAds()) initialize(activity, onReady);
            }
        };
        consent.requestConsentInfoUpdate(activity, new ConsentRequestParameters.Builder().build(),
                () -> {
                    if (activity.isFinishing() || activity.isDestroyed()) return;
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity, error -> {
                        if (error != null) Log.w("AdMob", error.getMessage());
                        finish.run();
                    });
                }, error -> {
                    Log.w("AdMob", error.getMessage());
                    // UMP may still allow requests using consent from the previous session.
                    finish.run();
                });
    }

    private static void initialize(Activity activity, Runnable onReady) {
        // All queue mutations and callbacks run on the UI thread.
        if (initialized) {
            onReady.run();
            return;
        }
        pending.add(() -> {
            if (!activity.isFinishing() && !activity.isDestroyed()) onReady.run();
        });
        if (initializing) return;
        initializing = true;
        new Thread(() -> MobileAds.initialize(activity.getApplicationContext(), status ->
                MAIN.post(() -> {
                    initialized = true;
                    List<Runnable> callbacks = new ArrayList<>(pending);
                    pending.clear();
                    for (Runnable callback : callbacks) callback.run();
                })), "AdMob-init").start();
    }

    public static boolean privacyOptionsRequired(Activity activity) {
        return BuildConfig.ADMOB_CONFIGURED && !BuildConfig.ADMOB_TEST_ADS && UserMessagingPlatform.getConsentInformation(activity)
                .getPrivacyOptionsRequirementStatus() == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
    }

    public static void showPrivacyOptions(Activity activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, error -> {
            if (error != null) Log.w("AdMob", error.getMessage());
            // Drop existing ads and recheck consent before any further requests.
            if (!activity.isFinishing() && !activity.isDestroyed()) activity.recreate();
        });
    }
}
