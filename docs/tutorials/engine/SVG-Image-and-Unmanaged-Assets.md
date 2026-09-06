# SVG Image and Unmanaged Assets

Guidebee Android Game Engine also supports SVG Images ,let’s use a SVG file ([RainDrop.svg](https://github.com/GuidebeeGameEngine/AndroidGameEngine/blob/development/assets/raindrop.svg))

![RainDrop](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/randropsvg.png)

Vector images, which are made of thin lines and curves known as paths, are rooted in mathematical theory, a vector image will always appear smooth no matter how large you make it or how close you zoom in,another advantage to using vector images is file-size efficiency.

Let’s the raindrop.svg in the project’s assets directory.

and use assetmanager to load the SVG resources:

```java
assetManager.load("raindrop.svg", SVGImage.class);
```

Then we modify our Raindrop to use SVG image and because of performance and memory useage of the pixmap and Texture, we share the Texture resources for all RainDrop, there’s no need to create an individual Texture for each Raindrop actor. SVG Image has to be converted to Texture before it can be used by the OpenGL library.

```java
private static Texture rainDropTexture;
 
    static{
        if(rainDropTexture==null){
            SVGImage svgImage = GameEngine.assetManager.get("raindrop.svg", SVGImage.class);
            Pixmap pixmap=svgImage.getPixmap(0.25f);//scale to 0.25 of original size
            rainDropTexture=new Texture(pixmap);
            pixmap.dispose();
        }
    }
 
    public RainDrop(){
        super("RainDrop");
        if(rainDropTexture!=null) {
            setTexture(rainDropTexture);
        }
 
    }
...
 
}

```

SVGImage first need to converted to Pixmap, here you can set the scale of the original size ,here we used 0.25 a quarter of original size. The create a texture from the pixmap.

[![IMAGE ALT TEXT HERE](http://img.youtube.com/vi/4ynkQIAChZw/0.jpg)](http://www.youtube.com/watch?v=4ynkQIAChZw)

Where use SVG image or dynamically created image (like the Pear created with drawing library) ,there’s one problem, if you pause the application(like press Home key or phone call come in) ,later you resume the application, these dynamically created texture will be lost.

![Unmanaged texture](http://i2.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/unmanagedassets.png)

On Android your OpenGL context is lost each time the Activity is going to the background, e.g. on an incoming call, pressing the home button, starting a new activity on top of the current activity, letting the screen sleep and so on and so forth.When the OpenGL activity gets resumed the context is recreated.An OpenGL context is responsible for managing resources such as textures, meshes, shaders or frame buffer objects that reside in video memory.The OpenGL driver is responsible for allocating memory for each resource in video memory and keeps track of those allocations in the OpenGL context an application has acquired. If the context is destroyed the driver will deallocate the video memory for the resources. So each time our Activity is paused all our textures, meshes, shaders and frame buffer objects are lost. That’s why you sell black block for those dynamically created images.
While the other assets are static and used AssetManager to manage them, they are called “Managed assets”, AssetManager will reload them when application resume

while SVG image and other dynamically created images are called “Unmanaged assets”, to avoid black block on screen ,you have to manage them yourself.

So we implemented the following two methods for RainDrop

```java
public static void reloadTexture(){
        if(rainDropTexture==null){
            SVGImage svgImage = GameEngine.assetManager.get("raindrop.svg", SVGImage.class);
            Pixmap pixmap=svgImage.getPixmap(0.25f);
            rainDropTexture=new Texture(pixmap);
            pixmap.dispose();
        }
    }
 
    public static void unloadTexture(){
        if(rainDropTexture!=null){
            rainDropTexture.dispose();
            rainDropTexture=null;
        }
    }
```

And in the DrawScene’s show (resume) and pause(pause) methods, we reload and unload (manage) Texture for the raindrop

```java
@Override
    public void show() {
        ...
        RainDrop.reloadTexture();
    }
 
 @Override
    public void pause() {
        ...
        RainDrop.unloadTexture();
    }
```

Now even you suspend the game and put the game in background and then bring the application to the foreground. The Raindrop texture is restored.

---

[← Back to tutorial index](../README.md)
