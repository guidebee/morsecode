# Phase 2 notes — Android lifecycle & windowing (2026-09-13, in progress)

## Status: two real bugs fixed, the deep crash is NOT resolved

Started Phase 2 with the highest-value, best-understood item: the
rapid-relaunch `SIGSEGV` in `VertexBufferObject.bind → glBufferData` found
and root-caused during Phase 0 (see `docs/perf-baseline-2026-09.md`'s
"On-device validation" section and `docs/GAMEENGINE_UPGRADE_PLAN.md` Phase
2's audit entry).

### Fix 1 — `Graphics.onDrawFrame`'s pause/destroy notify ordering (kept, real bug, insufficient alone)

`GameActivityWrapper.onPause()` calls `graphics.pause()`/`graphics.destroy()`,
which block the calling thread until the GL thread's `onDrawFrame`
acknowledges the corresponding flag. The acknowledgement
(`pause = false; synch.notifyAll();` / same for `destroy`) previously
happened **before** the actual `ApplicationListener.pause()`/`dispose()`
callback ran (that callback was outside the `synchronized` block, after the
notify). This meant `graphics.destroy()` could return to the calling thread
**before** `dispose()` had actually finished freeing native
Meshes/VBOs/the Box2D world — a real, verifiable race in the wait/notify
protocol, independent of whether it's the sole cause of the observed crash.

**Fix**: moved the flag-clear/`notifyAll()` to run *after* the corresponding
callback completes (a second `synchronized (synch)` block after the
`if (lpause)`/`if (ldestroy)` callback sections in `onDrawFrame`). This is
still in the tree — it's a legitimate correctness fix and should be kept
regardless of the outcome below.

### Fix 2 — missing `debugRenderer.dispose()` (kept, real bug, insufficient alone)

Found while investigating: **every** Box2D tutorial stage's
`Box2DDebugRenderer` (and the `ShapeRenderer`/`Mesh`/`VertexBufferObject`
+ native "unsafe" malloc'd buffer it owns) was never disposed anywhere —
`Box2DGameStage` (the base class for 9 of the 10 stages) and `RayCastStage`
(which extends `Stage` directly with its own independent, near-duplicate
implementation — not `Box2DGameStage`) both lacked a `dispose()` override
to clean it up. Every stage instance leaked its debug-renderer's GPU buffer
object and native heap allocation on teardown.

**Fix**: added `dispose()` overrides to both `Box2DGameStage` (covers
`BasicBox2DStage`, `BodyTypeStage`, `BulletStage`, `CollisionStage`,
`ForceAndImpulseStage`, `JointsOverviewStage`, `SelfControlStage`,
`SensorStage`, `ShapeTypeStage`) and `RayCastStage` individually, each
calling `super.dispose()` then `debugRenderer.dispose()`. Still in the tree
— legitimate fix, unrelated bug from the one below, worth keeping regardless.

### The actual crash: still unresolved after both fixes

**First read was wrong and needs a correction of its own**: after landing
both fixes, a 15-iteration rapid launch/back/relaunch test showed 0/15
crashes, which looked like confirmation the fixes worked. A follow-up
25-iteration test *alternating between different stage classes* crashed
constantly (18 crash entries), and a re-run of the *original, previously-clean*
single-stage test then also started crashing immediately — on the same
device state, same build. This pointed to the Mali GPU driver having
degraded from the volume of `SIGSEGV`s produced during testing (~20+ by
that point), not to stage-switching being a distinct trigger.

**The user rebooted the device to get a clean state.** Re-ran the original
single-stage repeat test (`RayCastStage`, launch → back → relaunch,
~0.5s/0.4s spacing) immediately after reboot: **crashed on iteration 2.**
Same exact signature (`libGLES_mali.so` → `Java_..._GL20_glBufferData` →
`VertexBufferObject.bind` → `Mesh.bind` → ... → the stage's `draw()` chain)
as originally found in Phase 0.

**Conclusion: neither fix resolves the crash.** They were real, worth
fixing, but not the (or not the whole) root cause. The earlier "15/15 clean"
result was almost certainly a methodology artifact, not evidence of a fix —
likely the same "Activity not started, intent delivered to top-most
instance" issue seen elsewhere in this investigation, where `am start`
redelivers to an already-resumed instance instead of creating a fresh one,
so several of those 15 "iterations" weren't actually exercising the
relaunch path at all.

## Where this leaves Phase 2

This is a deeper bug than a single-file fix — likely in the fundamental
interaction between Android's `GLSurfaceView`/`Activity` lifecycle and this
engine's static, "current app" globals (`GameEngine.app`/`.graphics`/`.world`
etc. get reassigned on every `onResume()`), or a genuine EGL context
creation/destruction race at the driver level under fast succession, or
something not yet identified. Properly root-causing it likely needs either:

- A native debugger (`lldb`) attached to reproduce and inspect the actual
  memory being accessed at the crash address, rather than inferring from
  Java-level reasoning and `adb logcat` timestamps.
- Or porting substantially more of current libGDX's `AndroidGraphics`
  surface-lifecycle handling (which has accumulated a decade of guards for
  exactly this class of bug) rather than patching this fork's version
  incrementally.

**Recommendation**: don't keep grinding on this via trial-and-error without
better tooling — it's consumed significant effort for two real (kept) bug
fixes but no resolution of the actual crash. Treat it as a known,
deep, pre-existing issue; flag it prominently (already done in
`docs/GAMEENGINE_UPGRADE_PLAN.md`'s Phase 2 section and here). **Parked per
explicit user direction** — come back to it with `lldb` or more logging
infrastructure in a dedicated session; in the meantime, the practical
mitigation is already documented (avoid rapid back-then-relaunch; a single
clean launch of any stage/game is safe, confirmed extensively).

## Edge-to-edge modernization (done)

`GameActivityWrapper` used deprecated, reflection-based APIs for
fullscreen/immersive handling: `Window.setFlags(FLAG_FULLSCREEN, ...)` and
`View.setSystemUiVisibility(int)` invoked via reflection (likely to avoid a
hard compile-time dependency on a specific API level, which is moot now that
`minSdk = 21` already guarantees these methods exist directly). These are
formally deprecated and superseded by mandatory edge-to-edge layout on apps
targeting API 35+ (`targetSdk = 37` here).

**Changes**:
- Added `androidx.core:core:1.13.1` as a `gameengine` dependency (previously
  zero AndroidX dependencies in this module) for `WindowCompat`/
  `WindowInsetsControllerCompat`.
- Replaced `Window.setFlags(FLAG_FULLSCREEN, ...)` with
  `WindowCompat.setDecorFitsSystemWindows(window, false)`.
- Replaced the reflection-based `hideStatusBar()`/`useImmersiveMode()` with
  `WindowInsetsControllerCompat.hide(...)`/`setSystemBarsBehavior(...)`.
- Removed now-dead version guards (`getVersion() < 11/13/19`) — all
  unreachable since `minSdk = 21` — and the now-unused `Method`/`TargetApi`
  imports.

**Verified on-device**: all 3 shipped games (Flappy Bird, Battle City,
Mario) launched cleanly multiple times each, rendering correctly full
edge-to-edge with no status bar, no crashes. (One early false alarm: Mario
and Battle City initially appeared to render solid-black in portrait
dimensions — turned out to be normal asset-loading time exceeding my
2.5-4s test wait, not a regression; both render correctly landscape/edge-to-edge
once given ~10s to load on first launch.)

## Predictive back gesture (no change needed)

Confirmed via `grep` across `app/` and `gameengine/`: **zero** custom
`onBackPressed()` overrides, `OnBackPressedCallback`/`OnBackInvokedCallback`
usage, or `android:enableOnBackInvokedCallback` manifest overrides anywhere
in the project. All back-button handling relies on the platform default
(`Activity.finish()` on back), which is inherently compatible with
predictive back's preview animation — there's no custom logic that could
run at the wrong time or be skipped. `targetSdk = 37` already gets
predictive back enabled by default (Android enables it by default for apps
targeting API 34+). No code change needed for this item.

## EGL10 manual config chooser simplification — deferred

Not started this session (time budget). `EglConfigChooser`/`GLSurfaceView20`
still implement `javax.microedition.khronos.egl.EGL10`-based manual config
scoring, inherited complexity from an era of much more varied GLES driver
support that current Android devices don't need. Lower priority than the
above — it's a maintainability/simplification item, not a bug or
user-visible issue, and current behavior is confirmed working correctly
across all 3 games + both tutorial demos throughout this session's
extensive testing. Left for a future pass.
