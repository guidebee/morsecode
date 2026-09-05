package au.com.guidebee.morsetoolkit.training

import android.content.Context

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Persists the new UI's theme choice, independent of [au.com.guidebee.morsetoolkit.ConfigInfo]'s legacy-screen settings. */
object ThemePreference {
    private const val PREFS_NAME = "guidebee.morse.training"
    private const val KEY_THEME_MODE = "theme_mode"

    fun get(context: Context): ThemeMode {
        val raw = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return runCatching { ThemeMode.valueOf(raw ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)
    }

    fun set(context: Context, mode: ThemeMode) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME_MODE, mode.name).apply()
    }
}
