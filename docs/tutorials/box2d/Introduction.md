# Introduction

Box2D is the world’s most ubiquitous 2D physics engine. It’s light, robust, efficient and highly portable. It has been battle-proven in many applications on many platforms, and it’s open-source and free. Check out the Box2D website athttp://www.box2d.org.

Box2D is written in C++,but it’s built in Guidebee Game Engine with java wrapper (via JNI). In this tutorial serials ,we’ll use Guidebee Game Engine on Android as the development environment for our Box2D tutorials.

You can find the Guidebee Game Engine basic development guide at http://www.guidebee.com.au/index.php/tutorials/.

# Box2DGameStage and Common Actors
We will still follow Stage-Actor pattern, to cater for all Box2D tutorials ,we create some common Actors and base GameStage.

Box2DGameStage will serve the base Game Stage for all the tutorial stage, the default behaviour for this game Stage is if you touch any actors on screen, it will apply some force to the actor(body) you touched, default it applied upward force with 500 unit. But you can over write with different values on both x and y directions

```java
package com.guidebee.game.tutorial.box2d.stage;
 
 
import com.guidebee.game.GameEngine;
import com.guidebee.game.camera.viewports.StretchViewport;
import com.guidebee.game.physics.Body;
import com.guidebee.game.physics.BodyDef;
import com.guidebee.game.physics.Box2DDebugRenderer;
import com.guidebee.game.physics.Fixture;
import com.guidebee.game.physics.QueryCallback;
import com.guidebee.game.scene.Stage;
import com.guidebee.game.tutorial.box2d.Configuration;
import com.guidebee.math.Matrix4;
import com.guidebee.math.Vector2;
import com.guidebee.math.Vector3;
 
import static com.guidebee.game.GameEngine.input;
import static com.guidebee.game.GameEngine.toBox2D;
 
public abstract class Box2DGameStage extends Stage implements QueryCallback {
 
    protected final Matrix4 debugMatrix;
    protected final Box2DDebugRenderer debugRenderer;
 
    protected final Vector2 touchPoint;
    protected Fixture touchFixture = null;
 
    protected float forceX=0f;
    protected float forceY=500f;
 
    public Box2DGameStage() {
        super(new StretchViewport(Configuration.SCREEN_WIDTH,
                Configuration.SCREEN_HEIGHT));
        initWorld();
        touchPoint = new Vector2();
        debugMatrix = new Matrix4(getCamera().combined);
        debugMatrix.scale(GameEngine.pixelToBox2DUnit,
                GameEngine.pixelToBox2DUnit, 0);
        debugRenderer = new Box2DDebugRenderer();
    }
 
    @Override
    public void act(float delta) {
        super.act(delta);
        if (input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(input.getX(), input.getY(), 0);
            getCamera().unproject(touchPos);
            touchPoint.set(toBox2D(touchPos.x),
                    toBox2D(touchPos.y));
            getWorld().queryAABB(this, touchPoint.x - 0.001f,
                    touchPoint.y - 0.001f,
                    touchPoint.x + 0.001f, touchPoint.y + 0.001f);
            if (touchFixture != null) {
                Body body = touchFixture.getBody();
                body.applyForce(forceX, forceY * body.getMass(),
                        touchPoint.x, touchPoint.y, true);
            }
        }
    }
 
    @Override
    public void draw() {
        super.draw();
        debugRenderer.render(GameEngine.world, debugMatrix);
    }
 
    @Override
    public boolean reportFixture(Fixture fixture) {
        Body body = fixture.getBody();
        if (body.getType() == BodyDef.BodyType.DynamicBody) {
            Boolean inside = fixture.testPoint(touchPoint);
            if (inside) {
                this.touchFixture = fixture;
                return false;
            }
        }
        return true;
    }
}
```
As usual, we still use 800 X 480 as default screen resolution. Also in the base stage, we called initWorld(), this method will initialize the Box2D world.

To make box2d tutorial less dull ,we added some different shaped face (box, circle, hexagon ,triangle) and some image for ground. Here’s texture atlas we used for box2d tutorials.

