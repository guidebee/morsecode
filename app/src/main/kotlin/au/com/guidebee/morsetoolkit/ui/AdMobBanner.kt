package au.com.guidebee.morsetoolkit.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import au.com.guidebee.morsetoolkit.ads.AdMobBannerView
import au.com.guidebee.morsetoolkit.ads.AdMobManager

@Composable
internal fun AdMobBanner(onConsentUpdated: () -> Unit) {
    val activity = LocalContext.current as ComponentActivity
    val banner = remember(activity) { AdMobBannerView(activity) }
    val latestCallback = rememberUpdatedState(onConsentUpdated)
    AndroidView(factory = { banner }, modifier = Modifier.fillMaxWidth())
    DisposableEffect(banner, activity) {
        val lifecycle = activity.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> banner.resume()
                Lifecycle.Event.ON_PAUSE -> banner.pause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) banner.pause()
        AdMobManager.prepare(activity, { banner.load() }, { latestCallback.value() })
        onDispose {
            lifecycle.removeObserver(observer)
            banner.destroy()
        }
    }
}
