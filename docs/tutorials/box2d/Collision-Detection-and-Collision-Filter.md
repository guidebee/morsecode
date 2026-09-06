# Collision Detection and Collision Filter

In previous tutorial Guidebee Android Game Engine — Collision Detection We have talked about Stage’s build-in Collision Detection Mechanism , when using Box2D ,there’s no much different .

# Collision Detection
We still implement CollisionListener to listen to Collision Events, you still can use setCollisionListener use Colliable.BOUND_RECT as the collision Type. but for Box2D specific ,you can use Collidable.BOX2D_CONTACT.

```java
public class CollisionStage extends Box2DGameStage 
    implements CollisionListener {
 
    ...
    public CollisionStage(){
 
    ...
    setCollisionListener(this, Collidable.BOX2D_CONTACT);
}
 
@Override
public void collisionDetected(Collision collision) {
   Collidable objectA=collision.getObjectA();
   Collidable objectB=collision.getObjectB();
    Log.d("Collide", objectA.getName() + ":" +
                    objectB.getName());
}

```

# Collision Filter
So far in every scene we have made, all the bodies were able to collide with all the other bodies. That is the default behavior.  When we created Box2D body for the Actor , we ignored the Fixtures on purpose, most of the time you don’t need to deal with Fixture directly, the Game Engine handle them on your behalf. But when we talk about Collision Filters, we need to use Fixtures.

Collision filtering allows you to prevent collision between fixtures. For example, say you make a character that rides a bicycle. You want the bicycle to collide with the terrain and the character to collide with the terrain, but you don’t want the character to collide with the bicycle (because they must overlap). Box2D supports such collision filtering using categories and groups.

Box2D supports 16 collision categories. For each fixture you can specify which category it belongs to.You also specify what other categories this fixture can collide with. For example, you could specify in a multiplayer game that all players don’t collide with each other and monsters don’t collide with each other, but players and monsters should collide. This is done with masking bits.

* categoryBits
* maskBits
* groupIndex

Let’s create a CollionStage, on the stage they are several different type of shapes. Triangle Faces, Hexagon Faces, Circle Shapes, plus some other actors (like ground, players, a box shape).

We want to the same type of shapes don’t collide with each others since they are friends :-). but collide with different types of shapes.


