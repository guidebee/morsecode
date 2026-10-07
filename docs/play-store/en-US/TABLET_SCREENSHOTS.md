# Small Tablet screenshots

Eight screenshots captured directly from the connected `Small_Tablet` AVD
(`emulator-5554`) in its current portrait orientation. Each file is a
1200 x 1920 RGB PNG. Display size and density were not changed. The app uses
its light theme on this device. Android demo mode supplied the clean status
bar and was restored after capture.

The capture build used `-PadmobAppId=`; generated BuildConfig was checked to
confirm `ADMOB_CONFIGURED = false` before installation. No ads are requested.
The real app and banner IDs remain in the repository's `gradle.properties`
for normal builds.

No game screens are included. Home was also omitted because it contains
game shortcuts. No app UI or text was retouched. Send Practice shows RST
entered using the on-screen Morse key, rather than inserted text.

Upload the PNGs from `tablet-screenshots` to the 7-inch tablet screenshot
section in Play Console, in filename order. Use `tablet-index.html` to
preview the set, or extract `morse-code-toolkit-small-tablet-en-US.zip`.

| File | Suggested alt text |
| --- | --- |
| 01-koch-trainer.png | Koch Trainer with an on-screen key, listening controls, and adjustable character speed and spacing. |
| 02-send-practice.png | Sending practice with RST keyed correctly and decoded as a matching answer. |
| 03-transmit.png | Transmit word drill with a target word and on-screen Morse key. |
| 04-receive.png | Receive listening drill with replay and an on-screen Morse key. |
| 05-audio-decoder.png | Audio decoder panel with detection modes, signal display, microphone power, and playback controls. |
| 06-flashcards.png | Morse alphabet flashcard with audio playback and card navigation. |
| 07-content-library.png | Q-code reference entries with meanings, playback, and radio procedure tabs. |
| 08-handbook.png | International Morse Code handbook showing letters, numbers, patterns, and audio playback. |

The dimensions and RGB encoding meet the basic
[Play screenshot requirements](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en).
Google separately recommends a 9:16 portrait aspect ratio for large-screen
promotional formats; these captures preserve the tablet's actual 5:8 ratio.

Rebuild the package with `py -3 tools/play-store/package.py --tablet`.
