package au.com.guidebee.morsetoolkit.activity.mario.hud;

import com.guidebee.game.microedition.LayerManager;
import com.guidebee.game.ui.Label;
import com.guidebee.game.ui.Skin;

import au.com.guidebee.morsetoolkit.activity.mario.state.GameStateController;

/**
 * The classic top-of-screen SCORE/COINS/LIVES/WORLD bar, plus a centered
 * one-shot message label (used for "GAME OVER" - see {@code
 * MarioGameScreen#handlePlayerDeath}). Built from the engine's bundled
 * {@code skin/default} font/skin (see {@code MarioResourceManager#uiSkin()})
 * rather than sliced digit-sprite art like Battle City's/Flappy Bird's own
 * score HUDs - avoids needing new atlas art for a HUD the original engine
 * never actually finished itself (its own {@code DrawScore}'s SCORE/TIME
 * lines are commented out, and {@code Player.IncreaseLife()}'s body is
 * commented out too - so unlike {@link #worldLabel}'s own "WORLD X-Y" text,
 * which {@code DrawScore} does draw for real, the original has no lives
 * display of any kind to match; this port's own LIVES label is an addition
 * needed to make the game-over flow a real feature).
 *
 * <p>Like every other HUD element in {@code MarioGameScreen} (see its class
 * doc), these Labels render through the shared, moving/zooming world camera,
 * so {@link #reposition} must be called every frame to keep them
 * screen-anchored at a constant size - {@code MarioGameScreen#repositionHud}
 * is the caller.
 */
public class ScoreHud {

    private static final float MARGIN = 8f;
    private static final float LINE_HEIGHT = 22f;

    private final Label scoreLabel;
    private final Label livesLabel;
    private final Label worldLabel;
    private final Label messageLabel;

    /** @param worldLevelLabel "1-1"/"8-5" etc (see {@code LevelNumbering#label}) - constant for the level's whole lifetime, so this is set once here rather than in {@link #update}. */
    public ScoreHud(LayerManager layerManager, Skin skin, String worldLevelLabel) {
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
    public void update(GameStateController state) {
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
     * the visible world, in world pixels ({@code CameraController#getX/getY});
     * {@code screenWidth/screenHeight} its current size
     * ({@code CameraController#getEffectiveWidth/Height}); {@code zoom} the
     * shared camera's current zoom (see {@code MarioGameScreen}'s
     * "Pinch-to-zoom" section) - each label is scaled by it so its on-screen
     * text size stays constant regardless.
     */
    public void reposition(float screenLeft, float screenTop, float screenWidth, float screenHeight, float zoom) {
        scoreLabel.setScale(zoom);
        scoreLabel.setPosition(screenLeft + MARGIN * zoom, screenTop + MARGIN * zoom);

        livesLabel.setScale(zoom);
        livesLabel.setPosition(screenLeft + MARGIN * zoom, screenTop + (MARGIN + LINE_HEIGHT) * zoom);

        // Ported from DrawScore's own right-side "WORLD"/world-number text,
        // set apart from the left-aligned SCORE/COINS/LIVES stack above.
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
