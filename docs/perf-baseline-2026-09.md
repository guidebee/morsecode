# Phase 0 baseline — 2026-09-13

Baseline commit: `47d162f` (tag: `pre-gameengine-upgrade`, branch `upgrade-gameengine`).
No `gameengine` source changes at this point — doc-only.

## Build environment

- Gradle 9.7.1, AGP 9.4.0, Kotlin 2.4.0
- `ndkVersion = 21.4.7075529` (pre-upgrade)
- `compileSdk`/`targetSdk` = 37, `minSdk` = 21
- ABIs built: `armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64`

## APK size (debug build, `:app:assembleDebug`)

| Artifact | Size |
|---|---|
| `app/build/outputs/apk/debug/app-debug.apk` — before merging in the Box2D/Raindrop tutorial demos | 57,240,209 bytes (~54.6 MB) |
| `app/build/outputs/apk/debug/app-debug.apk` — **current baseline, tutorial demos merged in (see below)** | **59,130,264 bytes** (~56.4 MB) |

The +1.9 MB is the two tutorial demos' own assets (`box2d.atlas`/`.png`,
`raindrop.atlas`/`.png`/`.svg`, `tiledmap/forest.tmx`, etc.) merged into
`app/src/main/assets/`. Everything from Phase 1 onward diffs against the
**59,130,264-byte** figure, since the tutorial demos are now permanent,
in-app regression fixtures, not an optional add-on.

## Native library size (`libgameengine.so`, stripped, as shipped in the APK)

| ABI | Size |
|---|---|
| `arm64-v8a` | 731,336 bytes |
| `armeabi-v7a` | 624,356 bytes |
| `x86` | 804,624 bytes |
| `x86_64` | 854,488 bytes |

Expected to shrink after Phase 1 (dead `AndroidGL20.cpp` shim removed, `x86`/`x86_64`
dropped from the shipped `app` build).

## Native build time (cold, all 4 ABIs, `:gameengine:clean` + `:gameengine:assembleDebug`)

**3 min 8 s** (`real 3m9.417s`).

## Native compiler warnings (baseline count)

**110** `warning:` lines across the clean build log (≈27-28 unique warning
sites × 4 ABIs). Dominant sources, for reference when re-diffing after
Phase 1/4's NDK bump and Box2D re-sync:

- `Wrapper/Box2D/Common/JPGD.cpp` — multiple `-Wshift-negative-value` (undefined-behavior shifts of negative signed values)
- `Wrapper/Box2D/Common/Stb_Image.cpp` — `-Wself-assign`
- `Wrapper/Box2D/Common/ETC1Utils.cpp` — unused variables
- `Wrapper/Box2D/Dynamics/Joints/RopeJoint.cpp` — `-Wreturn-type` (control reaches end of non-void function)

Phase 4 exit criteria is "zero *new* warnings" relative to this count, not
zero warnings outright — these pre-existing ones are tracked, not blocking.

## Java/Kotlin compiler warnings

8 warnings (`deprecation`, `unchecked`) on `:gameengine:assembleDebug` — not
itemized in this pass; re-check with `-Xlint:deprecation,unchecked` if they
need to be tracked individually later.

## On-device performance capture — BLOCKED

`adb devices` returned no connected devices/emulators in this environment.
Per the plan's Phase 0 step 3, frame-time/dropped-frame/cold-start capture
requires **physical hardware** (not just an emulator, per the plan's own
guidance in the device test matrix) across at least one low/mid/high-end
device. **Not captured in this pass** — connect a device and re-run before
starting Phase 3 (rendering pipeline), since that phase's exit criteria
depends on comparing against this number.

