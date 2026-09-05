package au.com.guidebee.morsetoolkit.training

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Day-based streak plus a simple XP/level counter, the habit-loop layer the legacy app has none of. */
class StreakTracker(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val currentStreak: Int get() = prefs.getInt(KEY_STREAK, 0)
    val longestStreak: Int get() = prefs.getInt(KEY_LONGEST, 0)
    val totalXp: Int get() = prefs.getInt(KEY_XP, 0)
    val level: Int get() = 1 + totalXp / XP_PER_LEVEL
    val xpIntoLevel: Int get() = totalXp % XP_PER_LEVEL

    /** Call after each answered round; awards XP and advances the streak at most once per calendar day. */
    fun recordSession(xpEarned: Int): SessionResult {
        val today = LocalDate.now().format(FORMATTER)
        val yesterday = LocalDate.now().minusDays(1).format(FORMATTER)
        val lastDay = prefs.getString(KEY_LAST_DAY, null)

        val newStreak = when (lastDay) {
            today -> currentStreak.coerceAtLeast(1)
            yesterday -> currentStreak + 1
            else -> 1
        }
        val newXp = totalXp + xpEarned
        prefs.edit()
            .putString(KEY_LAST_DAY, today)
            .putInt(KEY_STREAK, newStreak)
            .putInt(KEY_LONGEST, maxOf(longestStreak, newStreak))
            .putInt(KEY_XP, newXp)
            .apply()

        return SessionResult(
            streak = newStreak,
            streakExtended = lastDay != today,
            xpEarned = xpEarned,
            totalXp = newXp
        )
    }

    data class SessionResult(
        val streak: Int,
        val streakExtended: Boolean,
        val xpEarned: Int,
        val totalXp: Int
    )

    companion object {
        private const val PREFS_NAME = "guidebee.morse.training"
        private const val KEY_STREAK = "streak_current"
        private const val KEY_LONGEST = "streak_longest"
        private const val KEY_XP = "xp_total"
        private const val KEY_LAST_DAY = "streak_last_day"
        private const val XP_PER_LEVEL = 200
        private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }
}
