# Sensors

In the previous topic we saw some settings that can be used to prevent two fixtures from colliding. They just pass through each other as if they couldn’t see each other at all, even though we can see on the screen that they are ovelapping. While this might be the physical behaviour we want, it comes with a drawback: since the fixtures don’t collide, they never give us any BeginContact/EndContact information!

What if we want to have two entities not collide, but we still wanted to get some feedback about when they are overlapping. For example, the Tank’s radar can detect incoming helicopters.
we don’t want the radar scanning area collides with helicopters,but we want to know when the helicopters enter and leave the radar scanning area:

![radarsensor](http://i2.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/radarsensor.png "radarsensor")

This is done by using sensors. A sensor is a fixture that detects collision but does not produce a response. You can flag any fixture as being a sensor. Sensors may be static, kinematic, or dynamic. Remember that you may have multiple fixtures per body and you can have any mix of sensors and solid fixtures. Also, sensors only form contacts when at least one body is dynamic, so you will not get a contact for kinematic versus kinematic, kinematic versus static, or static versus static.

# Tank With Radar
Now we need add a Radar sensor for the Tank we used before. in Box2D ,a sensor is just a normal fixture with isSensor flag on. So we use a circle Shape to represent the Radar scanning area,also implement Sensor listener.

```java
/**
 * Sensor listener.
 */
public interface SensorListener {
    /**
     * begin contact
     * @param contact contact object.
     */
    void beginContact(Contact contact);
 
 
    /**
     * end contgact
     * @param contact
     */
    void endContact(Contact contact);
}

```
Here is our enhanced Tank (equipped with Radar)

```java
package com.guidebee.game.tutorial.box2d.actor;
 
 
import android.util.Log;
 
import com.guidebee.game.physics.BodyDef;
import com.guidebee.game.physics.CircleShape;
import com.guidebee.game.physics.Contact;
import com.guidebee.game.physics.Fixture;
import com.guidebee.game.scene.Actor;
import com.guidebee.game.scene.collision.SensorListener;
import com.guidebee.math.Vector2;
import com.guidebee.math.geometry.Rectangle;
 
import static com.guidebee.game.GameEngine.toBox2D;
 
public class TankWithRadar extends  Tank
        implements SensorListener{
 
    private final Radar radar;
 
    private Actor[] actors=new Actor[2];
 
    public TankWithRadar(Radar radar){
        this.radar=radar;
        setX(200);
        CircleShape radarArea=new CircleShape();
        radarArea.setRadius(toBox2D(250));
        radarArea.setPosition(new Vector2(0,toBox2D(-60)));
        Rectangle dropRect=new Rectangle(0,0,
                tankTextRegion.getRegionWidth(),
                tankTextRegion.getRegionHeight());
        addBodyShape(BodyDef.BodyType.KinematicBody,
                radarArea, dropRect, 1.0f, 0.5f, 0.1f, true);
 
    }
 
        @Override
        public void beginContact(Contact contact) {
            handleContact(true, contact);
 
        }
 
        @Override
        public void endContact(Contact contact) {
            handleContact(false, contact);
 
        }
 
 
        private void setHelicopterPos(Actor helicopter,int index,
                                      boolean enterOrLeave){
            float x=helicopter.getX();
            float y=helicopter.getY();
            if(enterOrLeave) {
                radar.helicopterPostions[index].x =
                        (x - getCenterX()) / 4 + radar.getCenterX();
                radar.helicopterPostions[index].y =
                        (y - getCenterY()) / 4 + radar.getCenterY();
 
            }else{
                radar.helicopterPostions[index].x
                        =radar.helicopterPostions[index].y=0;
            }
            if(enterOrLeave){
                //start tracking this helicopter
                actors[index]=helicopter;
            }else{
                //stop tracking this helicopter
                actors[index]=null;
            }
 
        }
 
        private void handleContact(boolean enterOrLeave,
                                   Contact contact){
            Fixture fixtureA = contact.getFixtureA();
            Fixture fixtureB = contact.getFixtureB();
 
            Actor actorA = (Actor)fixtureA.getBody().getUserData();
            Actor actorB = (Actor)fixtureB.getBody().getUserData();
 
            if(fixtureA.isSensor()) {//Tank's radar
                //ActorB is Helicopter
                if(actorB.getName()=="Helicopter1"){
                    setHelicopterPos(actorB, 0, enterOrLeave);
 
 
                }else if(actorB.getName()=="Helicopter2"){
                    setHelicopterPos(actorB, 1, enterOrLeave);
 
                }
            }
 
            if(fixtureB.isSensor()){//Tank's radar
                //ActorA is Helicopter
                if(actorA.getName()=="Helicopter1"){
                    setHelicopterPos(actorA, 0, enterOrLeave);
 
                }else if(actorA.getName()=="Helicopter2"){
                    setHelicopterPos(actorA, 1, enterOrLeave);
 
                }
            }
 
 
 
            if(enterOrLeave){
                Log.d("Enter", actorA.getName()
                        +", "+actorB.getName());
            }else{
                Log.d("Leave", actorA.getName()
                        +", "+actorB.getName());
            }
        }
 
 
    @Override
    public void act(float delta) {
        super.act(delta);
        //tracking helicopters
        for(int i=0;i<2;i++){
            if(actors[i]!=null){
                setHelicopterPos(actors[i],i,true);
            }
        }
 
    }
 
 
 
}

```
In this demo, we used size 2 array actors to tracking the helicopters, because we know there are only two helicopters, in real games, you may need a Array. The beginContact event happens when two fixtures start to touch each others ,and endContact happens when they leave each other.
From the Contact object you can get the fixtures which collide and some other attributes of the touching like speed, normal, friction, location etc.
The tank will start tracking Helicopter’s position when it enters radar scanning area ,and stop tracking when it leaves.
and we call

```java
public void addBodyShape(BodyDef.BodyType type ,
     Shape polygonShape,Rectangle rect,
     float density,float restitution,float friction,
     boolean isSensor)

```
to add the radar to the tank, give isSensor a true value.

# Radar HUD components
The Radar image on up right should be HUD components, but here we use an Actor, it displays helicopter’s relative location to the tank’s center.

```java
package com.guidebee.game.tutorial.box2d.actor;
 
import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.scene.Actor;
import com.guidebee.game.tutorial.box2d.Configuration;
import com.guidebee.math.Vector2;
 
import static com.guidebee.game.GameEngine.assetManager;
 
public class Radar extends Actor {
 
    private final TextureRegion radarTextRegion;
    private final TextureRegion redDotTextRegion;
 
    public final Vector2[] helicopterPostions=new Vector2[2];
 
 
    public Radar(){
        super("Radar");
 
        TextureAtlas textureAtlas=assetManager.get("box2d.atlas",
                TextureAtlas.class);
        radarTextRegion =textureAtlas.findRegion("radar");
        setTextureRegion(radarTextRegion);
        redDotTextRegion =textureAtlas.findRegion("reddot");
        setPosition(Configuration.SCREEN_WIDTH
                 -radarTextRegion.getRegionWidth()-10,
                Configuration.SCREEN_HEIGHT
                 -radarTextRegion.getRegionHeight()-10);
        helicopterPostions[0]=new Vector2();
        helicopterPostions[1]=new Vector2();
    }
 
    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch,parentAlpha);
        for(Vector2 pos: helicopterPostions){
            if(pos.x * pos.y!=0)
            batch.draw(redDotTextRegion,pos.x,pos.y);
        }
 
    }
}

```
Here’s video demo

<a href="http://www.youtube.com/watch?feature=player_embedded&v=QNgN84KJohc
" target="_blank"><img src="http://img.youtube.com/vi/QNgN84KJohc/0.jpg" 
alt="IMAGE ALT TEXT HERE" width="240" height="180" border="10" /></a>

---

[← Back to tutorial index](../README.md)
