package au.com.guidebee.morsetoolkit.decoder;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Baseline coverage for the dot/dash/letter/word timing state machine, plus
 * regression tests for the WPM-collapse bug fixed in this class: a noise
 * glitch used to be able to feed estimateWPM() and, if its length happened
 * to sit at a "confusable" ratio (2.5x-6x) to the last real element,
 * collapse dotLimit toward the glitch's tiny length - after which almost
 * every tick looks like a valid dot or dash, and everything decodes as a
 * flood of 'e'/'t'. dotLimit=10 gives round tick thresholds: partLimit=15,
 * charLimit=25, wordLimit=55 (see the constructor math above).
 */
public class MorseCodePatternMatchTest {

    private static final int DOT_LIMIT = 10;

    private MorseCodePatternMatch decoder;
    private final List<Character> emitted = new ArrayList<>();
    private final List<String> charStarts = new ArrayList<>();
    private final List<String> charEnds = new ArrayList<>();

    @Before
    public void setUp() {
        decoder = new MorseCodePatternMatch(DOT_LIMIT);
        decoder.addListener(new MorseCodePatternMatch.MorseCodeListener() {
            @Override
            public void onEmit(Character character) {
                emitted.add(character);
            }

            @Override
            public void onCharStart() {
                charStarts.add("start");
            }

            @Override
            public void onCharEnd(String dotOrDash, int length) {
                charEnds.add(dotOrDash + ":" + length);
            }
        });
    }

    private void feed(int ticks, boolean tone) {
        for (int i = 0; i < ticks; i++) {
            decoder.process(tone);
        }
    }

    @Test
    public void singleDotEmitsE() {
        feed(8, true);   // > partLimit/2 (7.5), <= partLimit (15) -> dot
        // tick 1 of this run ends the tone (emits the dot); the remaining
        // 26 ticks accumulate spaceCounter past charLimit (25) -> flush letter
        feed(27, false);

        assertEquals(1, charStarts.size());
        assertEquals(1, charEnds.size());
        assertEquals(".:8", charEnds.get(0));
        assertEquals(List.of('e'), emitted);
    }

    @Test
    public void singleDashEmitsT() {
        feed(16, true);  // > partLimit (15) -> dash
        feed(27, false); // tick 1 ends the tone; 26 more cross charLimit

        assertEquals("-:16", charEnds.get(0));
        assertEquals(List.of('t'), emitted);
    }

    @Test
    public void twoLettersInAWordDecodeInOrder() {
        feed(16, true);
        feed(27, false); // crosses charLimit once -> emits 't', resets spaceCounter to 0
        feed(8, true);
        feed(27, false); // emits 'e'

        assertEquals(List.of('t', 'e'), emitted);
    }

    @Test
    public void longGapAfterALetterEmitsWordSpace() {
        feed(8, true);
        feed(27, false); // emits 'e', spaceCounter resets to 0
        feed(56, false); // spaceCounter climbs past charLimit again (no-op: morseMsg empty) then past wordLimit (55)

        assertEquals(List.of('e', ' '), emitted);
    }

    @Test
    public void invalidPatternEmitsPlaceholder() {
        // Six dots has no mapping in MorseHelper's reverse table.
        for (int i = 0; i < 6; i++) {
            feed(8, true);
            feed(9, false); // < charLimit, keeps accumulating within the same letter
        }
        feed(26, false); // now flush

        assertEquals(List.of('^'), emitted);
    }

    @Test
    public void shortGlitchBetweenOnsetAndDotThresholdIsDiscardedNotStuck() {
        // 5 ticks clears the 3-tick onset (onCharStart fires) but falls short of
        // partLimit/2 (7.5) - too short to be a real dot. Without discarding it,
        // decodeDataState used to latch at Tone forever, silently corrupting
        // every element measured afterwards.
        feed(5, true);
        feed(4, false); // well under charLimit, morseMsg is still empty either way
        feed(8, true);
        feed(27, false); // a completely normal dot, unaffected by the earlier glitch

        assertEquals(2, charStarts.size()); // the discarded glitch still fires onCharStart on its own onset
        assertEquals(List.of(".:8"), charEnds); // but never onCharEnd - it's discarded, not reported as an element
        assertEquals(List.of('e'), emitted);
    }