Suggested capture command once a device is available:
```
adb shell dumpsys gfxinfo <applicationId> framestats
```
for each of the 3 games (Flappy Bird, Battle City, Mario) after a ~60s play
session, on each device class in the [device test matrix](GAMEENGINE_UPGRADE_PLAN.md#6-device-test-matrix).

## Regression-suite import — merged into `app/` as two extra menu entries

Done in this pass, **revised from an earlier attempt** that added the two
tutorial repos as separate Gradle modules (`testapps/box2d-tutorial`,
`testapps/raindrop-tutorial`); that approach was reverted in favor of folding
both into the existing `app/` module directly, as two more Home-screen tool
tiles alongside Flappy Bird/Battle City/Mario — one app to build, sign and
install, not three.

| Demo | Source repo | Entry point | Notes |
|---|---|---|---|
| **Box2D Demo** | `C:\workspace\Box2D` | `HomeScreen` tile → `com.guidebee.game.tutorial.box2d.StagePickerActivity` | Lists all 10 concrete stages, launches `Box2DGameActivity` with the chosen stage class name as an Intent extra; `Box2DGameScene` now instantiates it via `Class.forName(...).newInstance()` instead of the original hardcoded `new BulletStage()`. Copied with **zero package renaming**. Dropped the original repo's unused `AppCompat`-dependent `styles.xml`/mipmap launcher icons/`res/` entirely (confirmed zero `R.*` references in its Java source). |
| **Raindrop Demo** | `C:\workspace\Raindrop` | `HomeScreen` tile → `com.mapdigit.game.tutorial.LessonPickerActivity` | Lists all 4 lessons (`basics`, `coords`, `drop`, `microedition`) — only `drop` was reachable in the original repo's manifest, the other 3 existed as dormant source. Copied with zero package renaming; `res/` dropped for the same reason as Box2D. |

**Two upstream asset gaps found and patched during the merge** (both in the
Raindrop repo, discovered by cross-checking every asset filename string
referenced in the copied Java against what the repo actually shipped):
- `coords.CoordinateGamePlay`/`CoordinateActor` load `"coords.png"` — file
  does not exist anywhere in `C:\workspace\Raindrop`. Patched by copying
  `droplet.png` → `coords.png` (cosmetic-only; the Coordinates lesson is
  about camera/viewport math, not this texture's content).
- `microedition.actor.Fly` loads `"fly.png"` via
  `new Sprite(texture, 128, 64)` (128×64 **per frame**) and then calls
  `setFrameSequence(new int[]{0,1,2,...})`, which requires at least 3 frames
  (`Sprite.setFrameSequence` throws `ArrayIndexOutOfBoundsException` if any
  sequence index exceeds `rows*cols - 1`) — the file does not exist upstream
  at all. **First patch attempt was wrong**: a single 128×64 frame (1 frame
  total) still crashed with `ArrayIndexOutOfBoundsException` at
  `Sprite.setFrameSequence` on-device (caught by on-device testing, not by
  the build — this is exactly the "build success ≠ runtime correctness" gap
  the plan calls out). Corrected to a proper 384×64 sheet — 3 frames of
  128×64 each (`droplet.png` tiled into each frame) — matching what
  `setFrameSequence`'s index range actually requires.

Both lessons would have crashed on launch as shipped upstream — this was a
pre-existing gap in the tutorial repos, not something introduced by the
import.

**Six duplicate asset filenames** (`Back_08.png`, `Button_08_Normal_Shoot.png`,
`Button_08_Normal_Virgin.png`, `Button_08_Pressed_Shoot.png`,
`Button_08_Pressed_Virgin.png`, `Joystick_08.png`) existed in both
`app/src/main/assets/` (already used by Battle City's/Mario's touchpad skin)
and Raindrop's assets, with **different byte content** (different skin
variants of the same stock GGE joystick art). Confirmed via `grep` that
neither is ever loaded by that literal filename at runtime — both are
pre-packing source art baked into each demo's own `.atlas` file's regions —
so Raindrop's copies were simply skipped rather than namespaced.

Full project (`app`, `gameengine`, `decoder`) builds green with both demos
merged in; both picker classes and every copied/patched asset confirmed
present in the packaged debug APK.

## On-device validation (2026-09-13, device: Solana "Seeker", MediaTek mt6878, arm64, API 36)

A device became available mid-Phase-0. Results:

**All 10 Box2D Demo stages confirmed working** (`BasicBox2DStage`, `BodyTypeStage`,
`ShapeTypeStage`, `ForceAndImpulseStage`, `CollisionStage`, `SensorStage`,
`RayCastStage`, `JointsOverviewStage`, `SelfControlStage`, `BulletStage`) — each
launched individually via a **clean process start** (`am force-stop` then
`am start` with a `stage_class` extra; required temporarily flipping
`Box2DGameActivity`'s manifest `exported` flag to `true` for scripted `adb`
access, reverted immediately after), screenshotted, and confirmed rendering
correct tutorial content with zero crashes in the crash log buffer across all
10. This is the Phase 0 golden-reference baseline this plan's §5.4 calls for
(screenshots only for now — logged body-state values deferred, see below).

**Found and root-caused a real, pre-existing engine bug** — a use-after-free
race in `gameengine`'s Activity-pause/GLSurfaceView-teardown handshake, **not**
something introduced by this session's changes (confirmed: zero diff in any
of `Graphics.java`, `GameActivityWrapper.java`, `Mesh.java`,
`VertexBufferObject.java`, `ImmediateModeRenderer20.java`, `ShapeRenderer.java`,
`Box2DDebugRenderer.java`):

