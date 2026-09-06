# More on Bodies

Previous we have talked about Box2D Body types .Since Body is the most important concepts in Box2D, we will talk more about Body in this tutorial.

Bodies have position and velocity. You can apply forces, torques, and impulses to bodies. Bodies can be static, kinematic, or dynamic. Here are the body type definitions:

# About Body
**StaticBody**
A static body does not move under simulation and behaves as if it has infinite mass. Internally, Box2D stores zero for the mass and the inverse mass. Static bodies can be moved manually by the user. A static body has zero velocity. Static bodies do not collide with other static or kinematic bodies.

**KinematicBody**
A kinematic body moves under simulation according to its velocity. Kinematic bodies do not respond to forces. They can be moved manually by the user, but normally a kinematic body is moved by setting its velocity. A kinematic body behaves as if it has infinite mass, however, Box2D stores zero for the mass and the inverse mass. Kinematic bodies do not collide with other kinematic or static bodies.

**DynamicBody**
A dynamic body is fully simulated. They can be moved manually by the user, but normally they move according to forces. A dynamic body can collide with all body types. A dynamic body always has finite,non-zero mass. If you try to set the mass of a dynamic body to zero, it will automatically acquire a mass of one kilogram and it won’t rotate.Bodies are the backbone for fixtures (shapes). Bodies carry fixtures and move them around in the world.Bodies are always rigid bodies in Box2D. That means that two fixtures attached to the same rigid body never move relative to each other and fixtures attached to the same body don’t collide. Fixtures have collision geometry and density. Normally, bodies acquire their mass properties from the fixtures. However, you can override the mass properties after a body is constructed.

# Body Definition
Actor ‘s initBody internal create BodyDef for you. you can specify Body type when you call initBody or addBody.

**Position and Angle**

The body definition gives you the chance to initialize the position of the body on creation. This has far better performance than creating the body at the world origin and then moving the body. When Body is created ,it uses Actor’s position as it’s location.

A body has two main points of interest. The first point is the body’s origin. Fixtures and joints are attached relative to the body’s origin. The second point of interest is the center of mass. The center of mass is determined from mass distribution of the attached shapes or is explicitly set with MassData. Much of Box2D’s internal computations use the center of mass position. For example Body stores the linear velocity for the center of mass. When you are building the body definition, you may not know where the center of mass is located.
Therefore you specify the position of the body’s origin. You may also specify the body’s angle in radians, which is not affected by the position of the center of mass. If you later change the mass properties of the body, then the center of mass may move on the body, but the origin position does not change and the attached shapes and joints do not move.

**Damping**

Damping is used to reduce the world velocity of bodies. Damping is different than friction because friction only occurs with contact. Damping is not a replacement for friction and the two effects should be used together.

Damping parameters should be between 0 and infinity, with 0 meaning no damping, and infinity meaning full damping. Normally you will use a damping value between 0 and 0.1. I generally do not use linear damping because it makes bodies look like they are floating
```java
bodyDef.linearDamping = 0.0f;
bodyDef.angularDamping = 0.01f;
```

Damping is approximated for stability and performance. At small damping values the damping effect is mostly independent of the time step. At larger damping values, the damping effect will vary with the time step.

**Gravity Scale**

You can use the gravity scale to adjust the gravity on a single body. Be careful though, increased gravity can decrease stability.
```java
// Set the gravity scale to zero so this body will float
bodyDef.gravityScale = 0.0f;
```

**Sleep Parameters**

What does sleep mean? Well it is expensive to simulate bodies, so the less we have to simulate the better. When a body comes to rest we would like to stop simulating it.
When Box2D determines that a body (or group of bodies) has come to rest, the body enters a sleep state which has very little CPU overhead. If a body is awake and collides with a sleeping body, then the sleeping body wakes up. Bodies will also wake up if a joint or contact attached to them is destroyed. You can also wake a body manually.
The body definition lets you specify whether a body can sleep and whether a body is created sleeping.

```java
bodyDef.allowSleep = true;
bodyDef.awake = true;
```

**Fixed Rotation**

You may want a rigid body, such as a character, to have a fixed rotation. Such a body should not rotate,even under load. You can use the fixed rotation setting to achieve this:
```java
bodyDef.fixedRotation = true;
```
The fixed rotation flag causes the rotational inertia and its inverse to be set to zero.

**Bullets**