![Box2D](http://i2.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/box2d.png "Box2D")

For the box2d Tutorials ,these images are not need, the debugRender defined in base game stage, is a box2D debug feature, it draws shapes in the Box2D world , it will looks like

![Box2D](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/box2d1.png "Box2D")

Some common Actors are as following (more actors will be added as needed)

* Face — A box face

* AnimatedFace — Cirle, Triangle, Hexgaon, Box faces with animation.

* AnimatedFaceGroup  — Actor group, and random spawn animated faces.

* Ground — The ground actor.

If we combine the box2d and Actor together , we will have the followings image:

![Box2D](http://i2.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/box2dwithactor.png "Box2D")

# Face class

```java

package com.guidebee.game.tutorial.box2d.actor;
 
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.scene.Actor;
import com.guidebee.game.tutorial.box2d.Configuration;
 
import static com.guidebee.game.GameEngine.assetManager;
 
public class Face extends Actor {
 
    private final TextureRegion faceTextRegion;
 
    public  Face(){
        super("Face");
        TextureAtlas textureAtlas=assetManager.get("box2d.atlas",
                TextureAtlas.class);
        faceTextRegion=textureAtlas.findRegion("face_box");
        setTextureRegion(faceTextRegion);
 
        setPosition((Configuration.SCREEN_WIDTH
                        -faceTextRegion.getRegionWidth())/2,
                (Configuration.SCREEN_HEIGHT
                        -faceTextRegion.getRegionHeight())/2);
        initBody();
 
    }
 
}

```

Here’s we called initBody in constructor, it will initialize box2d body for this actor. we will cover body ,shape, texture later.

# Ground

```java
package com.guidebee.game.tutorial.box2d.actor;
 
 
import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.scene.Actor;
import com.guidebee.game.tutorial.box2d.Configuration;
 
import static com.guidebee.game.GameEngine.*;
 
public class Ground extends Actor{
 
    private final TextureRegion groundTextRegion;
 
    public Ground(){
        super("Ground");
        TextureAtlas textureAtlas = assetManager.get("box2d.atlas",
                TextureAtlas.class);
        groundTextRegion = textureAtlas.findRegion("ground");
        setSize(Configuration.SCREEN_WIDTH,
                groundTextRegion.getRegionHeight());
        setPosition(0, 0);
        initEdgeBody(0,groundTextRegion.getRegionHeight(),
                Configuration.SCREEN_WIDTH,
                groundTextRegion.getRegionHeight(),1f,0.5f,0.3f);
 
    }
 
    @Override
    public void draw(Batch batch, float parentAlpha) {
        int backWidth = groundTextRegion.getRegionWidth();
        int size = Configuration.SCREEN_WIDTH / backWidth;
        if (size * backWidth < Configuration.SCREEN_WIDTH) size++;
        for (int i = -1; i < size; i++) {
            batch.draw(groundTextRegion,  i * backWidth, 0);
        }
 
 
    }
}

```
for ground,we used initEdgeBody to initialize a edge body for the ground.

# AnimatedFace

```java
package com.guidebee.game.tutorial.box2d.actor;
 
import com.guidebee.game.GameEngine;
import com.guidebee.game.graphics.Animation;
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.scene.Actor;
import com.guidebee.game.tutorial.box2d.Configuration;
import com.guidebee.utils.collections.Array;
 
import static com.guidebee.game.GameEngine.assetManager;
 
 
public class AnimatedFace extends Actor{
 
    /**
     * Animated face types
     */
    public enum Type{
        Box,
        Circle,
        Triangle,
        Hexagon
 
    }
 
    private float elapsedTime = 0;
    private final TextureRegion textureRegion;
    private final Animation animation;
    private final int SPRITE_FRAME_SIZE=2;
    private float tick=0.2f;
 
    private final Type faceType;
 
    public AnimatedFace(Type type){
        faceType=type;
        TextureAtlas textureAtlas=assetManager.get("box2d.atlas",
           TextureAtlas.class);
        switch(faceType){
 
            case Circle:
                textureRegion=textureAtlas.findRegion("face_circle_tiled");
                setName("Face_Circle");
                break;
            case Hexagon:
                textureRegion=textureAtlas.findRegion("face_hexagon_tiled");
                setName("Face_Hexagon");
                break;
            case Triangle:
                textureRegion=textureAtlas.findRegion("face_triangle_tiled");
                setName("Face_Triangle");
                break;
            case Box:
            default:
                textureRegion=textureAtlas.findRegion("face_box_tiled");
                setName("Face_Box");
                break;
        }
 
        Array<TextureRegion> keyFrames = new Array<>();
        int spriteHeight = textureRegion.getRegionHeight();
        int spriteWidth = textureRegion.getRegionWidth() / SPRITE_FRAME_SIZE;
        for (int i = 0; i < SPRITE_FRAME_SIZE; i++) {
            TextureRegion region = new TextureRegion(textureRegion,
                    i * spriteWidth,
                    0,
                    spriteWidth, spriteHeight);
            keyFrames.add(region);
        }
        animation = new Animation(tick, keyFrames);
        setTextureRegion(animation.getKeyFrame(0));
        setPosition(Configuration.SCREEN_WIDTH / 2,
                Configuration.SCREEN_HEIGHT / 2);
 
 
    }
 
    @Override
    public void act(float delta){
        super.act(delta);
        elapsedTime += GameEngine.graphics.getDeltaTime();
        setTextureRegion(animation.getKeyFrame(elapsedTime, true));
 
    }
 
 
 
}

```
This class can display Box, Circle, Hexgon, and Triangle face.

# AnimatedFaceGroup

```java
package com.guidebee.game.tutorial.box2d.actor;
 
import com.guidebee.game.GameEngine;
import com.guidebee.game.physics.BodyDef;
import com.guidebee.game.physics.PolygonShape;
import com.guidebee.game.physics.Shape;
import com.guidebee.game.scene.Group;
import com.guidebee.game.tutorial.box2d.Configuration;
import com.guidebee.math.MathUtils;
import com.guidebee.math.geometry.Rectangle;
import com.guidebee.utils.TimeUtils;
 
 
public class AnimatedFaceGroup extends Group {
    private long lastDropTime =0;
 
    private void spawnFace(){
        int type=(MathUtils.random(4) + 1) % 4;
 
        AnimatedFace.Type faceType=AnimatedFace.Type.values()[type];
        AnimatedFace animatedFace=new AnimatedFace(faceType);
        animatedFace.setPosition(MathUtils.random(0,
                        Configuration.SCREEN_WIDTH - 64),
                Configuration.SCREEN_HEIGHT);
 
        Rectangle dropRect=new Rectangle(0,0,32,32);
        switch(faceType){
            case Box:
                animatedFace.initBody();
                break;
            case Circle:
                animatedFace.initBody(BodyDef.BodyType.DynamicBody,
                        Shape.Type.Circle,dropRect,1.0f,0.5f,0.1f);
 
                break;
            case Triangle:
                PolygonShape triangleShape=new PolygonShape();
 
                float [] triangleVertices=getTriangleVertices();
                triangleShape.set(triangleVertices);
                animatedFace.initBody(BodyDef.BodyType.DynamicBody,
                        triangleShape,dropRect,1.0f,0.5f,0.1f);
                break;
            case Hexagon:
                PolygonShape shape=new PolygonShape();
 
                float [] vertices=getHexagonVertices();
                shape.set(vertices);
                animatedFace.initBody(BodyDef.BodyType.DynamicBody,
                        shape,dropRect,1.0f,0.5f,0.1f);
 
                break;
        }
 
 
        addActor(animatedFace);
        lastDropTime= TimeUtils.nanoTime();
    }
 
    private float [] getHexagonVertices(){
        float[] vertices=new float[12];
        vertices[0]=0;
        vertices[1]=-16;
        vertices[2]=14;
        vertices[3]=-8;
        vertices[4]=14;
        vertices[5]=8;
 
        vertices[6]=0;
        vertices[7]=16;
        vertices[8]=-14;
        vertices[9]=8;
        vertices[10]=-14;
        vertices[11]=-8;
 
        return GameEngine.toBox2DVertices(vertices);
 
    }
 
    private float [] getTriangleVertices(){
        float[] vertices=new float[6];
        vertices[0]=-16;
        vertices[1]=-16;
        vertices[2]=16;
        vertices[3]=-16;
        vertices[4]=0;
        vertices[5]=16;
 
        return GameEngine.toBox2DVertices(vertices);
 
    }
 
    @Override
    public void act(float delta) {
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000) {
            spawnFace();
        }
 
        AnimatedFace [] animatedFaces=getChildren().toArray(AnimatedFace.class);
        for(AnimatedFace rainDrop: animatedFaces){
            float y = rainDrop.getY();
            if(y+64 <0){
                removeActor(rainDrop);
            }
 
        }
    }
 
}

```
We use different method to create bodies for rectangle, triangle, hexagon and circle ,will cover this in more detail later.

Now we have our test stage, will continue our tutorial in later blogs.

<a href="http://www.youtube.com/watch?feature=player_embedded&v=PMDg7VPr-C4
" target="_blank"><img src="http://img.youtube.com/vi/PMDg7VPr-C4/0.jpg" 
alt="IMAGE ALT TEXT HERE" width="240" height="180" border="10" /></a>

Wiht images

<a href="http://www.youtube.com/watch?feature=player_embedded&v=OksI4PQgokA
" target="_blank"><img src="http://img.youtube.com/vi/OksI4PQgokA/0.jpg" 
alt="IMAGE ALT TEXT HERE" width="240" height="180" border="10" /></a>

---

[← Back to tutorial index](../README.md)