- **Symptom:** `SIGSEGV` deep inside the Mali GPU driver
  (`/vendor/lib64/egl/mt6878/libGLES_mali.so`), reached via
  `Java_com_guidebee_game_engine_platform_GL20_glBufferData` ←
  `VertexBufferObject.bind` ← `Mesh.bind`/`render` ←
  `ImmediateModeRenderer20.flush`/`end` ← `ShapeRenderer.end` ←
  `Box2DDebugRenderer.renderBodies`/`render` ← a Box2D stage's `draw()` ←
  `Scene.render` ← `GamePlay.render` ← `Graphics.onDrawFrame` (the
  `GLSurfaceView` render thread).
- **Trigger:** rapid navigation — launching a Box2D stage, pressing Back,
  and relaunching (same or different stage) quickly. **Deterministically
  reproduced** via a scripted launch→back×5 loop; a single clean launch of
  any stage (including `RayCastStage`, the one the user's original crash
  report named) never crashed, confirmed via 10/10 clean isolated launches
  plus a sustained multi-second run showing steady 120 fps.
- **Root cause (read, not yet fixed):**
  `GameActivityWrapper.onPause()` (`gameengine/src/main/java/com/guidebee/game/activity/GameActivityWrapper.java`,
  around line 475) calls `graphics.clearManagedCaches()`/`graphics.destroy()`
  — which drive `ApplicationListener.dispose()`, tearing down native
  Meshes/VBOs/Box2D world objects — **before** calling
  `graphics.onPauseGLSurfaceView()` (which is what actually invokes
  `GLSurfaceView.onPause()`). `Graphics.pause()`/`destroy()` do have their
  own wait/notify handshake with the GL thread (`Graphics.java`'s `synch`
  monitor, `pause`/`destroy` flags checked inside `onDrawFrame`), but that
  handshake's `notifyAll()` fires — and the waiting calling thread can
  resume — **before** `onDrawFrame` finishes invoking
  `ApplicationListener.dispose()` on the GL thread, since the flag-clear/notify
  and the actual listener callback are on opposite sides of releasing the
  `synch` lock. Under fast back-then-relaunch, this leaves a window where a
  new Activity/GL context can start touching state while the old one's
  native-resource teardown is still in flight — a classic use-after-free
  shape, consistent with the crash always bottoming out in `glBufferData`
  reading/uploading a buffer that's mid-free.
- **Disposition:** this is exactly the class of issue
  `docs/GAMEENGINE_UPGRADE_PLAN.md` Phase 2 ("Android lifecycle & windowing
  modernization") already flagged as a risk area (EGL context loss across
  fast Activity transitions) — **decision: defer the actual fix to Phase 2/3
  rather than patch it now**, per explicit user direction, to keep Phase 0
  scoped to test-fixture setup. Recorded here with full repro steps and the
  root-cause read so Phase 2 doesn't have to re-derive it.
- **Testing workaround for now:** avoid rapid back-then-relaunch when manually
  clicking through Box2D Demo stages; a clean launch of any single stage is
  safe and matches how the golden-reference validation above was performed.

**Not yet done:**
- [ ] Click through all 4 Raindrop Demo lessons on-device (only the Box2D
      Demo's 10 stages were validated in this pass) — particularly the two
      patched-asset lessons (Coordinates, Microedition), since those are the
      ones least exercised upstream and most likely to reveal another gap
      like the `fly.png` one.
- [ ] Capture logged Box2D body-state values (not just screenshots) per
      docs/GAMEENGINE_UPGRADE_PLAN.md §5.4, if a numeric golden reference is
      wanted before Phase 4 rather than only visual comparison.
- [ ] On-device perf capture for the 3 shipped games (device is now
      available — this is no longer hardware-blocked, just not yet done).
- [ ] `app-release.apk` reference build — no `signingConfig` exists anywhere
      in the repo (`grep -r signingConfig` returns nothing), so `assembleRelease`
      produces an **unsigned** APK; fine for size comparison, not installable.
      Debug build size above is used as the primary baseline instead for
      apples-to-apples comparison across phases.