Game simulation usually generates a sequence of images that are played at some frame rate. This is called discrete simulation. In discrete simulation, rigid bodies can move by a large amount in one time step. If a physics engine doesn’t account for the large motion, you may see some objects incorrectly pass through each other. This effect is called tunneling.
By default, Box2D uses continuous collision detection (CCD) to prevent dynamic bodies from tunneling through static bodies. This is done by sweeping shapes from their old position to their new positions.The engine looks for new collisions during the sweep and computes the time of impact (TOI) for these collisions. Bodies are moved to their first TOI and then the solver performs a sub-step to complete the full time step. There may be additional TOI events within a sub-step.Normally CCD is not used between dynamic bodies. This is done to keep performance reasonable. In
some game scenarios you need dynamic bodies to use CCD. For example, you may want to shoot a high speed bullet at a stack of dynamic bricks. Without CCD, the bullet might tunnel through the bricks.Fast moving objects in Box2D can be labeled as bullets. Bullets will perform CCD with both static and dynamic bodies. You should decide what bodies should be bullets based on your game design. If you decide a body should be treated as a bullet, use the following setting.

```java
bodyDef.bullet = true;
```

The bullet flag only affects dynamic bodies.

**Activation**

You may wish a body to be created but not participate in collision or dynamics. This state is similar to sleeping except the body will not be woken by other bodies and the body’s fixtures will not be placed in the broad-phase. This means the body will not participate in collisions, ray casts, etc. You can create a body in an inactive state and later re-activate it.

```java
bodyDef.active = true;
```

Joints may be connected to inactive bodies. These joints will not be simulated. You should be careful when you activate a body that its joints are not distorted.
Note that activating a body is almost as expensive as creating the body from scratch. So you should not use activation for streaming worlds. Use creation/destruction for streaming worlds to save memory.

**User Data**

After you call initBody, the body’s user data always set to the actor associated with the Box2D body.

**Mass Data**

A body has mass (scalar), center of mass (2-vector), and rotational inertia (scalar). For static bodies, the mass and rotational inertia are set to zero. When a body has fixed rotation, its rotational inertia is zero. Normally the mass properties of a body are established automatically when fixtures are added to the body. You can also adjust the mass of a body at run-time. This is usually done when you have special game scenarios that require altering the mass.

```java
setMassData(MassData data);
```

After setting a body’s mass directly, you may wish to revert to the natural mass dictated by the fixtures.
You can do this with:

```java
resetMassData();
```
The body’s mass data is available through the following functions:

```java
float getMass();
float getInertia();
Vector2 getLocalCenter();
MassData getMassData();
```

**State Information**

There are many aspects to the body’s state. You can access this state data efficiently through the following functions:

```java
void setType(BodyType type);
BodyType GetType();
void getBullet(boolean flag);
boolean isBullet();
void setSleepingAllowed(boolean flag);
boolean isSleepingAllowed();
void setAwake(boolean flag);
boolean isAwake();
void setActive(boolean flag);
boolean isActive();
void setFixedRotation(boolean flag);
boolean isFixedRotation();
Position and Velocity
```

You can access the position and rotation of a body. This is common when rendering your associated game actor. You can also set the position, although this is less common since you will normally use Box2D to simulate movement.

```java
boolean setTransform(Vector2 position, float angle);
Transform getTransform();
Vector2 GetPosition();
float GetAngle();
```

You can access the center of mass position in local and world coordinates. Much of the internal simulation in Box2D uses the center of mass. However, you should normally not need to access it.
Instead you will usually work with the body transform. For example, you may have a body that is square. The body origin might be a corner of the square, while the center of mass is located at the center of the square.

Here’s a demo, we allow the player to shoot bullets to those faces:

