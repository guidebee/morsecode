package au.com.guidebee.morsetoolkit.decoder

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Regression tests for [ToneDetector], the narrowband alternative selectable
 * via [AudioMorseCodeDecoder.DetectionMode.NARROWBAND].
 */
class ToneDetectorTest {

    private val sampleRate = 44100
    private val blockSize = 128

    private fun sineWave(freqHz: Double, durationMs: Int, amplitude: Double, phaseOffset: Int = 0): ShortArray {
        val count = sampleRate * durationMs / 1000
        return ShortArray(count) { i ->
            (sin(2.0 * PI * (i + phaseOffset) * freqHz / sampleRate) * amplitude).toInt().toShort()
        }
    }

    private fun silence(durationMs: Int): ShortArray = ShortArray(sampleRate * durationMs / 1000)

    private fun broadbandNoise(durationMs: Int, amplitude: Double, seed: Long): ShortArray {
        val random = Random(seed)
        val count = sampleRate * durationMs / 1000
        val freqs = doubleArrayOf(180.0, 1300.0, 2600.0, 4200.0)
        return ShortArray(count) { i ->
            var sum = 0.0
            for (f in freqs) sum += sin(2.0 * PI * i * f / sampleRate)
            sum /= freqs.size
            sum += (random.nextDouble() - 0.5) * 0.3
            (sum * amplitude).toInt().toShort()
        }
    }

    /**
     * True random (white) noise, unlike [broadbandNoise] above - that one is
     * a handful of discrete sine tones plus a little dither, which is a fine
     * stress test in general but a bad fit here: 1300Hz and 2600Hz both fall
     * inside this detector's scanned band, so a wideband Goertzel scan
     * correctly reads them as real tones. Genuine white noise spreads its
     * energy thin and randomly across every candidate instead, which is
     * what "does broadband noise avoid false-triggering" should mean for a
     * detector that scans a wide band looking for the loudest candidate.
     */
    private fun whiteNoise(durationMs: Int, amplitude: Double, seed: Long): ShortArray {
        val random = Random(seed)
        val count = sampleRate * durationMs / 1000
        return ShortArray(count) { ((random.nextDouble() - 0.5) * 2.0 * amplitude).toInt().toShort() }
    }

    private fun classifyAll(detector: ToneDetector, samples: ShortArray): List<Boolean> {
        val results = ArrayList<Boolean>()
        var offset = 0
        while (offset + blockSize <= samples.size) {
            results.add(detector.classify(samples, offset, blockSize))
            offset += blockSize
        }
        return results
    }

    @Test
    fun cleanToneIsDetectedAndSilenceRecovers() {
        val detector = ToneDetector(sampleRate)
        val lead = silence(300)
        val tone = sineWave(700.0, 150, 20000.0)
        val trail = silence(300)

        val leadResults = classifyAll(detector, lead)
        val toneResults = classifyAll(detector, tone)
        val trailResults = classifyAll(detector, trail)

        assertFalse("silence before the tone should never classify as tone", leadResults.any { it })
        assertTrue("a clean strong 700Hz tone should be detected", toneResults.any { it })
        assertFalse("silence after the tone should recover to no-tone", trailResults.takeLast(20).any { it })
    }

    @Test
    fun broadbandNoiseDuringSilenceDoesNotFalselyTriggerTone() {
        // The threshold is relative to a decaying *peak* (see the class doc:
        // this is deliberately the same philosophy the broadband detector
        // uses, to fix real-world cases where a fixed ratio off a separate
        // noise floor could never be met for a quieter-but-still-clearly-
        // loudest tone). The trade-off is the same one the broadband
        // detector already has: with no separate "typical quiet level"
        // being tracked, a random fluctuation need only be a bit louder
        // than *recent* noise (not a fixed multiple of some floor) to
        // register - fine for real CW's sharp tone/silence contrast, not a
        // claim of immunity to sustained loud random noise with no such
        // contrast. This amplitude reflects realistic ambient background,
        // not a stress test.
        val detector = ToneDetector(sampleRate)
        val noise = whiteNoise(1000, amplitude = 50.0, seed = 42)

        val results = classifyAll(detector, noise)

        assertFalse(
            "moderate broadband noise must not read as a CW tone",
            results.drop(30).any { it }
        )
    }

    @Test
    fun offBandInterferenceDoesNotFalselyTriggerTone() {
        // The tracked band is now a wide 300-3000Hz scan (real CW/phone-speaker
        // pitch can land almost anywhere in it), so "off-band" here means
        // clearly outside that band, not just off the old 700Hz default.
        val detector = ToneDetector(sampleRate)
        val lead = silence(200)
        val interference = sineWave(4000.0, 500, 20000.0)

        classifyAll(detector, lead)
        val results = classifyAll(detector, interference)

        assertFalse(
            "a strong tone outside the scanned band must not be classified as CW tone",
            results.any { it }
        )
    }

    @Test
    fun loudTransientNoiseDoesNotPermanentlyDeafenDetector() {
        val detector = ToneDetector(sampleRate)
        classifyAll(detector, silence(200))
        classifyAll(detector, broadbandNoise(300, amplitude = 30000.0, seed = 7))
        classifyAll(detector, silence(200))

        val recoveryTone = sineWave(700.0, 150, 20000.0)
        val recoveryResults = classifyAll(detector, recoveryTone)

        assertTrue(
            "a genuine tone right after a loud noise burst must still be detected " +
                "(a max-hold AGC would have latched the threshold too high)",
            recoveryResults.any { it }
        )
    }

    @Test
    fun tracksSlightlyMistunedTonePitch() {
        // Sidetone pitch varies by radio/operator; 660Hz is a plausible mismatch
        // against the 700Hz default target.
        val detector = ToneDetector(sampleRate)
        classifyAll(detector, silence(200))
        val tone = sineWave(660.0, 400, 20000.0)

        val results = classifyAll(detector, tone)

        assertTrue("a strong nearby-pitch tone should still be detected", results.any { it })
    }
}
