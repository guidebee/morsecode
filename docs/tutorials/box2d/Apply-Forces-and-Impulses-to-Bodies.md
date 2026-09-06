# Apply Forces and Impulses to Bodies

To move things around, you’ll need to apply forces or impulses to a body. Forces act gradually over time to change the velocity of a body while impulses can change a body’s velocity immediately. You can also move a body instantaneously by simply setting its location. This can be handy for games where you need a teleport feature.
Angular movement can also be controlled by forces and impulses, with the same gradual/immediate characteristics as their linear versions. Angular force is called torque.

Here we will create four bodies on the stage:

![force](http://i2.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/force.png "force")

* Face1 is a box shape ,we will apply upwards gradual force to it.
* Face2 is a triangle shape, will apply immediate force (impulse) upwards to it.
* Face3 is a circle shape, will teleport it to a new position.
* Face4 is a Hexagon Shape, we will apply gradual torque on it.

```java
private class ClickButton extends Button {
 
    @Override
    protected void clickEventHandler() {
        face1.getBody().applyForce(new Vector2(0, 50),
                face1.getBody().getWorldCenter(), true);
        face2.getBody().applyLinearImpulse(new Vector2(0, 50),
                face2.getBody().getWorldCenter(), true);
 
        face3.getBody().setTransform(new Vector2(
                GameEngine.toBox2D(150f),
                GameEngine.toBox2D(350f)), 0);
        face3.getBody().setAwake(true);
 
        face4.getBody().applyTorque(20,true);
 
 
    }
}

```
A Video demo is at

<a href="http://www.youtube.com/watch?feature=player_embedded&v=m8BJc_haafg
" target="_blank"><img src="http://img.youtube.com/vi/m8BJc_haafg/0.jpg" 
alt="IMAGE ALT TEXT HERE" width="240" height="180" border="10" /></a>

we applied the same magnitude (50) for both impulse and force, so why does the force seem to be weaker than the impulse? Well, remember that gravity is a force too. Try turning gravity off and the answer might become clear. The reason is that the force acts a little bit each timestep to move the body up, and then gravity acts to push it back down again, in a continual up down up down struggle. The impulse on the other hand, does all its work before gravity gets a chance to interfere. With gravity off, try turning the force on for about one second, then off. You will notice that after one second, the forced body has the same velocity as the impulsed body.

Now what about the last parameter of the apply force/impulse functions that we have ignored? So far we set this using the GetWorldCenter() of the body which will apply the force at the center of mass. As we can see, a force applied to the center of mass does not affect the rotation of the body. Imagine a CD on a friction-less flat surface, like an air-hockey table. If you put your finger in the hole in the middle of the CD and flick it across the table, it will not spin around as it moves. But if you do this with your finger anywhere else on the CD, it will spin around as it moves, and the further away from the middle your finger was, the more it will spin.

![force](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/worldcenter.png "force")

# Angular movement
Angular movement is controllable by using angular forces (torque) and angular impulses. These behave similar to their linear counterparts in that force is gradual and impulse is immediate.
As with the linear forces, given the same magnitude parameter, ApplyTorque will take one second to gain as much rotational velocity as ApplyAngularImpulse does immediately.

---

[← Back to tutorial index](../README.md)