![bulletbody](http://i2.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/bulletbody.png "bulletbody")

We add a bullet actor

```java
package com.guidebee.game.tutorial.box2d.actor;
 
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.physics.BodyDef;
import com.guidebee.game.physics.Shape;
import com.guidebee.game.scene.Actor;
import com.guidebee.game.tutorial.box2d.Configuration;
import com.guidebee.math.Vector2;
import com.guidebee.math.geometry.Rectangle;
 
import static com.guidebee.game.GameEngine.assetManager;
 
 
public class Bullet extends Actor {
 
    private static final TextureRegion bulletTextRegion;
 
    private volatile  boolean touchedOther=false;
 
    static{
        TextureAtlas textureAtlas=assetManager.get("box2d.atlas",
                TextureAtlas.class);
        bulletTextRegion =textureAtlas.findRegion("reddot");
 
    }
 
    public void setTouchedOther(){
        touchedOther=true;
    }
 
    public  Bullet(float x,float y){
        super("Bullet");
        setTextureRegion(bulletTextRegion);
        setPosition(x, y);
        Rectangle dropRect=new Rectangle(0,0,
                bulletTextRegion.getRegionWidth(),
                bulletTextRegion.getRegionHeight());
        initBody(BodyDef.BodyType.DynamicBody,
                Shape.Type.Circle,dropRect,1.0f,0.5f,0.1f);
 
 
    }
 
    @Override
    public void act(float delta){
        super.act(delta);
        if(getX()<0 || getX()>Configuration.SCREEN_WIDTH ||
                getY() > Configuration.SCREEN_HEIGHT  || touchedOther){
            remove();
        }
 
    }
 
 
}
```

Notice here in the act method ,if the bullet go out of the screen or touches other object, it remove itself from the scene.

Then we add the “Fire” button event handler for the player

```java
if (button == GameButton.BUTTON_A) { //shoot
    Bullet bullet=null;
    float bulletX=getCenterX();
    float bulletY=getY()+getHeight();
    float xForce=0;
    float yForce=bulletForce;
 
    switch(actorFaceDirection){
 
        case SOUTHEAST:
            bulletX=getX()+getWidth();
            bulletY=getY();
            xForce=bulletForce/2;
            yForce=-bulletForce/2;
            break;
        case SOUTHWEST:
            bulletX=getX();
            bulletY=getY();
            xForce=-bulletForce/2;
            yForce=-bulletForce/2;
            break;
        case NORTHEAST:
            bulletX=getX()+getWidth();
            bulletY=getY()+getHeight();
            xForce=bulletForce/2;
            yForce=bulletForce/2;
            break;
 
        case NORTHWEST:
            bulletX=getX();
            bulletY=getY()+getHeight();
            xForce=-bulletForce/2;
            yForce=bulletForce/2;
            break;
        case SOUTH:
            bulletX=getCenterX();
            bulletY=getY();
            xForce=0;
            yForce=-bulletForce;
 
 
            break;
        case EAST:
 
            bulletX=getX()+getWidth();
            bulletY=getY()+getHeight()/4;
            xForce=bulletForce;
            yForce=0;
 
 
            break;
        case WEST:
 
            bulletX=getX();
            bulletY=getY()+getHeight()/4;
            xForce=-bulletForce;
            yForce=0;
 
 
            break;
        case NORTH:
        default:
            bulletX=getCenterX();
            bulletY=getY()+getHeight();
            xForce=0;
            yForce=bulletForce;
 
            break;
    }
 
 
    bullet=new Bullet(bulletX,bulletY);
    bullet.getBody().setBullet(true);
    bullet.getBody().applyLinearImpulse(
            new Vector2(xForce, yForce),
            bullet.getBody().getWorldCenter(),true);
    setFilter(bullet.getBody());
    bulletGroup.addActor(bullet);
}

```
The player can fire bullets in 8 different directions, we use applyLinearImpulse to simulate fast bullet moving.

And in the collision event handler ,we set bullet’s touchedOther flag to true

```java
@Override
public void collisionDetected(Collision collision) {
    Collidable objectA=collision.getObjectA();
    Collidable objectB=collision.getObjectB();
 
    //collision event can happen multiple times,
 
    if(objectA.getName()=="Bullet"){
        ((Bullet)objectA).setTouchedOther();
    }
    if(objectB.getName()=="Bullet"){
        ((Bullet)objectB).setTouchedOther();
    }
 
    if(!(objectA.getName()=="Ground" && objectB.getName()=="Ground")) {
        Log.d("Collide", collision.getObjectA().getName() + ":" +
                collision.getObjectB().getName());
    }
 
}

```
Note: you may think of remove the bullet in the collision event itself, this may cause some problem since the collision event may happen multiple times, if you try to delete same bullets multiple time, it can cause app to crash.

<a href="http://www.youtube.com/watch?feature=player_embedded&v=EQqw-FueoFU
" target="_blank"><img src="http://img.youtube.com/vi/EQqw-FueoFU/0.jpg" 
alt="IMAGE ALT TEXT HERE" width="240" height="180" border="10" /></a>

---

[← Back to tutorial index](../README.md)
