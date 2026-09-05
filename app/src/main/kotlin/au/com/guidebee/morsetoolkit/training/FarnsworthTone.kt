package au.com.guidebee.morsetoolkit.training

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import au.com.guidebee.morsetoolkit.helper.MorseHelper
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/**
 * Plays Morse at Farnsworth timing: dits/dahs and the gap between them run
 * at [characterWpm] (so a character always *sounds* like it's sent at full
 * speed), while inter-character and inter-word gaps stretch out to
 * [effectiveWpm] — the standard fix for a learner who counts dits instead of
 * hearing the character's shape. Deliberately independent of the legacy
 * [au.com.guidebee.morsetoolkit.helper.MorseEncoder], which only supports a
 * single bucketed speed and can't decouple the two.
 *
 * Uses the classic [AudioTrack] constructor (not the API 23+ Builder) so it
 * still works down to this app's minSdk 21.
 */
class FarnsworthTone(
    private val sampleRate: Int = 44100,
    private val toneHz: Double = 700.0
) {
    private var audioTrack: AudioTrack? = null

    fun play(text: String, characterWpm: Int, effectiveWpm: Int, volume: Float = 0.85f) {
        stop()
        val charUnitMs = 1200.0 / characterWpm
        val spaceUnitMs = 1200.0 / min(effectiveWpm, characterWpm)

        val samples = ArrayList<Short>()
        var firstCharInWord = true
        for (rawChar in text.lowercase()) {
            if (rawChar == ' ') {
                appendSilence(samples, spaceUnitMs * 7)
                firstCharInWord = true
                continue
            }
            val pattern = MorseHelper.morseCodeData[rawChar] ?: continue
            if (!firstCharInWord) appendSilence(samples, spaceUnitMs * 3)
            firstCharInWord = false
            pattern.forEachIndexed { index, symbol ->
                if (index > 0) appendSilence(samples, charUnitMs)
                appendTone(samples, if (symbol == '-') charUnitMs * 3 else charUnitMs, volume)
            }
        }

        val shortArray = samples.toShortArray()
        if (shortArray.isEmpty()) return

        @Suppress("DEPRECATION")
        val track = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            shortArray.size * 2,
            AudioTrack.MODE_STATIC
        )
        track.write(shortArray, 0, shortArray.size)
        track.play()
        audioTrack = track
    }

    fun stop() {
        val track = audioTrack
        audioTrack = null
        if (track != null) {
            try {
                track.stop()
                track.release()
            } catch (_: IllegalStateException) {
                // already stopped/released
            }
        }
    }

    private fun appendTone(buffer: ArrayList<Short>, durationMs: Double, volume: Float) {
        val sampleCount = (sampleRate * durationMs / 1000.0).toInt()
        val rampSamples = min(sampleCount / 8, sampleRate / 200).coerceAtLeast(1)
        for (i in 0 until sampleCount) {
            val angle = 2.0 * PI * i * toneHz / sampleRate
            val envelope = when {
                i < rampSamples -> i / rampSamples.toDouble()
                i > sampleCount - rampSamples -> (sampleCount - i) / rampSamples.toDouble()
                else -> 1.0
            }
            buffer.add((sin(angle) * envelope * volume * Short.MAX_VALUE).toInt().toShort())
        }
    }

    private fun appendSilence(buffer: ArrayList<Short>, durationMs: Double) {
        val sampleCount = (sampleRate * durationMs / 1000.0).toInt()
        repeat(sampleCount) { buffer.add(0) }
    }
}
