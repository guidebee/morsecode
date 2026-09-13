# Phase 1 results — Build-tooling modernization (2026-09-13)

Baseline to diff against: `docs/perf-baseline-2026-09.md` (Phase 0, tag
`pre-gameengine-upgrade`). All steps below match
`docs/GAMEENGINE_UPGRADE_PLAN.md` Phase 1.

## What changed

1. **NDK bumped 21.4.7075529 → 29.0.14206865** (`gameengine/build.gradle`
   `ndkVersion`). No newer NDK was already installed locally; installed via
   `sdkmanager "ndk/29.0.14206865"` (latest stable, non-RC, available at the
   time). `APP_PLATFORM` stayed at `android-21`, matching `minSdk = 21` —
   confirmed NDK r29 still supports that platform level, no change needed.
2. **ABI set trimmed from 4 to 2** (`armeabi-v7a`, `arm64-v8a` — dropped
   `x86`/`x86_64`). **Finding**: `Application.mk`'s own `APP_ABI :=` line
   does **not** control this for a Gradle/AGP-driven `ndkBuild` — AGP passes
   `APP_ABI=<abi>` per-invocation on the `ndk-build` command line, overriding
   whatever `Application.mk` says. The actual control point is
   `android.defaultConfig.ndk.abiFilters` in `gameengine/build.gradle`.
   (First attempt — editing `Application.mk` alone — silently had no
   effect; all 4 ABIs still built. Caught by checking the build log's task
   list, not by assuming it worked.)
