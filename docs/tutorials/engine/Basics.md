# Basics

As said before, Guidebee Game Engine was derived from Libgdx ,so we’ll also start with a simple Libgdx simple game RainDrop.

The game idea is very simple:

* Catch raindrops with a bucket.
* Bucket located in the lower part of the screen
* Raindrops spawn randomly at the top of the screen every second and accelerate downwards.
* Player can drag the bucket horizontally via the mouse/touch or move it via the left and right cursor keys.
* The game has no end, think of it as a zen-like experience :)
[![RainDrop Demo](http://img.youtube.com/vi/DAQcXbLIkmY/0.jpg)](http://www.youtube.com/watch?v=DAQcXbLIkmY)

# Coordinate System
Before we start, I think it’s necessary to talk about the game engine coordinate system first. Like Libgdx, guidebee game engine also uses y-up with origin (0,0) at bottom left.

![Coordinate System](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/10/coords.png)

which is a standard euclidian coordinate system with y-up.

# Basic Concepts
To better understand some basic game programming concerts. Assume we go to see a broadway play on stage. Say Romeo and Juliet ,this play consists of many different scenes, each scene may have different stage setup , on the stage actors do the performance, and some people will in charge of sound ,music etc.

![Stage](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/10/stage-glossary.jpg)

bear these in mind, we will design our simple raindrop game. Guidebee Game Engine support most of the Play terminology . Each game is a game play, and a GamePlay consists may Scene. Each Scene assonates with One Stage object. And on the Stage there are Actors.

# The Assets

We need a few images and sound effects to make the game look somewhat pretty. For the graphics we need to define a target resolution of 800×480 pixels (landscape mode on Android). If the device the game is run on does not have that resolution, we simply scale everything to fit on the screen. Note: for high profile games you might want to consider to have different assets for different screen densities. This is a big topic on its own and won’t be covered here.

The raindrop and the bucket should take up a small-ish portion of the screen vertically, so we’ll let them have a size of 64×64 pixels.

I took the assets from the following sources:

* water drop sound by junggle, seehttp://www.freesound.org/people/junggle/sounds/30341/
* rain by acclivity, see http://www.freesound.org/people/acclivity/sounds/28283/
* droplet sprite by mvdv, see https://www.box.com/s/peqrdkwjl6guhpm48nit
* bucket sprite by mvdv, see https://www.box.com/s/605bvdlwuqubtutbyf4x

To make the assets available to the game, we have to put them in the Android project’s assets folder. I named the 4 files: drop.wav, rain.mp3, droplet.png and bucket.png and put them in the /assets/ folder.

# The GamePlay

Each game is a gameplay,GamePlay class normally used to manage different scene and load assets. we define DropGamePlay as following:

```java

package com.mapdigit.game.tutorial.drop;
 
import com.guidebee.game.GamePlay;
import com.guidebee.game.audio.Music;
import com.guidebee.game.audio.Sound;
import com.guidebee.game.graphics.Texture;
 
import static com.guidebee.game.GameEngine.assetManager;
 
public class DropGamePlay extends GamePlay{
    @Override
    public void create() {
        loadAssets();
        DropScene screen=new DropScene();
        setScreen(screen);
    }
 
    @Override
    public void dispose(){
        assetManager.dispose();
    }
 
    private void loadAssets(){
        assetManager.load("droplet.png",Texture.class);
        assetManager.load("bucket.png",Texture.class);
        assetManager.load("drop.wav",Sound.class);
        assetManager.load("rain.mp3",Music.class);
        assetManager.finishLoading();
    }
}
```
In DropGamePlay, we set our main Scene (DropScene) and loads all the assets (images and sounds). You may also load assets on a as-needed basis. for this simple game, we preload all the assets.

# The Scene
This game only needs one scene, we define a DropScene as follows:

```java

package com.mapdigit.game.tutorial.drop;
 
import com.guidebee.game.audio.Music;
import com.guidebee.game.camera.viewports.*;
import com.guidebee.game.scene.Scene;
import com.mapdigit.game.tutorial.drop.actor.Bucket;
import com.mapdigit.game.tutorial.drop.actor.RainDropGroup;
 
import static com.guidebee.game.GameEngine.assetManager;
import static com.guidebee.game.GameEngine.graphics;
 
public class DropScene extends Scene {
 
    private Music rainMusic = assetManager.get("rain.mp3", Music.class);
    private Bucket bucket = new Bucket();
    private RainDropGroup rainDropGroup = new RainDropGroup();
 
 
    public DropScene() {
        super(new ScalingViewport(800,480));
        rainMusic.setLooping(true);
        sceneStage.addActor(bucket);
        sceneStage.addActor(rainDropGroup);
        sceneStage.setCollisionListener(rainDropGroup);
 
    }
 
 
    @Override
    public void render(float delta) {
        graphics.clearScreen(0, 0, 0.2f, 1);
        super.render(delta);
    }
 
    @Override
    public void dispose() {
        super.dispose();
        assetManager.dispose();
    }
 
    @Override
    public void pause() {
        rainMusic.stop();
    }
 
    @Override
    public void show() {
        rainMusic.play();
    }
}
```

For each scene, there’s one associated Stage object called sceneStage, Stage class is used to manage actors. RainDrop game defines two actors: one for the bucket and one for the raindrop. after we have the actor object, call stage’s Add method to add them to the stage.

# The Actors
RainDrop game needs two Actor classes, One is for Bucket ,the other is RainDrop, since we may have many raindrops on screen, for easier raindrop management, we defines a Group class, Group class is a sub class of Actor,but it can contains other Actors.

Bucket Class
```java
package com.mapdigit.game.tutorial.drop.actor;
 
import com.guidebee.game.Input;
import com.guidebee.game.graphics.Texture;
import com.guidebee.game.scene.Actor;
import com.guidebee.math.Vector3;
 
import static com.guidebee.game.GameEngine.*;
 
public class Bucket extends Actor {
 
 
    public Bucket(){
        super("Bucket");
        setTexture(assetManager.get("bucket.png",Texture.class));
        setPosition(800/2-64/2,20);
 
    }
 
    @Override
    public void act(float delta){
        if(input.isTouched()){
            Vector3 touchPos=new Vector3();
            touchPos.set(input.getX(),input.getY(),0);
            getStage().getCamera().unproject(touchPos);
            setX(touchPos.x-64/2);
        }
        if(input.isKeyPressed(Input.Keys.LEFT)){
            setX(getX() - 200 * graphics.getDeltaTime());
        }
        if(input.isKeyPressed(Input.Keys.RIGHT)){
            setX(getX() + 200*graphics.getDeltaTime());
        }
 
        if(getX()<0) setX(0); if(getX() > 800- 64) setX(800-64);
    }
}
```
in Bucket’s actor method, we process user input to move the bucket left or right.

RainDrop class

```java

package com.mapdigit.game.tutorial.drop.actor;
 
import com.guidebee.game.graphics.Texture;
import com.guidebee.game.scene.Actor;
import static com.guidebee.game.GameEngine.assetManager;
 
public class RainDrop extends Actor {
    public RainDrop(){
        super("RainDrop");
        setTexture(assetManager.get("droplet.png",Texture.class));
    }
}

```
We’ll handle raindrop in RainDropGroup. thus we ignore RainDrop’s actor method

RainDropGroup class

```java
package com.mapdigit.game.tutorial.drop.actor;
 
import com.guidebee.game.audio.Sound;
import com.guidebee.game.scene.Actor;
import com.guidebee.game.scene.Group;
import com.guidebee.game.scene.collision.Collision;
import com.guidebee.game.scene.collision.CollisionListener;
import com.guidebee.math.MathUtils;
import com.guidebee.utils.TimeUtils;
 
 
import static com.guidebee.game.GameEngine.assetManager;
import static com.guidebee.game.GameEngine.graphics;
 
public class RainDropGroup extends Group implements CollisionListener{
 
    private long lastDropTime =0;
    private Sound dropSound=assetManager.get("drop.wav",Sound.class);
 
 
    private void spawnRainDrop(){
        RainDrop rainDrop=new RainDrop();
        //rainDrop.rotateBy((float)(Math.random()*360));
        //rainDrop.debug();
 
        rainDrop.setPosition(MathUtils.random(0,800-64),480);
        addActor(rainDrop);
        lastDropTime= TimeUtils.nanoTime();
    }
 
    private void collisionDetected(Actor actor){
        dropSound.play();
        removeActor(actor);
    }
 
 
    @Override
    public void act(float delta){
        if(TimeUtils.nanoTime()-lastDropTime>1000000000) {
            spawnRainDrop();
        }
 
        RainDrop [] rainDrops=getChildren().toArray(RainDrop.class);
        for(RainDrop rainDrop: rainDrops){
            float y = rainDrop.getY() - 200 * graphics.getDeltaTime();
            rainDrop.setY(y);
            if(y+64 <0){
                removeActor(rainDrop);
            }
 
        }
    }
 
    @Override
    public void collisionDetected(Collision collision) {
        if(collision!=null){
            Actor actor1=(Actor)collision.getObjectA();
            Actor actor2=(Actor)collision.getObjectB();
            if(actor1!=null && actor2!=null){
                if(actor1.getName()=="Bucket" && actor2.getName()=="RainDrop"){
                    actor2.getParent().removeActor(actor2);
                    dropSound.play();
                }else if(actor2.getName()=="Bucket" && actor1.getName()=="RainDrop"){
                    actor1.getParent().removeActor(actor1);
                    dropSound.play();
                }
            }
        }
 
 
    }
}

```
in RainDropGroup’s act method, we move the raindrop downwards and check if any raindrop moves out of the screen, if so, we remove it from the group.

and if the player moves bucket, and bucket catches the raindrop, we also need to remove the raindrop (normally player scores, for simplicity , we just remove the rain drop and play the sound). Stage can monitor object collision, if any two or more actors collides, it triggers collision listeners.

# The GameActivity
Finally, since we are writing game for android, we need to hook up our game logic to Android Activity, GameActivity is the bridge between GamePlay and Android Activity. it use configuration to initialise GamePlay.

```java
package com.mapdigit.game.tutorial.drop;
 
import android.os.Bundle;
import com.guidebee.game.Configuration;
import com.guidebee.game.activity.GameActivity;
 
 
public class DropGameActivity extends GameActivity {
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration config = new Configuration();
 
        config.useAccelerometer = false;
        config.useCompass = false;
 
        initialize(new DropGamePlay(), config);
    }
}

```
# The GameEngine
GameEngine provides to the AssetMaagner,Application, Graphics, Audio, Files and Input instances. The references are held in public static fields which allows static access to all sub systems. Do not use Graphics in a thread that is not the rendering thread.

We’ll continue write blogs for more details on Guidebee Game Engine.

---

[← Back to tutorial index](../README.md)
