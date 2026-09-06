# Basic Graphics

Graphics is one of most important part for game design. Guidebee Android Game Engine includes many packages and classes to help you deal with graphics, following are some major classes and packages to support graphics (access hardware graphics processor , vector images, raster images and SVG images).

# Graphics class

![Graphics](http://i1.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/devicegraphics.png)

This interface encapsulates communication with the graphics processor.If supported by the back-end, this interface lets you query the available display modes (graphics resolution and color depth) and change it.This interface can be used to switch between continuous and non-continuous rendering.
By default the rendering thread calls the render() method of your GameActivity continuously, with a frequency that depends on your hardware (30-50-80 times per second).
If you have many still frames in your game (think about a card game) you can save precious battery power by disabling the continuous rendering, and calling it only when you really need it.

All you need is put the following lines in your GameActivity’s create() method

```java
GameEngine.graphics.setContinuousRendering(false);
GameEngine.graphics.requestRendering();
```

The first line tells the game to stop calling the render() method automatically. The second line triggers the render() method once. You have to use the second line wherever you want the render() method to be called.

If continuous rendering is set to false, the render() method will be called only when the following things happen.

* An input event is triggered
* GameEngine.graphics.requestRendering() is called
* GameEngine.app.postRunnable() is called

Graphics’s getWidth and getHeight() can return display screen’s resolution in pixels.

# game.graphics
This package defines class relate to graphics like Sprite,Animation,SVG ,Pixmap, Texture. TextureRegion

![image graphics](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/gamegraphics.png)

FrameBuffer class encapsulates OpenGL ES 2.0 frame buffer objects, most of the time, you dont need to use it directly if you don’t want to know much details about OpenGL ES.
Pixmap represents an image in memory ,it pretty much likes Bitmap object in other platforms.It supports simple file loading and draw operations for basic image manipulation. The most typical use is preparation of an image for upload to the GPU by wrapping in a Texture instance.As a Pixmap resides in native heap memory it must be disposed of by calling dispose() when no longer needed to prevent memory leaks.
A Texture is nothing more than a decoded image loaded into the GPU’s memory in raw format. since the game engine internally uses OpenGL which require your image files to be in power of two dimensions. This means your width and height are 2,4,8,16,32,64,128,256,512,1024,2048, etc… pixels in size.
Most of the times the game has multiple drawable elements. To draw all the images at once onto the screen, each element needs to be turned into a texture. It sounds straight-forward but in reality it is very expensive on the GPU side as each texture needs to be uploaded to the GPU, before being drawn and bound so OpenGL will take the active one to map it. The texture binding and switching is very expensive.
TextureRegion solves this issue by cutting out a region from a texture and lets SpriteBatch work with it. This means that a single texture can be created for all the elements needed to be drawn and when drawing them, only the region corresponding to the element will be drawn. A texture that contains multiple elements (sprites) is also called a sprite sheet.

# drawing package
drawing and drawing.geometry package support vector graphics drawing, the packages includes following classes, Graphics2D is the vector canvas where vector graphics draw upon.

![drawing package](http://i1.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/drawingpackage.png)

let’s use drawing package to draw a vector Pear

```java
package com.mapdigit.game.tutorial.drop.drawing;
 
import com.guidebee.drawing.Color;
import com.guidebee.drawing.Graphics2D;
import com.guidebee.drawing.SolidBrush;
import com.guidebee.drawing.geometry.Area;
import com.guidebee.drawing.geometry.Ellipse;
import com.guidebee.game.graphics.Pixmap;
import com.guidebee.game.graphics.Texture;
import com.guidebee.utils.Disposable;
 
/**
 * draw a pear with path.
 */
public class Pear implements Disposable {
 
    private Ellipse circle, oval, leaf, stem;
    private Area circ, ov, leaf1, leaf2, st1, st2;
 
    private Graphics2D graphics2D;
 
    private Texture texture;
 
    public Pear() {
        draw();
    }
 
    public void init() {
        graphics2D = new Graphics2D(100, 100);
        graphics2D.clear();
        circle = new Ellipse();
        oval = new Ellipse();
        leaf = new Ellipse();
        stem = new Ellipse();
        circ = new Area(circle);
        ov = new Area(oval);
        leaf1 = new Area(leaf);
        leaf2 = new Area(leaf);
        st1 = new Area(stem);
        st2 = new Area(stem);
    }
 
    private void draw() {
        init();
        int w = 100;
        int h = 100;
        int ew = w / 2;
        int eh = h / 2;
        SolidBrush brush = new SolidBrush(Color.GREEN);
        graphics2D.setDefaultBrush(brush);
        // Creates the first leaf by filling the intersection of two Area
        //objects created from an ellipse.
        leaf.setFrame(ew - 16, eh - 29, 15, 15);
        leaf1 = new Area(leaf);
        leaf.setFrame(ew - 14, eh - 47, 30, 30);
        leaf2 = new Area(leaf);
        leaf1.intersect(leaf2);
        graphics2D.fill(null, leaf1);
 
        // Creates the second leaf.
        leaf.setFrame(ew + 1, eh - 29, 15, 15);
        leaf1 = new Area(leaf);
        leaf2.intersect(leaf1);
        graphics2D.fill(null, leaf2);
 
        brush = new SolidBrush(Color.BLACK);
        graphics2D.setDefaultBrush(brush);
 
        // Creates the stem by filling the Area resulting from the
        //subtraction of two Area objects created from an ellipse.
        stem.setFrame(ew, eh - 42, 40, 40);
        st1 = new Area(stem);
        stem.setFrame(ew + 3, eh - 47, 50, 50);
        st2 = new Area(stem);
        st1.subtract(st2);
        graphics2D.fill(null, st1);
 
        brush = new SolidBrush(Color.YELLOW);
        graphics2D.setDefaultBrush(brush);
 
        // Creates the pear itself by filling the Area resulting from the
        //union of two Area objects created by two different ellipses.
        circle.setFrame(ew - 25, eh, 50, 50);
        oval.setFrame(ew - 19, eh - 20, 40, 70);
        circ = new Area(circle);
        ov = new Area(oval);
        circ.add(ov);
        graphics2D.fill(null, circ);
        Pixmap pixMap = new Pixmap(100, 100, Pixmap.Format.RGBA8888);
        pixMap.drawRGB(graphics2D.getRGB(), 100, 100);
        if (texture != null) texture.dispose();
        texture = new Texture(pixMap);
 
    }
 
    public Texture getTexture() {
        return texture;
    }
 
    @Override
    public void dispose() {
        texture.dispose();
    }
 
 
}
```

and use this pear to replace the raindrop in the raindrop game.

```java
package com.mapdigit.game.tutorial.drop.actor;
 
import com.guidebee.game.scene.Actor;
import com.mapdigit.game.tutorial.drop.drawing.Pear;
 
public class RainDrop extends Actor {
    static Pear pear=new Pear();
 
    public RainDrop(){
        super("RainDrop");
        setTexture(pear.getTexture());
    }
}

```

now we use bucket to catch falling pears:-)

![Bucket Pear](http://i2.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/bucketpear.png)

Most of game will use PNG format images, we will focus in more details in raster images in the following blogs.

---

[← Back to tutorial index](../README.md)
