# Body Types

This tutorial will focus on Box2D Body Types. Bodies have position and velocity. You can apply forces, torques, and impulses to bodies. Bodies can be static, kinematic, or dynamic.

![bodytypes](http://i1.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/bodytypes.png "bodytypes")

Here are the body type definitions:

# Static Body
A static body does not move under simulation and behaves as if it has infinite mass. Internally, Box2D stores zero for the mass and the inverse mass. Static bodies can be moved manually by the user. A static body has zero velocity. Static bodies do not collide with other static or kinematic bodies.

The ground Actor is an example of Static Body, let see how we create the ground actor.

```java
public class Ground extends Actor{
    private int groundWidth;
    private final TextureRegion groundTextRegion;
    ...
 
    private float []getVertices(){
        float [] vertices=new float[8];
        int height=groundTextRegion.getRegionHeight();
        vertices[0]=groundWidth;
        vertices[1]=0;
 
        vertices[2]=groundWidth;
        vertices[3]=height;
 
        vertices[4]=0;
        vertices[5]=height;
 
        vertices[6]=0;
        vertices[7]=0;
        return GameEngine.toBox2DVertices(vertices);
 
    }
 
    ...
    public Ground(int x ,int y, int width){
        super("Ground");
        ...
 
        ChainShape chainShape=new ChainShape();
        chainShape.createChain(getVertices());
        initChainBody(BodyDef.BodyType.StaticBody, chainShape,1f,0.5f,0.3f);
        chainShape.dispose();
   }
 
}

```

We use ChainShape (line segements) to represent the body (two vertical lines and one horizontal line ,the green lines which outline the ground image). Then use initChainBody to create the ground body.

# Kinematic Body
A kinematic body moves under simulation according to its velocity. Kinematic bodies do not respond to forces. They can be moved manually by the user, but normally a kinematic body is moved by setting its velocity. A kinematic body behaves as if it has infinite mass, however, Box2D stores zero for the mass and the inverse mass. Kinematic bodies do not collide with other kinematic or static bodies.

In the demo, the two moving platforms are kinematic bodies, we manually move the platforms back and forth with different speed. Now let create the Platform Actor

```java
package com.guidebee.game.tutorial.box2d.actor;
 
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.physics.BodyDef;
import com.guidebee.game.scene.Actor;
import com.guidebee.game.tutorial.box2d.Configuration;
 
import static com.guidebee.game.GameEngine.assetManager;
 
 
public class Platform extends Actor {
 
    private final TextureRegion platformTextRegion;
 
    private int direction =1;
    private final int limit = 100;
    private int step =1;
    private int currentPos=0;
 
    private final int posX;
    private final int posY;
 
 
    public Platform(int x,int y,int step){
        super("Platform");
        posX=x;
        posY=y;
        this.step=step;
        TextureAtlas textureAtlas=assetManager.get("box2d.atlas",
                TextureAtlas.class);
        platformTextRegion =textureAtlas.findRegion("Platform");
        setTextureRegion(platformTextRegion);
        setPosition(posX,posY);
 
        initBody(BodyDef.BodyType.KinematicBody);
    }
 
    public  Platform(){
        this(Configuration.SCREEN_WIDTH- 300,
                Configuration.SCREEN_HEIGHT - 250,1);
 
 
    }
 
    @Override
    public void act(float delta) {
        super.act(delta);
        currentPos+=direction*step;
        if(currentPos>0 && currentPos>=limit) {
            currentPos=limit;
            direction = -1 ;
 
        }
        if(currentPos<0 && currentPos<-limit){
            currentPos=-limit;
            direction = 1 ;
        }
        setX(posX+currentPos);
 
 
    }
}

```
we just call initBody(BodyDef.BodyType.KinematicBody); The actor will create related Kinematic body for the actor, and by default, the body’s position will sync with the Actor’s position.

# Dynamic Body
A dynamic body is fully simulated. They can be moved manually by the user, but normally they move according to forces. A dynamic body can collide with all body types. A dynamic body always has finite,non-zero mass. If you try to set the mass of a dynamic body to zero, it will automatically acquire a mass of one kilogram and it won’t rotate.

Bodies are the backbone for fixtures (shapes). Bodies carry fixtures and move them around in the world.Bodies are always rigid bodies in Box2D. That means that two fixtures attached to the same rigid body never move relative to each other and fixtures attached to the same body don’t collide.Fixtures have collision geometry and density. Normally, bodies acquire their mass properties from the fixtures. However, you can override the mass properties after a body is constructed.You usually keep pointers to all the bodies you create. This way you can query the body positions to update the positions of your graphical entities. You should also keep body pointers so you can destroy them when you are done with them.

Most of the Actors in the game are dynamic bodies,like the “Player” and Faces in our demo. you can simply call initBody() to create a dynamic body for the actor.
Actor has a getBody() method, from which you can get an reference to the body objects relate to the Actor, then you can make some change for the actor.
For example, we want our player Actor awake all the time. we can call

```java
getBody().setSleepingAllowed(false);
```

Here’s the video demo of how these body type interact with each other

<a href="http://www.youtube.com/watch?feature=player_embedded&v=L2a5RJNdwOw
" target="_blank"><img src="http://img.youtube.com/vi/L2a5RJNdwOw/0.jpg" 
alt="IMAGE ALT TEXT HERE" width="240" height="180" border="10" /></a>

---

[← Back to tutorial index](../README.md)
