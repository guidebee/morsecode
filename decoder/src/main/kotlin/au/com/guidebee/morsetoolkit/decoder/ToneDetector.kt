package au.com.guidebee.morsetoolkit.decoder

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Narrowband CW tone/silence classifier: an alternative to
 * [AudioMorseCodeDecoder]'s default broadband detector, selectable via
 * [AudioMorseCodeDecoder.DetectionMode.NARROWBAND]. Each [classify] call
 * consumes one fixed-size tick of new samples, folds them into a sliding
 * window, and answers whether a tone is present at the currently tracked
 * target frequency.
 *
 * Two things make this different from the default broadband detector:
 *  - A Goertzel filter only looks at energy near the CW pitch, so
 *    broadband noise (voice, wind, traffic) doesn't read as "tone" - at
 *    the cost of only working within the tracked frequency band, unlike
 *    the default detector which is frequency-agnostic.
 *  - Entering vs. leaving "tone" uses separate thresholds (hysteresis)
 *    plus a minimum number of consecutive ticks (debounce), so a single
 *    noisy or dropped frame can't flip the state.
 *
 * Frequency selection is a full scan across the whole band every tick
 * (cheap: ~20-25 Goertzel evaluations of a 256-sample window), not an
 * incremental hill-climb from a fixed starting guess. That was tried first
 * and measured badly on real acoustic playback: a phone speaker's frequency
 * response can shift or unevenly color a tone's pitch/harmonics relative to
 * the source file, and a slow ±40Hz/tick drift from a 700Hz guess often
 * couldn't catch up before a short message ended. A wide scan finds the
 * true peak in one tick regardless of where it actually is.
 *
 * The tone/silence threshold is relative to a decaying *peak* magnitude
 * (fast attack, slow decay), the same philosophy the default broadband
 * detector uses (its threshold is a fraction of its own observed peak) -
 * not a fixed ratio off a separately-tracked noise floor, which an earlier
 * version used. That fixed-ratio design assumed the *contrast* between
 * tone and background always exceeds some multiple; it doesn't when the
 * absolute signal is quieter (a tone picked up from across a room through
 * an external speaker, say), so the ratio could simply never be met -
 * missing real elements and corrupting the measured WPM, even though the
 * tone was still clearly the loudest thing present. Comparing against a
 * self-adjusting peak instead makes this work regardless of the tone's
 * absolute level, exactly like the broadband detector already does.
 */
