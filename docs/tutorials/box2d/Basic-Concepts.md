# Basic Concepts

In the first tutorials ,we know there’s World, Body, Shape in the Box2D world. so What are they?
Before we go through Body, Fixture, Shape etc one by one, let’s first take a look on the relations among these concepts, this diagram will help you understand individual concepts better.

![box2dconcepts](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/box2dconcepts.png "box2dconcepts")

# World
A physics world is a collection of bodies, fixtures, and constraints that interact together. Normally we only create one world for a give game,it’s the main entity in which all the Box2D bodies live in. When you create or delete a body, you call a function of the world object to do this, so the world is managing all the allocations for the objects within it too. This means that the world is pretty important, so let’s take a look at what we can do with one.

* define gravity
* tune the physics simulation
* find fixtures in a given region
* cast a ray and find intersected fixtures

In Guidebee Game Engine, the world singleton is stored in GameEngine.world . By default ,it’s null, When use Stage-Actor pattern for game design, before create any Box2D body, you need to call Stage.initWorld() method to initialize the Box2D world.  When stage is disposed , the world associated with this Stage is also disposed.  remember to re-initialize GameEngine.world if you have multiple stages uses Box2D.

# Bodies
Bodies are the fundamental objects in the physics scene, but they are not what you actually see bouncing around and colliding with each other.

You can think of a body as the properties of an object that you cannot see (draw) or touch (collide with). These invisible properties are:

* mass – how heavy it is
* velocity – how fast and which direction it’s moving
* rotational inertia – how much effort it takes to start or stop spinning
* angular velocity – how fast and which way it’s rotating
* location – where it is
* angle – which way it is facing

Even if you know all of these characteristics of an object, you still don’t know what it looks like or how it will react when it collides with another object. To define the size and shape of an object we need to use **fixtures**.

There are three types of body available: static, dynamic and kinematic.  We will cover each in more details in later tutorial.

Guidebee Game Engine provide an easy way to create Box2D body counterpart for Actors.  If Actor’s shape is rectangle or Circle (Like The Box and Circle face in the basic example)

```java
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

```
For Rectangle Actor,just simply call initBody it will create an rectangle shape body in the Box2D world, for circle shaped Actor, specify Shape.Type.Circle type and bounding rectangle .
For other shapes (normally we all use polygon shape roughly represent the outline of the actor image) will use PolygonShape to describe the vertices of the polygon. these vertices have to use Box2D unit (MKS). GameEngine provide easy to convert Box2D unit to Pixel and vice versa.

```java
/**
 * pixel to box2d unit ratio
 */
public static float pixelToBox2DUnit = 32.0f;
 
/**
 * Conversion between pixel unit and box2d unit
 * @param pixel
 * @return
 */
public static float toBox2D(float pixel) {
    return pixel / pixelToBox2DUnit;
}
 
 
/**
 * To box2d vertices
 * @param vertices
 * @return
 */
public static float [] toBox2DVertices(float[] vertices){
    for(int i=0;i&amp;amp;lt;vertices.length;i++){
        vertices[i]= toBox2D(vertices[i]);
    }
    return vertices;
}
 
 
/**
 * to pixel vertices
 * @param vertices
 * @return
 */
public static float [] toPixelVertices(float[] vertices){
    for(int i=0;i&amp;amp;lt;vertices.length;i++){
        vertices[i]= toPixel(vertices[i]);
    }
    return vertices;
}
 
/**
 * Conversion between box2d unit and pixel unit
 * @param box2d
 * @return
 */
public static float toPixel(float box2d) {
    return box2d * pixelToBox2DUnit;
}

```

Default we use 32 pixel to 1 unit (meter) in Box2D world, you can change the value based on your game, Box2D world works better between 0.1m to 50 meters.
And when you calculate the pixel coordinates ,refer to following diagram:

![boundrectangle](http://i1.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/boundrectangle.png "boundrectangle")

Normally the bound rectangle is the same size with the sprite image size ,but some time you may only want the Box2D a bit different with the images, you can specify a different bound Rectangle.

Let’s see the example we used in our Basic Tutorial ,we use the Hexagon .

![hexagoncoords](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/hexagoncoords.png "hexagoncoords")

The image size is 32 X 32 pixels , The Bound Rectangle we use the same size as the image size

```java
Rectangle dropRect=new Rectangle(0,0,32,32);
```

but the coordinate of the hexagon vertices ‘s origin is at the center of the bound rectangle. Thus we have the hexagon’s 6 vertices has the following values:

```java
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
```

Like we have said, Box2D have to use MKS (meter ,Kilogram, and seconds) ,we need to convert Pixel unit to Meters use GameEngine’s help methods

```java
return GameEngine.toBox2DVertices(vertices)
```

The other values used when call initBody are restitution ,density and friction.  That’s fixture’s attributes.

# Fixtures
Fixtures are used to describe the size, shape, and material properties of an object in the physics scene. One body can have multiple fixtures attached to it, and the center of mass of the body will be affected by the arrangement of its fixtures. When two bodies collide, their fixtures are used to decide how they will react. The main properties of fixtures are:

* shape – a polygon or circle
* restitution – how bouncy the fixture is
* friction – how slippery it is
* density – how heavy it is in relation to its area.

You can attach multiple fixtures to a single body, if this the case, you need to directly use Box2D physics API to do this.

# Shapes
Every fixture has a shape which is used to check for collisions with other fixtures as it moves around the scene. A shape can be a circle or a polygon.

Actor’s initBody() method internally will create fixture and shape for the body. So if you use Stage-Actor pattern, you wont need to deal with Fixture directly. For simple shape like Rectangle, Circle you don’t even need to touch Shape either.

# Density
how heavy it is in relation to its area,The density of a fixture multiplied by it’s area becomes the mass of that fixture.

# Friction
how slippery a body’s surface is. **friction is a property of individual fixtures and not the body itself**.

# Restitution
Restitution measures how ‘bouncy’ a fixture is. Like friction, it is given a value between 0 and 1, where zero means that the fixture will not bounce at all, and one means that all the energy of the bounce will be conserved. When two fixtures collide with each other, the resulting restitution tends toward the higher of their restitution values.

---

[← Back to tutorial index](../README.md)
