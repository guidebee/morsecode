# Morse Code Toolkit

An Android app for learning and practicing Morse code: transmit letters/words/free text as light, sound or vibration, receive and decode practice sessions, listen to and decode real Morse audio through the microphone, review flashcards and a reference handbook, and play two arcade mini-games (Flappy Bird, Battle City) built on an in-tree Morse-controlled game engine.

## Features

- **Transmit** — encode letters, words or free text into Morse (flash/tone/vibration).
- **Receive** — practice decoding letters, words or free text.
- **Decoder** — listens through the device microphone and decodes live Morse audio in real time, with a choice of two detection modes (see below).
- **Flashcards** — quick drill through the Morse alphabet.
- **Handbook** — reference material.
- **Options** — configure transmit/receive modes and app behavior.
- **Games** — Flappy Bird and Battle City, controlled via Morse input, running on a bundled 2D/OpenGL game engine.

## Project structure

- `app/` — the Android application (Compose UI, transmit/receive/decoder screens, navigation drawer, ViewModels).
- `decoder/` — a pure-JVM (no Android dependencies) library module holding the audio-to-Morse decoding core: the timing state machine, the broadband and narrowband tone detectors, and the shared Morse lookup tables. Extracted from `app/` so the decoding logic can be unit tested with plain JUnit and reasoned about independently of Android. `app/` depends on it for the actual mic-capture/UI plumbing.
- `gameengine/` — an in-tree Android library module containing the Guidebee Game Engine (Java game framework + JNI/OpenGL ES 2.0 + Box2D native code, built via `ndkBuild`). The two games in `app/` depend on this module directly; there is no external `game-engine` artifact.

### Decoder architecture

- `DecoderViewModel` (`app/`) owns the mic-capture lifecycle, UI state and settings persistence, surviving rotation/navigation; `DecoderScreen` is a thin Compose observer of its `StateFlow`.
- `AudioDecoderController` (`app/`) wraps `AudioRecord`, exposing capture as a suspend function driven by a coroutine (`viewModelScope.launch { controller.capture() }`) rather than a raw `Thread`.
- `AudioMorseCodeDecoder` (`decoder/`) supports two `DetectionMode`s, switchable from the Decoder screen and persisted across launches:
  - **Classic (broadband)** — the original, simpler magnitude-threshold detector; the default, and generally the more robust of the two.
  - **Narrowband** — a Goertzel-based single-tone detector with a self-adjusting decaying-peak-follower threshold; more selective against background noise, but currently a secondary option to Classic.
- `MorseCodePatternMatch` (`decoder/`) is the dot/dash/letter/word timing state machine. It adapts to the sender's actual WPM using a median of recent dot/dash lengths (rather than reacting to any single element), which bounds how much a single noisy or atypical element can swing calibration.

## Requirements

- JDK 17+
- Android SDK with platform/build-tools for API level 37 installed
- Android NDK `21.4.7075529` (pinned in `gameengine/build.gradle` for the native engine build)

## Building

```
./gradlew assembleDebug      # build the debug APK
./gradlew installDebug       # build and install on a connected device/emulator
./gradlew :decoder:test      # run the decoder's plain-JUnit test suite (no emulator needed)
./gradlew :app:testDebugUnitTest  # run app-level unit tests, incl. decoding the bundled sample wav
```

Gradle wrapper is pinned to Gradle 9.7.1; the Android Gradle Plugin version is set in the root `build.gradle`.

## Notable configuration

- `minSdk 21`, `compileSdk`/`targetSdk 37`.
- `RECORD_AUDIO` permission is required for the Decoder screen.
- The `gameengine` module builds its native library via `externalNativeBuild { ndkBuild { ... } }` pointing at `gameengine/src/main/jni/Android.mk`; no manual native build step is needed — Gradle invokes `ndkBuild` automatically as part of the normal build.
- Native game-engine libraries are linked with 16 KB ELF LOAD-segment alignment for compatibility with Android devices using 16 KB memory pages.
