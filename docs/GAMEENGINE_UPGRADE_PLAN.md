# GGE (`gameengine`) Modernization Plan — Android-only, 2D-only, Box2D upgrade

Author: solution-architect pass, 2026-09-13.
Scope: `gameengine/` module only. Consumers: `app/` (3 games — **Flappy Bird**,
**Battle City**, **Mario**) and `decoder/`. Non-goals: no iOS/desktop/GWT/HTML
backends (GGE never had them and won't get them), no 3D pipeline, no engine
rewrite onto raw `com.badlogic.gdx` — see [Strategy decision](#strategy-decision)
for why.

**Test fixtures used by this plan (updated 2026-09-13):** two standalone
GuidebeeGameEngine tutorial repos exist as sibling checkouts —
`C:\workspace\Box2D` (the official GGE Box2D tutorial series, 10 physics
demo stages) and `C:\workspace\Raindrop` (the official GGE general-engine
tutorial series — scene graph, collision, camera/viewport, MIDP-style API,
tiled maps). Both import `com.guidebee.game.*` directly (not a renamed/older
API — `com.mapdigit.game.tutorial.*` in Raindrop is just the sample app's own
package, its engine imports are already `com.guidebee.game.*`), so both are
**source-compatible with the current `gameengine` module as-is**. They are
old, dormant Gradle projects (Gradle 2.4, AGP 1.3.0, `jcenter()`, a dead
`com.guidebee:game-engine:0.9.x` Bintray dependency) and each only wires up
one demo screen per manifest even though the source tree contains several —
see [5.4](#54-box2d-regression-suite-box2d-tutorial-repo) and
[5.5](#55-general-engine-regression-suite-raindrop-tutorial-repo) for how
this plan turns them into living regression suites instead of building a
regression harness from scratch.

**Integration point (revised 2026-09-13):** these are folded directly into
the existing `app/` module as two extra Home-screen menu entries — **not**
separate Gradle modules/APKs. `app/src/main/java/com/guidebee/game/tutorial/box2d/`
and `app/src/main/java/com/mapdigit/game/tutorial/` hold the tutorial source
verbatim (original package names kept, so no import changes were needed);
`HomeScreen.kt`'s tool grid gets two new tiles, **Box2D Demo** and
**Raindrop Demo**, alongside Flappy Bird/Battle City/Mario, each opening a
picker screen (`StagePickerActivity`, `LessonPickerActivity`) that lists that
demo's stages/lessons. An earlier pass of this plan proposed a separate
`testapps/box2d-tutorial` / `testapps/raindrop-tutorial` Gradle module pair;
that was reverted in favor of this single-module approach — same regression
coverage, one fewer thing to keep building/signing/installing separately.
Two pre-existing gaps in the upstream tutorial repos were fixed during the
merge (both repos were missing an asset their own code references —
`coords.png` and `fly.png` — patched with placeholder images so those two
lessons don't crash on launch; see `docs/perf-baseline-2026-09.md`).

---

## 1. Current-state audit

Findings from reading `gameengine/`, its 3 consumers, and `C:\workspace\libgdx`
(current libGDX `1.14.3`, used here purely as a **reference/upstream source to
port fixes and idioms from**, not as a dependency to pull in).

| Area | Current state | Why it matters |
|---|---|---|
| Fork age | `com.guidebee.*` packages are a ~10-year-old snapshot of pre-1.0 libGDX (`Batch`, `SpriteBatch`, `Stage`/`Actor`, `LayerManager`/`Sprite` MIDP API, `ParticleEffect`, etc.), Android-only from day one. | A decade of libGDX Android-backend perf work, GL state-cache fixes, and Box2D JNI bug fixes never made it back into this fork. |
| Build/AGP/Kotlin | `build.gradle` (root) is already on **AGP 9.4.0**, Kotlin `2.4.0`, Gradle `9.7.1`, `compileSdk/targetSdk = 37`. The **app** side is modern. | The gap is entirely inside `gameengine/`'s native + rendering layer, not the surrounding project. Good — the upgrade is scoped and won't drag in an app-wide migration. |
| NDK | `ndkVersion = "21.4.7075529"` (NDK r21, released 2020), built via `externalNativeBuild { ndkBuild { ... } }` (`Android.mk`/`Application.mk`), `APP_PLATFORM=android-21`, `APP_STL=c++_static`. | r21 predates Apple-silicon-independent LLVM improvements, predates default 16 KB page-size alignment support (the project currently hand-patches this with `-Wl,-z,max-page-size=16384` in `Application.mk` — fragile, easy to lose on a merge), predates several `-O2`/LTO codegen improvements. |
| ABIs | `APP_ABI := armeabi-v7a arm64-v8a x86 x86_64`. | `x86`/`x86_64` device share is ~0 in 2026; Play Store requires 64-bit-only for new devices anyway. Shipping 4 ABIs roughly doubles native-lib APK weight and native build time for no real benefit. |
| GL binding path | `GameEngine`'s `GL20`/`GL30` (`gameengine/src/main/java/com/guidebee/game/engine/platform/GL20.java`) call `android.opengl.GLES20`/`GLES30` directly from Java — **correct**, this is the low-overhead path. | Good news: the perf problem is *not* "JNI overhead on every GL call." |
| Dead native code | `jni/Wrapper/AndroidGL20.cpp/.h` is a full JNI↔GLES2 shim, compiled into `libgameengine.so` (`Android.mk` lists it) but **never called from any Java code** (`grep` confirms zero references). | Pure dead weight — bigger `.so`, slower native build, larger APK. Safe, zero-risk deletion. |
| EGL config / surface | `GLSurfaceView20`/`EglConfigChooser`/`GLSurfaceViewAPI18` implement `javax.microedition.khronos.egl.EGL10`-based manual config selection, with an `...API18` code path (Android 4.3, min supported by this file — but the module's actual `minSdk` is already 21). | The `API18` fork is unreachable dead branching (min is 21, not 18) — same category as the AndroidGL20 shim: safe to delete, shrinks the surface area you have to reason about when debugging EGL/context-loss issues. |
| Render mode | `Graphics.java` drives `GLSurfaceView.RENDERMODE_CONTINUOUSLY` — the standard libGDX-style render thread pumping `eglSwapBuffers` every vsync. | Not itself wrong, but there's no adaptation for high-refresh-rate panels (90/120 Hz) or thermal throttling — worth a deliberate frame-pacing pass (see [3.3](#33-frame-pacing--modern-window-behavior)) rather than assuming "vsync-locked" == "fine." |
| Box2D version | `jni/Box2D/Common/b2Settings.cpp`: `b2_version = {2, 3, 1}` — **byte-for-byte identical** to `C:\workspace\libgdx\extensions\gdx-box2d\gdx-box2d\jni\Box2D\Common\b2Settings.cpp`. | LibGDX itself never moved past Box2D 2.3.1's C++ core (Box2D v3.x is a from-scratch C rewrite with a completely different API that LibGDX has not adopted). **"Upgrade Box2D" here correctly means: re-sync the JNI wrapper layer + native build hygiene against the current `gdx-box2d`, not chase a new physics-engine major version.** |
| Box2D usage in the 3 games | `grep` across all of `app/src/main/java` for `com.guidebee.game.physics`, `b2Body`, `createBody`, `applyForce`, `setLinearVelocity` → **zero hits**. Flappy Bird hand-rolls gravity in `Bird.act()`; Battle City is grid/AABB; Mario's actor tree (checked `actors/bricks`, `actors/enemies`) shows no physics-package imports either. | The 3 games carry **no direct Box2D regression risk** — but `gameengine` itself wires Box2D into the scene graph (`GameEngine.java`, `Actor.java`, `Group.java`, `Scenery.java`, `Stage.java`, `scene/collision/{Collision,SensorListener}.java` all import `com.guidebee.game.physics`). That path has **no test coverage from the 3 shipped games**, so it needs its own dedicated smoke test (see [5.4](#54-box2d-regression-suite)) — a Box2D regression could ship invisibly otherwise. |
| Texture compression | `Wrapper/Box2D/Common/ETC1.cpp`/`ETC1Utils.cpp` — ETC1 only. | ETC1 has no alpha channel (games fake it with a separate alpha atlas region) and is obsolete next to ETC2 (guaranteed on all GLES 3.0 hardware, i.e. anything shipped after ~2015) or ASTC. Since `minSdk=21` already implies GLES 3.0 availability on the overwhelming majority of real devices, this is a real texture-memory/bandwidth win. |
| `GL30`/`IGL30` | Present (`engine/platform/GL30.java`, `engine/graphics/opengles/IGL30.java`) — so GLES 3.0 plumbing exists but isn't fully leveraged (no VAOs, no ETC2/ASTC loader, no instancing) per the graphics package scan. | Confirms the GLES3 groundwork is already there from the original fork; this is a "finish what's started" job, not a "build from zero" job. |

### Strategy decision

Two possible shapes for "upgrade the engine":

- **(A) In-place modernization (recommended, and what this plan executes):**
  keep the `com.guidebee.*` public API frozen (so `app/`'s 3 games and
  `decoder/` need **zero or near-zero code changes**), and port
  performance/correctness improvements from current libGDX (`1.14.3`) and
  current Android platform guidance into the existing fork's internals: build
  tooling, GL state-cache/Android backend behavior, Box2D JNI layer, texture
  pipeline.
- **(B) Replace `gameengine` with real libGDX:** rewrite the 3 games against
  `com.badlogic.gdx`. Rejected for this exercise — it's a full game rewrite
  (different package names throughout `Bird`, `BattleField`, every Mario
  actor), multi-week per game, and gains nothing (B) doesn't already gain
  via (A), since GGE's actual bottleneck is internal engine code, not the
  public API shape.

Everything below assumes **(A)**.

---

## 2. Guiding principles

1. **API-compatible by default.** Any change to a public `com.guidebee.game.*`
   class signature is a red flag — stop and check all 3 games + `decoder`
   before proceeding. The whole point of (A) is that `app/` doesn't need to
   change.
2. **One risk axis per PR.** Native build changes, Box2D changes, and GL
   pipeline changes land in separate PRs/commits so a regression can be
   bisected to a single subsystem, not a grab-bag.
3. **Every phase ends with all 3 games installed and manually played**, not
   just `assembleDebug` succeeding. A green build is not a green game.
4. **Keep a rollback path.** Tag the pre-upgrade state
   (`git tag pre-gameengine-upgrade`) before starting; each phase is its own
   branch merged only after its own test pass (see [Section 6](#6-testing-plan)).

---

## 3. Step-by-step upgrade process

### Phase 0 — Baseline & safety net (½–1 day)

1. `git tag pre-gameengine-upgrade` and push it — the rollback anchor.
2. Build `app-release.apk` from current `main` and keep it aside as the
   "known-good" reference build for A/B comparison.
3. Record a **baseline performance profile** for each of the 3 games using
   Android GPU Inspector or `adb shell dumpsys gfxinfo <pkg> framestats` on
   at least one mid-range and one low-end physical device (not just an
   emulator — GL driver behavior on emulators is not representative):
   - average/95th-percentile frame time,
   - dropped-frame count over a fixed 60s play session,
   - APK size and cold-start time.
   This is the number the whole upgrade is trying to move — without it you
   can't tell "upgraded" from "different."
4. Write down current device test matrix (min: one API 21-23 device or
   emulator, one API 29-31, one current API 35/36 device, one x86_64 emulator
   for CI) — see [Section 7](#7-device-test-matrix).
5. Spot-check `decoder/` for any accidental dependency on `gameengine` — it's
   listed as a sibling module in `settings.gradle`, but is a plain
   `java-library`/`kotlin.jvm` module with no Android and no `gameengine`
   dependency (confirmed by reading `decoder/build.gradle`). Excluded from
   further regression scope.
6. **Import and modernize the two tutorial repos as in-app regression
   fixtures** (new step, added after finding `C:\workspace\Box2D` and
   `C:\workspace\Raindrop`). Do this in Phase 0, not Phase 4, so the Box2D
   regression suite exists *before* any engine code changes and can itself be
   validated against the pre-upgrade engine first. **Folded directly into
   `app/`** as two extra Home-screen menu entries, alongside Flappy
   Bird/Battle City/Mario, rather than as separate Gradle modules/APKs — one
   app to build, sign and install for the whole regression pass:
   - Copied each repo's `src/main/java` package verbatim into `app/src/main/java/`
     — `com/guidebee/game/tutorial/box2d/` and `com/mapdigit/game/tutorial/` —
     with **no package renaming**, so no import changes were needed anywhere
     in the copied source. Their `res/` directories were dropped entirely
     (confirmed zero `R.*` references in either tutorial's Java source — the
     original repos' `AppCompat`-themed `styles.xml`, launcher `ic_launcher`
     mipmaps, and Raindrop's unused `layout/main.xml` were all dead weight,
     same category as the engine's own dead `AndroidGL20.cpp`/API18 findings
     in the audit table above).
   - Merged each repo's `assets/` into `app/src/main/assets/`. Six loose PNGs
     (`Back_08.png`, `Button_08_Normal_Shoot.png`, `Button_08_Normal_Virgin.png`,
     `Button_08_Pressed_Shoot.png`, `Button_08_Pressed_Virgin.png`,
     `Joystick_08.png`) collided by filename with assets already in `app/` —
     confirmed these are pre-packing source art baked into `raindrop.atlas`'s
     regions and never loaded individually at runtime (`assetManager.load`/`.get`
     never references them by that literal path), so they were skipped rather
     than namespaced into a subfolder.
   - **Fixed two pre-existing upstream gaps** found while verifying every
     asset filename referenced in the copied Java against what actually
     shipped in each repo: `coords.CoordinateGamePlay`/`CoordinateActor` load
     `"coords.png"`, and `microedition.actor.Fly` loads `"fly.png"` (needs a
     128×64 two-frame sheet) — **neither file exists anywhere in the original
     Raindrop repo.** Both lessons would have crashed on launch as shipped
     upstream. Patched with placeholders (`coords.png` is a copy of
     `droplet.png`; `fly.png` is `droplet.png` tiled twice into a 128×64
     sheet) so both lessons run instead of crashing — cosmetic-only fix, not
     a behavior change worth blocking on.
   - Registered 7 new `<activity>` entries in `app/src/main/AndroidManifest.xml`
     (fully-qualified names, since these packages sit outside the app's own
     `au.com.guidebee.morsetoolkit.activity` namespace): two picker/launcher
     screens plus the demo screens they lead to.
   - Both repos' manifests originally declared only **one** launcher
     `<activity>` even though the source tree contains several dormant
     Activities per lesson (Box2D: `Box2DGameActivity` existed, but
     `Box2DGameScene`'s constructor hardcoded a single stage, `BulletStage`;
     Raindrop: only `.drop.DropGameActivity` was wired up, while
     `basics.HelloWorldActivity`, `coords.CoordinateGameActivity`, and
     `microedition.DropGameActivity` existed as unreferenced source). Added a
     picker so every lesson is reachable from the Home screen instead of
     requiring a source edit to switch demos:
     - **`com.guidebee.game.tutorial.box2d.StagePickerActivity`** — a plain
       `ListActivity` naming the 10 concrete stages (`BasicBox2DStage`,
       `BodyTypeStage`, `BulletStage`, `CollisionStage`, `ForceAndImpulseStage`,
       `JointsOverviewStage`, `RayCastStage`, `SelfControlStage`,
       `SensorStage`, `ShapeTypeStage` — `Box2DGameStage` itself is
       `abstract`, the shared base the other 10 extend, not launchable),
       passing the chosen class name as an Intent extra to
       `Box2DGameActivity`. `Box2DGameActivity`→`Box2DGamePlay`→
       `Box2DGameScene` now thread that `stageClass` string through to a
       `Class.forName(stageClass).getDeclaredConstructor().newInstance()`
       call in `Box2DGameScene`, replacing the original hardcoded
       `new BulletStage()`.
     - **`com.mapdigit.game.tutorial.LessonPickerActivity`** — same pattern,
       listing all 4 lessons (`basics`, `coords`, `drop`, `microedition`) and
       launching each lesson's own already-distinct Activity class directly
       (no reflection needed here — each lesson is its own fixed entry
       point, not a parametrized shared scene).
   - Wired both pickers into `HomeScreen.kt`'s existing tool grid as two new
     `ToolItem`s (**Box2D Demo**, **Raindrop Demo**) next to Flappy
     Bird/Battle City/Mario, threaded through `MorseApp.kt` the same way the
     3 games already are (`onLaunchGame(StagePickerActivity::class.java)` /
     `onLaunchGame(LessonPickerActivity::class.java)`) — no changes needed to
     the generic `onLaunchGame = { startActivity(Intent(this, it)) }` launcher
     in `HomeActivity.kt`.
   - Confirmed `./gradlew :app:assembleDebug` builds clean with both demos
     merged in, and that both picker classes plus every copied/patched asset
     land in the packaged APK (`unzip -l`/`javac` output spot-checked).

**Exit criteria:** baseline APK + baseline perf numbers captured and committed
to `docs/` (e.g. `docs/perf-baseline-2026-09.md`) so later phases have
something to diff against; the Box2D Demo and Raindrop Demo menu entries
build into `app` and every lesson/stage is reachable and behaves correctly
against the pre-upgrade engine (this baseline run doubles as the golden
reference for [5.4](#54-box2d-regression-suite-box2d-tutorial-repo)).

---

### Phase 1 — Build-tooling modernization (1–2 days)

Goal: same `Android.mk`/`ndkBuild` architecture (don't fight two migrations —
build-system *and* rendering — at once), but on current, supported tooling.

1. Bump `ndkVersion` in `gameengine/build.gradle` from `21.4.7075529` to the
   current LTS (check `local.properties`/Android Studio's bundled NDK; as of
   this plan, NDK r27/r28 LTS is the safe target — pick whatever the
   installed Android Studio ships, to avoid a separate NDK download step).
2. Raise `APP_PLATFORM` from `android-21` to match the module's actual
   `minSdk = 21` explicitly (already aligned — just confirm after the NDK
   bump, since newer NDKs sometimes deprecate very old `APP_PLATFORM` values).
3. Remove `Application.mk`'s hand-rolled
   `APP_LDFLAGS := -Wl,-z,max-page-size=16384` **once the new NDK is
   confirmed to default to 16 KB alignment** (r23+ does) — delete the patch
   rather than carry a redundant/possibly-conflicting flag. Verify with:
   ```
   ndk-utils elf-info gameengine/build/.../libgameengine.so   # or
   readelf -l libgameengine.so | grep -A1 LOAD                # check p_align
   ```
4. Trim `Application.mk`'s `APP_ABI` to `armeabi-v7a arm64-v8a` for
   dev/instrumented builds; keep `x86_64` only in a debug-only product
   flavor / CI variant for emulator testing, not in the shipped `app` build.
5. Delete the dead `AndroidGL20.cpp`/`.h` and its `LOCAL_SRC_FILES` entry in
   `Android.mk` (confirmed zero Java callers in the audit above).
6. Delete the unreachable `GLSurfaceViewAPI18`/`GLSurfaceView20API18` classes
   (module `minSdk` is 21, these guard for API 18) — check `Graphics.java`
   for the branch that selects them and collapse to the single modern path.
7. Rebuild, confirm `.so` size drop and native build time drop (record both —
   cheap, visible wins to report).

**Exit criteria:** clean `./gradlew :gameengine:assembleDebug` on the new NDK,
all 4 device classes in the test matrix launch all 3 games without a native
crash (`UnsatisfiedLinkError`/SIGSEGV in Box2D JNI are the classic failure
mode after an NDK bump — test on **real arm64 hardware**, not just an x86_64
emulator, since alignment/ABI bugs frequently only show up there).

---

### Phase 2 — Android lifecycle & windowing modernization (2–3 days)

1. Audit `GameActivity`/`BaseGameActivity`/`GameActivityWrapper` against
   current libGDX's `AndroidApplication`/`AndroidGraphics` (in
   `C:\workspace\libgdx\backends\gdx-backend-android\src\com\badlogic\gdx\backends\android\`)
   for lifecycle edge cases GGE's fork predates:
   - `onPause`/`onResume`/EGL context loss handling across multi-window /
     split-screen / picture-in-picture (didn't exist when GGE forked).
   - Configuration-change handling (fold/unfold, external display).
   - **Confirmed, reproducible finding from Phase 0 on-device testing
     (2026-09-13):** rapid Activity relaunch (launch a Box2D Demo stage →
     Back → relaunch) deterministically crashes with `SIGSEGV` inside the
     Mali GPU driver via `VertexBufferObject.bind → glBufferData` — a
     use-after-free. Root cause: `GameActivityWrapper.onPause()` calls
     `graphics.destroy()` (which runs `ApplicationListener.dispose()`,
     freeing native Meshes/VBOs/Box2D world state) **before**
     `graphics.onPauseGLSurfaceView()`, and the `Graphics.pause()`/`destroy()`
     wait/notify handshake with the GL thread releases the waiting caller
     before `onDrawFrame`'s `ApplicationListener.dispose()` call has actually
     finished — so a fast-enough relaunch can start a new GL context while
     the old one's native teardown is still in flight. A single clean launch
     of any stage never crashes (confirmed on all 10 Box2D Demo stages).
     Full repro/analysis in `docs/perf-baseline-2026-09.md`'s "On-device
     validation" section — start Phase 2's lifecycle fix there instead of
     re-deriving it. Likely fix shape: reorder `onPause()` so
     `onPauseGLSurfaceView()` (which synchronously stops the GL thread) runs
     *before* `clearManagedCaches()`/`destroy()`, but confirm this doesn't
     starve `pause()`'s own listener notifications, which need the GL thread
     still alive to run.
2. **Edge-to-edge enforcement**: `targetSdk = 37` means the games are already
   subject to Android's mandatory edge-to-edge behavior (enforced since
   Android 15/API 35 for apps targeting that SDK or above). Verify
   `GameActivity` explicitly uses `WindowCompat.setDecorFitsSystemWindows`
   and consumes `WindowInsets` for the `GLSurfaceView`'s bounds, rather than
   relying on old `SYSTEM_UI_FLAG_FULLSCREEN`-style immersive flags (those
   still work but are deprecated and interact badly with gesture nav /
   cutouts on current devices — check all 3 games' full-screen game views for
   content hidden behind the status bar / gesture bar / camera cutout).
3. Predictive back gesture (Android 13+, default-on at `targetSdk 35+`):
   confirm none of the 3 games rely on `onBackPressed()` overrides that break
   under predictive back's `OnBackAnimationCallback` model — audit
   `MainWindow`/menu screens in each game for back-navigation handling.
4. Replace any remaining `javax.microedition.khronos.egl.EGL10`-based manual
   config chooser code that isn't already required by `GLSurfaceView`'s API
   contract with the simpler modern default (`setEGLContextClientVersion(2)`
   + a plain `EGLConfigChooser` that just asks for RGBA8888, no manual
   config-scoring loop) — the current `EglConfigChooser`'s scoring logic
   (`chooseConfig`) is inherited complexity from an era of much more varied
   OpenGL ES driver support; today's Android devices don't need it.

**Exit criteria:** all 3 games render edge-to-edge correctly (no clipped HUD,
no content under status/nav bar) on an API 35+ device, back gesture works
from every menu screen, no regression in orientation-change / app-switch
survival (Flappy Bird game state, Battle City level state, Mario level state
all resume correctly after backgrounding).

---

### Phase 3 — Rendering pipeline modernization (3–5 days)

This is the phase that should move the needle on the user's original
complaint ("OpenGL performance not as good as current Android platform").

#### 3.1 GL state-cache correctness

Port the accumulated GL-state-cache bug fixes from current libGDX's
`com.badlogic.gdx.graphics.glutils.HdpiUtils` / `GL20`/`Mesh`/`SpriteBatch`
equivalents (`C:\workspace\libgdx\gdx\src\com\badlogic\gdx\graphics\`) into
`gameengine/src/main/java/com/guidebee/game/graphics/{Mesh,SpriteBatch,Batch,Texture}.java`:

- Redundant state-change elimination (skip `glBindTexture`/`glUseProgram`/
  `glBindBuffer` calls when the target is already bound — a 10-year-old fork
  is very likely missing several of these that were added to libGDX over
  the following decade as driver-behavior bug reports came in).
- Confirm `SpriteBatch`/`PolygonSpriteBatch` upload path uses
  `glBufferSubData` for the per-frame-changing vertex data against a
  pre-allocated `GL_DYNAMIC_DRAW` buffer, not a full `glBufferData`
  re-allocation every flush — this is one of the single biggest sprite-batch
  perf differences between "old" and "current" mobile GL code.
- Compare flush/batch-size heuristics (max sprites per batch, texture-switch
  triggered flush) against current libGDX defaults; the mid-2010s constants
  chosen for that era's typical texture-atlas sizes and GPU vertex throughput
  are conservative today.

#### 3.2 GLES 3.0 completion

`GL30`/`IGL30` already exist but aren't fully used. Since `minSdk = 21`
already implies near-universal GLES 3.0 hardware support in 2026, opt the
default context into GLES 3.0 (`setEGLContextClientVersion(3)` with a GLES2
context as a documented fallback path only, not the default) and use it for:

- **VAOs** (`glGenVertexArrays`/`glBindVertexArray`) instead of re-specifying
  vertex attrib pointers every draw call.
- **ETC2/ASTC texture compression** for the games' atlases
  (`flappybird.atlas`, `morsecode.atlas`, Battle City/Mario tilesets),
  replacing the ETC1-only path in `Wrapper/Box2D/Common/ETC1.cpp`. This is a
  genuine bandwidth/memory win and directly addresses "GL perf" — texture
  bandwidth is frequently the actual bottleneck on integrated mobile GPUs,
  not draw-call CPU overhead.
- Instanced draws for repeated tile rendering in `TiledLayer`
  (`com.guidebee.game.microedition`) and Battle City's brick-wall rendering —
  currently very likely one `glDrawArrays`/`glDrawElements` per tile or per
  batch-flush; instancing collapses many identical brick draws into one call.

Keep the GLES2 path alive and selectable (`Configuration` flag) as the
fallback for the rare remaining GLES2-only device, so this is additive, not
a hard cutover.

#### 3.3 Frame pacing & modern window behavior

- Verify `Graphics.java`'s continuous render-mode loop respects the display's
  actual refresh rate (90/120 Hz panels) rather than assuming 60 Hz anywhere
  in delta-time math (`Bird.act()`'s hand-rolled gravity integration is
  exactly the kind of code that silently breaks — moves too fast — on a
  120 Hz panel if any fixed-60fps assumption leaked in; audit for
  `1f/60f`-style literals across the 3 games and the engine's own `Actor`/
  tween code).
- Consider (optional, lower priority) wiring
  `Window.setSustainedPerformanceMode(true)`/Android's Game Mode APIs so
  sustained gameplay doesn't get thermal-throttled into stutter on longer
  sessions — a real "why does it get janky after 5 minutes" cause on modern
  SoCs with aggressive thermal management.

**Exit criteria:** re-run the Phase-0 perf capture on the same devices;
frame-time and dropped-frame numbers must be **equal to or better than**
baseline, with no visual regressions (screenshot-diff each game's menu +
one gameplay frame against Phase-0 baseline screenshots).

---

### Phase 4 — Box2D native layer upgrade (2–4 days)

Since the Box2D **version** is already at parity with upstream (`2.3.1`), this
phase is a **JNI wrapper + native build re-sync**, not a physics-API migration.

1. Diff `gameengine/src/main/jni/Box2D/` against
   `C:\workspace\libgdx\extensions\gdx-box2d\gdx-box2d\jni\Box2D\` file-by-file
   (`diff -rq` two directories) — both are 2.3.1, so any delta is either a
   local Guidebee patch (must be preserved/re-applied) or a libGDX-side bug
   fix that never got pulled back (should be adopted).
2. Same diff for `Wrapper/Box2D/` vs `gdx-box2d`'s own Java↔C++ glue
   (`gdx-box2d/jni/com.badlogic.gdx.physics.box2d.*`) — this is where JNI
   correctness bugs (reference leaks, `GetDirectBufferAddress` misuse,
   stale local refs across `World.step()` callbacks) tend to live, and where
   a decade of upstream bug reports would have accumulated fixes.
3. Rebuild the Box2D translation unit under the modernized NDK from Phase 1
   with the same warnings-as-errors bar the rest of the native module uses;
   fix any new compiler warnings from the newer Clang/LLVM (common ones:
   narrowing conversions, `-Wreorder`, deprecated `register` keyword still
   present in old Box2D 2.3.1 headers).
4. Re-verify float determinism / solver iteration constants
   (`GameEngine.VELOCITY_ITERATIONS`/`POSITION_ITERATIONS` or equivalent
   static fields called out in `docs/GAME_ENGINE.md`) are unchanged — Box2D
   is sensitive to compiler optimization flags affecting floating-point
   codegen; a stricter `-O2`/LTO setting on the new NDK could subtly change
   simulation results. Confirm with the regression suite in
   [5.4](#54-box2d-regression-suite-box2d-tutorial-repo).

**Exit criteria:** all 10 stages in the in-app Box2D Demo (5.4) pass
bit-for-bit or visually-identical against the Phase-0 baseline; native build
has zero new warnings.

---

### Phase 5 — Dead-code cleanup & final hardening (1–2 days)

1. Remove now-confirmed-dead code identified across the audit (AndroidGL20
   shim, API18 GL surface classes) — done incrementally in earlier phases,
   this is the final sweep for anything missed (grep for `@Deprecated`,
   unused imports, `TODO`/`FIXME` markers left over from the original port).
2. Re-run static analysis / lint (`./gradlew :gameengine:lintDebug` —
   `lintOptions.abortOnError = false` is currently set; review the report
   manually rather than relying on the build to fail).
3. Update `docs/GAME_ENGINE.md` to reflect: GLES3-by-default, ETC2 texture
   pipeline, updated NDK/ABI list, and note the Box2D-version-parity finding
   so future readers don't assume "upgrade Box2D" means chasing v3.x.
4. Tag `post-gameengine-upgrade` once all exit criteria across all phases are
   green.

---

## 4. What's explicitly out of scope

- Any 3D rendering path (GGE is being kept 2D-only, per the request).
- iOS/desktop/GWT backends (GGE never had them).
- Migrating the 3 games' own gameplay code/architecture (Actions/Tween usage,
  Entity System Framework usage) — only the engine internals change.
- Adopting Box2D v3.x's rewritten C API — explicitly rejected, see the audit
  table; would break every `com.guidebee.game.physics.*` signature for zero
  benefit to the 3 games (none use it directly) and real risk to the engine's
  internal scene-graph collision integration.
- `decoder/` module — confirm-and-exclude in Phase 0.

---

## 5. Testing plan

### 5.1 Test pyramid for this upgrade

| Level | What | When |
|---|---|---|
| Native build smoke | `./gradlew :gameengine:assembleDebug` on clean checkout, all 3 target ABIs | Every phase, every commit |
| Instrumented/unit | Any existing `gameengine` unit tests (math, collision helpers) + the Box2D (5.4) and general-engine (5.5) tutorial-app regression suites | Every phase |
| Manual per-game regression checklist | Full playthrough checklist per game (5.2) | End of every phase, on the full device matrix |
| Perf capture | `dumpsys gfxinfo`/GPU Inspector frame-time capture, diffed against Phase-0 baseline | End of Phase 3, end of Phase 4, final sign-off |
| Visual regression | Screenshot diff (menu screen + one representative gameplay frame) per game | End of Phase 3 (GL pipeline changes are the highest visual-risk phase) |

### 5.2 Per-game manual regression checklist

Run this full checklist after **every phase**, not just at the end — catching
a regression one phase late is much cheaper to bisect than catching it after
all 5 phases are merged.

**Flappy Bird**
- [ ] Main menu bird bounce animation (`Actions` chain) plays smoothly, no
      stutter, no rotation snapping.
- [ ] Tap-to-flap responsiveness — no added input latency vs. baseline.
- [ ] `ChallengeLetter` HUD (`morsecode.atlas`) renders dot/dash glyphs
      correctly — this is a second, separately-loaded atlas; a texture
      pipeline change (3.2/ETC2) must be verified against **both** atlases,
      not just the primary one.
- [ ] Collision (pipe gaps) timing unchanged — gravity/velocity math must not
      have drifted from any delta-time/refresh-rate change (3.3).
- [ ] Score screen, store screen, options screen — Table/Skin layout intact
      (regression surface for any `Batch`/text-rendering change in 3.1).
- [ ] Background/pause/resume mid-flight preserves game state.

**Battle City**
- [ ] `BattleField.readBattlefieldFromLedLetter()` level generation — brick
      layout still spells the correct letters (dot-matrix + Morse brick runs)
      — a tiled-rendering/instancing change (3.2) is the highest-risk item
      for this game specifically since bricks are the tile-heavy content.
- [ ] Virtual `GameController` touchpad + fire buttons — input mapping to
      `KeyEvent` D-pad codes unaffected by windowing changes (edge-to-edge
      insets, Phase 2) — touch targets must not shift under the nav bar.
- [ ] Tank/bullet/explosion sprite animation and z-ordering via
      `LayerManager` unchanged.
- [ ] `drawExtra()` scorebar overlay draws in the correct screen position
      after any EGL/viewport changes.
- [ ] Level-to-level regeneration performance (no new GC pauses/frame hitches
      from the texture-pipeline change when atlases reload).

**Mario**
- [ ] Full actor roster spot-check: at minimum one of each category —
      `bricks/QuestionMark`, `bricks/Bouncer`, `enemies/EnemyTurtle`,
      `enemies/PiranhaPlant`, `items/Coin` — animate and collide correctly.
      Mario is the largest actor tree of the 3 games and the best stress
      test for sprite-batch/atlas changes (3.1/3.2) at volume.
- [ ] Scroll performance across a full level — this is the game most likely
      to expose any sprite-batch flush-heuristic regression (3.1) since it
      has the most on-screen moving actors simultaneously.
- [ ] Enemy/hazard collision detection (`scene/collision/Collision`,
      `SensorListener`) — **this is the one path in all 3 games that
      actually exercises the engine's Box2D-backed collision integration**;
      confirm which of Mario's collision checks route through
      `com.guidebee.game.scene.collision` vs. hand-rolled AABB before/after
      Phase 4, since this is the only real end-to-end Box2D signal the 3
      games provide.
- [ ] Level transition, boss encounter, life/death state machine unaffected.

### 5.3 Cross-cutting checks (all 3 games, every phase)

- [ ] Cold start time (APK launch → first playable frame) not regressed.
- [ ] APK size delta tracked and reported (expect a **decrease** after
      Phase 1's dead-code/ABI trimming).
- [ ] No new `UnsatisfiedLinkError`, no new native crash in `logcat`
      (`adb logcat | grep -i "libgameengine\|SIGSEGV\|SIGABRT"`) across a full
      play session per game.
- [ ] Backgrounding/foregrounding, screen rotation (if supported), and
      recent-apps thumbnail rendering all correct.
- [ ] Edge-to-edge / gesture-nav / cutout handling correct on an API 35+
      device specifically (Phase 2's target).

### 5.4 Box2D regression suite (Box2D tutorial repo)

Because none of the 3 shipped games exercise `com.guidebee.game.physics`
directly, this plan **no longer proposes building a from-scratch test
harness** — `C:\workspace\Box2D` already is one, and a much better one than
anything worth hand-rolling: it's the official GGE Box2D tutorial series, and
its 10 concrete stage classes (`gameengine`-relative names, all under
`com.guidebee.game.tutorial.box2d.stage`) map directly onto the physics
subsystems that matter for this upgrade:

| Stage | Exercises |
|---|---|
| `BasicBox2DStage` | World creation, basic body/fixture setup |
| `BodyTypeStage` | Static / kinematic / dynamic body type behavior |
| `ShapeTypeStage` | Circle, polygon, edge, chain shapes |
| `ForceAndImpulseStage` | `applyForce`/`applyLinearImpulse`-style APIs |
| `CollisionStage` | Contact detection + collision filtering (`Filter`) |
| `SensorStage` | Sensor fixtures / overlap-without-collision |
| `RayCastStage` | Raycast queries against the world |
| `JointsOverviewStage` | `MotorJoint` (confirmed present, see below) — and, per its actor set, the joint types the wiki's "Joints" lesson covers |
| `SelfControlStage` | Player-controlled body (`Player`/`TankWithRadar`-style actors) — direct velocity/position control interacting with the solver |
| `BulletStage` | Continuous collision detection (CCD) / fast-moving-body tunneling prevention — the current HEAD's default stage in `Box2DGameScene` |
| `Box2DGameStage` | Base class the others extend — shared world-stepping/rendering plumbing |

This is broader and more realistic coverage (real `Actor`/`Stage`
integration, real joints, real fixture filters) than a synthetic harness
would have been, and it already exists — the work is packaging it as a
runnable regression suite, not authoring test scenarios from scratch:

1. Complete the Phase 0 import (the in-app Box2D Demo menu entry) and its stage
   picker so all 10 concrete stages are individually launchable.
2. At the **Phase 0 baseline**, for each of the 10 stages, capture a short
   recorded reference: either (a) a screen-recorded video of ~10s of
   interaction per stage (cheapest, good for the visually-obvious failures —
   a body falling through the ground, a joint not constraining, sensors not
   firing), and/or (b) instrument each `*Stage` to log body
   position/velocity/angle every N steps to `logcat`, captured as a golden
   text file per stage. (b) is strictly more useful for Phase 4 specifically
   since it gives a numeric diff, not just an eyeball check — prefer adding
   it if time allows, but (a) alone is enough to unblock the plan.
3. After Phase 4 (Box2D native/JNI re-sync), re-run all 10 stages on the
   same device(s) and diff against the Phase 0 golden reference: same visual
   behavior, and (if (b) was done) body state logs matching within a small
   floating-point epsilon.
4. Specifically watch `JointsOverviewStage`/`SelfControlStage` for JNI-glue
   correctness issues (dangling native pointers, stale local refs across
   `World.step()` callbacks, wrong struct layout after a recompile) — these
   fail by silently corrupting simulation state rather than crashing, which
   is exactly why a numeric golden-value diff (3b) matters more here than for
   the visually-obvious stages like `BulletStage`.

**Exit criteria for Phase 4 specifically:** all 10 Box2D tutorial stages
behave identically (visually, and numerically where instrumented) to the
Phase 0 baseline; only then is the Box2D native upgrade considered safe to
merge — independent of the 3 shipped games showing no visible symptom (they
wouldn't, since none of them use `com.guidebee.game.physics` — the tutorial
app is the only end-to-end signal for this subsystem).

### 5.5 General engine regression suite (Raindrop tutorial repo)

`C:\workspace\Raindrop` is not Box2D-specific (its "drop" mini-game uses the
lightweight `scene/collision` AABB path, not `physics`), but it's valuable
regression coverage for engine surface area the 3 shipped games exercise
*less* than the games themselves suggest, or in different combinations:

| Lesson (source package) | Exercises | Relevant to |
|---|---|---|
| `basics` (`HelloWorldActivity`/`Screen`/`Actor`) | Minimal `Stage`/`Actor`/`Screen` bring-up | Sanity check that the core lifecycle chain (Phase 2) still works stand-alone |
| `coords` (`CoordinateGameActivity`/`Scene`) | Camera & viewport basics | Phase 3's viewport/EGL-surface changes |
| `drop` (`Mario`, `RainDrop`, `CollisionDirector`, `Score` HUD) | `scene.collision.CollisionListener`, `Touchpad`/`GameControllerListener` input, HUD, `TextureAtlas`, **a `forest.tmx` tiled map asset** (`assets/tiledmap/`) | The one asset in either fixture that exercises `com.guidebee.game.maps.tiled` at all — neither Flappy Bird, Battle City, nor Mario ship a TMX map, so this is the only tiled-map regression coverage available; keep it in the suite specifically for that reason |
| `microedition` (`DropGameActivity` variant, `Bucket`/`Fly`/`RainDrop` actors) | `LayerManager`/`Sprite`-style MIDP API, parallel to Battle City's approach but a distinct actor set | Any change to `com.guidebee.game.microedition` (Phase 3.2's tile-instancing work touches this package directly) |

Treat this as a lighter-weight companion suite: run its full lesson list
(after the Phase 0 manifest fix makes all four reachable) at the end of every
phase alongside the 3 games' checklists (5.2), same pass/fail bar, but no
dedicated golden-value capture is needed — visual/functional correctness is
sufficient since nothing here is float-precision-sensitive the way Box2D is.

---

## 6. Device test matrix

| Class | Example | Why |
|---|---|---|
| Low `minSdk` real device | Any API 21-24 device, or an AVD if no hardware available | Confirms the trimmed-ABI, dropped-legacy-branch changes (Phase 1/2) don't break the floor of the supported range. |
| Mid-range current | A common API 30-33 arm64 device | Represents the actual bulk of the current install base. |
| Current-gen high refresh-rate | An API 35/36 device with a 90/120 Hz panel | Specifically validates Phase 2 (edge-to-edge, predictive back) and Phase 3.3 (frame pacing at non-60 Hz). |
| x86_64 emulator | Standard Android Studio AVD | CI convenience only — **not** a substitute for real-hardware testing of the native/Box2D phases (Phase 1, Phase 4); emulator GL drivers and alignment behavior are not representative. |

---

## 7. Effort estimate & sequencing

| Phase | Effort | Can start after |
|---|---|---|
| 0 — Baseline (incl. importing/modernizing the 2 tutorial apps) | 1.5–2.5 days | — |
| 1 — Build tooling | 1–2 days | Phase 0 |
| 2 — Lifecycle/windowing | 2–3 days | Phase 1 (needs the new NDK/build to test against) |
| 3 — Rendering pipeline | 3–5 days | Phase 1 (independent of Phase 2, could run in parallel if two people are available) |
| 4 — Box2D native upgrade | 2–4 days | Phase 1; independent of 2/3, can run in parallel |
| 5 — Cleanup/hardening | 1–2 days | All of 1–4 |

**Total: ~11–18 working days** for one engineer working sequentially;
Phases 2, 3, and 4 are mutually independent once Phase 1 lands, so they can
be parallelized across 2-3 engineers to compress the calendar time to roughly
Phase 1 + max(Phase 2, Phase 3, Phase 4) + Phase 5 ≈ **7–10 working days**.

---

## 8. Acceptance criteria (overall)

- [ ] All 3 games (Flappy Bird, Battle City, Mario) pass their full manual
      regression checklist (5.2) on the full device matrix (Section 6).
- [ ] All 10 stages in the in-app Box2D Demo (5.4) and all 4 lessons in
      the in-app Raindrop Demo (5.5) pass their regression checks on the
      full device matrix.
- [ ] Perf capture at final sign-off is **equal to or better than** the
      Phase-0 baseline on every device class, on all 3 games.
- [ ] APK size is smaller than the pre-upgrade baseline (dead-code + ABI
      trimming should guarantee this).
- [ ] Zero new `UnsatisfiedLinkError`/native crashes across a full play
      session per game, and per tutorial-app stage/lesson, per device class.
- [ ] `docs/GAME_ENGINE.md` updated to describe the new baseline (GLES3
      default, ETC2 textures, current NDK, Box2D-version-parity note) so the
      next engineer doesn't have to re-derive this audit.
- [ ] The Box2D Demo and Raindrop Demo menu entries are kept in `app/`
      post-upgrade as a standing regression suite for any future
      `gameengine` change, not deleted once this upgrade ships.
