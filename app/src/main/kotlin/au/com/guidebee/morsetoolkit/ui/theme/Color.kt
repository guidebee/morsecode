package au.com.guidebee.morsetoolkit.ui.theme

import androidx.compose.ui.graphics.Color

// Same identity as the legacy app's res/values/colors.xml, so the new
// Compose screens and the classic Views screens read as one app instead of
// two: colorPrimary (#2196f3), colorPrimaryDark (#1976D2), colorAccent
// (#00BFA5).
val BrandBlue = Color(0xFF2196F3)
val BrandBlueDark = Color(0xFF64B5F6)
val BrandBlueContainerLight = Color(0xFFD3E9FD)
val BrandBlueContainerDark = Color(0xFF0D47A1)
val OnBrandBlueContainerLight = Color(0xFF0D3C61)
val OnBrandBlueContainerDark = Color(0xFFD3E9FD)

val BrandTeal = Color(0xFF00BFA5)
val BrandTealDark = Color(0xFF64FFDA)

val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E1E1E)
val BackgroundLight = Color(0xFFFAFAFA)
val BackgroundDark = Color(0xFF121212)
val OnSurfaceLight = Color(0xFF1A1A1A)
val OnSurfaceDark = Color(0xFFEDEDED)
val SurfaceVariantLight = Color(0xFFECECEC)
val SurfaceVariantDark = Color(0xFF2A2A2A)
val OnSurfaceVariantLight = Color(0xFF5F5F5F)
val OnSurfaceVariantDark = Color(0xFFB0B0B0)

// Exactly TransmitLetterActivity's correctAnswerColor / wrongAnswerColor
// (0xff669900 / 0xffcc0000) — the Koch trainer's pass/fail moment should
// look like the legacy Receive drill's, not invent a new pair.
val CorrectGreen = Color(0xFF669900)
val WrongRed = Color(0xFFCC0000)
val WrongRedDark = Color(0xFFFF6E6E)

// Instrument-panel palette for the Decoder screen's oscilloscope redesign.
// Deliberately independent of the light/dark app theme — a scope's brushed
// metal bezel and phosphor screen read as "instrument", not "light mode" or
// "dark mode".
val ScopeBezel = Color(0xFF34383B)
val ScopeBezelDark = Color(0xFF202325)
val ScopeBezelHighlight = Color(0xFF4A4F53)
val ScopeScreen = Color(0xFF041A0E)
val ScopeGrid = Color(0xFF1E5B39)
val ScopePhosphor = Color(0xFF4CFF7E)
val ScopeAmber = Color(0xFFFFB300)
val ScopeLedOff = Color(0xFF44494C)
val ScopePanelText = Color(0xFFD8DED9)
val ScopePanelTextDim = Color(0xFF8A9490)
