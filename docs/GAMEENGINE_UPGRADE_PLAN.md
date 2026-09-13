# GGE (`gameengine`) Modernization Plan — Android-only, 2D-only, Box2D upgrade

Author: solution-architect pass, 2026-09-13.
Scope: `gameengine/` module only. Consumers: `app/` (3 games — **Flappy Bird**,
**Battle City**, **Mario**) and `decoder/`. Non-goals: no iOS/desktop/GWT/HTML
backends (GGE never had them and won't get them), no 3D pipeline, no engine
rewrite onto raw `com.badlogic.gdx` — see [Strategy decision](#strategy-decision)
for why.

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
5. Spot-check `decoder/` for any accidental dependency on `gameengine` (it's
   listed as a sibling module in `settings.gradle`); if none, exclude it from
   further regression scope.

**Exit criteria:** baseline APK + baseline perf numbers captured and committed
to `docs/` (e.g. `docs/perf-baseline-2026-09.md`) so later phases have
something to diff against.

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
   [5.4](#54-box2d-regression-suite).

**Exit criteria:** Box2D regression suite (below) passes bit-for-bit or
visually-identical against the Phase-0 baseline; native build has zero new
warnings.

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
| Instrumented/unit | Any existing `gameengine` unit tests (math, collision helpers) + new Box2D regression harness (5.4) | Every phase |
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

### 5.4 Box2D regression suite

Because none of the 3 games exercise `com.guidebee.game.physics` directly,
build a **small standalone test harness** (a 4th, throwaway `GameActivity`
under `gameengine`'s own `androidTest`/a debug-only test screen, not shipped
in `app`) that:

1. Creates a `World`, drops a handful of dynamic bodies (box + circle) onto a
   static ground body, steps the simulation for a fixed number of frames with
   a fixed timestep, and records final positions/velocities.
2. Exercises at least one of each joint type actually compiled into the
   native lib (`DistanceJoint`, `RevoluteJoint`, `WeldJoint`, `MouseJoint`,
   `WheelJoint` — all present per `Android.mk`'s source list) since these are
   exactly the areas where a JNI-glue bug (dangling native pointer, wrong
   struct layout after a recompile) would silently corrupt state rather than
   crash.
3. Exercises `Stage`/`Actor`'s built-in Box2D integration
   (`scene/collision/SensorListener`) with a simple sensor-overlap scenario,
   since that's the actual code path the engine ships that the 3 games don't
   test.
4. Captures the final body positions/velocities from the **pre-upgrade**
   build (Phase 0 baseline) as golden values, then re-runs the identical
   harness after Phase 4 and asserts equality within a small floating-point
   epsilon. A meaningful drift here — beyond float noise — indicates the
   solver iteration constants or compiler flags changed simulation behavior
   and needs investigation before merging.

**Exit criteria for Phase 4 specifically:** this harness's golden-value
comparison passes; only then is the Box2D native upgrade considered safe to
ship, independent of the 3 games showing no visible symptom (they wouldn't,
since none of them use it — the harness is the only signal).

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
| 0 — Baseline | 0.5–1 day | — |
| 1 — Build tooling | 1–2 days | Phase 0 |
| 2 — Lifecycle/windowing | 2–3 days | Phase 1 (needs the new NDK/build to test against) |
| 3 — Rendering pipeline | 3–5 days | Phase 1 (independent of Phase 2, could run in parallel if two people are available) |
| 4 — Box2D native upgrade | 2–4 days | Phase 1; independent of 2/3, can run in parallel |
| 5 — Cleanup/hardening | 1–2 days | All of 1–4 |

**Total: ~10–17 working days** for one engineer working sequentially;
Phases 2, 3, and 4 are mutually independent once Phase 1 lands, so they can
be parallelized across 2-3 engineers to compress the calendar time to roughly
Phase 1 + max(Phase 2, Phase 3, Phase 4) + Phase 5 ≈ **6–9 working days**.

---

## 8. Acceptance criteria (overall)

- [ ] All 3 games (Flappy Bird, Battle City, Mario) pass their full manual
      regression checklist (5.2) on the full device matrix (Section 6).
- [ ] Perf capture at final sign-off is **equal to or better than** the
      Phase-0 baseline on every device class, on all 3 games.
- [ ] APK size is smaller than the pre-upgrade baseline (dead-code + ABI
      trimming should guarantee this).
- [ ] Box2D regression harness (5.4) golden-value comparison passes.
- [ ] Zero new `UnsatisfiedLinkError`/native crashes across a full play
      session per game per device class.
- [ ] `docs/GAME_ENGINE.md` updated to describe the new baseline (GLES3
      default, ETC2 textures, current NDK, Box2D-version-parity note) so the
      next engineer doesn't have to re-derive this audit.
