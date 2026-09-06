# Shape Types

As said before, Box2D body defines some invisible properties of an object, these invisible properties includes:

* mass – how heavy it is
* velocity – how fast and which direction it’s moving
* rotational inertia – how much effort it takes to start or stop spinning
* angular velocity – how fast and which way it’s rotating
* location – where it is
* angle – which way it is facing
Shape describe the visible geometries of an object. the shape is also used for collision detection.  be used independently of physics simulation. At aminimum, you should understand how to create shapes that can be later attached to rigid bodies

Box2D shapes implement the b2Shape base class. The base class defines functions to:

* Test a point for overlap with the shape.
* Perform a ray cast against the shape.
* Compute the shape’s AABB.
* Compute the mass properties of the shape.
Box2D support several different kinds of Shapes as following:

![shapetype3](http://i1.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/shapetype3.png "shapetype3")

In This demo ,we will create all these four shapes, some we have used before. also we will create one complicated body Thank (combined with several shapes).

![shapetype3](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/shapetypes1.png "shapetype3")

With Images

![shapetype3](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/shapetypes2.png "shapetype3")

This demo includes

* ChainShape — The Ground
* CircleShape — The Circle face
* EdgeShape  —  Not included in this demo, but Ground was created as EdgeShape in the previous Tutorial.
* PolygonShape  — The Player, Triangle Face, Hexagon Face, Box Face and the Tank.

# Edge Shapes
Edge shapes are line segments. These are provided to assist in making a free-form static environment for your game. A major limitation of edge shapes is that they can collide with circles and polygons but not with themselves.

Previous ,we created the Ground object used EdgeShape:

```java
initEdgeBody(0,groundTextRegion.getRegionHeight(),
                Configuration.SCREEN_WIDTH,
               groundTextRegion.getRegionHeight(),1f,0.8f,0.3f);
```

For Edge shape, you specify two end point of the edge when call initEdgeBody.

In many cases a game environment is constructed by connecting several edge shapes end-to-end. This can give rise to an unexpected artifact when a polygon slides along the chain of edges. In the figure below we see a box colliding with an internal vertex. These ghost collisions are caused when the polygon collides with an internal vertex generating an internal collision normal.

![shapetype3](http://i2.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/ghostcollision.png "shapetype3")

If edge1 did not exist this collision would seem fine. With edge1 present, the internal collision seems like a bug. But normally when Box2D collides two shapes, it views them in isolation.
Fortunately, the edge shape provides a mechanism for eliminating ghost collisions by storing the adjacent ghost vertices. Box2D uses these ghost vertices to prevent internal collisions.

In general stitching edges together this way is a bit wasteful and tedious. This brings us to chain shapes.

# Chain Shapes
The chain shape provides an efficient way to connect many edges together to construct your static game worlds. Chain shapes automatically eliminate ghost collisions and provide two-sided collision.

![shapetype3](http://i1.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/chainshape.png "shapetype3")

For the Ground Actor, it’s body includes 3 edges, so we use ChainShape to represent it’s body.

```java
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
 
ChainShape chainShape=new ChainShape();
chainShape.createChain(getVertices());
initChainBody(BodyDef.BodyType.StaticBody, chainShape,1f,0.5f,0.3f);
chainShape.dispose();
```

You may have a scrolling game world and would like to connect several chains together. You can connect chains together using ghost vertices

```java
// Install ghost vertices
chain.SetPrevVertex(b2Vec2(3.0f, 1.0f));
chain.SetNextVertex(b2Vec2(-2.0f, 0.0f));
```

You may also create loops automatically.
```java
chain.CreateLoop(vs, 4);
```
Self-intersection of chain shapes is not supported.It might work, it might not.

![shapetype3](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/selfintectsetchainshape.png "shapetype3")

Circle Shapes
Circle shapes have a position and radius. Circles are solid. You cannot make a hollow circle using the circle shape.
This is how we create a circle Shape for a body.

animatedFace.initBody(BodyDef.BodyType.DynamicBody,
                        Shape.Type.Circle,dropRect,1.0f,0.5f,0.1f);
# Polygon Shapes
Polygon shapes are solid convex polygons. A polygon is convex when all line segments connecting two points in the interior do not cross any edge of the polygon. Polygons are solid and never hollow. A polygon must have 3 or more vertices.

You can create a polygon shape by passing in a vertex array. For simple Actor (Sprite) like the Box Face, Triangle Face, Hexagon Face and Player. It’s easy to calculate the vertexes of the polygons. and if your body size is same as image size, you can just simply initBody() without any parameter ,Actor will create an rectangle Shape for you.

This is how we create Triangle and Hexagon, we need to calculate the coordinate for all the vertices. (the origin is the center of the bound rectangle).

```java
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
 
 
private AnimatedFace createFace(AnimatedFace.Type type){
 
    AnimatedFace.Type faceType=type;
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
 
    return animatedFace;
 
}

```
For the more complicated Actor, like the Tank, there are a lot of tools can help you define the polygon for you. I use [PhysicsEditor](https://www.codeandweb.com/physicseditor)
It’s pretty simple to use

![shapetype3](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/physicaleditor.png "shapetype3")

Note: remember to move the Anchor point to the center. when publish with Plain Text format, since Box2D only support Convex polygon, we will only focus on the Convex sub polygons part

```
Convex sub polygons:

 
     (15, 42)  , (33, 5)  , (33, 48)  , (31, 50)  , (20, 52) 
     (-30, 50)  , (-32, 48)  , (-32, 5)  , (-13, 41)  , (-19, 52) 
     (8, -56)  , (-6, 36)  , (-3, -56)  , (2, -59) 
     (-22, -64)  , (-28, -14)  , (-32, -16)  , (-35, -25)  , (-36, -60) 
     (29, -64)  , (37, -60)  , (37, -25)  , (33, -16)  , (29, -14)  , (18, -58)  , (23, -63) 
     (4, 36)  , (29, -2)  , (33, 5)  , (15, 42) 
     (-32, 5)  , (-29, -2)  , (-6, 36)  , (-13, 41) 
     (-28, -14)  , (-22, -64)  , (-17, -59)  , (-6, 36)  , (-29, -2) 
     (-2, 67)  , (-3, 36)  , (4, 36)  , (2, 68) 
     (4, 36)  , (-6, 36)  , (14, -57)  , (18, -58)  , (29, -14)  , (29, -2) 
     (-3, -56)  , (-6, 36)  , (-17, -59) 
     (14, -57)  , (-6, 36)  , (8, -56) 

```
I noticed that the y coordinate value is negative to what we need, so in the code ,we need to do some processing. The Tank Body is composed with 12 polygons. we can call initBody with multiple polygons to create the body for the tank.

```java
package com.guidebee.game.tutorial.box2d.actor;
 
import com.guidebee.game.GameEngine;
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.physics.BodyDef;
import com.guidebee.game.physics.PolygonShape;
import com.guidebee.game.scene.Actor;
import com.guidebee.game.tutorial.box2d.Configuration;
import com.guidebee.math.geometry.Rectangle;
 
import static com.guidebee.game.GameEngine.assetManager;
 
 
public class Tank extends Actor {
 
    private final TextureRegion tankTextRegion;
 
    public  Tank(){
        super("Tank");
        TextureAtlas textureAtlas=assetManager.get("box2d.atlas",
                TextureAtlas.class);
        tankTextRegion =textureAtlas.findRegion("tank");
        setTextureRegion(tankTextRegion);
 
        setPosition((Configuration.SCREEN_WIDTH
                        - tankTextRegion.getRegionWidth())/2,
                (Configuration.SCREEN_HEIGHT
                        - tankTextRegion.getRegionHeight())/2+100);
        PolygonShape []polygonShapes=new PolygonShape[tankPolygons.length];
        for(int i=0;i<polygonShapes.length;i++){
            polygonShapes[i]=new PolygonShape();
            polygonShapes[i].set(getTankVertices(i));
        }
 
        Rectangle dropRect=new Rectangle(0,0,
                tankTextRegion.getRegionWidth(),
                tankTextRegion.getRegionHeight());
 
        initBody(BodyDef.BodyType.KinematicBody,
                polygonShapes, dropRect, 1.0f, 0.5f, 0.1f);
 
    }
 
 
    private final String [] tankPolygons=new String[] {
            "15, 42  , 33, 5  , 33, 48  , 31, 50  , 20, 52",
            "-30, 50  , -32, 48  , -32, 5  , -13, 41  , -19, 52",
            "8, -56  , -6, 36  , -3, -56  , 2, -59",
            "-22, -64  , -28, -14  , -32, -16  , -35, -25  , -36, -60",
            "29, -64  , 37, -60  , 37, -25  , 33, -16  , 29, -14  , 18, -58  , 23, -63",
            "4, 36  , 29, -2  , 33, 5  , 15, 42",
            "-32, 5  , -29, -2  , -6, 36  , -13, 41",
            "-28, -14  , -22, -64  , -17, -59  , -6, 36  , -29, -2",
            "-2, 67  , -3, 36  , 4, 36  , 2, 68",
            "4, 36  , -6, 36  , 14, -57  , 18, -58  , 29, -14  , 29, -2",
            "-3, -56  , -6, 36  , -17, -59",
            "14, -57  , -6, 36  , 8, -56",
    };
 
 
    private float [] getTankVertices(int index){
        float[] vertices=GameEngine.getVerticesFromString(tankPolygons[index]);
 
        for(int i=0;i<vertices.length/2;i++){
            vertices[i*2+1]=-vertices[i*2+1];
        }
        return GameEngine.toBox2DVertices(vertices);
 
    }
 
 
 
}

```
Polygons inherit a radius from Shape. The radius creates a skin around the polygon. The skin is used in stacking scenarios to keep polygons slightly separated. This allows continuous collision to work against the core polygon.

![shapetype3](http://i1.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/polyonskin.png "shapetype3")

The polygon skin helps prevent tunneling by keeping the polygons separated. This results in small gaps between the shapes. Your visual representation can be larger than the polygon to hide any gaps.

![shapetype3](http://i2.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/polyonskincollide.png "shapetype3")

---

[← Back to tutorial index](../README.md)
