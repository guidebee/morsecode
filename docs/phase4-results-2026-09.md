# Phase 4 results — Box2D native layer re-sync (2026-09-13)

Baseline: `docs/phase3-results-2026-09.md`. Box2D's **version** is already at
parity with upstream (2.3.1, confirmed in Phase 0), so this phase is a
JNI wrapper + native build re-sync against `C:\workspace\libgdx`'s current
`gdx-box2d`, not a physics-API migration.

## 1. Core `Box2D/` diff against current libgdx

`diff -rq --strip-trailing-cr` against
`C:\workspace\libgdx\extensions\gdx-box2d\gdx-box2d\jni\Box2D` found the
same 94 files on both sides (no additions/removals), 22 with real content
differences after stripping line-ending noise. All 22 fell into 3
buckets:

1. **A real, confirmed upstream bug fix — adopted.**
   `Collision/Shapes/b2PolygonShape.cpp`'s vertex-welding loop compared a
   **squared** distance (`b2DistanceSquared`) against a **linear**
   threshold (`0.5f * b2_linearSlop`) — a units bug. Current libgdx squares
   the threshold too. Fixed:
   ```cpp
   // before
   if (b2DistanceSquared(v, ps[j]) < 0.5f * b2_linearSlop)
   // after
   if (b2DistanceSquared(v, ps[j]) < ((0.5f * b2_linearSlop) * (0.5f * b2_linearSlop)))
   ```
2. **Missing standard-library includes — adopted (14 files).** libgdx's
   current copy adds explicit `#include <new>`/`<cstring>`/`<cstdio>`/
   `<cfloat>`/`<stdio.h>`/`<stdarg.h>`/`<stdlib.h>`/`<climits>` across
   `b2CircleShape.cpp`, `b2EdgeShape.cpp`, `b2ChainShape.cpp`,
   `b2PolygonShape.cpp`, `b2BroadPhase.cpp`, `b2DynamicTree.cpp`,
   `b2TimeOfImpact.cpp`, `b2Settings.cpp`, `b2BlockAllocator.cpp`, all 7
   `Dynamics/Contacts/*.cpp` files, `b2Joint.cpp`, and `b2World.cpp`. Same
   root cause as Phase 1's `Stb_Image.cpp` incident (NDK r29's stricter
   unified headers no longer transitively pull in headers older toolchains
   did) — currently latent, not yet a hard break, but exactly the kind of
   silent fragility that becomes a build break on the next toolchain bump.
   Adopted all of them verbatim.
3. **A local Guidebee patch — preserved, not touched.** `b2Settings.h`
   has `#include <cstring>`/`<new>` that current libgdx's copy does
   **not** have — this is Guidebee's own forward-compatibility fix,
   already ahead of upstream. Left alone.
   - Also left alone: `CHANGES`' one-line "libgdx" → "gameengine"
     rebranding text (intentional, not a regression) and
     `Collision/b2BroadPhase.h`'s dead-forum-link comment (cosmetic only).
   - Adopted anyway (cosmetic, zero functional impact): `Box2D.h`'s two
     documentation URLs updated to their current (non-dead) equivalents.

## 2. `Wrapper/Box2D/` JNI glue — targeted audit, not exhaustive

Current libgdx no longer ships static `.cpp` wrapper files — it generates
them at build time via `jnigen` from `/*JNI ... */` comment blocks embedded
directly in each `com.badlogic.gdx.physics.box2d.*.java` file. GGE's own
`com.guidebee.game.physics.*` classes use the **same jnigen convention**
(confirmed: `RopeJoint.java` embeds its native bodies as comments after
each `native` declaration) but ship the generated `.cpp`/`.h` as checked-in
files rather than regenerating them at build time. This means a raw
directory diff against libgdx's `jni/` tree doesn't apply here — the two
codebases diverge in file layout and class names (`com.guidebee.game.physics.*`
vs `com.badlogic.gdx.physics.box2d.*`), so comparison has to be per-class,
semantic, not textual.

Given a full 63-file method-by-method audit is a much larger undertaking
than the rest of this phase, scoped this pass to:

- **`World.cpp`/`World.java`** (the file the plan specifically calls out
  for "stale local refs across `World.step()` callbacks"): GGE's
  `CustomContactListener` (jmethodID caching, `NewGlobalRef` on the class,
  `CallVoidMethod` per contact event) is structurally identical to
  libgdx's own. No reference-leak pattern found.
