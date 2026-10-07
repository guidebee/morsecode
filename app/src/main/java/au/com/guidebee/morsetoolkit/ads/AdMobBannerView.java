package au.com.guidebee.morsetoolkit.ads;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

import au.com.guidebee.morsetoolkit.activity.BuildConfig;

/** One adaptive banner, shared by the Compose UI and game Activities. */
public final class AdMobBannerView extends FrameLayout {
    private AdView adView;
    private boolean destroyed;
    private boolean paused;
    private boolean loadRequested;
    private final boolean adaptive;

    public AdMobBannerView(Activity activity) {
        this(activity, true);
    }

    public AdMobBannerView(Activity activity, boolean adaptive) {
        super(activity);
        this.adaptive = adaptive;
    }

    public void load() {
        loadRequested = true;
        if (destroyed || adView != null) return;
        post(() -> {
            if (destroyed || adView != null) return;
            int width = getWidth();
            if (width <= 0 && getParent() instanceof View) width = ((View) getParent()).getWidth();
            if (width <= 0) return;
            Activity activity = (Activity) getContext();
            int widthDp = Math.max(1, (int) (width / getResources().getDisplayMetrics().density));
            adView = new AdView(activity);
            adView.setAdUnitId(BuildConfig.ADMOB_BANNER_ID);
            adView.setAdSize(adaptive
                    ? AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, widthDp)
                    : AdSize.BANNER);
            adView.setAdListener(new AdListener() {
                @Override public void onAdFailedToLoad(LoadAdError error) {
                    Log.i("AdMob", error.toString());
                }
            });
            addView(adView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            adView.loadAd(new AdRequest.Builder().build());
            if (paused) adView.pause();
        });
    }

    @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        // Consent/initialization can finish before the first layout pass.
        if (width > 0 && loadRequested && adView == null && !destroyed) load();
    }

    public void resume() {
        paused = false;
        if (adView != null) adView.resume();
    }

    public void pause() {
        paused = true;
        if (adView != null) adView.pause();
    }

    public void destroy() {
        destroyed = true;
        if (adView != null) {
            removeView(adView);
            adView.destroy();
            adView = null;
        }
    }
}
