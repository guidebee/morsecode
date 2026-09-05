package au.com.guidebee.morsetoolkit.decoder

import au.com.guidebee.morsetoolkit.helper.MorseHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * End-to-end tests for [AudioMorseCodeDecoder] in its default
 * [AudioMorseCodeDecoder.DetectionMode.BROADBAND] mode, using synthetic
 * audio. Includes the regression coverage for the WPM-collapse bug fixed
 * in [MorseCodePatternMatch] (shared by both detection modes).
 */
class AudioMorseCodeDecoderTest {

    private val sampleRate = 44100
    private val wpm = 20
    private val unitMs = 1200.0 / wpm

    /** dot length in ticks (128-sample blocks) at [wpm]/[sampleRate], rounded to the nearest tick. */
    private val dotLimitTicks = ((unitMs / 1000.0 * sampleRate) / 128.0).let { Math.round(it).toInt() }

    private fun newDecoder(): AudioMorseCodeDecoder =
        AudioMorseCodeDecoder(dotLimitTicks, 800, 50)

    private fun decode(decoder: AudioMorseCodeDecoder, audio: ShortArray): String {
        val output = StringBuilder()
        decoder.addListener(object : MorseCodePatternMatch.MorseCodeListener {
            override fun onEmit(character: Char) {
                if (character != '^') output.append(character)
            }
            override fun onCharStart() {}
            override fun onCharEnd(dotOrDash: String, length: Int) {}
        })
        decoder.processAudioBuffer(audio, audio.size)
        return output.toString()
    }

    private fun generateMorseAudio(text: String, amplitude: Double, toneHz: Double = 700.0): ShortArray {
        val samples = ArrayList<Short>()

        fun appendTone(durationMs: Double) {
            val count = (sampleRate * durationMs / 1000.0).toInt()
            val ramp = min(count / 8, sampleRate / 200).coerceAtLeast(1)
            for (i in 0 until count) {
                val envelope = when {
                    i < ramp -> i / ramp.toDouble()
                    i > count - ramp -> (count - i) / ramp.toDouble()
                    else -> 1.0
                }
                val angle = 2.0 * PI * i * toneHz / sampleRate
                samples.add((sin(angle) * envelope * amplitude).toInt().toShort())
            }
        }

        fun appendSilence(durationMs: Double) {
            repeat((sampleRate * durationMs / 1000.0).toInt()) { samples.add(0) }
        }

        var firstInWord = true
        for (rawChar in text.lowercase()) {
            if (rawChar == ' ') {
                // MorseCodePatternMatch flushes a letter once spaceCounter crosses
                // charLimit, which *resets* spaceCounter - so the word-space check
                // only sees whatever gap remains *after* that reset, not the whole
                // inter-word silence. A textbook 7-unit gap isn't enough to also
                // clear wordLimit from that reset point; ~10 units reliably is.
                appendSilence(unitMs * 10)
                firstInWord = true
                continue
            }
            val pattern = MorseHelper.morseCodeData[rawChar] ?: continue
            if (!firstInWord) appendSilence(unitMs * 3)
            firstInWord = false
            pattern.forEachIndexed { index, symbol ->
                if (index > 0) appendSilence(unitMs)
                appendTone(if (symbol == '-') unitMs * 3 else unitMs)
            }
        }
        // Trailing silence so the final letter's charLimit-flush actually fires
        // (but short of wordLimit, so it doesn't also emit a trailing space).
        appendSilence(unitMs * 4)
        return samples.toShortArray()
    }

