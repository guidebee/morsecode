# Packages

Guidebee Game Engine comprises several packages that provide useful features for most of the game architectures. Following is a diagram of major packages included in the Android Game Engine:
![Packages](http://i2.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/10/GuidebeeModules.png)

Here I used 4 different colors to denote the important level for a typical games. Most of games will use features defined in the Green Packages:

|Package	|Descriptions|
| ------------- |-------------| 
|Activity|This package defines Game Activity for Android game application.|
|Scene|The scene package define game scene classes (actor, stage ,scenery etc.)|
|Graphics|The package defines class relate to graphics like sprite, animation, SVG etc.|
|Input|Provides a unified input model and handler for all platforms. Supports keyboard, touchscreen, accelerometer and mouse where available.|
|Audio|Facilitates sound recording and playback on all platforms.|
|Files|Abstracts file access on all platforms by providing convenient methods for read/write operations regardless of the media.|

The next three packages are Cameras, Map, UI.  Cameras can be used to track actor movement , coordinate conversion between screen and world. and If you have tiled map or other maps ,there are a lot of classes will help you use maps for your games. UI provides high level UI compoments, such as buttons, menus ,Dialog, Labels ,Textbox etc.

|Package	|Descriptions|
| ------------- |-------------| 
|Camera|	This package defines two common cameras: OrthographicCamera and PerspectiveCamera.|
|Maps|	The maps package provides class for using game maps (like tile map).|
|UI| The package defines high level UI components|

Packages in dark blue are Net, Math, Utils, Net package support networking ,online games. Math and Utils package defines some Math and Utility help classes.

|Package	|Descriptions|
| ------------- |-------------| 
|Net|Provides methods to perform networking operations, such as simple HTTP get and post requests, and TCP server/client socket communication.|
|Math|	This package define math and math algorithms.|
|Utils|	This package define utility classes.|


For more advanced game design, the following four packages: Drawing package provides functions for advance vector 2D drawing ,include SVG image. Physics package supports Box2D physical engine. Entity package defines Entity System Framework (will discuss in later blogs) ,Tween for animations.

|Package	|Descriptions|
| ------------- |-------------| 
|Drawing|The Drawing package provides access to basic 2D graphics functionality.|
|Entity|This package defines entity system framework main classes.|
|Physics|The physics package provides wrapper classes for native Box2D library.|
|Tween|The tween package defines Universal Tween Engine enables the interpolation of every attribute from any object in any Java project.|

---

[← Back to tutorial index](../README.md)
