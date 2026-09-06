# User Interface and UI Components

Beside the game scene, a complete game normally have Menus and Options. You can use another Android Activity to create Menus and Options for the game. Here’s the another alternative if you want to stick to a single Activity (GameActivity) application, and want to use GamePlay to manager multiple screens. Guidebee Game Engine also provides some high level UI components to help you build user interfaces. Those UI components are pretty much you have seen in other platforms (include Android). Following is the diagram of UI components:

![UI](http://i1.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/uicomponents.png "UI")

Widget normally is a simple UI component (only include one component) like Image ,Label, List, SelectBox, TextField etc.

Widget Group can be a container which used to manage/layout child widgets or  composite widgets. The VerticialGroup use to layout child widgets vertically , HorizontalGroup use to manage child widget horizontally, Table ,probably is the most versatile can manage child widgets as Table (like Table tag in HTML).

Window class is a sub class of ScreenAdapter ,thus you can call GamePlay’s setScreen to display a user interface (like Menus or Settings etc).

In this tutorial ,we will create two simple window (the images was taken from Bunnies Shooter)

# Main Window

![UI](http://i2.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/ui1.png "UI")

Main Window has a background, a Bird image (has animations) and a Play image button, when click “Play”, the second window is shown. In real game ,it can start the game scene.

# Second Window

![UI](http://i2.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/ui2.png "UI")

The second Window has a different background, two buttons show in the center of screen, a back button is shown bottom left, if click ,it returns to Main Window.

# Skin

The Skin class stores resources for UI widgets to use. It is a convenient container for texture regions, ninepatches, fonts, colors, etc. Skin also provides convenient conversions, such as retrieving a texture region as a ninepatch, sprite, or drawable.

Resources in a skin typically come from a texture atlas, widget styles and other objects defined using JSON, and objects added to the skin via code. Even when JSON is not used, it is still recommended to use Skin with a texture atlas and objects added via code. This is much more convenient to obtain instances of drawables and serves as a central place to obtain UI resources.

Each resource in the skin has a name and type. The regions from a texture atlas can be made available as resources in the skin. Texture regions can be retrieved as a ninepatch, sprite, tiled drawable, or drawable.

```java
TextureAtlas atlas = ...
Skin skin = new Skin();
skin.addRegions(atlas);
...
TextureRegion hero = skin.get("hero", TextureRegion.class);
Resources can also be defined for a skin using JSON or added using code:

Skin skin = new Skin();
skin.add("logo", new Texture("logo.png"));
...
Texture logo = skin.get("logo", Texture.class);
There are convenience methods to retrieve resources for commons types.

Skin skin = ...
Color red = skin.getColor("red");
BitmapFont font = skin.getFont("large");
TextureRegion region = skin.getRegion("hero");
NinePatch patch = skin.getPatch("header");
Sprite sprite = skin.getSprite("footer");
TiledDrawable tiled = skin.getTiledDrawable("pattern");
Drawable drawable = skin.getDrawable("enemy");
```

Skin is a useful container for providing texture regions and other resources that UI widgets need. It can also store the UI widget styles that define how widgets look.

```java
TextButtonStyle buttonStyle = skin.get("bigButton", TextButtonStyle.class);
TextButton button = new TextButton("Click me!", buttonStyle);
```

# Skin JSON

A skin can be populated programmatically. Alternatively, JSON can be used to describe named objects in the skin. This makes it convenient to define the UI widget styles. Note the JSON does not describe texture regions, ninepatche splits, or other information which comes from the texture atlas. However, the JSON may reference the regions, ninepatches, and other resources in the skin by name. The JSON looks like this:

```
{
    className: {
        name: resource,
        ...
    },
    className: {
        name: resource,
        ...
    },
    ...
}
```

className is the fully qualified Java class name for the objects. name is the name of each resource. resource is the JSON for the actual resource object. The JSON corresponds exactly to the names of the fields in the resource’s class.

For this demo, we use Skin json as follows:

```
{
 
  com.guidebee.game.ui.Button$ButtonStyle: {
  play: { down: button_play_down, up: button_play_up },
  shop: { down: button_shop_down,  up: button_shop_up },
  score: { down: button_score_down,  up: button_score_up },
 
 
  },
 
  com.guidebee.game.ui.Image: {
        background : { drawable : background},
        soundon: {drawable: soundon} ,
        soundoff: {drawable: soundoff},
        bird1: {drawable: bird1},
  }
}
```

For button style the fields down, up, and checked are of type Drawable. the button_play_down is the name of altas file.
So normally there’s 3 files when use Skin. i.e
uiskin.json the Skin Json file to describe some properties for each ui components.
uiskin.atlas normal texture altas file
uiskin.png

if you want to use BitmapFont, there might another .fnt and .png file for fonts.
For your convenience, Window has default skin defaultSkin.

Now we can create our MainWindow

```java
package com.guidebee.game.tutorial.ui;
 
 
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.ui.Button;
import com.guidebee.game.ui.Event;
import com.guidebee.game.ui.EventListener;
import com.guidebee.game.ui.Image;
import com.guidebee.game.ui.Table;
import com.guidebee.game.ui.drawable.TextureRegionDrawable;
import com.guidebee.math.Interpolation;
 
import static com.guidebee.game.GameEngine.*;
import static com.guidebee.game.ui.actions.Actions.*;
public class MainWindow extends BaseWindow{
 
 
    public MainWindow(final UIGamePlay gamePlay){
        super(gamePlay);
 
        Image image=new Image(uiskin,"background");
        image.setFillParent(true);
        stack.addComponent(image);
 
        Table table=new Table();
        table.setFillParent(true);
        stack.addComponent(table);
 
        Image birdImage=new Image(uiskin,"bird1");
        table.add(birdImage);
        table.row();
 
        birdImage.addAction(
                sequence(
                        moveTo(100, 100),
                        delay(1.0f),
                        parallel(
                                moveBy(50.f, 9, 5.0f),
                                rotateBy(360f, 5f, Interpolation.swingIn)
                        )
 
 
                ));
 
 
        TextureAtlas textureAtlas=assetManager
                .get("uidemo.atlas",
                        TextureAtlas.class);
        TextureRegion playButton=textureAtlas
                .findRegion("mainmenu_button_play");
        TextureRegion playUpTextRegion=new TextureRegion(
                playButton,0,0,
                playButton.getRegionWidth()/2,
                playButton.getRegionHeight());
        TextureRegion playDownTextRegion=new TextureRegion(
                playButton,
                playButton.getRegionWidth()/2,0,
                playButton.getRegionWidth()/2,
                playButton.getRegionHeight());
 
        Button button=new Button(
                new TextureRegionDrawable(playUpTextRegion)
                ,new TextureRegionDrawable(playDownTextRegion));
 
        table.add(button);
 
        button.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                gamePlay.setScreen(new SecondWindow(gamePlay));
                return false;
            }
        });
 
 
    }
 
 
}
```

The BaseWindow is defined as follows:

```java
package com.guidebee.game.tutorial.ui;
 
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.ui.Skin;
import com.guidebee.game.ui.Stack;
import com.guidebee.game.ui.Window;
 
import static com.guidebee.game.GameEngine.files;
 
 
public abstract class BaseWindow extends Window {
 
    protected final Stack stack=new Stack();
 
    protected final Skin uiskin;
 
    protected final UIGamePlay gamePlay;
 
    public BaseWindow(UIGamePlay gamePlay) {
        super(Configuration.SCREEN_WIDTH,
                Configuration.SCREEN_HEIGHT);
        stack.setFillParent(true);
        addComponent(stack);
        uiskin = new Skin(files.internal("skin/uiskin.json"), 
                new TextureAtlas("skin/uiskin.atlas"));
        this.gamePlay=gamePlay;
    }
}
```

we specify the Skin json and TextureAtlas to create a skin object. later when you create other UI components ,you can reference value in the skin json file.
for example ,the play button, we can use following simple creation
```java
Button button=new Button(uiskin,"play");
```
or use the code in our example to read up and down images from a texture altas, the directly specify the resources for the button. Of course, use skin is my clearer and simpler.

# Second Window

```java
package com.guidebee.game.tutorial.ui;
 
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.tutorial.ui.component.BackgroundImage;
import com.guidebee.game.ui.Button;
import com.guidebee.game.ui.Event;
import com.guidebee.game.ui.EventListener;
import com.guidebee.game.ui.HorizontalGroup;
import com.guidebee.game.ui.Table;
import com.guidebee.game.ui.drawable.TextureRegionDrawable;
 
import static com.guidebee.game.GameEngine.assetManager;
 
 
public class SecondWindow extends BaseWindow{
 
    public SecondWindow(final UIGamePlay gamePlay) {
        super(gamePlay);
        BackgroundImage backgroundImage=new BackgroundImage();
        backgroundImage.setFillParent(true);
        stack.add(backgroundImage);
        Table table=new Table();
        stack.add(table);
        HorizontalGroup horizontalGroup=new HorizontalGroup();
 
        Button scoreButton=new Button(uiskin,"score");
        Button shopButton=new Button(uiskin,"shop");
 
        table.padLeft(10);
 
        table.add(scoreButton);
        table.row();
 
        table.add(shopButton);
        table.pack();
 
        TextureAtlas textureAtlas=assetManager.get("uidemo.atlas",
         TextureAtlas.class);
        TextureRegion playButton=textureAtlas.findRegion("back");
        TextureRegion playUpTextRegion=new TextureRegion(playButton,0,0,
                playButton.getRegionWidth()/2,
                playButton.getRegionHeight());
        TextureRegion playDownTextRegion=new TextureRegion(playButton,
                playButton.getRegionWidth()/2,0,
                playButton.getRegionWidth()/2,
                playButton.getRegionHeight());
 
        Button button=new Button(
             new TextureRegionDrawable(playUpTextRegion)
                ,new TextureRegionDrawable(playDownTextRegion));
 
 
        button.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                gamePlay.setScreen(new MainWindow(gamePlay));
                return false;
            }
        });
        horizontalGroup.bottom();
        horizontalGroup.addComponent(button);
 
        stack.add(horizontalGroup);
 
 
 
 
    }
 
}
```

Here we used horizontalGroup to put the back button. for more information on the usage of the Widget ,please refer to the [javadoc](http://guidebeegameengine.com/javadoc/reference/packages.html)

The video demo

<a href="http://www.youtube.com/watch?feature=player_embedded&v=3ZSSmsaFhQw
" target="_blank"><img src="http://img.youtube.com/vi/3ZSSmsaFhQw/0.jpg" 
alt="IMAGE ALT TEXT HERE" width="480" height="360" border="10" /></a>

---

[← Back to tutorial index](README.md)