    private fun mix(a: ShortArray, b: ShortArray): ShortArray {
        val length = min(a.size, b.size)
        return ShortArray(length) { i ->
            (a[i] + b[i]).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun broadbandNoise(length: Int, amplitude: Double, seed: Long): ShortArray {
        val random = Random(seed)
        val freqs = doubleArrayOf(180.0, 1300.0, 2600.0, 4200.0)
        return ShortArray(length) { i ->
            var sum = 0.0
            for (f in freqs) sum += sin(2.0 * PI * i * f / sampleRate)
            sum /= freqs.size
            sum += (random.nextDouble() - 0.5) * 0.3
            (sum * amplitude).toInt().toShort()
        }
    }

    /** A short broadband click every [periodMs], simulating scattered acoustic/static glitches. */
    private fun periodicClicks(length: Int, periodMs: Int, clickMs: Int, amplitude: Double, seed: Long): ShortArray {
        val random = Random(seed)
        val periodSamples = sampleRate * periodMs / 1000
        val clickSamples = sampleRate * clickMs / 1000
        val freqs = doubleArrayOf(220.0, 1500.0, 3100.0)
        return ShortArray(length) { i ->
            if (i % periodSamples >= clickSamples) return@ShortArray 0
            var sum = 0.0
            for (f in freqs) sum += sin(2.0 * PI * i * f / sampleRate)
            sum /= freqs.size
            sum += (random.nextDouble() - 0.5) * 0.4
            (sum * amplitude).toInt().toShort()
        }
    }

    @Test
    fun decodesCleanMessage() {
        val audio = generateMorseAudio("SOS", amplitude = 20000.0)
        assertEquals("sos", decode(newDecoder(), audio))
    }

    @Test
    fun decodesMultiWordMessage() {
        val audio = generateMorseAudio("CQ DE", amplitude = 20000.0)
        assertEquals("cq de", decode(newDecoder(), audio))
    }

    @Test
    fun decodesUnderBroadbandNoise() {
        // Plain RMS has no frequency selectivity, so its noise tolerance is
        // modest by construction: proving graceful handling of mic self-noise/
        // room hiss, not immunity to loud broadband interference.
        val tone = generateMorseAudio("SOS", amplitude = 20000.0)
        val noise = broadbandNoise(tone.size, amplitude = 50.0, seed = 99)
        val noisy = mix(tone, noise)

        assertEquals(
            "quiet background noise shouldn't corrupt decoding",
            "sos",
            decode(newDecoder(), noisy)
        )
    }

    @Test
    fun decodesDespiteOffBandInterferingTone() {
        val tone = generateMorseAudio("SOS", amplitude = 20000.0)
        val interference = generateMorseAudio("SOS", amplitude = 18000.0, toneHz = 2200.0)
        val interfered = mix(tone, interference)

        assertEquals(
            "a strong unrelated tone must not corrupt decoding of the real CW tone",
            "sos",
            decode(newDecoder(), interfered)
        )
    }

    @Test
    fun recoversAfterLoudNoiseBurstMidMessage() {
        val decoder = newDecoder()
        val firstWord = generateMorseAudio("SOS", amplitude = 20000.0)
        // A ~40ms percussive click (shorter than a dot at this WPM) - the
        // kind of static crash real HF audio produces. The old max-hold AGC
        // would have latched its threshold to this spike's level permanently
        // (only a very long silence ever reset it); this decoder's noise
        // floor must recover in time to correctly read the second word.
        val burst = broadbandNoise(sampleRate * 4 / 100, amplitude = 30000.0, seed = 7)
        val gap = ShortArray(sampleRate / 3) // 333ms silence to let the noise floor settle
        val secondWord = generateMorseAudio("SOS", amplitude = 20000.0)

        val output = StringBuilder()
        decoder.addListener(object : MorseCodePatternMatch.MorseCodeListener {
            override fun onEmit(character: Char) {
                if (character != '^') output.append(character)
            }
            override fun onCharStart() {}
            override fun onCharEnd(dotOrDash: String, length: Int) {}
        })
        decoder.processAudioBuffer(firstWord, firstWord.size)
        decoder.processAudioBuffer(burst, burst.size)
        decoder.processAudioBuffer(gap, gap.size)
        decoder.processAudioBuffer(secondWord, secondWord.size)

        val occurrences = Regex("sos").findAll(output.toString()).count()
        assertEquals(
            "expected both the pre- and post-burst 'SOS' to decode correctly " +
                "in <$output> - a loud transient must not permanently raise the threshold",
            2,
            occurrences
        )
    }

    @Test
    fun scatteredGlitchesDoNotCollapseDecodingIntoRepeatedEOrT() {
        // Regression: a short glitch used to feed MorseCodePatternMatch's WPM
        // estimator, and if its length happened to sit at a "confusable" ratio
        // to the last real element, dotLimit could collapse toward the
        // glitch's tiny length - after which almost every tick looks like a
        // valid dot or dash, and everything decodes as a flood of 'e'/'t'.
        // This scatters many short clicks across a real message and checks
        // decoding never degenerates into that flood, regardless of what
        // garbage characters the clicks themselves happen to produce.
        val message = generateMorseAudio("SOS SOS SOS", amplitude = 20000.0)
        val clicks = periodicClicks(message.size, periodMs = 200, clickMs = 12, amplitude = 50.0, seed = 123)
        val noisy = mix(message, clicks)

        val output = decode(newDecoder(), noisy)

        assertTrue(
            "expected at least one clean 'sos' to survive the scattered glitches in <$output>",
            output.contains("sos")
        )
        assertFalse(
            "decoding collapsed into a flood of tiny elements (the WPM-collapse bug) in <$output>",
            Regex("[et]{5,}").containsMatchIn(output)
        )
    }
}
