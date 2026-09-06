# 1. Project Setup and the Application Lifecycle

Every tutorial in this series uses the two games that ship in this repo — **Flappy
Bird** and **Battle City** (`app/src/main/java/au/com/guidebee/morsetoolkit/activity/{flappybird,battlecity}/`) —
as the running examples, instead of a separate standalone demo. This first page covers
how a Guidebee Game Engine (GGE) game is wired into an Android app and how control
flows from the OS down into your game code.

## The engine is a library module, not a dependency

GGE predates Gradle's remote-artifact-friendly world; historically you'd add it as a
`compile 'com.guidebee:game-engine:...'` Maven/JCenter coordinate. That artifact is long
gone (JCenter/Bintray, which hosted it, shut down in 2021). In this repo the entire
engine — Java framework plus the JNI/Box2D native code — lives in-tree as the
`gameengine/` Android library module, and `app/build.gradle` depends on it as a normal
project module:

```gradle
// app/build.gradle
dependencies {
    implementation project(':gameengine')
}
```

`gameengine/build.gradle` builds the native side via `externalNativeBuild { ndkBuild { ... } }`
automatically — see the main [README](../../../README.md#building) for the exact NDK
version and build commands. Nothing here requires a separate native build step.

## Three levels: Activity → GamePlay → Screen

Every GGE game is a chain of three objects, one nested inside the next:

```
GameActivity  (Android Activity — owns the GLSurfaceView)
  └─ GamePlay   (one per game — loads shared assets, switches Screens)
       └─ Screen  (one per game screen — menu, gameplay, score, shop, ...)
```

### 1. `GameActivity`

`com.guidebee.game.activity.GameActivity` is a normal Android `Activity` subclass that
creates the `GLSurfaceView` and forwards the Android lifecycle (`onPause`/`onResume`/...)
into the engine. `FlappyBirdGameActivity` is about as small as this gets:

```java
public class FlappyBirdGameActivity extends GameActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration config = new Configuration();
        config.useAccelerometer = false;
        config.useCompass = false;
        View gameView = initializeForView(new FlappyBirdGamePlay(this), config);
        RelativeLayout mainLayout = new RelativeLayout(this);
        mainLayout.addView(gameView);
        setContentView(mainLayout);
    }
}
```

`initializeForView(ApplicationListener, Configuration)` is the one call that actually
starts the engine: it builds the `GLSurfaceView`, and wires your `GamePlay` in as the
`ApplicationListener` that will receive `create()`/`resize()`/`render()`/`pause()`/`resume()`/`dispose()`.
`Configuration` here is engine-level device setup (accelerometer/compass sensors, not to
be confused with each game's own `config.Configuration` class for game constants).

### 2. `GamePlay`

`com.guidebee.game.GamePlay` implements `ApplicationListener` and represents one whole
game. Its `create()` runs once, loads every shared asset, and hands off to the first
`Screen`:

```java
public class FlappyBirdGamePlay extends GamePlay {

    FlappyBirdGameActivity gameActivity;

    public FlappyBirdGamePlay(FlappyBirdGameActivity activity) {
        gameActivity = activity;
    }

    @Override
    public void create() {
        loadAssets();
        setScreen(new MainWindow(this));
    }

    @Override
    public void dispose() {
        assetManager.dispose();
    }
}
```

`GamePlay.setScreen(Screen)` is how a game moves between menu, gameplay, score, and
shop screens — it just hides the old screen and shows the new one. Flappy Bird's
`MainWindow` menu calls `gamePlay.setScreen(new FlappyBirdScene(gamePlay))` when its
Play button is tapped (see [13. UI Components and HUD](13-ui-components-and-hud.md)).

### 3. `Screen`

`com.guidebee.game.Screen` (or its no-op-filled adapter, `ScreenAdapter`) is one game
screen. It receives `render(delta)` every frame, plus `show`/`hide`/`pause`/`resume`/`resize`.
`FlappyBirdScene` is the actual gameplay screen:

```java
public class FlappyBirdScene extends ScreenAdapter {

    protected final FlappyBirdStage sceneStage;

    public FlappyBirdScene(FlappyBirdGamePlay gamePlay) {
        sceneStage = new FlappyBirdStage(
                new StretchViewport(Configuration.SCREEN_WIDTH, Configuration.SCREEN_HEIGHT),
                gamePlay);
    }

    @Override
    public void render(float delta) {
        graphics.clearScreen(0, 0, 0.2f, 1);
        sceneStage.act();
        sceneStage.draw();
    }

    @Override
    public void pause() {
        sceneStage.pauseGame();
    }

    @Override
    public void resize(int width, int height) {
        sceneStage.getViewport().update(width, height, false);
    }
}
```

Battle City follows the exact same three-level shape (`BattleCityGameActivity` →
implicit `GamePlay` → `BattleCityGameScene`), but its `Screen` drives a `LayerManager`
instead of a `Stage` — the difference between the two is the subject of
[12. The Microedition Game API](12-microedition-game-api.md).

## Where to look

| Concept | Flappy Bird | Battle City |
|---|---|---|
| `GameActivity` | `flappybird/FlappyBirdGameActivity.java` | `battlecity/BattleCityGameActivity.java` |
| `GamePlay` | `flappybird/FlappyBirdGamePlay.java` | `battlecity/BattleCityGamePlay.java` |
| Gameplay `Screen` | `flappybird/FlappyBirdScene.java` | `battlecity/BattleCityGameScene.java` |

---

[← Back to tutorial index](../README.md) · Next: [2. Package Tour](02-package-tour.md)
