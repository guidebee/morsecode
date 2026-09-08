package au.com.guidebee.morsetoolkit.platformer.core;

/** Immutable per-game world-grid metrics. Square tiles only, per this doc's scope. */
public final class TileMetrics {
    public final int tileSize;
    public TileMetrics(int tileSize) {
        this.tileSize = tileSize;
    }
}
