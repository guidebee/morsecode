# Android Gradle Project

In the previous tutorials we have talked the basics and some features that Guidebee Game Engine provides. Then how can you use Guidebee Game Engine in your Game? It’s easy.  Just add the following one line in the app module’s build.gradle

```
compile 'com.guidebee:game-engine:0.9.8'
```

That’s it, you just added guidebee game engine support for your game ,it’s that easy :-)

Guidebee Android engine has been uploaded to bintary/jcenter repository, by default, when you create a application with Android, it default adds jcenter as it’s library repository:

```
allprojects {
    repositories {
        jcenter()
    }
}
```

Note: Better not to use class defined in com.guidebee.game.engine. ,the classes included in this package normally is for game engine internal use, they may change any time.

You can disable auto-import for this package with Android Studio   Settings -> Editor->Auto Import ->Exclude from import and Completion

![Android Studio Settings](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/autoimport.png)

---

[← Back to tutorial index](../README.md)
