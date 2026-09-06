# Ray Casts

You can use ray casts to do line-of-sight checks, fire guns, etc. You perform a ray cast by implementing a callback class and providing the start and end points. The world class calls your class with each fixture hit by the ray. Your callback is provided with the fixture, the point of intersection, the unit normal vector,and the fractional distance along the ray.You cannot make any assumptions about the order of the callbacks.
You control the continuation of the ray cast by returning a fraction. Returning a fraction of zero indicates the ray cast should be terminated. A fraction of one indicates the ray cast should continue as if no hit occurred. If you return the fraction from the argument list, the ray will be clipped to the current intersection point. So you can ray cast any shape, ray cast all shapes, or ray cast the closest shape by returning the appropriate fraction.
You may also return of fraction of -1 to filter the fixture. Then the ray cast will proceed as if the fixture does not exist.

The Radar the tank has is a bit advanced, it knows immediately when enemies enters it’s scanning area. Now we downgrade the radar a little, it has to use radar wave to sweep given area, only when the wave  hits the enemy targets, it detects enemy’s existence.

![raycasting](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/raycasting.png "raycasting")

We will use Ray Casting to simulate the radar wave. it turns around and around. it the ray hits/touches helicopter , it then gets helicopter’s position, otherwise it lost tracking of the helicopter.

# Tank with Radar

First we need to define a sweeping vector ,it’s center is located at the tank’s center ,each time it turn 2.5 degree.

```java
private final Vector2 sweepVector;
 
private final float rotateStep =2f;//degree
private final float rayLength = 400f; 
private final Vector2 v1=new Vector2();
private final Vector2 v2 =new Vector2();
 
@Override
public void act(float delta) {
    super.act(delta);
    ...
    sweepVector.rotate(rotateStep);
    v1.x=toBox2D(getCenterX());
    v1.y=toBox2D(getCenterY());
    Vector2 v3=getSweepVector();
    v2.x= toBox2D(v3.x + getCenterX());
    v2.y= toBox2D(v3.y + getCenterY());
    world.rayCast(this,v1,v2);
 
}

```
The v1, v2 Vector are used to Ray casting ,the Ray’s center is at tank’s center.

For debug purpose , we need to draw the ray on screen, we need to sub class Box2DDebugRenderer to draw the line

```java
private class RayCastDebugRenderer extends Box2DDebugRenderer{
 
 
    public RayCastDebugRenderer(){
        renderer.setAutoShapeType(true);
    }
 
    @Override
    public void drawOverlays(){
        Color color=renderer.getColor();
        renderer.setColor(Color.GREEN);
        renderer.line(tank.getV1(), tank.getV2());
        renderer.setColor(color);
    }
}

```
Our new Tank now implements RayCastCallback.

```java
package com.guidebee.game.tutorial.box2d.actor;
 
 
import com.guidebee.game.physics.Fixture;
import com.guidebee.game.physics.RayCastCallback;
import com.guidebee.game.scene.Actor;
import com.guidebee.math.Vector2;
import com.guidebee.utils.TimeUtils;
 
import static com.guidebee.game.GameEngine.toBox2D;
import static com.guidebee.game.GameEngine.world;
 
public class TankWithSweepRadar extends  Tank
        implements RayCastCallback{
 
    private final Radar radar;
 
    private Actor[] actors=new Actor[2];
 
    private final float rotateStep =2f;//degree
    private final float rayLength = 400f;
    private final Vector2 contactPoint=new Vector2();
 
    private final Vector2 sweepVector;
 
    private final Vector2 v1=new Vector2();
    private final Vector2 v2 =new Vector2();
 
    private long lastDropTime =0;
 
    public TankWithSweepRadar(Radar radar){
        this.radar=radar;
        setX(200);
        sweepVector=new Vector2(0,rayLength);
 
    }
 
    public Vector2 getV1(){
        return v1;
    }
 
    public Vector2 getV2(){
        return v2;
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
 
 
    private void clearHelicopterPosition(){
        lastDropTime= TimeUtils.nanoTime();
        for(int i=0;i<2;i++){
            if(actors[i]!=null){
                setHelicopterPos(actors[i],i,false);
            }
        }
        contactPoint.x=contactPoint.y=0;
    }
 
 
    @Override
    public void act(float delta) {
        super.act(delta);
        if(TimeUtils.nanoTime()-lastDropTime>1000000000) {
            clearHelicopterPosition();
        }
 
        sweepVector.rotate(rotateStep);
        v1.x=toBox2D(getCenterX());
        v1.y=toBox2D(getCenterY());
        Vector2 v3=getSweepVector();
        v2.x= toBox2D(v3.x + getCenterX());
        v2.y= toBox2D(v3.y + getCenterY());
        world.rayCast(this,v1,v2);
 
    }
 
    public Vector2 getSweepVector(){
        return sweepVector;
    }
 
 
    @Override
    public float reportRayFixture(Fixture fixture, Vector2 point,
                                  Vector2 normal, float fraction) {
        Actor actor=(Actor)fixture.getBody().getUserData();
        if(actor.getName()=="Tank") return -1.0f;
 
        if(actor!=null) {
            if(actor.getName()=="Helicopter1"){
                actors[0]=actor;
                setHelicopterPos(actors[0],0,true);
            }else if(actor.getName()=="Helicopter2"){
                actors[1]=actor;
                setHelicopterPos(actors[1],1,true);
            }
            contactPoint.x = point.x;
            contactPoint.y = point.y;
        }
        //return fraction;
        return 0f;
    }
}

```

The ray cast call back reports fixture it touches, contact point positions, normal and fraction distance along the ray.

We want to ray cast ignore tank itself. so we return -1f;

```java
if(actor.getName()=="Tank") return -1.0f;
if ray touches helicopter,we report their positions and continue to scan

if(actor!=null) {
    if(actor.getName()=="Helicopter1"){
        actors[0]=actor;
        setHelicopterPos(actors[0],0,true);
    }else if(actor.getName()=="Helicopter2"){
        actors[1]=actor;
        setHelicopterPos(actors[1],1,true);
    }
    contactPoint.x = point.x;
    contactPoint.y = point.y;
}

```
The video demo

<a href="http://www.youtube.com/watch?feature=player_embedded&v=kMNXZuykBDE
" target="_blank"><img src="http://img.youtube.com/vi/kMNXZuykBDE/0.jpg" 
alt="IMAGE ALT TEXT HERE" width="240" height="180" border="10" /></a>

---

[← Back to tutorial index](../README.md)
