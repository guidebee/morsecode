package au.com.guidebee.morsetoolkit.training

import android.content.Context
import android.content.SharedPreferences

/** A single character's drill history: how it's done overall, and its current Leitner bucket (0 = weakest, 4 = mastered). */
data class CharacterStat(
    val char: Char,
    val attempts: Int = 0,
    val correct: Int = 0,
    val bucket: Int = 0
) {
    val accuracy: Float get() = if (attempts == 0) 0f else correct.toFloat() / attempts
}

/**
 * Koch-method progression: characters unlock one at a time in [KochOrder],
 * gated by a rolling accuracy window over whatever is currently unlocked —
 * not each character's own all-time accuracy, which is what
 * [SessionScheduler] uses instead to decide what to drill next.
 */
class KochProgression(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var unlockedCount: Int
        get() = prefs.getInt(KEY_UNLOCKED_COUNT, MIN_UNLOCKED)
            .coerceIn(MIN_UNLOCKED, KochOrder.sequence.size)
        private set(value) {
            prefs.edit().putInt(KEY_UNLOCKED_COUNT, value).apply()
        }

    val unlockedCharacters: List<Char> get() = KochOrder.sequence.take(unlockedCount)

    val isFullyUnlocked: Boolean get() = unlockedCount >= KochOrder.sequence.size

    fun statFor(char: Char): CharacterStat {
        val raw = prefs.getString(statKey(char), null) ?: return CharacterStat(char)
        val parts = raw.split(':')
        return CharacterStat(
            char = char,
            attempts = parts.getOrNull(0)?.toIntOrNull() ?: 0,
            correct = parts.getOrNull(1)?.toIntOrNull() ?: 0,
            bucket = parts.getOrNull(2)?.toIntOrNull() ?: 0
        )
    }

    /** Records one answer for [char]; returns the character newly unlocked as a result, or null. */
    fun recordAnswer(char: Char, wasCorrect: Boolean): Char? {
        val stat = statFor(char)
        val newBucket = if (wasCorrect) {
            (stat.bucket + 1).coerceAtMost(MAX_BUCKET)
        } else {
            (stat.bucket - 2).coerceAtLeast(0)
        }
        prefs.edit().putString(
            statKey(char),
            "${stat.attempts + 1}:${stat.correct + if (wasCorrect) 1 else 0}:$newBucket"
        ).apply()

        return recordRollingResult(wasCorrect)
    }

    private fun recordRollingResult(wasCorrect: Boolean): Char? {
        val window = (prefs.getString(KEY_ROLLING_WINDOW, "") ?: "")
            .plus(if (wasCorrect) "1" else "0")
            .takeLast(ROLLING_WINDOW_SIZE)
        prefs.edit().putString(KEY_ROLLING_WINDOW, window).apply()

        if (isFullyUnlocked || window.length < ROLLING_WINDOW_SIZE) return null
        val accuracy = window.count { it == '1' } / window.length.toFloat()
        if (accuracy < UNLOCK_THRESHOLD) return null

        val next = KochOrder.sequence[unlockedCount]
        unlockedCount += 1
        prefs.edit().putString(KEY_ROLLING_WINDOW, "").apply()
        return next
    }

    fun reset() {
        prefs.edit().clear().apply()
    }

    private fun statKey(char: Char) = "char_$char"

    companion object {
        private const val PREFS_NAME = "guidebee.morse.training"
        private const val KEY_UNLOCKED_COUNT = "unlocked_count"
        private const val KEY_ROLLING_WINDOW = "rolling_window"
        private const val MIN_UNLOCKED = 2
        private const val MAX_BUCKET = 4
        private const val ROLLING_WINDOW_SIZE = 20
        private const val UNLOCK_THRESHOLD = 0.9f
    }
}
