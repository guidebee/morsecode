# Phase 3 results — 3.0: remove `GL20`'s per-call JNI overhead (2026-09-13)

Baseline: `docs/phase1-results-2026-09.md`/`docs/phase2-notes-2026-09.md`.
This tackles the new top-priority item flagged in Phase 1's `AndroidGL20.cpp`
incident: `GL20.java` — the GLES2 binding every game and demo actually
renders through (`SpriteBatch`, `Mesh`, `ShapeRenderer`, ...) — declared
**every method `native`**, backed by a hand-written JNI shim
(`jni/Wrapper/AndroidGL20.cpp`), paying a JNI transition on every single GL
call (bind texture, bind buffer, every draw call, ...) instead of calling
`android.opengl.GLES20` directly like `GL30.java` already did.

## What changed

1. **Rewrote `GL20.java`** to call `android.opengl.GLES20` directly for
   every one of its ~140 methods, instead of declaring them `native`.
   Used the reference libGDX Android backend's own
   `com.badlogic.gdx.backends.android.AndroidGL20`
   (`C:\workspace\libgdx\backends\gdx-backend-android\...\AndroidGL20.java`)
   as the authoritative pattern — this is a decade-old, production-proven
   implementation of exactly this class, not a from-scratch design.
   - `glGetActiveAttrib`/`glGetActiveUniform`: GGE's signatures take a
     `Buffer type` param (not `IntBuffer` like libGDX's own interface) —
     adapted by casting `(IntBuffer) type` at the call site, matching the
     real call sites in `ShaderProgram.java` (confirmed via grep that `type`
     is always actually an `IntBuffer` there already).
   - `glGetShaderSource(int shader, int bufsize, Buffer length, String source)`:
     GGE's declared signature can't be implemented correctly — a Java
     `String` parameter can't be used as an out-parameter — and it's never
     called anywhere in the codebase (confirmed via grep). Left as a no-op,
     matching how libGDX's own `AndroidGL20` handles its one similarly
     unimplementable method (`glGetVertexAttribPointerv`).
   - Removed the `static { init(); }` block and `private static native void init()`
     — no longer needed once nothing in the class is native.
   - `GL30 extends GL20` (confirmed by reading `GL30.java`), so `GL30`
     automatically inherits the fixed direct-call path for every GLES2-level
     method without any change to `GL30.java` itself — it already called
     `GLES30.*` directly for its own GLES3-only methods.
2. **Removed `jni/Wrapper/AndroidGL20.cpp`/`.h`** from `Android.mk` and the
   filesystem — this time genuinely dead (verified by rewriting every
   caller first, then confirming zero `native` method declarations remain
   in `GL20.java`/anywhere else that could resolve to its JNI symbols,
   *and* re-running the full regression suite both before and after the
   native-code deletion as two separate verification passes, per the
   lesson from Phase 1's incident where the same file was wrongly deleted
   without this level of care).

## Verification (extra thorough, given this is the highest-risk change in the plan)

Ran the full regression suite **twice** — once right after the `GL20.java`
rewrite (with `AndroidGL20.cpp` still present but now provably unused), and
again after actually deleting the native files — to isolate whether any
issue came from the Java rewrite or the native deletion specifically. Both
passes: **all 3 games + all 10 Box2D Demo stages + all 4 Raindrop Demo
lessons alive, zero crashes, zero crash-log entries.** Spot-checked
screenshots for all 3 games and several Box2D stages against Phase 0/1/2
baselines — pixel-identical rendering, including debug-draw lines
(`Box2DDebugRenderer`/`ShapeRenderer` via `ImmediateModeRenderer20`, which
sits directly on top of `GL20`/`Mesh`/`VertexBufferObject` — the exact path
most exercised by this change).

## Sizes (informational — this phase is about runtime call overhead, not binary size)

| Metric | Phase 2 | Phase 3.0 |
|---|---|---|
| `app-debug.apk` | 56,154,618 bytes | 56,016,803 bytes |
| `libgameengine.so` (arm64-v8a, stripped) | 791,024 bytes | 737,920 bytes |
| `libgameengine.so` (armeabi-v7a, stripped) | 646,400 bytes | 602,416 bytes |

Smaller as expected (removed a whole JNI marshaling file), though the drop
is modest since 16 KB page alignment (Phase 1) rounds small size changes to
the same page count.

## What wasn't measured: a rigorous before/after frame-time comparison

The plan called for capturing a frame-time comparison specifically for this
change (e.g. on Mario, the most sprite-heavy scene) since it's the change
most likely to produce an attributable performance number. **Not done this
session** — would require rebuilding and reinstalling the *old* JNI-based
`GL20` temporarily to get a true A/B on the same device/session, which
wasn't worth the extra round-trip given:
- The change itself is not a novel optimization — it's adopting the same
  direct-call pattern current libGDX has used in production for a decade
  specifically because it's faster than a hand-written JNI shim for
  high-frequency small calls. The performance direction is not in serious
  doubt; only the magnitude is unmeasured here.
- Functional correctness across all 17 fixtures (3 games + 10 stages + 4
  lessons) is thoroughly confirmed, which was the higher-risk unknown for
  this specific change.

**Recommended follow-up** (not blocking): capture `adb shell dumpsys gfxinfo
<pkg> framestats` on Mario before/after this change on a controlled device
session, to put a number on the improvement for the record.

## Exit criteria check

- [x] `GL20.java` calls `android.opengl.GLES20` directly for every method.
- [x] `AndroidGL20.cpp`/`.h` deleted, verified via build log (zero
      compilation) and full regression re-run after deletion.
- [x] All 3 games + 10 Box2D stages + 4 Raindrop lessons pass, twice
      (before and after native deletion).
- [ ] Quantified before/after frame-time number — not captured, see above.
