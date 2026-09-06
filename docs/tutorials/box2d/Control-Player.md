# Control Player

From the demo, we have seen how Box2D simulate the real world physics, let the gravity do the jobs. But want if we want to control some of the actors in the game ,like the player. Let create a Player and put him into the stage.

![controlplayer](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/controlplayer.png "controlplayer")

Note: Don’t worry too much about the green line under the ground, and red rectangle around the Player sprite. these are just Box2D debug drawings ,it can be disabled later.

We want to control the Player move around on the stage.  There are two ways to control the Player:

Full Control of the Player, the player’s position, rotation etc are all controlled by you.
Partial Control of the Player, you give the player some initial speed (both directions ) and let the gravity (physics) do the remaining.
If you want to fully control the player ,just call Actor’s setSelfControl(true) method:

```java
public class Player extends Actor implements GameControllerListener{
 
  private boolean isSelfControl=true;
  ...
  public Player(){
        super("Player");
        ...
        Rectangle boundRect=new Rectangle(2,0,SPRITE_WIDTH-4,SPRITE_HEIGHT-6);
 
        scaleBy(scale);
        initBody(BodyDef.BodyType.DynamicBody, boundRect);
 
        setSelfControl(isSelfControl);
        getBody().setFixedRotation(true);
 
        ...
   }
}
 
```
 
Since the original Player image is a bit small 24 X 32. we used scaleBy to scale the player up a bit. And there's some empty space of the original image size ,so when use call initBody, we used smaller bounding rectangle. this is one of case that Bounding Rectangle have different size with the image size.
 
And we don't our player to rotate by itself (when bounce with other objects), so we setFixRotation to false.
 
If you want to partially control the player, just give it some push or initial speed ,use false when call setSelfControl. or not use setSelfControl at all, default the body is controlled by Box2d physics.
 
So for the game controller event ,we have the following event handler for both Fully Controlled and Partially controller player.
 
```java
 
private void handleKeyPress(){
    oldX = getX();
    oldY=getY();
    switch (currentDirection) {
        case NORTHWEST:
            setTextureRegion(forwardAnimation.getKeyFrame(elapsedTime, true));
            if(isSelfControl){
                setY(getY() + 100 * graphics.getDeltaTime());
                setX(getX() - 100 * graphics.getDeltaTime());
            }else {
 
                getBody().setLinearVelocity(-speed, speed);
            }
 
            break;
        case NORTHEAST:
            setTextureRegion(forwardAnimation.getKeyFrame(elapsedTime, true));
            if(isSelfControl) {
                setY(getY() + 100 * graphics.getDeltaTime());
                setX(getX() + 100 * graphics.getDeltaTime());
            }else {
                getBody().setLinearVelocity(speed, speed);
            }
 
            break;
 
        case SOUTHWEST:
            setTextureRegion(backwardAnimation.getKeyFrame(elapsedTime, true));
            if(isSelfControl) {
                setY(getY() - 100 * graphics.getDeltaTime());
                setX(getX() - 100 * graphics.getDeltaTime());
            }else {
                getBody().setLinearVelocity(-speed, -speed);
            }
 
            break;
        case SOUTHEAST:
            setTextureRegion(backwardAnimation.getKeyFrame(elapsedTime, true));
            if(isSelfControl) {
                setY(getY() - 100 * graphics.getDeltaTime());
                setX(getX() + 100 * graphics.getDeltaTime());
            }else {
                getBody().setLinearVelocity(speed, -speed);
            }
 
            break;
 
        case WEST:
            setTextureRegion(leftAnimation.getKeyFrame(elapsedTime, true));
            if(isSelfControl) {
                setX(getX() - 200 * graphics.getDeltaTime());
            }
            else {
                getBody().setLinearVelocity(-speed, 0);
            }
            break;
        case EAST:
 
            setTextureRegion(rightAnimation.getKeyFrame(elapsedTime, true));
            if(isSelfControl) {
                setX(getX() + 200 * graphics.getDeltaTime());
            }else {
                getBody().setLinearVelocity(speed, 0);
            }
            break;
        case NORTH:
 
            setTextureRegion(forwardAnimation.getKeyFrame(elapsedTime, true));
            if(isSelfControl) {
                setY(getY() + 200 * graphics.getDeltaTime());
            }else {
                getBody().setLinearVelocity(0, speed);
            }
 
            break;
        case SOUTH:
            setTextureRegion(backwardAnimation.getKeyFrame(elapsedTime, true));
            if(isSelfControl) {
                setY(getY() - 200 * graphics.getDeltaTime());
            }else {
                getBody().setLinearVelocity(0, -speed);
            }
            break;
 
    }
    currentDirection=Direction.NONE;
    if (getX() < 0) {
        setX(0);
        if(!isSelfControl) {
            getBody().setLinearVelocity(0,0);
            resetBodyWithSprite();
        }
        currentDirection=Direction.NONE;
    }
    if (getY() < 0) {
        setY(0);
        if(!isSelfControl) {
            getBody().setLinearVelocity(0,0);
            resetBodyWithSprite();
        }
        currentDirection=Direction.NONE;
    }
    if (getX() > Configuration.SCREEN_WIDTH - SPRITE_WIDTH*scale) {
        setX(Configuration.SCREEN_WIDTH - SPRITE_WIDTH*scale);
        if(!isSelfControl) {
            getBody().setLinearVelocity(0,0);
            resetBodyWithSprite();
        }
        currentDirection=Direction.NONE;
    }
    if (getY() > Configuration.SCREEN_HEIGHT - SPRITE_HEIGHT*scale) {
        setY(Configuration.SCREEN_HEIGHT - SPRITE_HEIGHT*scale);
        if(!isSelfControl) {
            getBody().setLinearVelocity(0,0);
            resetBodyWithSprite();
        }
        currentDirection=Direction.NONE;
    }
 
 
}
```

If it’s fully self controller, we use setX,setY directly control the player’s position, the player still can interact with other bodies ,but it’s position can not affected by other object, it’s like the player has infinite mass.

<a href="http://www.youtube.com/watch?feature=player_embedded&v=QCOjDJcoipE
" target="_blank"><img src="http://img.youtube.com/vi/QCOjDJcoipE/0.jpg" 
alt="IMAGE ALT TEXT HERE" width="240" height="180" border="10" /></a>

In this case, the Player can pass through the ground, since the position of the player’s is fully controlled by you. but you can see the player can still interact with other bodies on stage. You have to use Collision Listener or code not to let player pass through the ground.

Partially controlled Player, when key pressed, we give the player some initial speed ( vertical or horizontal ) or apply some force (we will talk about this in later tutorials) and then let gravity or physics take over remaining.

<a href="http://www.youtube.com/watch?feature=player_embedded&v=cNA3Kad8YCg
" target="_blank"><img src="http://img.youtube.com/vi/cNA3Kad8YCg/0.jpg" 
alt="IMAGE ALT TEXT HERE" width="240" height="180" border="10" /></a>

When in piratically controlled mode, if you to change Player’s position, you still can call setPosition or setX, setY, but you have to call resetBodyWithSprite() to sync Box2D body position with Actor’s position.

And when you directly change Body’s position ,rotation ,you want to sync Actor’s position with Box2D body’s, use resetSpriteWithBody()

---

[← Back to tutorial index](../README.md)
