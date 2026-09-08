package au.com.guidebee.morsetoolkit.platformer.hud;

import com.guidebee.game.microedition.LayerManager;
import com.guidebee.game.ui.Label;
import com.guidebee.game.ui.Skin;

import au.com.guidebee.morsetoolkit.platformer.state.ScoreLivesState;

/**
 * The classic top-of-screen SCORE/COINS/LIVES/WORLD bar, plus a centered
 * one-shot message label (e.g. "GAME OVER") - generalized from Mario's own
 * {@code ScoreHud} (see PLATFORMER_ENGINE_ARCHITECTURE.md §2.2): {@link
 * #update} now reads the generic {@link ScoreLivesState} base rather than a
 * concrete Mario type, so any game's own state subclass satisfies it for
 * free. The exact SCORE/COINS/LIVES text formatting itself is left as-is
 * (not further parameterized) - no concrete second consumer needs a
 * different layout yet, and a speculative formatter API would be
 * complexity paid for nothing (see PLATFORMER_ENGINE_ARCHITECTURE.md §6.1's
 * own premature-abstraction warning).
 *
 * <p>Built from the engine's bundled {@code skin/default} font/skin rather
 * than sliced digit-sprite art like Battle City's/Flappy Bird's own score
 * HUDs.
 *
 * <p>Renders through the shared, moving/zooming world camera, so {@link
 * #reposition} must be called every frame to keep these labels
 * screen-anchored at a constant size.
 */
public class StatusBar {

    private static final float MARGIN = 8f;
    private static final float LINE_HEIGHT = 22f;

    private final Label scoreLabel;
    private final Label livesLabel;
    private final Label worldLabel;
    private final Label messageLabel;

    /** @param worldLevelLabel "1-1"/"8-5" etc - constant for the level's whole lifetime, so this is set once here rather than in {@link #update}. */
    public StatusBar(LayerManager layerManager, Skin skin, String worldLevelLabel) {
        scoreLabel = new Label("", skin);
        livesLabel = new Label("", skin);
        worldLabel = new Label("WORLD " + worldLevelLabel, skin);
        // Constant for the level's whole lifetime (see this constructor's own
        // doc) - packed once here so reposition() can right-align it by its
        // real width instead of an initial zero-size Label default.
        worldLabel.pack();
        messageLabel = new Label("", skin);
        messageLabel.setVisible(false);

        layerManager.addHUDComponent(scoreLabel);
        layerManager.addHUDComponent(livesLabel);
        layerManager.addHUDComponent(worldLabel);
        layerManager.addHUDComponent(messageLabel);
    }

    /** Refreshes the score/coins/lives text - call once per frame before {@link #reposition}. */
    public void update(ScoreLivesState state) {
        scoreLabel.setText("SCORE " + pad(state.getScore(), 6) + "   COINS " + pad(state.getCoins(), 2));
        livesLabel.setText("LIVES " + state.getLives());
    }

    /** Shows a centered one-shot message (e.g. "GAME OVER") over the level. */
    public void showMessage(String text) {
        messageLabel.setText(text);
        // Labels don't auto-resize on setText (see the field's own layout()) -
        // pack() recomputes getWidth()/getHeight() from the new text so
        // reposition() can still center it correctly.
        messageLabel.pack();
        messageLabel.setVisible(true);
    }

    public void hideMessage() {
        messageLabel.setVisible(false);
    }

    /**
     * Re-anchors every label to the current screen edges - see the class
     * doc. {@code screenLeft/screenTop} are the current top-left corner of
     * the visible world, in world pixels; {@code screenWidth/screenHeight}
     * its current size; {@code zoom} the shared camera's current zoom - each
     * label is scaled by it so its on-screen text size stays constant
     * regardless.
     */
    public void reposition(float screenLeft, float screenTop, float screenWidth, float screenHeight, float zoom) {
        scoreLabel.setScale(zoom);
        scoreLabel.setPosition(screenLeft + MARGIN * zoom, screenTop + MARGIN * zoom);

        livesLabel.setScale(zoom);
        livesLabel.setPosition(screenLeft + MARGIN * zoom, screenTop + (MARGIN + LINE_HEIGHT) * zoom);

        // Right-side "WORLD"/world-number text, set apart from the
        // left-aligned SCORE/COINS/LIVES stack above.
        worldLabel.setScale(zoom);
        worldLabel.setPosition(
                screenLeft + screenWidth - (worldLabel.getWidth() + MARGIN) * zoom,
                screenTop + MARGIN * zoom);

        messageLabel.setScale(zoom);
        messageLabel.setPosition(
                screenLeft + screenWidth / 2f - messageLabel.getWidth() * zoom / 2f,
                screenTop + screenHeight / 2f - messageLabel.getHeight() * zoom / 2f);
    }

    private static String pad(int value, int digits) {
        String text = Integer.toString(Math.max(0, value));
        StringBuilder padded = new StringBuilder();
        for (int i = text.length(); i < digits; i++) {
            padded.append('0');
        }
        return padded.append(text).toString();
    }
}
