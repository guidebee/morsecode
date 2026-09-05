package au.com.guidebee.morsetoolkit.training

import au.com.guidebee.morsetoolkit.helper.MorseHelper
import kotlin.random.Random

enum class LetterDrillResult { CORRECT, RETRY, REVEAL }

/** UI-facing round state for a letter drill (shared by TransmitScreen and ReceiveScreen). */
enum class LetterRoundState { WAITING, CORRECT, REVEAL }

/**
 * The legacy Transmit/Receive Letter drill, unchanged: pick uniformly from
 * the configured letter/number/punctuation set (no Koch gating — this mode
 * tests everything you've turned on in Settings), three tries before
 * revealing the pattern. Mirrors TransmitLetterActivity exactly.
 */
class LetterDrill(private val letterType: () -> Int, private val random: Random = Random.Default) {
    var currentLetter: Char = 'a'; private set
    var tries: Int = 0; private set
    var totalCorrect: Int = 0; private set
    var totalAttempts: Int = 0; private set

    fun next(): Char {
        tries = 0
        val pool = MorseHelper.initTestLetters(letterType())
        currentLetter = pool[random.nextInt(pool.size)]
        totalAttempts += 1
        return currentLetter
    }

    fun answer(char: Char): LetterDrillResult {
        if (char.equals(currentLetter, ignoreCase = true)) {
            totalCorrect += 1
            return LetterDrillResult.CORRECT
        }
        tries += 1
        return if (tries >= TRY_LIMIT) LetterDrillResult.REVEAL else LetterDrillResult.RETRY
    }

    companion object {
        const val TRY_LIMIT = 3
    }
}

enum class WordLetterResult { CORRECT, WRONG, WORD_COMPLETE }

/**
 * The legacy Transmit/Receive Word drill: spell the target letter by letter
 * via the key: a wrong tap doesn't advance and doesn't end the round, it
 * just flashes red (mirrors TransmitWordActivity.onEmit exactly).
 */
class WordDrill(private val random: Random = Random.Default) {
    var currentWord: String = "GUIDEBEE"; private set
    var typedSoFar: String = ""; private set
    var totalCorrect: Int = 0; private set
    var totalAttempts: Int = 0; private set

    fun next(): String {
        currentWord = PracticeWords.list[random.nextInt(PracticeWords.list.size)]
        typedSoFar = ""
        totalAttempts += 1
        return currentWord
    }

    fun answer(char: Char): WordLetterResult {
        val expected = currentWord[typedSoFar.length]
        if (!char.equals(expected, ignoreCase = true)) return WordLetterResult.WRONG
        typedSoFar += expected
        return if (typedSoFar.length == currentWord.length) {
            totalCorrect += 1
            WordLetterResult.WORD_COMPLETE
        } else {
            WordLetterResult.CORRECT
        }
    }
}

/** A curated short-word pool for the Word drill (alpha only, length < 10 — same shape as the legacy word list). */
object PracticeWords {
    val list: List<String> = listOf(
        "THE", "AND", "FOR", "ARE", "BUT", "NOT", "YOU", "ALL", "CAN", "HER",
        "WAS", "ONE", "OUR", "OUT", "DAY", "GET", "HAS", "HIM", "HOW", "MAN",
        "NEW", "NOW", "OLD", "SEE", "TWO", "WAY", "WHO", "BOY", "DID", "ITS",
        "LET", "PUT", "SAY", "SHE", "TOO", "USE", "DARK", "DASH", "DIT", "DAH",
        "CODE", "MORSE", "RADIO", "SIGNAL", "TOWER", "LIGHT", "SOUND", "WAVE",
        "SPEED", "LEARN", "PRACTICE", "TRAIN", "ANTENNA", "STATION", "OPERATOR",
        "CONTACT", "MESSAGE", "RECEIVE", "TRANSMIT", "FREQUENCY", "GUIDEBEE",
        "PERTH", "AUSTRALIA", "KEY", "TAP", "TONE", "BEEP", "QUICK", "SLOW",
        "STEADY", "RHYTHM", "PATTERN", "ALPHABET", "NUMBER", "LETTER", "WORD"
    )
}
