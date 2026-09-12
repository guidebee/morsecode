package au.com.guidebee.morsetoolkit.platformer.core;

/**
 * Anything a {@link TileWorld} should treat as solid terrain alongside its
 * own tile grid (Mario's own {@code InteractiveBrick} is the one implementor
 * today - a brick lives outside the tile grid but still blocks movement).
 * Registered under this marker (see {@link TileWorld#register}) so
 * {@link TileWorld#containsImpassableArea} and {@link TileWorld#findActiveBrickAt}
 * can query it generically, without the toolkit needing to know about any
 * game-specific actor type.
 */
public interface SolidTile {
    boolean isActive();
    boolean overlaps(float x, float y, int width, int height);
    float getX();
    float getY();
    float getWidth();
    float getHeight();
}
