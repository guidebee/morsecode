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
 * glitch used to be able to feed calibration and, if its length happened
 * to sit at a "confusable" ratio to the last real element, collapse
 * dotLimit toward the glitch's tiny length - after which almost every tick
 * looks like a valid dot or dash, and everything decodes as a flood of
 * 'e'/'t'. dotLimit=10 gives round tick thresholds: partLimit=15,
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
        // Reproduces the actual regression: calibration used to run even on
        // discarded glitches. A short glitch compared against a recently
        // calibrated element could land at a "confusable ratio" to it,
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
        feedTicks(d, 1, false); // dash, len 69 -> dashHistory=[69], but a lone sample doesn't yet recalibrate
        feedTicks(d, 23, true);
        feedTicks(d, 1, false); // dot, len 23 -> now two samples exist; confirms calibration, dotLimit stays ~23

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
        // calibration, nothing would ever correct it without the stuck-discard
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

        // One legitimate dash first, so dashHistory has a real value to seed
        // calibration from - it won't recalibrate dotLimit on its own yet
        // (a lone sample needs a second before recalibration kicks in), but
        // primes the estimate the escalation below draws on.
        feedTicks(d, 16, true);
        feedTicks(d, 1, false); // dash, len 16 -> dashHistory=[16]

        // Three consecutive 3-tick "dots" - each discarded individually
        // (3 < partLimit/2 == 7.5), but the third triggers the stuck-discard
        // escape hatch, recording it as a dot and recalibrating dotLimit down
        // toward the median of the (still sparse) history.
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
    public void oneAtypicallyLongDotBarelyMovesMedianCalibratedDotLimit() {
        // Demonstrates the actual improvement over the old single-sample
        // estimator: a lone atypical-but-legitimately-classified element no
        // longer directly becomes the new calibration. dotLimit=23 matches
        // production: partLimit=34.5, partLimit/2=17.25.
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

        // Fill the dot history with six typical dots (length 23) - median
        // recalibrates dotLimit right back to its starting point each time.
        for (int i = 0; i < 6; i++) {
            feedTicks(d, 23, true);
            feedTicks(d, 1, false);
        }

        // One atypically long dot: still legitimately a dot (34 <= partLimit
        // 34.5, so not a dash; well above the onset, so not a discard), but
        // much longer than the rest of the recent history. Under the old
        // single-sample estimator this alone would become the new estimate,
        // pushing partLimit past 51 and beyond.
        feedTicks(d, 34, true);
        feedTicks(d, 1, false);

        // Proof: with the median barely moved off 23, partLimit is still
        // ~34.5, so a 35-tick tone right after classifies as a dash, not a
        // dot - it wouldn't if that one long dot had swung calibration.
        feedTicks(d, 35, true);
        feedTicks(d, 1, false);

        assertEquals(List.of(".:23", ".:23", ".:23", ".:23", ".:23", ".:23", ".:34", "-:35"), ends);
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
