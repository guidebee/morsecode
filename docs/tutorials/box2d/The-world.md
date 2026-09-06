# The world

Last we will talk about the Box2D world.

The world class contains the bodies and joints. It manages all aspects of the simulation and allows for asynchronous queries (like AABB queries and ray-casts).

Normally you only need one world for each application.The instance is stored globally in GameEngine.world. can you can call Stage.initWorld to initialize the world. Each time you call Stage.initWorld it will destroy exist world if there is one.

# Simulation
The world class is used to drive the simulation. In stage’s internal act method

```java
if (world != null) {
    world.step(delta, GameEngine.VelocityIterations,
            GameEngine.PositionIterations);
    for (Body body : bodiesTobeDeleted) {
        world.destroyBody(body);
    }
    bodiesTobeDeleted.clear();
 
}
```

The default value for VelocityIterations and PositionIterations are 6 ,3. The iteration count controls how many times the constraint solver sweeps over all the contacts and joints in the world. More iteration always yields a better simulation. But don’t trade a small time step for a large iteration count. 60Hz and 10 iterations is far better than 30Hz and 20 iterations.

After stepping, you should clear any forces you have applied to your bodies. This is done with the command World.clearForces. This lets you take multiple sub-steps with the same force field. Default stage wont call world’s clearForces for you.

# Exploring the World
The world is a container for bodies, contacts, and joints. You can grab the body, contact, and joint lists off the world and iterate over them.

# AABB Queries

![alt text](http://i1.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/queryaabb.png "Logo Title Text 1")

Sometimes you want to determine all the shapes in a region. The World class has a fast log(N) method for this using the broad-phase data structure. You provide an AABB in world coordinates and an implementation of QueryCallback. The world calls your class with each fixture whose AABB overlaps the query AABB. Return true to continue the query, otherwise return false. For example, the following
code finds all the fixtures that potentially intersect a specified AABB and wakes up all of the associated bodies. For example, in our Box2DGameStage ,we want to find out which body is touched by you ,if so, apply upward force on it.

```java
public abstract class Box2DGameStage extends Stage 
   implements QueryCallback {
 
    ..
 
    protected final Vector2 touchPoint;
    protected Fixture touchFixture = null;
 
    protected float forceX=0f;
    protected float forceY=500f;
 
  ...
 
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
    public boolean reportFixture(Fixture fixture) {
        Body body = fixture.getBody();
        if (body.getType() == BodyDef.BodyType.DynamicBody) {
            Boolean inside = fixture.testPoint(touchPoint);
            if (inside) {
                this.touchFixture = fixture;
                return false;
            }
        }
        this.touchFixture=null;
        return true;
    }
}

```
# Ray Casts
We have talked about Ray Casts before, here’s some recap.

You can use ray casts to do line-of-sight checks, fire guns, etc. You perform a ray cast by implementing a callback class and providing the start and end points. The world class calls your class with each fixture hit by the ray. Your callback is provided with the fixture, the point of intersection, the unit normal vector, and the fractional distance along the ray. You cannot make any assumptions about the order of the callbacks.
You control the continuation of the ray cast by returning a fraction. Returning a fraction of zero indicates the ray cast should be terminated. A fraction of one indicates the ray cast should continue as if no hit occurred. If you return the fraction from the argument list, the ray will be clipped to the current intersection point. So you can ray cast any shape, ray cast all shapes, or ray cast the closest shape by returning the appropriate fraction.You may also return of fraction of -1 to filter the fixture. Then the ray cast will proceed as if the fixture
does not exist.

Here the fraction value refers to the ‘fraction’ parameter that is passed to the callback. If I can be bothered I might make a diagram for this later, but if the above description is not clear just remember the common cases:

*To find only the closest intersection:
 return the fraction value from the callback
 use the most recent intersection as the result

*To find all intersections along the ray:
 return 1 from the callback
 store the intersections in a list

*To simply find if the ray hits anything:
 if you get a callback, something was hit (but it may not be the closest)
 return 0 from the callback for efficiency

# Forces and Impulses
You can apply forces, torques, and impulses to a body. When you apply a force or an impulse, you provide a world point where the load is applied. This often results in a torque about the center of mass.

```java
public void applyAngularImpulse (float impulse, boolean wake)
public void applyForce (Vector2 force, Vector2 point, boolean wake)
public void applyTorque (float torque, boolean wake)
public void applyForceToCenter (Vector2 force, boolean wake)
public void applyLinearImpulse (Vector2 impulse, 
    Vector2 point, boolean wake)
```

Applying a force, torque, or impulse wakes the body. Sometimes this is undesirable. For example, you may be applying a steady force and want to allow the body to sleep to improve performance.

# Coordinate Transformations
The body class has some utility functions to help you transform points and vectors between local and world space. If you don’t understand these concepts, please read “Essential Mathematics for Games and Interactive Applications” by Jim Van Verth and Lars Bishop.
```java
Vector2 getWorldPoint();
Vector2 getWorldVector();
Vector2 getLocalPoint();
Vector2 getLocalVector();
```

# Implicit Destruction
Box2D doesn’t use reference counting. So if you destroy a body it is really gone. Accessing a pointer to a destroyed body has undefined behavior. In other words, your program will likely crash and burn. recall Stage’ act method, in Guidebee Game Engine, we use a array bodiesTobeDeleted to queue bodies to be deleted, then in next loop, these bodies will be destroyed to avoid such problem.

When you destroy a body, all its attached shapes, joints, and contacts are destroyed. This is called implicit destruction. Any body connected to one of those joints and/or contacts is woken. This process is usually convenient.

Implicit destruction is a great convenience in many cases. It can also make your program fall apart. You may store pointers to shapes and joints somewhere in your code. These pointers become orphaned when an associated body is destroyed. The situation becomes worse when you consider that joints are often created by a part of the code unrelated to management of the associated body.
You can implement a DestructionListener that allows World to inform you when a shape or joint is implicitly destroyed because an associated body was destroyed. This will help prevent your code from accessing orphaned Objects.

# Pixels and Coordinate Systems
Recall that Box2D uses MKS (meters, kilograms, and seconds) units and radians for angles. You may have trouble working with meters because your game is expressed in terms of pixels. To deal with this in the GameEngine class, we provide you some help methods like toBox2D,toPixel help you convert coordinates between Pixel unit and MKS.
The default conversion ration is defined as follows

```java
/**
 * pixel to box2d unit ratio
 */
public static float pixelToBox2DUnit = 32.0f;
```

You may adjust as needed, Box2D works better between 0.1m to 50 meters.

This concludes this serials of Guidebee Game Engine Box2D tutorial. If you want to contact us for Android Development/Consulting work in Perth, please drop us a mail at james.shen@guidebee.com.

---

[← Back to tutorial index](../README.md)
