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

---

# Phase 3.2 — GLES 3.0 completion (2026-09-13)

## 3.2a — default to a GLES 3.0 context, fall back to ES2 — done, committed

See `docs/GAMEENGINE_UPGRADE_PLAN.md`'s Phase 3.2 section for the full
writeup. Summary: `GameEngine.gl30` was never assigned anywhere before this
(0% used, not "partially used"). `GLSurfaceView20.ContextFactory` now tries
ES3 first with an ES2 fallback; the real, active config chooser turned out
to be `EglConfigChooser.java` (not `GLSurfaceView20`'s own internal
`ConfigChooser`, which is dead code — always overridden right after
construction). `Configuration.useGL30` (default `true`) is the opt-out.
Verified on-device (Mali-G615 MC2 successfully negotiated ES 3.2, first time
ever on this device/engine pairing) with the full 17-fixture regression
suite passing, pixel-identical rendering. Committed as `8028698`.

## 3.2b — VAOs in Mesh — attempted, broke rendering, reverted

Added `VertexBufferObjectWithVAO` (ported from libGDX's own reference
implementation at
`C:\workspace\libgdx\gdx\src\com\badlogic\gdx\graphics\glutils\VertexBufferObjectWithVAO.java`)
and wired `Mesh`'s three "default" constructors (the ones every real caller
— `SpriteBatch`, `ImmediateModeRenderer20`, etc. — actually uses) to pick it
automatically over the plain `VertexBufferObject` whenever
`GameEngine.gl30 != null`, via a new `Mesh.createVertexData(...)` helper.
Compiled clean.

**Broke on-device**: all sprite rendering (Flappy Bird's menu, Box2D Demo
stage sprites, everything drawn via `SpriteBatch`) went invisible/black,
while `Box2DDebugRenderer`'s wireframe outlines (drawn via
`ImmediateModeRenderer20`, `glDrawArrays`, no index buffer) kept rendering
fine. This indexed-vs-non-indexed split is the one solid clue: `SpriteBatch`
draws quads via `glDrawElements` against an `IndexBufferObject`, and VAOs
capture the currently-bound `GL_ELEMENT_ARRAY_BUFFER` as part of their
state. Best (**unconfirmed**) hypothesis: `IndexBufferObject.bind()`'s own
`isBound`-gated caching skips the real `glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ...)`
call needed to (re-)associate the index buffer with whichever VAO is
*currently* bound, so a second/different `Mesh`'s VAO never gets the
element-buffer association it needs even though `Mesh.bind()` calls
`indices.bind()` unconditionally right after `vertices.bind()` — but this
was never actually confirmed with a GL frame debugger, just inferred from
the symptom shape.

**Decision: revert rather than debug further live.** This change touches
the mesh-bind path for every draw call in every game — exactly the kind of
high-blast-radius change that (per this plan's own stated principle) should
be isolated to its own commit and thoroughly proven before landing, not
patched forward under uncertainty. `git checkout` restored `Mesh.java`;
`VertexBufferObjectWithVAO.java` was deleted. Confirmed on-device after
revert: sprites render correctly again (`BasicBox2DStage` screenshot
matches the pre-VAO baseline exactly). The GLES3 context switch (3.2a)
is untouched by this revert and remains in place — `Mesh` now simply always
uses the plain `VertexBufferObject` regardless of GL version, same as
before either 3.2 sub-item started.

**For whoever picks this back up**: don't re-run the same implementation
and hope — get an actual GL frame capture (e.g. Android GPU Inspector, or
`adb shell dumpsys` isn't enough here) and inspect the VAO's
`GL_ELEMENT_ARRAY_BUFFER_BINDING` state at draw time, or add temporary
`glGetError()`/`glGetIntegerv(GL_ELEMENT_ARRAY_BUFFER_BINDING, ...)` logging
around `Mesh.bind()`/`render()` to see what's actually bound when the
`glDrawElements` call happens.

## 3.2c — ETC2/ASTC texture compression — blocked, deferred

No ETC2/ASTC encoder available in this environment (checked for
`etc2comp`/`astcenc`/`pvrtextool` and both installed NDKs; none ship one).
Without an encoder there's no real compressed asset to test against, so any
loader code written for it would be unverified. Deferred until an encoder
is brought into the environment — see `GAMEENGINE_UPGRADE_PLAN.md`'s 3.2
section for full reasoning.

## 3.2d — instanced draws for `TiledLayer`/Battle City — investigated, not warranted

The plan's premise was that tile rendering is "likely one `glDrawArrays`/
`glDrawElements` per tile." Checked `TiledLayer.paint()`
(`microedition/TiledLayer.java:319-371`): it calls `Batch.draw()` per tile,
i.e. `SpriteBatch.draw()` — this appends into `SpriteBatch`'s own growing
vertex array, it does **not** issue a GL draw call per tile. The real
question is whether Battle City's tile count could ever exceed
`SpriteBatch`'s flush threshold (1000 sprites, confirmed identical to
libGDX's default in the 3.1 investigation above) within a single frame,
since that's the only scenario where instancing would beat what already
exists.

Computed Battle City's actual grid: `BattleCityGameScene` uses
`gameWorldWidth=420`, `gameWoldHeight=240`, `ResourceManager.TILE_WIDTH=12`,
`barHeight=32` → `xTiles=35`, `yTiles=(240-32)/12=17`. `BattleField`'s
constructor doubles both for its internal grid: `WIDTH_IN_TILES=70`,
`HEIGHT_IN_TILES=34` → 2,380 total cells. `TiledLayer.paint()` iterates
that whole grid every frame (no viewport culling) but skips any cell with
tile id `0` (`TiledLayer.java:344-347`, a `continue`). Reading
`BattleField.drawRandomArea()` (`actors/BattleField.java:355-398`): the
play area is cleared to all-`0` first, then obstacle clusters are scattered
on a stride-6 grid with only a 45% placement chance per anchor, each
cluster covering a handful of cells — the field is overwhelmingly empty,
landing at roughly 100-300 actual tile draws per frame even in a fully
"unlucky" random roll, plus `drawLeftArea()`'s fixed-size border/letter
strip. This is well under the 1,000-sprite flush threshold, so
`SpriteBatch` already collapses the entire field (plus tanks/bullets/HUD,
sharing the same atlas) into a small, effectively constant number of
`glDrawElements` calls per frame — most likely just 1.

**Decision: don't implement.** Same outcome as the 3.1 investigation —
verifying the premise against the actual codebase found it false, so no
code was written. Instancing would add a new parallel rendering path (with
its own testing/maintenance burden) for no measurable benefit here.
**Phase 3.2 is complete**: 3.2a (GLES3 context) shipped, 3.2b (VAOs)
reverted with guidance for a future attempt, 3.2c (ETC2) deferred on
tooling, 3.2d (instancing) not warranted.
