package au.com.guidebee.morsetoolkit.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import au.com.guidebee.morsetoolkit.ConfigInfo
import au.com.guidebee.morsetoolkit.training.ThemeMode
import au.com.guidebee.morsetoolkit.training.ThemePreference
import au.com.guidebee.morsetoolkit.ui.theme.MorseTheme

/**
 * The app's modern front door: a Material 3 dashboard (streak, Koch trainer,
 * send practice, content library, settings) that also launches every legacy
 * Transmit/Receive/Decoder/Flashcard/Handbook/game screen unchanged.
 */
class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Same static prefs the classic Options screen writes — so the new
        // screens' key-click sound/volume honor whatever the user already set.
        ConfigInfo.loadConfiguration(this)
        enableEdgeToEdge()
        setContent {
            var themeMode by remember { mutableStateOf(ThemePreference.get(this)) }
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            MorseTheme(darkTheme = darkTheme) {
                MorseApp(
                    onLaunchGame = { activityClass -> startActivity(Intent(this, activityClass)) },
                    themeMode = themeMode,
                    onThemeModeChange = {
                        themeMode = it
                        ThemePreference.set(this, it)
                    }
                )
            }
        }
    }
}
