package au.com.guidebee.morsetoolkit.ads;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.guidebee.game.activity.GameActivity;

/** Overlays banners at the historic edges of a full-screen game view. */
public abstract class AdSupportedGameActivity extends GameActivity {
    private AdMobBannerView banner;

    protected void setGameContent(View gameView, boolean bannerAtTop) {
        FrameLayout content = new FrameLayout(this);
        content.addView(gameView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        banner = new AdMobBannerView(this, false);
        content.addView(banner, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                bannerAtTop ? Gravity.TOP : Gravity.BOTTOM));
        setContentView(content);
        AdMobManager.prepare(this, () -> banner.load());
    }

    public void showBanner() {
        runOnUiThread(() -> { if (banner != null) banner.setVisibility(View.VISIBLE); });
    }

    public void hideBanner() {
        runOnUiThread(() -> { if (banner != null) banner.setVisibility(View.GONE); });
    }

    @Override protected void onResume() {
        super.onResume();
        if (banner != null) banner.resume();
    }

    @Override protected void onPause() {
        if (banner != null) banner.pause();
        super.onPause();
    }

    @Override protected void onDestroy() {
        if (banner != null) banner.destroy();
        super.onDestroy();
    }
}
