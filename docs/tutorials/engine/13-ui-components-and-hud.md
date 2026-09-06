# 13. UI Components and HUD

Besides gameplay, a game needs menus, score screens, and in-game overlays (lives, score,
the current Morse challenge). GGE gives you `com.guidebee.game.ui`'s widget set for all
three, so you can stay inside a single `Activity`/`GamePlay` rather than launching
separate Android Activities for menus.

## `Window`: a screen built from widgets

`com.guidebee.game.ui.Window` is a `ScreenAdapter` that hosts a widget tree — pass it to
`GamePlay.setScreen(...)` exactly like a gameplay `Screen`. Flappy Bird's `BaseWindow` is
the shared setup every one of its menu screens (`MainWindow`, `ScoreWindow`,
`StoreWindow`, ...) extends:

```java
// BaseWindow.java
public class BaseWindow extends Window {
    protected final Stack stack = new Stack();
    protected final Skin uiSkin;
    protected final FlappyBirdGamePlay gamePlay;

    public BaseWindow(FlappyBirdGamePlay gamePlay, int width, int height) {
        super(width, height);
        stack.setFillParent(true);
        addComponent(stack);
        uiSkin = new Skin(files.internal("birdskin.json"), new TextureAtlas("birdmenu.atlas"));
        this.gamePlay = gamePlay;
    }
}
```

## `Skin`: named resources for widgets

A `Skin` is a lookup table from string names to drawables/styles, backed by a
`TextureAtlas` plus a JSON file describing widget styles (button up/down states, etc.).
Once built, widgets are constructed by name instead of by wiring up individual
`TextureRegion`s:

```java
Button playButton = new Button(uiSkin, "play");
```

`app/src/main/assets/birdskin.json` is where that mapping actually lives — a plain-text
table from a name to a set of atlas region names per widget state:

```
play: { down: button_play_down, up: button_play_up },
shop: { down: button_shop_down, up: button_shop_up },
score: { down: button_score_down, up: button_score_up },
```

`button_play_down`/`up` are region names inside `birdmenu.atlas` (see
[6. TextureAtlas](06-texture-atlases.md)). Changing the button's art later means editing
the atlas and this JSON file — zero Java changes.

## `Table`/`Button`/`Image`: laying out a menu

`MainWindow` builds Flappy Bird's main menu with a `Table` (an HTML-table-like layout
container) holding an animated logo `Image` and three `Button`s, each wired to switch
screens on click:

```java
// MainWindow.java
public MainWindow(final FlappyBirdGamePlay gamePlay) {
    super(gamePlay);

    Image image = new Image(uiSkin, "menu_background");
    image.setFillParent(true);
    stack.addComponent(image);

    Table table = new Table();
    table.setFillParent(true);
    stack.addComponent(table);

    Button playButton = new Button(uiSkin, "play");
    table.add(playButton).colspan(6);
    playButton.addListener(new EventListener() {
        @Override
        public boolean handle(Event event) {
            gamePlay.setScreen(new FlappyBirdScene(gamePlay));
            return true;
        }
    });
    table.row();
    // ... scoreButton, shopButton, backButton, similarly wired
}
```

`EventListener.handle(Event)` is the click-handling contract for every widget — the
pattern above (`gamePlay.setScreen(new XyzScreen(gamePlay))` inside a button's listener)
is how every screen transition in Flappy Bird's menus happens; see
[1. Project Setup](01-project-setup-and-lifecycle.md#2-gameplay) for the `GamePlay.setScreen`
side of this.

## HUD components: widgets layered over gameplay, not menus

A HUD element (score, lives, the Morse challenge readout) needs to render *on top of* a
`Stage`'s regular actors, in screen space, unaffected by camera movement. `Stage.addHUDComponent(Widget)`
exists for exactly this — it adds the widget to an internal overlay stage rather than the
main scene:

```java
// Stage.java
public void addHUDComponent(Widget widget) {
    internalStageHUD.addComponent(widget);
}
```

`FlappyBirdStage` adds two HUD components alongside its regular actors:

```java
// FlappyBirdStage.java
score = new Score();
addHUDComponent(score);

challengeLetter = new ChallengeLetter();
addHUDComponent(challengeLetter);
```

### A digit-based score HUD

`Score` (a `Table` subclass) is a compact example of building a widget entirely out of
`Image`s driven by cut `TextureRegion`s (the "digit font" technique from
[5. Textures and TextureRegions](05-textures-and-regions.md#cutting-a-digit-font-out-of-a-numbers-strip)):

```java
// Score.java
public void setScore(int score) {
    int newScore = score % 10000;
    thousands.setDrawable(numberDrawables[newScore / 1000]);
    hundreds.setDrawable(numberDrawables[(newScore % 1000) / 100]);
    tens.setDrawable(numberDrawables[(newScore % 100) / 10]);
    units.setDrawable(numberDrawables[newScore % 10]);
}
```

Swapping a widget's `Drawable` each frame (rather than rebuilding the widget) is the
standard way to update a HUD element's appearance without any layout churn.

### A Morse-specific HUD component

`ChallengeLetter` — mentioned in the [main engine walkthrough](../../GAME_ENGINE.md#where-the-morse-code-actually-lives) —
is also a `Table`, populated from a dedicated `morsecode.atlas` (letters, digits, and
dot/dash glyphs), updated every frame from `FlappyBirdStage.act()`:

```java
// FlappyBirdStage.act()
challengeLetter.setMorseCode(playground.getChallengeLetters());
```

```java
// ChallengeLetter.java
public void setMorseCode(char[] letters) {
    String morseString = MorseHelper.morseCodeData.get(letters[0]);
    for (int i = 0; i < morseString.length(); i++) {
        imageLetters[i].setLetterOrNumber(morseString.charAt(i));   // shows dot/dash glyphs
    }
}
```

This is the same widget-swap-a-`Drawable` technique as `Score`, just choosing between
letter/digit/dot/dash glyphs instead of number glyphs — the HUD showing the player which
Morse pattern they need to fly through next (tying directly back to
[11. Collision Detection](11-collision-detection.md#the-collision-that-doesnt-end-the-game)).

## Where to look

- `com.guidebee.game.ui.{Window,Skin,Table,Button,Image,EventListener}` (`gameengine/src/main/java/com/guidebee/game/ui/`).
- `com.guidebee.game.scene.Stage#addHUDComponent`.
- `flappybird/ui/{BaseWindow,MainWindow}.java`, `flappybird/hud/{Score,ChallengeLetter}.java`.

---

[← Back to tutorial index](../README.md) · Previous: [12. The Microedition Game API](12-microedition-game-api.md) · Next: [14. Camera and Viewports](14-camera-and-viewports.md)
