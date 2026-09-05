# Morse Code Toolkit

An Android app for learning and practicing Morse code: transmit letters/words/free text as light, sound or vibration, receive and decode practice sessions, listen to and decode real Morse audio through the microphone, review flashcards and a reference handbook, and play two arcade mini-games (Flappy Bird, Battle City) built on an in-tree Morse-controlled game engine.

## Features

- **Transmit** — encode letters, words or free text into Morse (flash/tone/vibration).
- **Receive** — practice decoding letters, words or free text.
- **Decoder** — listens through the device microphone and decodes live Morse audio in real time.
- **Flashcards** — quick drill through the Morse alphabet.
- **Handbook** — reference material.
- **Options** — configure transmit/receive modes and app behavior.
- **Games** — Flappy Bird and Battle City, controlled via Morse input, running on a bundled 2D/OpenGL game engine.

## Project structure

- `app/` — the Android application (UI, transmit/receive/decoder screens, navigation drawer).
- `gameengine/` — an in-tree Android library module containing the Guidebee Game Engine (Java game framework + JNI/OpenGL ES 2.0 + Box2D native code, built via `ndkBuild`). The two games in `app/` depend on this module directly; there is no external `game-engine` artifact.

## Requirements

- JDK 17+
- Android SDK with platform/build-tools for API level 37 installed
- Android NDK `21.4.7075529` (pinned in `gameengine/build.gradle` for the native engine build)

## Building

```
./gradlew assembleDebug      # build the debug APK
./gradlew installDebug       # build and install on a connected device/emulator
```

Gradle wrapper is pinned to Gradle 9.7.1; the Android Gradle Plugin version is set in the root `build.gradle`.

## Notable configuration

- `minSdk 21`, `compileSdk`/`targetSdk 37`.
- `RECORD_AUDIO` permission is required for the Decoder screen.
- The `gameengine` module builds its native library via `externalNativeBuild { ndkBuild { ... } }` pointing at `gameengine/src/main/jni/Android.mk`; no manual native build step is needed — Gradle invokes `ndkBuild` automatically as part of the normal build.
