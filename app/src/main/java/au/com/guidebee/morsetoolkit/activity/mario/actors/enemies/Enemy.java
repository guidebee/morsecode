package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;

/**
 * Common base for ground-walking enemies (EnemyMashroom/EnemyTurtle in the
 * original), which all share the same shape: walk at a constant pace, fall
 * under gravity, turn around at walls. See docs/MARIO_PORT_PLAN.md Step 6.1.
 *
 * <p>Stomp-vs-hurt is decided by {@code EnemyCollisionResolver}, not here -
 * this class only reacts via {@link #onStomped}/{@link #onTouchedSide}.
 */
public abstract class Enemy extends Sprite {

    private boolean active = true;
    protected boolean movingRight;

    protected Enemy(TextureRegion region, int frameWidth, int frameHeight, float x, float y, boolean movingRight) {
        super(region, frameWidth, frameHeight);
        setPosition(x, y);
        this.movingRight = movingRight;
    }

    public boolean isActive() {
        return active;
    }

    protected void deactivate() {
        active = false;
        remove();
    }

    public boolean overlaps(int x, int y, int width, int height) {
        return active
                && x < getX() + getWidth() && x + width > getX()
                && y < getY() + getHeight() && y + height > getY();
    }

    /** Player landed on top. Default: die (matches EnemyMashroom; EnemyTurtle overrides). */
    public void onStomped(Player player) {
        deactivate();
    }

    /** Player touched from the side (or from below). Default: hurt the player unless they have a star. */
    public void onTouchedSide(Player player) {
        if (player.hasStar()) {
            deactivate();
        } else {
            player.shrink();
        }
    }

    /** A fireball or a moving shell hit this enemy - always dies, star or not. */
    public void onDefeatedByProjectile() {
        deactivate();
    }
}
