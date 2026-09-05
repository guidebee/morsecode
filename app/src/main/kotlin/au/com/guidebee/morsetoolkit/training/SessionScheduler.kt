package au.com.guidebee.morsetoolkit.training

import kotlin.random.Random

/**
 * Picks which unlocked character to drill next, weighted toward characters
 * whose Leitner bucket is low (weak or newly unlocked) — the
 * spaced-repetition piece: review skews toward what the learner is actually
 * struggling with instead of a flat uniform pick over the whole set.
 */
class SessionScheduler(private val progression: KochProgression) {

    fun nextCharacter(random: Random = Random.Default): Char {
        val unlocked = progression.unlockedCharacters
        if (unlocked.size <= 1) return unlocked.first()

        val weights = unlocked.map { char ->
            val bucket = progression.statFor(char).bucket
            val recencyBoost = if (char == unlocked.last()) 2f else 0f
            (MAX_WEIGHT - bucket).toFloat() + recencyBoost
        }
        val total = weights.sum()
        var pick = random.nextFloat() * total
        for (i in unlocked.indices) {
            pick -= weights[i]
            if (pick <= 0f) return unlocked[i]
        }
        return unlocked.last()
    }

    private companion object {
        const val MAX_WEIGHT = 5
    }
}
