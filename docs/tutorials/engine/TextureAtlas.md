# TextureAtlas

In last blog,we used a Mario sprite sheet whose original size is 282 (W) X 256 (H), we have to padding the image to have a size of 512(W) X 256(H) to meets the POTS requirement for OpenGL Textures. This constraint will make image a big larger and consume more memory when load the image into games. Also  Guidebee Game Engines internal uses OpenGL as rendering library. In OpenGL, a texture is bound, some drawing is done, another texture is bound, more drawing is done, etc. Binding the texture is relatively expensive, so it is ideal to store many smaller images on a larger image, bind the larger texture once, then draw portions of it many times.

That’s why TextureAtlas comes to rescue.  There are many tools can help you to create TextureAltas for Libgdx ,since Guidebee Android Game Engine was built use libgdx ,so we also use libgdx format, here we used GUI-Based TexturePacker tool ,it can combine multiple images to a larger image with optimal size and satisfy the POTS constraint for OpenGL.

let’s use the tool and add the images we wants , besides the images we used for the Raindrop , I also added some image resources for On-screen game controls:

![RainDrop TextureAtlas](http://i1.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/raindrop.png)

After you public the sprite sheets, it create two files (raindrop.png and raindrop.txt), the raindrop.png is the combined image, while raindrop.txt is the description for individual image, such as it’s relative location in the combined image ,name etc.

Note:  you may need to disable allow rotate settings, rotate original image may save some spaces ,but it requires much more efforts to deal with rotated images, I found it normally it doesn’t worth the efforts.

```
raindrop.png
format: RGBA8888
filter: Linear,Linear
repeat: none
Back
  rotate: false
  xy: 2, 256
  size: 238, 237
  orig: 256, 256
  offset: 9, 9
  index: 8
Button_08_Normal_Shoot
  rotate: false
  xy: 282, 2
  size: 125, 125
  orig: 128, 128
  offset: 1, 2
  index: -1
Button_08_Normal_Virgin
  rotate: false
  xy: 282, 129
  size: 125, 125
  orig: 128, 128
  offset: 1, 2
  index: -1
Button_08_Pressed_Shoot
  rotate: false
  xy: 242, 383
  size: 120, 120
  orig: 128, 128
  offset: 4, 4
  index: -1
Button_08_Pressed_Virgin
  rotate: false
  xy: 364, 383
  size: 120, 120
  orig: 128, 128
  offset: 4, 4
  index: -1
Joystick
  rotate: false
  xy: 242, 256
  size: 125, 125
  orig: 128, 128
  offset: 1, 2
  index: 8
bucket
  rotate: false
  xy: 409, 2
  size: 64, 64
  orig: 64, 64
  offset: 0, 0
  index: -1
droplet
  rotate: false
  xy: 409, 68
  size: 42, 64
  orig: 64, 64
  offset: 12, 0
  index: -1
mario2
  rotate: false
  xy: 2, 2
  size: 278, 252
  orig: 512, 256
  offset: 4, 0
  index: -1
```

normally we give the .txt file new extension as .atlas. And you dont need to pay much attention on the details in the atlas description file. all you needs is the individual name for the images.

This tool can only pack images into multiple larger image ,depends on the layout size you set. then the atlas can have multiple images. for this demo, we only needs one image.

We put the raindrop.atlas and raindrop.png into the assets directory of the project. and modified code to load the atlas instead of individual images:

in DropGamePlay’s loadAssets

```java
private void loadAssets(){
 
       assetManager.load("raindrop.atlas", TextureAtlas.class);
       ...
   }
```

and modified Actor’s constructor to load images from TextureAltas:

```java
TextureAtlas textureAtlas=assetManager.get("raindrop.atlas",TextureAtlas.class);
setTextureRegion(textureAtlas.findRegion("bucket"));
 
...
TextureAtlas textureAtlas=assetManager.get("raindrop.atlas",TextureAtlas.class);
 setTextureRegion(textureAtlas.findRegion("droplet"));
 
...
private final TextureRegion marioTextureRegion;
ureAtlas=assetManager.get("raindrop.atlas",TextureAtlas.class);
marioTextureRegion=textureAtlas.findRegion("mario2");
```

TextureAtlas reads the pack file and loads all the page images. TextureAtlas.AtlasRegions can be retrieved, which are TextureRegions that provides extra information about the packed image, such as the frame index or any whitespace that was stripped. Sprites and NinePatches can also be created. If whitespace was stripped, the created Sprite will actually be a TextureAtlas.AtlasSprite, which allows the sprite to be used (mostly) as if whitespace was never stripped.

Note that findRegion is not very fast, so the value returned should be stored rather than calling this method each frame. Also note that createSprite and createNinePatch allocate a new instance.

TextureAtlas holds on to all the page textures. Disposing the TextureAtlas will dispose all the page textures.

---

[← Back to tutorial index](../README.md)