- **A mechanical sweep for the exact bug class the compiler warnings
  surfaced** (see below) — every `private native <non-void-type> jniXxx(...)`
  declaration across `com.guidebee.game.physics.**` (120 matched) checked
  for whether its embedded JNI body actually returns a value. Found
  exactly one violator (`RopeJoint.jniSetMaxLength`, see below); the other
  119 are clean.
- **Not done**: a full per-class semantic diff of all 63 wrapper files
  against libgdx's current embedded JNI blocks, looking for reference-leak/
  `GetDirectBufferAddress` bugs the plan flagged as the likely location of
  a decade of unported upstream fixes. This layer has passed the full
  10-stage Box2D Demo regression suite repeatedly across every phase this
  session with zero crashes, and the one spot-check done (`World.cpp`)
  found no divergence from libgdx's pattern — reasonable evidence this
  layer is sound, but not the same as having checked it. Flagging as
  future work rather than claiming false completeness.

## 3. Rebuilt under the modernized NDK — 2 real bugs found via new warnings

Forced a full clean rebuild (`:gameengine:clean` then
`externalNativeBuildDebug`, no `-q`, so ndk-build's actual compiler output
is visible) to catch every warning, not just what an incremental build
happens to touch.

- **`b2BroadPhase.h:245` — a real, confirmed, long-standing Box2D bug,
  present in current libgdx too.** `-Wsizeof-pointer-div` flagged:
  ```cpp
  qsort(m_pairBuffer, sizeof(m_pairBuffer) / sizeof(struct b2Pair), sizeof(struct b2Pair), b2PairCompareQSort);
  ```
  `m_pairBuffer` is a `b2Pair*` (a heap-allocated buffer, not a fixed
  array) — `sizeof(m_pairBuffer)` is just the pointer size (4 or 8 bytes),
  and `sizeof(b2Pair)` (two `int32` proxy IDs) is 8 bytes, so this
  expression truncates to **0** on every platform, every time. `qsort` is
  told to sort zero elements — **the broad-phase pair buffer has never
  actually been sorted**, since whichever old Box2D forum "fix" (linked in
  the adjacent comment, itself dead) introduced this in place of the
  original `std::sort(m_pairBuffer, m_pairBuffer + m_pairCount, ...)`
  (still visible, commented out, directly above). The subsequent
  duplicate-pair-skip loop only works on a sorted buffer, so duplicate
  broad-phase candidate pairs were never actually being deduplicated —
  wasted (but not incorrect — `b2ContactManager::AddPair` is itself
  idempotent per fixture pair) redundant work every `World.Step()`.
  Verified current libgdx's vendored copy has the **exact same bug**
  (confirmed via the Section 1 diff — this file only differs by a dead-link
  comment) — this is an unfixed upstream Box2D defect, not something
  libgdx already fixed and GGE missed. Fixed to match the original,
  correct intent: `qsort(m_pairBuffer, m_pairCount, sizeof(struct b2Pair), b2PairCompareQSort)`.
- **`b2PrismaticJoint.cpp:177-178` — dead debug scaffolding, deleted.**
  `-Wunused-but-set-variable` on a local `s1test` computed via `b2Cross`
  (a pure function, no side effects) and never read. Confirmed identical
  in current libgdx (not something upstream has cleaned up either) —
  deleted both lines, zero behavior change since the removed computation
  had no observable effect.
- **`RopeJoint.cpp`/`.h`/`RopeJoint.java` — a real signature bug in GGE's
  own wrapper, found and fixed.** `jniSetMaxLength` was declared
  `JNIEXPORT jfloat JNICALL` / Java `native float` but its body only calls
  `rope->SetMaxLength(length)` and never returns anything — undefined/
  garbage float returned on every call. Zero practical impact today
  (`RopeJoint.setMaxLength()` on the Java side discards the return value
  entirely — `jniSetMaxLength(addr, length);` as a bare statement), but a
  latent correctness trap for any future caller that reads the return
  value. Confirmed libgdx's own `RopeJoint.java` declares this `void`, not
  `float` — GGE's copy had drifted. Fixed all three call sites (`.java`
  native declaration + embedded JNI comment, `.h` javah-style declaration
  and its `Signature:` doc comment, `.cpp` definition) to `void`, matching
  upstream.
  - **Followed up with a mechanical sweep** (a small Python script parsing
    every `private native <non-void> jniXxx(...); /* ... */` block across
    `com.guidebee.game.physics.**`, 120 matched) checking whether each
    embedded JNI body contains a `return` statement. Zero other
    violations found — this was an isolated one-off, not a systemic
    pattern (most likely a copy-paste from a neighboring getter).