![collisonfilter](http://i2.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/collisonfilter.png "collisonfilter")

We define the following Category mask for the shapes.

```java
private final short BOX_CATEGORY_MASK=1;
private final short CIRCLE_CATEGORY_MASK=2;
private final short TRIANGLE_CATEGORY_MASK=4;
private final short HEXAGON_CATEGORY_MASK=8;
Here is the rule for a collision to occur

public boolean shouldCollide(Fixture fixtureA, Fixture fixtureB) {
    Filter filterA = fixtureA.getFilterData();
    Filter filterB = fixtureB.getFilterData();
 
    if (filterA.groupIndex == filterB.groupIndex && filterA.groupIndex != 0) {
        return filterA.groupIndex > 0;
    }
 
    boolean collide = (filterA.maskBits & filterB.categoryBits) != 0
            && (filterA.categoryBits & filterB.maskBits) != 0;
    return collide;
}

```

# Using group indexes
The groupIndex flag of a fixture can be used to override the category and mask settings above. As the name implies it can be useful to group together fixtures that should either always collide, or never collide. The groupIndex is used as a signed integer instead of a bitflag. Here’s how it works – read it slowly because it can be a bit confusing at first. When checking two fixtures to see if they should collide:

* if either fixture has a groupIndex of zero, use the category/mask rules as above
* if both groupIndex values are non-zero but different, use the category/mask rules as above
* if both groupIndex values are the same and positive, collide
* if both groupIndex values are the same and negative, don’t collide

The default value for the groupIndex is zero, so it has not been playing a part in anything so far.
Collisions between fixtures of different group indices are filtered according the category and mask bits.
In other words, group filtering has higher precedence than category filtering.
Note that additional collision filtering occurs in Box2D. Here is a list:

* A fixture on a static body can only collide with a dynamic body.
* A fixture on a kinematic body can only collide with a dynamic body.
* Fixtures on the same body never collide with each other.
* You can optionally enable/disable collision between fixtures on bodies connected by a joint.

 Let’s see how we implement this demo.

```java
package com.guidebee.game.tutorial.box2d.actor;
 
import com.guidebee.game.physics.Body;
import com.guidebee.game.physics.BodyDef;
import com.guidebee.game.physics.Filter;
import com.guidebee.game.physics.Fixture;
import com.guidebee.game.physics.PolygonShape;
import com.guidebee.game.physics.Shape;
import com.guidebee.game.tutorial.box2d.Configuration;
import com.guidebee.math.MathUtils;
import com.guidebee.math.geometry.Rectangle;
import com.guidebee.utils.TimeUtils;
import com.guidebee.utils.collections.Array;
 
public class FilterAnimatedFaceGroup extends AnimatedFaceGroup{
 
    private final short BOX_CATEGORY_MASK=1;
    private final short CIRCLE_CATEGORY_MASK=2;
    private final short TRIANGLE_CATEGORY_MASK=4;
    private final short HEXAGON_CATEGORY_MASK=8;
 
    private boolean useFilter=false;
 
    public void setUseFilter(boolean filter){
        useFilter=filter;
        clearChildren();
    }
 
    private void setFilter(Body body,short categoryBits, short maskBits){
        if(useFilter) {
            if (body != null) {
                Array<Fixture> fixtures = body.getFixtureList();
                Filter filter = new Filter();
                filter.categoryBits = categoryBits;
                filter.maskBits = maskBits;
                for (Fixture fixture : fixtures) {
                    fixture.setFilterData(filter);
      
                }
            }
        }
    }
 
    @Override
    protected void spawnFace(){
        int type=(MathUtils.random(4) + 1) % 4;
 
        AnimatedFace.Type faceType=AnimatedFace.Type.values()[type];
        if(faceType!=AnimatedFace.Type.Box) {
            AnimatedFace animatedFace = new AnimatedFace(faceType);
            animatedFace.setPosition(MathUtils.random(0,
                            Configuration.SCREEN_WIDTH - 64),
                    Configuration.SCREEN_HEIGHT);
 
            Rectangle dropRect = new Rectangle(0, 0, 32, 32);
            switch (faceType) {
                case Box:
                    animatedFace.initBody();
                    setFilter(animatedFace.getBody(), BOX_CATEGORY_MASK,
                            (short) (CIRCLE_CATEGORY_MASK
                                    | TRIANGLE_CATEGORY_MASK
                                    | HEXAGON_CATEGORY_MASK));
                    break;
                case Circle:
                    animatedFace.initBody(BodyDef.BodyType.DynamicBody,
                            Shape.Type.Circle, dropRect, 1.0f, 0.5f, 0.1f);
                    setFilter(animatedFace.getBody(), CIRCLE_CATEGORY_MASK,
                            (short) (BOX_CATEGORY_MASK
                                    | TRIANGLE_CATEGORY_MASK
                                    | HEXAGON_CATEGORY_MASK));
                    break;
                case Triangle:
                    PolygonShape triangleShape = new PolygonShape();
 
                    float[] triangleVertices = getTriangleVertices();
                    triangleShape.set(triangleVertices);
                    animatedFace.initBody(BodyDef.BodyType.DynamicBody,
                            triangleShape, dropRect, 1.0f, 0.5f, 0.1f);
                    setFilter(animatedFace.getBody(), TRIANGLE_CATEGORY_MASK,
                            (short) (BOX_CATEGORY_MASK
                                    | CIRCLE_CATEGORY_MASK
                                    | HEXAGON_CATEGORY_MASK));
                    break;
                case Hexagon:
                    PolygonShape shape = new PolygonShape();
 
                    float[] vertices = getHexagonVertices();
                    shape.set(vertices);
                    animatedFace.initBody(BodyDef.BodyType.DynamicBody,
                            shape, dropRect, 1.0f, 0.5f, 0.1f);
                    setFilter(animatedFace.getBody(), HEXAGON_CATEGORY_MASK,
                            (short) (BOX_CATEGORY_MASK
                                    | CIRCLE_CATEGORY_MASK
                                    | TRIANGLE_CATEGORY_MASK));
 
                    break;
            }
 
 
            addActor(animatedFace);
        }
        lastDropTime= TimeUtils.nanoTime();
 
    }
}

```

As said before, Filter is normally handled internally by the game engine, if you want access Fixture attached to a body, use Body’s getFixtureList(), each body can have multiple fixtures,(like the Tank in previous example. after we have the fixture object, we modified category bits and maskBits. and then call setFilterData on the Filter objects.

Here’s the video demo, the Green button is used to disable/enable collision filter.

<a href="http://www.youtube.com/watch?feature=player_embedded&v=kjx8-hkh_jI
" target="_blank"><img src="http://img.youtube.com/vi/kjx8-hkh_jI/0.jpg" 
alt="IMAGE ALT TEXT HERE" width="240" height="180" border="10" /></a>

# Fixture
Beside filter data , the fixture also responsible for some other properties: Fixtures are used to describe the size, shape, and material properties of an object in the physics scene. One body can have multiple fixtures attached to it, and the center of mass of the body will be affected by the arrangement of its fixtures. When two bodies collide, their fixtures are used to decide how they will react. The main properties of fixtures are:

* shape – a polygon or circle
* restitution – how bouncy the fixture is
* friction – how slippery it is
* density – how heavy it is in relation to its area

---

[← Back to tutorial index](../README.md)
