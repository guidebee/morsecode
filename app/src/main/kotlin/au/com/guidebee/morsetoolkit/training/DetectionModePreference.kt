package au.com.guidebee.morsetoolkit.training

import android.content.Context
import au.com.guidebee.morsetoolkit.decoder.AudioMorseCodeDecoder.DetectionMode

/** Persists the Decoder screen's Classic/Narrowband choice, same pattern as [ThemePreference]. */
object DetectionModePreference {
    private const val PREFS_NAME = "guidebee.morse.training"
    private const val KEY_DETECTION_MODE = "detection_mode"

    fun get(context: Context): DetectionMode {
        val raw = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DETECTION_MODE, DetectionMode.BROADBAND.name)
        return runCatching { DetectionMode.valueOf(raw ?: DetectionMode.BROADBAND.name) }
            .getOrDefault(DetectionMode.BROADBAND)
    }

    fun set(context: Context, mode: DetectionMode) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_DETECTION_MODE, mode.name).apply()
    }
}
