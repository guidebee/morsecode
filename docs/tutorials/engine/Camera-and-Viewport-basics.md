# Camera and Viewport basics

In the Raindrop example, when we created the Scene (DropScene) ,we passed a StretchViewport ,with 800 width and 480 height. because for simplicity ,we want our game scene to be 800 pixel wide and 480 pixel height.  But the real device may have different screen resolution ,like the one of devices I used for development is Galaxy Note 2 ,which has 1280 X 720 screen resolution (aspect ration 16:9).  we used StrechViewport to force screen display in 800 X 480 resolution. thus 800 X 480 is not physical pixel resolution, we called virtual pixel resolution, i.e each virtual pixel may contains more physical pixels (or less than 1 physical pixels). From the game design point of view. 800 X 480 is the game scene resolution no matter which device you are using. Viewport is the way mapping your virtual scene to physical device screen.

A close concept related to viewport is Camera. Following diagram is the class hierarchy of Camera and Viewport.

![viewport](http://i1.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/cameraviewport.png)

So “What’s a camera, what’s a viewport and how are they different?”. basically a camera is responsible for being the players “eye” into the game world. It’s an analogy to the way video camera’s work in the real world. A viewport represents how what the camera sees is displayed to the viewer. Any easy way to think about this is to think about your HD cable or satellite box and your HD TV. The video signal comes in to your box ( this is the camera ), this is the picture that is going to be displayed. Then your TV devices how to display the signal that comes in. For example, the box may send you a 480i image, or a 1080p image, and it’s your TV’s responsibility to decide how it’s displayed. This is what a viewport does… takes an incoming image and adapts it to run best on the device it’s sent to it. Sometime this means stretching the image, or displaying black bars or possibly doing nothing at all.

So, simple summary description…

* Camera – eye in the scene, determines what the player can see, used by the game engine to render the scene.
* Viewport – controls how the render results from the camera are displayed to the user, be it with black bars, stretched or doing nothing at all.

Camera comes in with two flavors: OrthographicCamera and PerspectiveCamera , PerspectiveCamera is a camera with perspective projection, it’s more like human eyes, object appears large when close to you ,while smaller if it’s further away. while we only deal with 2D game, so we mainly use OrthographicCamera. The object’s size doesn’t change with distance with OrthographicCamera .
The Camera class operates as a very simple real world camera. It is possible to

The Camera class operates as a very simple real world camera. It is possible to

* move and rotate the camera around,
* zoom in and out,
* change the viewport,
* project/unproject points to and from window coordinate/ world space


Using the camera is the easy way to move around a game world without having to manually operate on the matrices. All the projection and view matrix operations are hidden in the implementation. Our raindrop is a fixed scene game, i.e the scene is same size with screen and doesn’t scroll ,in later tutorial we will use a scroll scene game to demonstrate the usage of camera ,which can follow players.

Let’s focus on viewport for now. When dealing with different screens it is often necessary to decide for a certain strategy how those different screen sizes and aspect ratios should be handled. Camera and Stage support different viewport strategies, for example when doing picking via

```java
Camera.project(vec, viewportX, viewportY, viewportWidth, viewportHeight).
```

Viewport provides a more convenient way to solve this problem.

A viewport always manages a Camera’s viewportWidth and viewportHeight. Thus a camera needs to be supplied to the constructors.

```java
private Viewport viewport;
private Camera camera;
 
public void create() {
    camera = new PerspectiveCamera();
    viewport = new FitViewport(800, 480, camera);
}
```

Whenever a resize event occurs, the viewport needs to be informed about it and updated. This will automatically recalculate the viewport parameters and update the camera:

```java
public void resize(int width, int height) {
    viewport.update(width, height);
}
```

We encourage using Stage-Actor pattern. Our Scene class internally manages a stage. Here’s Scene class definition

```java
//--------------------------------- PACKAGE ------------------------------------
package com.guidebee.game.scene;
 
//--------------------------------- IMPORTS ------------------------------------
import com.guidebee.game.ScreenAdapter;
import com.guidebee.game.camera.viewports.Viewport;
import com.guidebee.game.graphics.Batch;
 
 
 
//[------------------------------ MAIN CLASS ----------------------------------]
/**
 * Alias of ScreenAdapter to support the game play concepts.
 */
 
public class Scene extends ScreenAdapter{
 
    /**
     * scene associated stage.
     */
    protected final Stage sceneStage;
 
 
    /**
     * Default constructor.
     */
    public Scene(){
        sceneStage=new Stage();
    }
 
    /**
     * Constructor
     * @param viewPort
     */
    public Scene(Viewport viewPort){
        sceneStage=new Stage(viewPort);
    }
 
 
    /**
     * Constructor
     * @param viewport
     * @param batch
     */
    public Scene(Viewport viewport,Batch batch){
        sceneStage=new Stage(viewport,batch);
    }
 
 
    @Override
    public void render(float delta){
        sceneStage.act();
        sceneStage.draw();
    }
 
 
    @Override
    public void dispose(){
        sceneStage.dispose();
 
    }
 
    /**
     * Get scene associated stage object.
     * @return scene associated stage object.
     */
    public Stage getStage(){
        return sceneStage;
    }
 
    @Override
    public void resize(int width, int height){
        sceneStage.getViewport().update(width,height,false);
    }
 
 
}
```

the Stage’s viewport needs to be updated when a resize event happens.

Now let’s for our 800 X 480 (aspect ration 5:3) games,how it displays on a 1280 X 720 (aspect ration 16:9) using different type of viewports:

# StretchViewport
The StretchViewport supports working with a virtual screen size. That means one can assume that a screen is always of the size virtualWidth x virtualHeight. This virtual viewport will then always be stretched to fit the screen. There are no black bars, but the aspect ratio may not be the same after the scaling took place.

```java
StretchViewport(800,480)
```

![StretchViewport](http://i1.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/stretchviewport.png)


# FitViewport
A FitViewport also supports a virtual screen size. The difference to StretchViewport is that it will always maintain the aspect ratio of the virtual screen size (virtual viewport), while scaling it as much as possible to fit the screen. One disadvantage with this strategy is that there may appear black bars.

```java
FitViewport (800,480)
```

![Fitviewport](http://i1.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/fitviewport.png)

# FillViewport
A FillViewport also keeps the aspect ratio of the virtual screen size, but in contrast to FitViewport, it will always fill the whole screen which might result in parts of the viewport being cut off.

```java
FillViewport (800,480)
```

![FillViewport](http://i2.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/fillviewport.png)


# ScreenViewport
The ScreenViewport does not have a constant virtual screen size; it will always match the window size which means that no scaling happens and no black bars appear. As a disadvantage this means that the gameplay might change, because a player with a bigger screen might see more of the game, than a player with a smaller screen size

```java
ScreenViewport()
```


![ScreenViewport](http://i2.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/screenviewport.png)

# ExtendViewport
The ExtendViewport keeps the world aspect ratio without black bars by extending the world in one direction. The world is first scaled to fit within the viewport, then the shorter dimension is lengthened to fill the viewport.
A maximum set of dimensions can be supplied to ExtendViewport, in which case, black bars will be added when the aspect ratio falls out of the supported range.

```java
ExtendViewport(800,480)
```

![extendviewport](http://i1.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/extendviewport.png)

If above Viewport still doesnt fit for your game, you can implement customized viewport. Different strategies may be implemented by doing CustomViewport extends Viewport and overriding update(width, height). Another approach is use the generic ScalingViewport and supplying another Scaling which is not yet covered by any other Viewport.

---

[← Back to tutorial index](../README.md)
