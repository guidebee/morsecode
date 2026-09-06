# Animate With Actions

Last tutorial ,we used Action to animate the bird(Image UI Component) a little bit. Action can also applied to Actors.

The Action offers a large collection of common actions to easily manipulate Actor or UI Components Objects. You can also animate every properties of Actor like position, scale, rotation etc. Some actions also allow you to specify the duration as well as the interpolation algorithm to be used. An action will always complete in an instant if the duration is either omitted or set to 0. Interpolation algorithms are provided by Interpolation class.

The following example illustrates the typical method signatures of the two actions:

```java
moveTo(x,y);
moveTo(x,y,duration);
moveTo(x,y,duration,interpolation);
 
rotateTo(rotation);
rotateTo(rotation,duration);
rotateTo(rotation,duration, interpolation);
```

The moveTo and rotateTo actions both have their actions-specific parameters x,y and rotation respectively. Following diagram give the whole list of actions provided Guidebee Game Engine:

![Actions](http://i2.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/allactions.png "Actions")

Simple actions like add, alpha, color, rotateTo, scaleTo etc are normally used to modified Actors or UI Components’s property. while the following Actions are used to control the order and time of action execution:

* after() This waits for other actions of an actor(ui component) to finish before it’s action is executed
* delay() This delays the execution of an action.
* forever() The repeats an action for ever.
* parallel() This executes a list of actions at the same time.
* repeat() This repeats an action for a given number of times.
* sequence() This executes a list of actions one after another.

To make things easier for developers, the methods of Actions class are intended for static import. There are mainly two reasons for this: convenience and increased readability.
In this demo, we will create 3 birds and two platform actors:

![Actions](http://i2.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/actions.png "Actions")

Bird class

```java
package com.guidebee.game.tutorial.actions.actor;
 
 
import com.guidebee.game.graphics.Animation;
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.scene.Actor;
import com.guidebee.utils.collections.Array;
 
import static com.guidebee.game.GameEngine.*;
 
public class Bird extends Actor {
 
    private float tick = 0.05f;
    private final Animation flyAnimation;
    private float elapsedTime = 0;
 
 
 
    public Bird(int x ,int y){
        super("Bird");
        TextureAtlas textureAtlas = assetManager.get("actions.atlas",
                TextureAtlas.class);
        TextureRegion bird1TextRegion = textureAtlas.findRegion("bird1");
        TextureRegion bird2TextRegion = textureAtlas.findRegion("bird2");
        TextureRegion bird3TextRegion = textureAtlas.findRegion("bird3");
        TextureRegion bird4TextRegion = textureAtlas.findRegion("bird4");
 
        Array<TextureRegion> keyFrames = new Array<TextureRegion>();
        keyFrames.add(bird1TextRegion);
        keyFrames.add(bird2TextRegion);
        keyFrames.add(bird3TextRegion);
        keyFrames.add(bird4TextRegion);
 
        flyAnimation = new Animation(tick, keyFrames);
        setTextureRegion(flyAnimation.getKeyFrame(0));
        setPosition(x,y);
 
 
 
 
    }
 
    @Override
    public void act(float delta) {
        elapsedTime += graphics.getDeltaTime();
        setTextureRegion(flyAnimation.getKeyFrame(elapsedTime, true));
    }
}

```
Platform class

```java
package com.guidebee.game.tutorial.actions.actor;
 
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.scene.Actor;
import com.guidebee.game.tutorial.actions.Configuration;
 
import static com.guidebee.game.GameEngine.assetManager;
 
/**
 * Created by James on 30/11/15.
 */
public class Platform extends Actor {
 
    private final TextureRegion faceTextRegion;
 
    public Platform(int x,int y){
        super("Platform");
        TextureAtlas textureAtlas=assetManager.get("actions.atlas",
                TextureAtlas.class);
        faceTextRegion=textureAtlas.findRegion("obj_obstacle");
        setTextureRegion(faceTextRegion);
        setPosition(x,y);
 
    }
 
 
 
}

```
Then we create a couple of Actions

```java
package com.guidebee.game.tutorial.actions;
 
 
import com.guidebee.game.ui.actions.Action;
import com.guidebee.game.ui.actions.MoveToAction;
import com.guidebee.math.Interpolation;
 
import static com.guidebee.game.ui.actions.Actions.*;
 
public class DemoActions {
 
    public static Action action1=sequence(
 
            moveTo(100,300,3f,Interpolation.swingIn),
            rotateTo(60),
 
            scaleTo(2.0f,2.0f,2f,Interpolation.bounce),
            delay(2f),
 
            scaleTo(1.0f, 1.0f),
            rotateTo(0),
            delay(2f),
            moveTo(100, 100,3f,Interpolation.elasticIn)
 
 
    );
 
    public static Action action2=sequence(
 
            moveTo(400,200,3f,Interpolation.swingIn),
            rotateTo(60),
 
            scaleTo(2.0f,2.0f,2f,Interpolation.bounce),
            delay(2f),
 
            scaleTo(1.0f, 1.0f),
            rotateTo(0),
            delay(2f),
            moveTo(200, 200,3f,Interpolation.elasticIn)
 
 
    );
 
    public static Action action3=sequence(parallel(
 
                    rotateTo(360, 3f, Interpolation.circle),
                    scaleTo(2.0f, 2.0f, 2f, Interpolation.bounce)),
            moveTo(300, 300),
            scaleTo(1f, 1f, 3f, Interpolation.bounceOut)
 
    );
 
    public static Action action4=forever(sequence(
                    moveTo(500, 400, 5f, Interpolation.swing),
                    moveTo(200, 400, 5f, Interpolation.swingOut)
 
            )
 
    );
 
    public static Action action5=repeat(4, sequence(
                    moveTo(600, 300, 2f, Interpolation.bounceOut),
                    moveTo(600, 100, 2f, Interpolation.bounceIn)
 
            )
 
    );
}

```
Then we apply actions for birds and platform in the scene

```java
package com.guidebee.game.tutorial.actions;
 
 
import com.guidebee.game.camera.viewports.StretchViewport;
import com.guidebee.game.scene.Scene;
import com.guidebee.game.tutorial.actions.actor.Background;
import com.guidebee.game.tutorial.actions.actor.Bird;
import com.guidebee.game.tutorial.actions.actor.Ground;
import com.guidebee.game.tutorial.actions.actor.Platform;
 
import static com.guidebee.game.GameEngine.graphics;
 
public class ActionScene extends Scene {
 
    private final Ground platform;
    private final Background background;
    private final Bird bird1;
    private final Bird bird2;
    private final Bird bird3;
 
    private final Platform platform1;
    private final Platform platform2;
 
 
    public ActionScene() {
        super(new StretchViewport(Configuration.SCREEN_WIDTH,
                Configuration.SCREEN_HEIGHT));
 
 
        background=new Background();
        sceneStage.addActor(background);
        platform=new Ground();
        sceneStage.addActor(platform);
 
        bird1 =new Bird(100,100);
        sceneStage.addActor(bird1);
        bird1.addAction(DemoActions.action1);
 
        bird2 =new Bird(200,200);
        sceneStage.addActor(bird2);
        bird2.addAction(DemoActions.action2);
 
        bird3 =new Bird(300,300);
        sceneStage.addActor(bird3);
        bird3.addAction(DemoActions.action3);
 
        platform1=new Platform(200,400);
        sceneStage.addActor(platform1);
        platform1.addAction(DemoActions.action4);
 
        platform2=new Platform(600,100);
        sceneStage.addActor(platform2);
        platform2.addAction(DemoActions.action5);
 
    }
 
    @Override
    public void render(float delta) {
 
        graphics.clearScreen(0, 0, 0.2f, 1);
        super.render(delta);
 
 
    }
 
}

```

For more detail usage of actions, please refer to the [javadoc](http://guidebeegameengine.com/javadoc/reference/packages.html)

The video demo

<a href="http://www.youtube.com/watch?feature=player_embedded&v=EgQQWPymAS8
" target="_blank"><img src="http://img.youtube.com/vi/EgQQWPymAS8/0.jpg" 
alt="IMAGE ALT TEXT HERE" width="480" height="360" border="10" /></a>

---

[← Back to tutorial index](README.md)
