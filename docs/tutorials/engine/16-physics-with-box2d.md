# 16. Physics with Box2D

Unlike every other page in this series, this one has no in-repo call site to walk
through — **neither Flappy Bird nor Battle City uses Box2D.** Flappy Bird integrates its
own gravity by hand in `Bird.act()` ([3. The Game Loop](03-the-game-loop.md)); Battle
City is a grid game with tile-based and sprite-based collision
([11](11-collision-detection.md)). Full rigid-body simulation would be overkill for
either. This page is a straight reference to the physics API that ships in the engine,
grounded in the real classes under `com.guidebee.game.physics`, for when a game
actually needs one.

## Box2D is a C++ engine with a Java wrapper

Box2D itself is compiled from C++ (`gameengine/src/main/jni/Box2D/`) and exposed to
Java through a JNI wrapper (`gameengine/src/main/jni/Wrapper/`) as
`com.guidebee.game.physics.*` — `World`, `Body`, `Fixture`, `Joint`, and their
`...Def` configuration counterparts (`BodyDef`, `FixtureDef`, `JointDef`). This is the
same Box2D used by libGDX and countless other 2D engines; if you've used Box2D
elsewhere, the vocabulary here transfers directly.

## Core vocabulary

- **`World`** — the physics simulation itself: a set of bodies, gravity, and the
  `step(timeStep, velocityIterations, positionIterations)` method that advances the
  simulation by one tick. `GameEngine.world` is a ready-made static field for it.
- **`Body`** — a physical object in the world, created via `world.createBody(BodyDef)`.
  A `BodyDef.type` is one of `BodyType.StaticBody` (never moves — walls, terrain),
  `BodyType.KinematicBody` (moves under your direct control, unaffected by forces), or
  `BodyType.DynamicBody` (fully simulated — forces, gravity, collisions all apply).
- **`Fixture`** — attaches a `Shape` (`CircleShape`, `PolygonShape`, `EdgeShape`,
  `ChainShape`) plus material properties (density, friction, restitution) to a `Body`.
  A body needs at least one fixture to collide with anything.
- **`Joint`** — constrains two bodies relative to each other. The engine ships the full
  Box2D joint set under `com.guidebee.game.physics.joints`: `RevoluteJoint` (a hinge),
  `PrismaticJoint` (a slider), `DistanceJoint`, `WeldJoint`, `PulleyJoint`, `RopeJoint`,
  `WheelJoint`, `GearJoint`, `MotorJoint`, `FrictionJoint`, and `MouseJoint` (for
  drag-with-touch interactions).
- **`ContactListener`** — a callback interface (`beginContact`/`endContact`/`preSolve`/`postSolve`)
  for reacting to collisions as they happen, rather than polling.

## Pixel ↔ Box2D unit conversion

Box2D bodies work best in small numbers — roughly 0.1 to 10 world units per object —
which rarely matches a screen's pixel dimensions directly. `GameEngine` provides the
conversion helpers every Box2D-based game ends up needing:

```java
// GameEngine.java
public static float pixelToBox2DUnit = 32.0f;

public static float toBox2D(float pixel) {
    return pixel / pixelToBox2DUnit;
}

public static float toPixel(float box2d) {
    return box2d * pixelToBox2DUnit;
}
```

The default ratio (32 pixels per Box2D unit) matches the common "one tile = one meter"
convention; adjust `pixelToBox2DUnit` if your art uses a different tile size.

## The shape of a Box2D-based game (sketch, not shipped code)

Roughly, wiring physics into a `Stage`-based game (the style [1](01-project-setup-and-lifecycle.md)
and [3](03-the-game-loop.md) cover for Flappy Bird) looks like this — a `World` stepped
once per frame, with actors reading their position back from a `Body` each draw:

```java
// Illustrative only — not code that ships in this repo.
World world = new World(new Vector2(0, -10f), true);   // gravity pointing down

BodyDef bodyDef = new BodyDef();
bodyDef.type = BodyDef.BodyType.DynamicBody;
bodyDef.position.set(GameEngine.toBox2D(x), GameEngine.toBox2D(y));
Body body = world.createBody(bodyDef);

PolygonShape box = new PolygonShape();
box.setAsBox(GameEngine.toBox2D(width / 2), GameEngine.toBox2D(height / 2));
FixtureDef fixtureDef = new FixtureDef();
fixtureDef.shape = box;
fixtureDef.density = 1f;
body.createFixture(fixtureDef);
box.dispose();

// once per frame, before drawing:
world.step(delta, GameEngine.VelocityIterations, GameEngine.positionIterations);
actor.setPosition(GameEngine.toPixel(body.getPosition().x),
                   GameEngine.toPixel(body.getPosition().y));
```

`GameEngine.VelocityIterations`/`positionIterations` are the default Box2D solver
iteration counts, already exposed as static fields for exactly this call.

## When to reach for this instead of hand-rolled physics

Hand-rolled physics (Flappy Bird's approach) is simpler and cheaper when your game has
one or two moving bodies with simple, well-understood rules (constant gravity, a single
bounce condition). Reach for Box2D once you need things hand-rolled physics gets
tedious fast: multiple bodies that need to realistically push each other apart, joints
connecting objects together, accurate restitution/friction, or contact
events (`ContactListener`) decoupled from your own per-frame polling.

## Where to look

- `com.guidebee.game.physics.*` and `com.guidebee.game.physics.joints.*` (`gameengine/src/main/java/com/guidebee/game/physics/`).
- `GameEngine.world`, `GameEngine.toBox2D`/`toPixel`, `GameEngine.VelocityIterations`/`positionIterations`.
- `gameengine/src/main/jni/Box2D/` — the underlying C++ Box2D sources, if you need to go deeper than the Java bindings.

---

[← Back to tutorial index](../README.md) · Previous: [15. Actions and Tweening](15-actions-and-tweening.md)
