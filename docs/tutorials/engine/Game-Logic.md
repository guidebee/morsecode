# Game Logic

Although there are many ways you can choose to write games using Guidebee Game Engine, but here we will try to enforce some best practices, if you follow these rules, not only it make your game design and development much more clear ,easy to maintain and extend, these rules also mimic real world stag play scenarios ,make you code easier to understand.

Let’s first discuss what a typical games consists of, from a high level point of view, a game can be split up into two parts: assets and game logic.

Game assets include everything like images, sounds, music ,level data etc.

Game Logic is responsible for keeping track of the current game state and to only allow a defined set of state transitions. These states will change a lot over time due to the events triggered either by the player or by the game logic itself.  For example, in the simple raindrop game, when user touch the screen and move the bucket ,the game logic  will decides whether the raindrop touches the bucket and decide for the appropriate actions ( play raindrop sound etc) to be taken. All this is better known as game play.

To give you a better idea of this, take a look at the following diagram:

![Game Logic](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/10/gamelogic.png)

The very first step is to initialize the game play. i.e loading assets into memory , creating the initial state of the game world and set up the game start scene.

When everything is up and running, the game logic is ready to take over and will loop for the rest of time until the scene hides or game ends (single scene game). This kind of looping is also referred to as the game loop. For Guidebee Game Engine, the game loop normally is handled by Stage class. Stage manages Actor objects (such as bucket and raindrop in the raindrop game), it has act and draw methods, normally act methods iterates all actor ,execute actor’s act methods to update game state ,draw methods iterate all actors ,render actors based on new state ,it also render background  (Scenery class),HUD, other no-Actor UI components. Following is more detailed diagram of all core classes for a game logic:

![Core classes](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/10/gameplay.png)

GameEngine is an static class, it provides convenient methods to access hardware like graphics (internally it’s OpenGL ES ), Record, speaker, Input (like keyboard, touch input etc) ,AssetManager (to load sound, music ,images resources) and the file system:

![GameEngine](http://i1.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/10/gameengine.png)

It’s very important to consider the speed at which update will occur in the game world. default the game would just run at the maximum speed of the available hardware. In most cases this is not a desirable effect because it makes your game dependent on the processing power and the complexity of the scene to be rendered, which will vary from devices  to devices. The key to tackle this issue is to use delta times to calculate the fractional progress of the game world.

---

[← Back to tutorial index](../README.md)
