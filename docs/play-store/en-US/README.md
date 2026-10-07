# English Play Store listing

The separate Small Tablet set (without ads or game screens) is documented in
`TABLET_SCREENSHOTS.md` and previewed in `tablet-index.html`.

Copy `short-description.txt` into Short description and
`full-description.txt` into Full description. Open `index.html` to preview
the text and all eight phone screenshots.

Upload `screenshots/*.png` to Phone screenshots in filename order:

| Order | File | Suggested alt text |
| --- | --- | --- |
| 1 | 01-home.png | Home dashboard with practice progress and shortcuts to Morse learning tools. |
| 2 | 02-audio-decoder.png | Audio decoder with a live signal trace, speed readout, and narrowband detection controls. |
| 3 | 03-koch-trainer.png | Koch listening trainer with an on-screen Morse key and adjustable character speed and spacing. |
| 4 | 04-send-practice.png | Sending practice with the target CQ successfully keyed and decoded. |
| 5 | 05-transmit.png | Transmit word drill with a target word and on-screen Morse key. |
| 6 | 06-receive.png | Receive listening drill with replay, remaining tries, and an on-screen Morse key. |
| 7 | 07-flashcards.png | Morse alphabet flashcard with audio playback and navigation controls. |
| 8 | 08-content-library.png | Q-code reference with meanings, audio playback, and tabs for prosigns, callsigns, and QSOs. |

## Capture details

These are actual app screens captured on the connected Seeker phone. The
display was temporarily configured as 1080 x 1920 at 360 dpi. Android demo
mode provided a clean status bar. Only PNG encoding was converted to RGB;
the app interface was not retouched or generated.

The capture APK was built with `-PadmobAppId=` so unrelated advertising did
not appear. The repository retains the real AdMob IDs. These screenshots show
the app without a loaded banner. No training progress or decoded text was
inserted into the images; CQ was sent using the on-screen key. The audio
decoder capture shows the live signal while the bundled sample plays.

## Validation

`tools/play-store/package.py` checks the 80-character short-description and
4,000-character full-description limits and verifies eight 1080 x 1920 RGB
PNG files before creating the preview and ZIP archive. The screenshot sizes,
aspect ratio, and encoding follow
[Google's preview asset guidance](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en).
Text limits follow
[Google's listing setup guide](https://support.google.com/googleplay/android-developer/answer/9859152?hl=en).

To capture again, set `ANDROID_SERIAL` when multiple devices are connected,
then use `tools/play-store/capture.py` to inspect controls, tap a label, or
capture the current app screen. It requires Python with Pillow and ADB.

While preparing these captures, the shared Morse key handler was corrected
to ignore timer-generated idle spaces in drills. Send Practice still accepts
one word gap after actual keyed text. The debug build and existing unit tests
passed, and device checks confirmed idle time does not score answers and CQ
is decoded correctly.
