package au.com.guidebee.morsetoolkit.training

import android.content.Context

/**
 * Tracks which in-app tutorials the user has already seen: the first-launch
 * onboarding pager, the Home screen coach-mark tour, and each tool screen's
 * "how this works" tip dialog. Independent of [ThemePreference] and
 * [au.com.guidebee.morsetoolkit.ConfigInfo] so resetting tutorials never
 * touches theme or training settings.
 */
object TutorialPreference {
    private const val PREFS_NAME = "guidebee.morse.tutorials"
    private const val KEY_ONBOARDING_SEEN = "onboarding_seen"
    private const val KEY_PREFIX_SEEN = "seen_"

    fun hasSeenOnboarding(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ONBOARDING_SEEN, false)

    fun markOnboardingSeen(context: Context) {
        prefs(context).edit().putBoolean(KEY_ONBOARDING_SEEN, true).apply()
    }

    fun hasSeen(context: Context, key: String): Boolean =
        prefs(context).getBoolean(KEY_PREFIX_SEEN + key, false)

    fun markSeen(context: Context, key: String) {
        prefs(context).edit().putBoolean(KEY_PREFIX_SEEN + key, true).apply()
    }

    /** Clears every "seen" flag so onboarding and every screen tip show again. */
    fun resetAll(context: Context) {
        prefs(context).edit().clear().apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
