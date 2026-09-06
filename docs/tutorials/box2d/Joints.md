# Joints

Box2D has a number of ‘joints’ that can be used to connect two bodies together. These joints can be used to simulate interaction between objects to form hinges, pistons, ropes, wheels, pulleys, vehicles, chains, etc. Learning to use joints effectively helps to create a more engaging and interesting scene.

Some joints provide limits so you can control the range of motion. Some joint provide motors which can be used to drive the joint at a prescribed speed until a prescribed force/torque is exceeded. Joint motors can be used in many ways. You can use motors to control position by specifying a joint velocity that is proportional to the difference between the actual and desired position. You can also use motors to simulate joint friction: set the joint velocity to zero and provide a small, but significant maximum motor force/torque. Then the motor will attempt to keep the joint from moving until the load becomes too strong.

Let first use a simple joint example to see how we use a joint.

![Joints](http://i2.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/jointsoverview.png "Joints")

The joint we use is called “Motor Joint” A motor joint lets you control the motion of a body by specifying target position and rotation offsets.You can set the maximum motor force and torque that will be applied to reach the target position and rotation. If the body is blocked, it will stop and the contact forces will be proportional the maximum motor force and torque.
```java
MotorJointDef motorJointDef=new MotorJointDef();
motorJointDef.maxForce=100.0f;
motorJointDef.maxTorque=100.f;
 
motorJointDef.initialize(ground.getBody(),face.getBody());
 
motorJoint=(MotorJoint)world.createJoint(motorJointDef);
```

Here we use MotoJointDef to define the joints, and then pass to world’s joint factory method to create the joint .Each joint type has a definition that derives from JointDef. All joints are connected between two different bodies. One body may static. Joints between static and/or kinematic bodies are allowed, but have no effect and use some processing time.

<a href="http://www.youtube.com/watch?feature=player_embedded&v=I6ok4kQE9fM
" target="_blank"><img src="http://img.youtube.com/vi/I6ok4kQE9fM/0.jpg" 
alt="IMAGE ALT TEXT HERE" width="240" height="180" border="10" /></a>

Normally you don’t need to destroy joints directly, when connected body is destroyed, so the joint is. At time you want to destroy joint yourself, call world’s destroyJoint method.

```java
public void destroyJoint (Joint joint)
``` 

You can specify user data for any joint type and you can provide a flag to prevent the attached bodies from colliding with each other. This is actually the default behavior and you must set the collideConnected Boolean to allow collision between to connected bodies.Many joint definitions require that you provide some geometric data. Often a joint will be defined by anchor points. These are points fixed in the attached bodies. Box2D requires these points to be specified in local coordinates. This way the joint can be specified even when the current body transforms violate the joint constraint — a common occurrence when a game is saved and reloaded. Additionally, some joint definitions need to know the default relative angle between the bodies. This is necessary to constrain rotation correctly.Initializing the geometric data can be tedious, so many joints have initialization functions that use the current body transforms to remove much of the work. However, these initialization functions should usually only be used for prototyping. Production code should define the geometry directly. This will make joint behavior more robust.

ts take a quick look at the available joints, then go over their characteristics, then finally we’ll make an example using a few of the most commonly used ones. Here are the joints in Box2D:

* Revolute – a hinge or pin, where the bodies rotate about a common point
* Distance – a point on each body will be kept at a fixed distance apart
* Prismatic – the relative rotation of the two bodies is fixed, and they can slide along an axis
* Wheel – a combination of revolute and prismatic joints, useful for modelling vehicle suspension
* Weld – holds the bodies at the same orientation
* Pulley – a point on each body will be kept within a certain distance from a point in the world,where the sum of these two distances is fixed.
* Friction – reduces the relative motion between the two bodies
* Gear – controls two other joints (revolute or prismatic) so that the movement of one affects the other
* Mouse – pulls a point on one body to a location in the world
* Rope – a point on each body will be constrained to a maximum distance apart
* Motor – A motor joint is used to control the relative motion between two bodies.

# Distance Joint
One of the simplest joint is a distance joint which says that the distance between two points on two bodies must be constant. When you specify a distance joint the two bodies should already be in place.Then you specify the two anchor points in world coordinates. The first anchor point is connected to body 1, and the second anchor point is connected to body 2. These points imply the length of the distance constraint.

![Joints](http://i1.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/distancejoint.png "Joints")

Here is an example of a distance joint definition. In this case we decide to allow the bodies to collide.
```java
DistanceJointDef jointDef;
jointDef.Initialize(myBodyA, myBodyB, worldAnchorOnBodyA, worldAnchorOnBodyB);
jointDef.collideConnected = true;
```
The distance joint can also be made soft, like a spring-damper connection.Softness is achieved by tuning two constants in the definition: frequency and damping ratio. Think of the frequency as the frequency of a harmonic oscillator (like a guitar string). The frequency is specified in Hertz. Typically the frequency should be less than a half the frequency of the time step. So if you are using a 60Hz time step, the frequency of the distance joint should be less than 30Hz. The reason is related to the Nyquist frequency. The damping ratio is non-dimensional and is typically between 0 and 1, but can be larger. At 1, the damping is critical (all oscillations should vanish).
```java
jointDef.frequencyHz = 4.0f;
jointDef.dampingRatio = 0.5f;
```

# Revolute Joint
A revolute joint forces two bodies to share a common anchor point, often called a hinge point. The revolute joint has a single degree of freedom: the relative rotation of the two bodies. This is called the joint angle.

![Joints](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/revolutejoint.png "Joints")

To specify a revolute you need to provide two bodies and a single anchor point in world space. The initialization function assumes that the bodies are already in the correct position.
In this example, two bodies are connected by a revolute joint at the first body’s center of mass.

```java
RevoluteJointDef jointDef;
jointDef.Initialize(myBodyA, myBodyB, myBodyA->getWorldCenter());
```

The revolute joint angle is positive when bodyB rotates CCW about the angle point. Like all angles in Box2D, the revolute angle is measured in radians. By convention the revolute joint angle is zero when the joint is created using Initialize(), regardless of the current rotation of the two bodies.In some cases you might wish to control the joint angle. For this, the revolute joint can optionally simulate a joint limit and/or a motor.
A joint limit forces the joint angle to remain between a lower and upper bound. The limit will apply as much torque as needed to make this happen. The limit range should include zero, otherwise the joint will lurch when the simulation begins.
A joint motor allows you to specify the joint speed (the time derivative of the angle). The speed can be negative or positive. A motor can have infinite force, but this is usually not desirable. Recall the eternal question:
“What happens when an irresistible force meets an immovable object?”
I can tell you it’s not pretty. So you can provide a maximum torque for the joint motor. The joint motor will maintain the specified speed unless the required torque exceeds the specified maximum. When the maximum torque is exceeded, the joint will slow down and can even reverse.

You can use a joint motor to simulate joint friction. Just set the joint speed to zero, and set the maximum torque to some small, but significant value. The motor will try to prevent the joint from rotating, but will yield to a significant load.
Here’s a revision of the revolute joint definition above; this time the joint has a limit and a motor enabled. The motor is setup to simulate joint friction.

```java
RevoluteJointDef jointDef;
jointDef.initialize(bodyA, bodyB, myBodyA->getWorldCenter());
jointDef.lowerAngle = -0.5f * b2_pi; // -90 degrees
jointDef.upperAngle = 0.25f * b2_pi; // 45 degrees
jointDef.enableLimit = true;
jointDef.maxMotorTorque = 10.0f;
jointDef.motorSpeed = 0.0f;
jointDef.enableMotor = true;
```

You can access a revolute joint’s angle, speed, and motor torque.

```java
getJointAngle();
getJointSpeed();
getMotorTorque();
```

You also update the motor parameters each step.

```java
setMotorSpeed(float speed);
setMaxMotorTorque(float torque);
```

Joint motors have some interesting abilities. You can update the joint speed every time step so you can make the joint move back-and-forth like a sine-wave or according to whatever function you want.

```java
... Game Loop Begin ...
myJoint->setMotorSpeed(cosf(0.5f * time));
... Game Loop End ...
```

You can also use joint motors to track a desired joint angle. For example:

```java
... Game Loop Begin ...
float angleError = myJoint->getJointAngle() - angleTarget;
float gain = 0.1f;
myJoint->setMotorSpeed(-gain * angleError);
... Game Loop End ...
```

Generally your gain parameter should not be too large. Otherwise your joint may become unstable.

# Prismatic Joint
A prismatic joint allows for relative translation of two bodies along a specified axis. A prismatic joint prevents relative rotation. Therefore, a prismatic joint has a single degree of freedom.

![Joints](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/prismaticjoint.png "Joints")

The prismatic joint definition is similar to the revolute joint description; just substitute translation for angle and force for torque. Using this analogy provides an example prismatic joint definition with a joint limit and a friction motor:

```java
PrismaticJointDef jointDef;
Vector2 worldAxis(1.0f, 0.0f);
jointDef.initialize(myBodyA, myBodyB, myBodyA->getWorldCenter(), worldAxis);
jointDef.lowerTranslation = -5.0f;
jointDef.upperTranslation = 2.5f;
jointDef.enableLimit = true;
jointDef.maxMotorForce = 1.0f;
jointDef.motorSpeed = 0.0f;
jointDef.enableMotor = true;
```

The revolute joint has an implicit axis coming out of the screen. The prismatic joint needs an explicit axis parallel to the screen. This axis is fixed in the two bodies and follows their motion.Like the revolute joint, the prismatic joint translation is zero when the joint is created using Initialize().So be sure zero is between your lower and upper translation limits.
Using a prismatic joint is similar to using a revolute joint. Here are the relevant member functions:

```java
getJointTranslation();
getJointSpeed();
getMotorForce();
setMotorSpeed(float speed);
setMaxMotorForce(float force);
```

# Pulley Joint
A pulley is used to create an idealized pulley. The pulley connects two bodies to ground and to each other. As one body goes up, the other goes down. The total length of the pulley rope is conserved according to the initial configuration.
```
length1 + length2 == constant
```
You can supply a ratio that simulates a block and tackle. This causes one side of the pulley to extend faster than the other. At the same time the constraint force is smaller on one side than the other. You can use this to create mechanical leverage.
```
length1 + ratio * length2 == constant
```
For example, if the ratio is 2, then length1 will vary at twice the rate of length2. Also the force in the rope attached to body1 will have half the constraint force as the rope attached to body2.

![Joints](http://i1.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/pulleyjoint.png "Joints")

Pulleys can be troublesome when one side is fully extended. The rope on the other side will have zero length. At this point the constraint equations become singular (bad). You should configure collision shapes to prevent this.
Here is an example pulley definition:
```java
Vector2 anchor1 = myBody1->getWorldCenter();
Vector2 anchor2 = myBody2->getWorldCenter();
Vector2 groundAnchor1(p1.x, p1.y + 10.0f);
Vector2 groundAnchor2(p2.x, p2.y + 12.0f);
float ratio = 1.0f;
PulleyJointDef jointDef;
jointDef.initialize(myBody1, myBody2, groundAnchor1, 
groundAnchor2, anchor1,
anchor2, ratio);
```

Pulley joints provide the current lengths.
```java
getLengthA();
getLengthB();
```
# Gear Joint
If you want to create a sophisticated mechanical contraption you might want to use gears. In principle you can create gears in Box2D by using compound shapes to model gear teeth. This is not very efficient and might be tedious to author. You also have to be careful to line up the gears so the teeth mesh smoothly. Box2D has a simpler method of creating gears: the gear joint.

![Joints](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/gearjoint.png "Joints")

The gear joint can only connect revolute and/or prismatic joints.

Like the pulley ratio, you can specify a gear ratio. However, in this case the gear ratio can be negative.Also keep in mind that when one joint is a revolute joint (angular) and the other joint is prismatic (translation), and then the gear ratio will have units of length or one over length.
```
coordinate1 + ratio * coordinate2 == constant
```

Here is an example gear joint. The bodies myBodyA and myBodyB are any bodies from the two joints, as long as they are not the same bodies.
```java
GearJointDef jointDef;
jointDef.bodyA = myBodyA;
jointDef.bodyB = myBodyB;
jointDef.joint1 = myRevoluteJoint;
jointDef.joint2 = myPrismaticJoint;
jointDef.ratio = 2.0f * b2_pi / myLength;

```
Note that the gear joint depends on two other joints. This creates a fragile situation. What happens if those joints are deleted?

Always delete gear joints before the revolute/prismatic joints on the gears.Otherwise your code will crash in a bad way due to the orphaned joint pointers in the gear joint. You should also delete the gear joint before you delete any of the bodies involved.

# Mouse Joint
The mouse joint is used in the testbed to manipulate bodies with the mouse. It attempts to drive a point on a body towards the current position of the cursor. There is no restriction on rotation.The mouse joint definition has a target point, maximum force, frequency, and damping ratio. The target point initially coincides with the body’s anchor point. The maximum force is used to prevent violent reactions when multiple dynamic bodies interact. You can make this as large as you like. The frequency and damping ratio are used to create a spring/damper effect similar to the distance joint.
Many users have tried to adapt the mouse joint for game play. Users often want to achieve precise positioning and instantaneous response. The mouse joint doesn’t work very well in that context. You may wish to consider using kinematic bodies instead.

# Wheel Joint
The wheel joint restricts a point on bodyB to a line on bodyA. The wheel joint also provides a suspension spring.

![Joints](http://i0.wp.com/www.guidebee.com.au/wordpress/wp-content/uploads/2015/11/wheeljoint.png "Joints")


# Weld Joint
The weld joint attempts to constrain all relative motion between two bodies.It is tempting to use the weld joint to define breakable structures. However, the Box2D solver is iterative
so the joints are a bit soft. So chains of bodies connected by weld joints will flex.
Instead it is better to create breakable bodies starting with a single body with multiple fixtures. When the body breaks, you can destroy a fixture and recreate it on a new body.

# Rope Joint
The rope joint restricts the maximum distance between two points. This can be useful to prevent chains of bodies from stretching, even under high load.

# Friction Joint
The friction joint is used for top-down friction. The joint provides 2D translational friction and angular friction.

# Motor Joint
A motor joint lets you control the motion of a body by specifying target position and rotation offsets. You can set the maximum motor force and torque that will be applied to reach the target position and rotation. If the body is blocked, it will stop and the contact forces will be proportional the maximum motor force and torque.

---

[← Back to tutorial index](../README.md)
