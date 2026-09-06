# 8. Sound and Music

`com.guidebee.game.audio` splits audio into two types, matching the usual mobile-game
split between short effects and streamed background tracks:

- **`Sound`** — a short effect, fully decoded into memory, safe to `play()` repeatedly
  with low latency (footsteps, gunshots, a coin pickup).
- **`Music`** — a streamed, loopable track for background music, with its own
  `play()`/`pause()`/`stop()`/`setLooping()`/`setVolume()`.

Both load through `GameEngine.assetManager` exactly like textures and atlases
(see [6](06-texture-atlases.md)):

```java
// FlappyBirdGamePlay.loadAssets()
assetManager.load("music.mp3", Music.class);
assetManager.load("sfx_wing.ogg", Sound.class);
assetManager.load("sfx_die.ogg", Sound.class);
assetManager.load("sfx_hit.ogg", Sound.class);
assetManager.load("sfx_point.ogg", Sound.class);
assetManager.load("powerup.wav", Sound.class);
```

## Wrapping playback behind a settings-aware helper

Neither game calls `sound.play()` straight from gameplay code — both wrap it so sound
effects respect the user's audio settings without every call site needing to check
them. Flappy Bird's version is a two-line static helper:

```java
// Helper.java
public static void playSound(Sound sound) {
    if (Configuration.soundOn) {
        sound.play(Configuration.userSettings.soundVolume);
    } else {
        sound.stop();
    }
}

public static void playMusic(Music music) {
    if (Configuration.musicOn) {
        music.setVolume(Configuration.userSettings.musicVolume);
        music.play();
    } else {
        music.stop();
    }
}
```

`Bird` then just calls `Helper.playSound(flapSound)` when it flaps, `Helper.playSound(dieSound)`
when it dies, and so on — the volume/on-off logic lives in exactly one place.

Battle City's `ResourceManager.playSound(int type)` takes the same idea further by also
centralizing *which* sound file corresponds to which game event, keyed by a small set of
named constants instead of passing `Sound` objects around:

```java
// ResourceManager.java
public static void playSound(int type) {
    Sound sound = null;
    switch (type) {
        case SCORE_SOUND:    sound = GameEngine.assetManager.get("snd1.wav", Sound.class); break;
        case OPEN_SOUND:     sound = GameEngine.assetManager.get("snd2.wav", Sound.class); break;
        case GAMEOVER_SOUND: sound = GameEngine.assetManager.get("snd3.wav", Sound.class); break;
        case EXPLODE_SOUND:  sound = GameEngine.assetManager.get("snd4.wav", Sound.class); break;
        case SHOOT_SOUND:    sound = GameEngine.assetManager.get("snd5.wav", Sound.class); break;
    }
    if (sound != null) sound.play();
}
```

Call sites just say `ResourceManager.playSound(ResourceManager.SHOOT_SOUND)` — no file
name, no direct `assetManager` call, and the mapping from event to asset can change in
one place.

## Background music: play/stop around the game and pause states

`FlappyBirdStage` starts its looping music when a game starts and stops it on game-over
or pause — the same lifecycle hooks covered in [3. The Game Loop](03-the-game-loop.md):

```java
music = assetManager.get("music.mp3", Music.class);
music.setLooping(true);

public void startGame() {
    // ...
    Helper.playMusic(music);
}

public void pauseGame() {
    paused = true;
    music.stop();
}
```

## Where to look

- `com.guidebee.game.audio.{Sound,Music}` (`gameengine/src/main/java/com/guidebee/game/audio/`).
- `flappybird/actor/Helper.java` — the settings-aware wrapper.
- `battlecity/ResourceManager.java` — the event-keyed sound lookup.

---

[← Back to tutorial index](../README.md) · Previous: [7. Input and the On-Screen Game Pad](07-input-and-game-pad.md) · Next: [9. Tiled Layers and Scenery](09-tiled-layers-and-scenery.md)
