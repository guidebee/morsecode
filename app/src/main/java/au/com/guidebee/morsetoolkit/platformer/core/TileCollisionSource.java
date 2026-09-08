package au.com.guidebee.morsetoolkit.platformer.core;

/** Anything TileMovement can query for tile-grid + interactive-actor solidity —
  * exactly MarioWorld's own containsImpassableArea shape, extracted so
  * TileMovement doesn't need to name a Mario-specific type. */
public interface TileCollisionSource {
    boolean containsImpassableArea(float x, float y, int width, int height);
    boolean containsImpassableArea(float x, float y, int width, int height, float duckAboveY);
    int tileSize();
}
