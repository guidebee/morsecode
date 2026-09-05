package au.com.guidebee.morsetoolkit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = SignalAmberLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = GoodGreenLight,
    onSecondary = Color.White,
    background = PaperLight,
    onBackground = InkTealLight,
    surface = SurfaceLight,
    onSurface = InkTealLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = ErrorRustLight,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = SignalAmberDark,
    onPrimary = OnPrimaryContainerLight,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = GoodGreenDark,
    onSecondary = Color.Black,
    background = PaperDark,
    onBackground = InkTealDark,
    surface = SurfaceDark,
    onSurface = InkTealDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = ErrorRustDark,
    onError = Color.Black
)

@Composable
fun MorseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