    @Test
    public void glitchAfterCalibrationDoesNotCorruptWpmEstimate() {
        // Reproduces the actual regression: estimateWPM() used to run even on
        // discarded glitches. A short glitch compared against a recently
        // calibrated element can land in the "confusable ratio" window
        // (2.5x-6x) that estimateWPM() treats as a genuine dot/dash pair,
        // collapsing dotLimit toward the glitch's tiny length - after which
        // almost every subsequent tick looks like a valid dot or dash, and
        // everything decodes as 'e'/'t'. dotLimit=23 matches production
        // (AudioDecoderController's default): partLimit=34.5, partLimit/2=17.25.
        MorseCodePatternMatch d = new MorseCodePatternMatch(23);
        List<String> ends = new ArrayList<>();
        d.addListener(new MorseCodePatternMatch.MorseCodeListener() {
            @Override
            public void onEmit(Character character) {}
            @Override
            public void onCharStart() {}
            @Override
            public void onCharEnd(String dotOrDash, int length) {
                ends.add(dotOrDash + ":" + length);
            }
        });

        feedTicks(d, 69, true);
        feedTicks(d, 1, false); // dash, len 69 -> estimateDotLength=69 (first sample, no recalibration yet)
        feedTicks(d, 23, true);
        feedTicks(d, 1, false); // dot, len 23 -> ratio 3.0 confirms calibration, dotLimit stays ~23

        feedTicks(d, 5, true);
        feedTicks(d, 1, false); // glitch: clears onset (>2) but under partLimit/2 (17.25) -> discarded

        feedTicks(d, 23, true);
        feedTicks(d, 1, false); // a normal dot again - must still classify as '.', not something else

        assertEquals(List.of("-:69", ".:23", ".:23"), ends); // no entry for the discarded glitch
    }

    @Test
    public void tooSlowInitialGuessRecoversFromRepeatedFastElements() {
        // The flip side of the collapse bug: if the *initial* dotLimit guess
        // is too slow relative to the real signal, every real (fast) element
        // gets discarded as a "glitch" - and since discards no longer feed
        // estimateWPM, nothing would ever correct it without the stuck-discard
        // escape hatch. dotLimit=10 (partLimit/2=7.5) is too slow for a real
        // signal whose actual dot length is only 3 ticks.
        MorseCodePatternMatch d = new MorseCodePatternMatch(10);
        List<String> ends = new ArrayList<>();
        d.addListener(new MorseCodePatternMatch.MorseCodeListener() {
            @Override
            public void onEmit(Character character) {}
            @Override
            public void onCharStart() {}
            @Override
            public void onCharEnd(String dotOrDash, int length) {
                ends.add(dotOrDash + ":" + length);
            }
        });

        // One legitimate dash first, so estimateDotLength has a real value to
        // compare against - estimateWPM() needs two samples to compute a
        // ratio, so the very first-ever call (with nothing to compare to)
        // can never recalibrate on its own.
        feedTicks(d, 16, true);
        feedTicks(d, 1, false); // dash, len 16 -> estimateDotLength seeded to 16

        // Three consecutive 3-tick "dots" - each discarded individually
        // (3 < partLimit/2 == 7.5), but the third triggers the stuck-discard
        // escape hatch: ratio 16/3 == 5.3, within the confusable window, so
        // dotLimit recalibrates down to 3.
        for (int i = 0; i < 3; i++) {
            feedTicks(d, 3, true);
            feedTicks(d, 1, false);
        }
        // A fourth 3-tick element, now with dotLimit recalibrated
        // (partLimit/2 == 2.25), should classify correctly as a dot instead
        // of being discarded again.
        feedTicks(d, 3, true);
        feedTicks(d, 1, false);

        assertEquals(List.of("-:16", ".:3"), ends);
    }

    private static void feedTicks(MorseCodePatternMatch decoder, int ticks, boolean tone) {
        for (int i = 0; i < ticks; i++) {
            decoder.process(tone);
        }
    }

    @Test
    public void dashThenDotAdaptsEstimatedDotLength() {
        feed(17, true);
        feed(26, false); // dash length 17
        feed(9, true);
        feed(26, false); // dot length 9

        assertEquals(17, decoder.getDotLength());
        assertTrue(decoder.hasValidWPM());
    }
}
