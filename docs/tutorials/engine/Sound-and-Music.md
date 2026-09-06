# Sound and Music

Frankly speaking use audio in game with guidebee game engine is fairly easy. The two main audio types are Music and Sound. A music  instance represents a streamed audio file. The interface supports pausing, resuming and so on.When you are done with using the Music instance you have to dispose it via the dispose() method.A Sound is a short audio clip that can be played numerous times in parallel. It’s completely loaded into memory so only load small audio files. Call the dispose() method when you’re done using the Sound.Sound effects can be stored in various formats. includes supports MP3, OGG and WAV files. On Android, a Sound instance can not be over 1mb in size. If you have a bigger file, use Music.
Following is the diagram of audio related classes provided by the game engine.

![Sound and Music](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/soundandmusic.png)

You can directly use GameEngine.audio.newSound and newMusic to create sound or Music instances. Or simple use AssetManager as we used in the raindrop demo:

```java
...
assetManager.load("drop.wav",Sound.class);
assetManager.load("rain.mp3",Music.class);
 
...
Music rainMusic = assetManager.get("rain.mp3", Music.class);
...
rainMusic.setLooping(true);
 
...
Sound dropSound=assetManager.get("drop.wav",Sound.class);
...
dropSound.play();
```

The other audio type is recorder ,you can reference related document for more information.

---

[← Back to tutorial index](../README.md)