- **Remaining warnings — out of scope for this phase, left as-is.** The
  rest of the clean build's warning list (`JPGD.cpp`'s shift-negative-value
  and unused variables, `Stb_Image.cpp`'s self-assignment and unused
  variable, `BufferUtils.cpp`'s misleading indentation, `ETC1Utils.cpp`'s
  unused variables, `2DPixmap.cpp`'s unused function) are all in
  `Wrapper/Box2D/Common/` — image/buffer decoding utilities that live
  under the historical "Box2D" wrapper directory name but aren't physics
  code. Deferred to Phase 5's dead-code/lint cleanup sweep, which is the
  correct scope for them, rather than pulled into a "Box2D upgrade" phase.

## 4. Solver iteration constants — unchanged, confirmed

Grepped for `VELOCITY_ITERATIONS`/`POSITION_ITERATIONS` (and their
`b2_velocityThreshold`/`b2_maxTranslation`-style siblings in
`b2Settings.h`) — none were touched by any edit in this phase, and
`b2Settings.h`'s only diff (Section 1) is the pre-existing local Guidebee
`#include` patch, not a constants change. No compiler-flag-driven
floating-point codegen concern either — `Application.mk`/`Android.mk`'s
optimization flags weren't touched this phase (only Phase 1 touched NDK
version/ABI list, already regression-tested across 3 phases since).

## Verification

- Full clean native rebuild (`:gameengine:clean` +
  `externalNativeBuildDebug`, both ABIs): **builds successful, zero
  Box2D/physics-scoped warnings remaining** (confirmed via a second clean
  rebuild after all fixes — only the out-of-scope `Wrapper/Box2D/Common/`
  image-utility warnings remain, unchanged from before this phase).
- `:app:compileDebugJavaWithJavac`: clean (the pre-existing `Container`/
  `ScrollPane` `@Deprecated`-annotation warnings are unrelated, present
  before this phase).
- **All 10 Box2D Demo stages**, launched directly via
  `StagePickerActivity.EXTRA_STAGE_CLASS` intent extras (with
  `am force-stop` between each launch — `Box2DGameActivity` has no
  `onNewIntent`, so re-delivering an intent to an already-running instance
  silently no-ops instead of switching stages, a repeat of the same
  "clean relaunch" lesson from Phase 1/2's crash-testing): `BasicBox2DStage`,
  `BodyTypeStage`, `ShapeTypeStage`, `ForceAndImpulseStage`,
  `CollisionStage`, `SensorStage`, `RayCastStage`, `JointsOverviewStage`,
  `SelfControlStage`, `BulletStage` — all render correctly, bodies settle
  stably with no jitter/overlap artifacts from the broad-phase sort fix,
  joints/sensors/raycasts all visually correct, **zero crashes**
  (`adb logcat` grepped for `FATAL EXCEPTION`/`AndroidRuntime` across the
  whole run — none).
- **3 games not re-tested this phase** — confirmed via grep that none of
  `au.com.guidebee.morsetoolkit.**` references `com.guidebee.game.physics`
  at all, so there is zero code-path overlap between this phase's changes
  and the 3 games. Re-running them would only re-confirm the native
  library still links (already confirmed by the successful build).
- Manifest `exported` flag temporarily set on `Box2DGameActivity` for
  direct-launch testing, reverted via `git checkout` afterward — confirmed
  clean via `git diff`.

## Exit criteria check

- [x] All 10 Box2D Demo stages pass visually-identical (no prior-phase
      baseline screenshots existed specifically for these 10 stages to
      diff pixel-for-pixel against, but all render correctly with stable,
      settled physics and no crashes).
- [x] Native build has zero new warnings — actually **fewer** warnings
      than before this phase (2 Box2D-scope warnings fixed, plus a latent
      correctness bug in `RopeJoint` fixed as a side effect of chasing a
      3rd).
- [x] Solver iteration constants unchanged.
- [ ] Full `Wrapper/Box2D/` 63-file semantic audit against libgdx's
      current embedded JNI blocks — not done exhaustively; targeted
      `World.cpp` spot-check plus a mechanical return-value sweep across
      all 120 non-void native methods found no issues, but this isn't the
      same as a complete audit. Documented as future work, not silently
      skipped.