class ToneDetector @JvmOverloads constructor(
    private val sampleRate: Int,
    initialTargetFrequencyHz: Double = DEFAULT_TARGET_FREQUENCY_HZ,
    private val windowSize: Int = DEFAULT_WINDOW_SIZE
) {
    companion object {
        const val DEFAULT_TARGET_FREQUENCY_HZ = 700.0
        const val DEFAULT_WINDOW_SIZE = 256
        private const val MIN_TARGET_FREQUENCY_HZ = 300.0
        private const val MAX_TARGET_FREQUENCY_HZ = 3000.0

        /**
         * Upper bound on how many candidates the scan tests each tick.
         * Picking the loudest of many candidates is a max over many samples,
         * so fewer candidates means fewer chances for one to spike on noise
         * alone - but see the candidate-generation comment below for why
         * this can't just be "300, 500, 700, ... Hz" style fixed spacing.
         */
        private const val MAX_CANDIDATES = 24

        private const val MIN_ABSOLUTE_FLOOR = 8.0
        private const val DEBOUNCE_TICKS = 2

        /** How much of the recent peak counts as "tone" - enter needs a clearer margin than staying counts as leaving. */
        private const val ENTER_FRACTION = 0.5
        private const val EXIT_FRACTION = 0.3

        /**
         * Peak decays by this fraction each tick when nothing louder is
         * seen. Slow enough that it doesn't meaningfully fade within a
         * normal inter-element gap (so exiting a dash doesn't itself reset
         * the calibration), fast enough to recover from a one-off loud
         * transient well within a real message.
         */
        private const val PEAK_DECAY_PER_TICK = 0.995

        /**
         * Safety valve: a sustained loud disturbance that gets misread as
         * "tone" would otherwise keep re-confirming itself as its own peak
         * forever (a stuck key/carrier, or interference that outlasts a
         * real dash). Even the slowest speed this app supports (4 WPM, see
         * MorseEncoder's dit periods) has a dash under 900ms; 1500ms of
         * continuous "tone" is not legitimate CW, so force a resync.
         */
        private const val MAX_TONE_TICKS = 520
    }

    /**
     * A Goertzel filter only resolves whole "bins" of width sampleRate/windowSize
     * - e.g. ~172Hz at 44100Hz/256, but only ~31Hz at 8000Hz/256. A fixed Hz
     * step (say every 200Hz) is fine at 44100Hz but badly under-samples an
     * 8000Hz signal: most candidates land in the gap *between* bins, where a
     * real tone's response has already rolled off, so only the rare
     * candidate that happens to land near the true peak reads strongly -
     * which is exactly what real testing showed (isolated magnitude spikes
     * on transients, weak everywhere else, instead of a sustained plateau
     * through the whole tone). Stepping in whole bins instead - decimated
     * so the candidate count stays bounded regardless of sample rate -
     * guarantees the scan can always land exactly on the true peak's bin.
     */
    private val candidateFrequenciesHz: DoubleArray = run {
        val binHz = sampleRate.toDouble() / windowSize
        val kMin = Math.round(MIN_TARGET_FREQUENCY_HZ / binHz).toInt().coerceAtLeast(1)
        val kMax = Math.round(MAX_TARGET_FREQUENCY_HZ / binHz).toInt().coerceAtLeast(kMin + 1)
        val totalBins = kMax - kMin + 1
        val kStep = max(1, totalBins / MAX_CANDIDATES)
        (kMin..kMax step kStep).map { k -> k * binHz }.toDoubleArray()
    }

    private val ring = ShortArray(windowSize)
    private var ringFill = 0
    private var targetFrequencyHz = initialTargetFrequencyHz
    private var peakMagnitude = MIN_ABSOLUTE_FLOOR
    private var toneState = false
    private var pendingState = false
    private var pendingCount = 0
    private var toneDurationTicks = 0

    /** Goertzel magnitude at the tracked frequency from the most recent tick, for waveform display only. */
    var displayMagnitude: Double = 0.0
        private set

    /** Feeds one tick of [length] new samples starting at [offset] in [chunk]; returns the tone/silence verdict. */
    fun classify(chunk: ShortArray, offset: Int, length: Int): Boolean {
        appendToRing(chunk, offset, length)
        if (ringFill < windowSize) {
            displayMagnitude = 0.0
            return false
        }

        val centerMagnitude = scanForBestFrequency()
        displayMagnitude = centerMagnitude

        // Compare against the peak as it stood *before* this tick - updating
        // it first would make "is this tick louder than the peak" trivially
        // true every time a new peak is set, which defeats the threshold
        // entirely. Updating afterward means a genuine new tone is judged
        // against the prior (quiet) calibration on its first tick, then
        // immediately recalibrates for the ticks that follow.
        val enterThreshold = max(peakMagnitude * ENTER_FRACTION, MIN_ABSOLUTE_FLOOR)
        val exitThreshold = max(peakMagnitude * EXIT_FRACTION, MIN_ABSOLUTE_FLOOR)
        val instantaneous = if (toneState) centerMagnitude > exitThreshold else centerMagnitude > enterThreshold

        if (instantaneous == pendingState) {
            pendingCount++
        } else {
            pendingState = instantaneous
            pendingCount = 1
        }
        if (pendingCount >= DEBOUNCE_TICKS) {
            toneState = pendingState
        }

        if (toneState) {
            toneDurationTicks++
            if (toneDurationTicks > MAX_TONE_TICKS) {
                // Not real CW: force a resync so a fresh peak can be established.
                toneState = false
                pendingState = false
                pendingCount = 0
                toneDurationTicks = 0
            }
        } else {
            toneDurationTicks = 0
        }

        peakMagnitude = max(centerMagnitude, peakMagnitude * PEAK_DECAY_PER_TICK)
        if (peakMagnitude < MIN_ABSOLUTE_FLOOR) peakMagnitude = MIN_ABSOLUTE_FLOOR

        return toneState
    }

    /** Clears the peak, hysteresis and frequency lock — call when (re)starting a listening session. */
    fun reset() {
        peakMagnitude = MIN_ABSOLUTE_FLOOR
        toneState = false
        pendingState = false
        pendingCount = 0
        toneDurationTicks = 0
        ringFill = 0
        displayMagnitude = 0.0
    }

    private fun appendToRing(chunk: ShortArray, offset: Int, length: Int) {
        if (length >= windowSize) {
            System.arraycopy(chunk, offset + length - windowSize, ring, 0, windowSize)
            ringFill = windowSize
            return
        }
        System.arraycopy(ring, length, ring, 0, windowSize - length)
        System.arraycopy(chunk, offset, ring, windowSize - length, length)
        ringFill = min(windowSize, ringFill + length)
    }

    /**
     * Scans every candidate frequency across the whole band and adopts
     * whichever is loudest this tick as [targetFrequencyHz]. Returns that
     * candidate's magnitude, which becomes this tick's detection value.
     */
    private fun scanForBestFrequency(): Double {
        var bestFrequencyHz = candidateFrequenciesHz[0]
        var bestMagnitude = goertzel(ring, bestFrequencyHz)
        for (i in 1 until candidateFrequenciesHz.size) {
            val freq = candidateFrequenciesHz[i]
            val magnitude = goertzel(ring, freq)
            if (magnitude > bestMagnitude) {
                bestMagnitude = magnitude
                bestFrequencyHz = freq
            }
        }
        targetFrequencyHz = bestFrequencyHz
        return bestMagnitude
    }

    private fun goertzel(samples: ShortArray, targetFrequencyHz: Double): Double {
        val n = samples.size
        val k = (0.5 + (n * targetFrequencyHz) / sampleRate).toInt()
        val omega = (2.0 * PI * k) / n
        val cosine = cos(omega)
        val coeff = 2.0 * cosine
        var q1 = 0.0
        var q2 = 0.0
        for (i in 0 until n) {
            val window = 0.54 - 0.46 * cos(2.0 * PI * i / (n - 1))
            val sample = samples[i] * window
            val q0 = coeff * q1 - q2 + sample
            q2 = q1
            q1 = q0
        }
        val real = q1 - q2 * cosine
        val imag = q2 * sin(omega)
        return sqrt(real * real + imag * imag) / n
    }
}