3. **Removed the manual 16 KB page-size linker flag**
   (`Application.mk`'s `-Wl,-z,max-page-size=16384`) — verified via
   `llvm-readelf -l` on the rebuilt `arm64-v8a` `.so` that NDK r29 defaults
   to `0x4000` (16384) LOAD-segment alignment with no flag needed.
4. **Fixed a genuine NDK r29 compile break**: `Wrapper/Box2D/Common/Stb_Image.cpp`
   (vendored, no `#include`s of its own — this whole native module relies on
   force-included headers via `Android.mk`'s `-include` compiler flags) failed
   with `use of undeclared identifier 'pow'`/`'ldexp'`. Root cause: the
   force-include list (`-include jni.h -include stdlib.h -include string.h
   -include assert.h`, etc.) never included `math.h`/`cmath` — it apparently
   worked on the old NDK via transitive header leakage that NDK r29's
   stricter unified headers no longer provide. Fixed by adding
   `-include math.h` (`LOCAL_CFLAGS`) / `-include cmath` (`LOCAL_CPPFLAGS`)
   to `Android.mk`, matching the file's existing forced-include convention
   rather than editing the vendored `Stb_Image.cpp` directly.
5. **Deleted confirmed-dead code**:
   - `GLSurfaceViewAPI18`/`GLSurfaceView20API18` (`engine/platform/surfaceview/`)
     — guarded behind `sdkVersion <= 10` checks, unreachable since
     `minSdk = 21`. Collapsed `Graphics.java`'s `createGLSurfaceView`/
     `preserveEGLContextOnPause`/`onPauseGLSurfaceView`/`onResumeGLSurfaceView`/
     `setContinuousRendering`/`requestRendering` to the single
     `GLSurfaceView20`/`GLSurfaceView` path, removed the two now-dead class
     files and their imports, and removed the dead
     `Configuration.useGLSurfaceView20API18` flag (confirmed zero references
     anywhere in `app/`, including all 3 games). **This one held up** —
     verified by a full on-device rebuild/reinstall/spot-check after the
     change with zero crashes.

## Mistake made and corrected: `AndroidGL20.cpp` was *not* dead code

The Phase 0 audit (see `docs/GAMEENGINE_UPGRADE_PLAN.md`'s "Dead native code"
row) claimed `jni/Wrapper/AndroidGL20.cpp`/`.h` was unused, based on
`grep`-ing Java sources for the literal string `AndroidGL20` and finding zero
matches. **That was the wrong check.** JNI native-method linkage works by
C symbol name (`Java_com_guidebee_game_engine_platform_GL20_init`, etc.),
not by any Java-side import or textual reference to the C++ file/class name
— so "no Java file mentions `AndroidGL20`" says nothing about whether it's
providing `GL20.java`'s native implementations. It was.

Deleting it in this phase (removed from `Android.mk` and the filesystem)
broke **every single GameEngine-based screen** — all 3 shipped games and
both tutorial demos — with:

```
java.lang.UnsatisfiedLinkError: No implementation found for void
com.guidebee.game.engine.platform.GL20.init() (tried
Java_com_guidebee_game_engine_platform_GL20_init and
Java_com_guidebee_game_engine_platform_GL20_init__) - is the library
loaded, e.g. System.loadLibrary?
    at com.guidebee.game.engine.platform.GL20.init(Native Method)
    at com.guidebee.game.engine.platform.GL20.<clinit>(GL20.java:29)
    at com.guidebee.game.engine.platform.Graphics.setupGL(Graphics.java:223)
```

**Caught by the Phase 1 exit-criteria spot-check** (re-running 3 Box2D Demo
stages on-device after the NDK/dead-code changes, per the plan's own "every
phase ends with all 3 games installed and manually played" principle) —
not by the build, which succeeds either way since this is a runtime-only
JNI linkage failure. All 3 stages crashed identically.

**Fix**: restored both files from git history (`git show <pre-deletion
commit>:path > path`), re-added `Wrapper/AndroidGL20.cpp` to `Android.mk`'s
source list, rebuilt, and re-verified the same 3 stages on-device — all
alive, zero crashes.

**Correction to the Phase 0 audit table**: `AndroidGL20.cpp`/`.h` is *not*
dead code — it's the JNI implementation of `com.guidebee.game.engine.platform.GL20`'s
native methods (`android.opengl.GLES20` is *not* called directly from Java
here, contrary to what the Phase 0 audit's "GL binding path" row also
claimed — that row's "GL20/GL30 call android.opengl.GLES20/GLES30 directly
from Java" conclusion needs re-checking too, since `GL20.init()` being a
native method contradicts it). **Do not delete this file** in any later
phase without first confirming JNI symbol coverage (e.g. `nm -D` the built
`.so` for `Java_com_guidebee_game_engine_platform_GL20_*` symbols, or
just run the app), not just grepping Java source for the class name.

## Results vs. Phase 0 baseline

Measured from a fully clean rebuild (`./gradlew clean` then
`:app:assembleDebug` / `:gameengine:assembleDebug`) to avoid incremental-build
artifacts skewing the numbers — an earlier same-session measurement was
thrown off by exactly that and had to be re-taken.

| Metric | Phase 0 baseline | Phase 1 result | Delta |
|---|---|---|---|
| `app-debug.apk` | 59,130,264 bytes (with tutorial demos merged in) | **56,154,618 bytes** | **−2.98 MB** (dropping x86/x86_64 native libs; both figures include the same pre-existing `:app:stripDebugDebugSymbols` "Unable to strip" behavior — see note below — so the comparison is apples-to-apples) |
| `libgameengine.so` (arm64-v8a, stripped, via `gameengine`'s own AAR build) | 731,336 bytes | 791,024 bytes | +59.7 KB (newer Clang codegen/metadata; net APK size still down significantly since 2 fewer ABIs ship) |
| `libgameengine.so` (armeabi-v7a, stripped) | 624,356 bytes | 646,400 bytes | +22 KB |
| Cold native build (`clean` + `assembleDebug`) | 3 min 8 s (4 ABIs) | **~1 min 35 s** (2 ABIs) | **−49%** |
| Native compiler warnings (unique sites) | 27 (4 ABIs) | 34 (2 ABIs) | +7 new sites — see below |

**Aside, not a Phase 1 blocker**: `:app:stripDebugDebugSymbols` logs
"Unable to strip the following libraries, packaging them as they are:
libgameengine.so" and ships the **unstripped** `.so` (6.0 MB arm64-v8a) in
`app-debug.apk`, even though `gameengine`'s own `:gameengine:assembleDebug`
successfully strips it (791 KB) into its AAR. Confirmed pre-existing (same
message appears in the original, pre-upgrade baseline build) — not
introduced by this phase. Worth a follow-up look in a later phase since a
correctly-stripped app-level `.so` would be a further several-MB win on top
of the ABI trim, but out of scope here.

**New warnings** (all inside vendored Box2D/Wrapper C++ files, none are
errors, none look like real correctness bugs — mostly newer Clang catching
`-Wunused-but-set-variable`/`-Wmisleading-indentation` that the NDK r21-era
Clang didn't flag): `b2PrismaticJoint.cpp:177`, `BufferUtils.cpp:190,204`,
`JPGD.cpp:1168,1647,1769`, `Stb_Image.cpp:2960`. Per the plan's phase split,
cleaning these up is Phase 4's job (Box2D re-sync), not Phase 1's — recorded
here so Phase 4 doesn't have to re-discover them; not blocking.

## Exit criteria check

- [x] Clean `./gradlew assembleDebug` (whole project) — passes.
- [x] Native `.so` size / build time recorded and compared (above).
- [x] Re-verified on real arm64 hardware after the NDK/dead-code changes —
      full 10-stage Box2D Demo + 4-lesson Raindrop Demo re-run against the
      final (post-`AndroidGL20.cpp`-fix) build, matching Phase 0's
      validation depth: 14/14 pass, zero crashes.
- [x] User confirmed the 3 shipped games (Flappy Bird, Battle City, Mario)
      work fine on-device against this exact final build.

## Full re-validation (2026-09-13, after the `AndroidGL20.cpp` fix)

Re-ran the complete Phase 0 validation depth against this corrected Phase 1
build — all 10 Box2D Demo stages and all 4 Raindrop Demo lessons, each via
clean process launch (temporarily re-exporting the relevant activities for
scripted `adb` access, reverted immediately after, confirmed via `git diff`
showing only the intended manifest activities left at `exported="false"`).

**Result: 14/14 pass, zero crashes, zero entries in the crash log buffer.**

The 3 shipped games (Flappy Bird, Battle City, Mario) were re-confirmed
working on-device by the user against this exact final build (post
`AndroidGL20.cpp` fix). **Phase 1 is fully closed**: all 3 games + all 14
tutorial-fixture entry points pass against the final build.

## Still open (carried into later phases, not blocking Phase 1 close)

- [ ] Consider whether `nm -D`/symbol-export auditing should become a
      standing pre-deletion check for any future native-code removal in this
      module, given how this phase's dead-code claim turned out to be wrong.
- [ ] Phase 3 planning should lead with 3.0 (removing `GL20`'s per-call JNI
      overhead) — see the update to `docs/GAMEENGINE_UPGRADE_PLAN.md`'s
      Phase 3 section.
