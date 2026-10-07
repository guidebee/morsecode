# AdMob banners

Bottom banners appear on Koch Trainer, Send Practice, Transmit, Receive,
Flashcards, and Settings. Home, Decoder, Content Library, Handbook, and
onboarding have no banner. Mario is hidden from Home and has no ads.

Game positions were checked against commit `3ec5b31` (2023-11-04):

- BattleCity: `RelativeLayout.ALIGN_PARENT_TOP` (the old comment incorrectly said bottom).
- Flappy Bird: `RelativeLayout.ALIGN_PARENT_BOTTOM`, with menu show/hide hooks.

Games use standard banners overlaid at those same edges. The game view fills
the screen behind the banner; loading, showing, or hiding an ad does not resize
or shift the game. Other screens use adaptive bottom banners.

## IDs and builds

Debug and release builds use the real app and banner IDs configured in the
repository's `gradle.properties`. Production requests are enabled after the
consent check. If the app ID property is cleared, requests are disabled and the
manifest uses a sample app ID so the app can still launch.

Configured IDs (override in user Gradle properties or with `-P` arguments):

```properties
admobAppId=ca-app-pub-1370558989807131~3437129600
admobBannerId=ca-app-pub-1370558989807131/4913862806
```

The app ID with a tilde is a separate value from the banner unit ID with a slash.
No production app ID was present in the 2023 source.

For testing, explicitly pass `-PadmobTestAds=true` to use Google's sample app
and banner IDs in either build type.

Production requests wait for UMP consent information and any required form
before Mobile Ads initializes. Configure privacy messages in the AdMob console.
Settings shows an ad privacy choices button when UMP requires one. Changing
choices recreates the Activity, releasing existing ads before rechecking consent.
Test-ID builds bypass UMP because the sample app is not associated with your
console privacy messages.

Mobile Ads 23.6.0 retains the app's minSdk 21 support. Google's newer 24.x/25.x
releases require a higher minimum Android API; upgrade the SDK when that minimum
can change. UMP uses 3.2.0 to retain minSdk 21 as well.

References: [SDK setup](https://developers.google.com/admob/android/quick-start),
[release notes](https://developers.google.com/admob/android/rel-notes),
[banners](https://developers.google.com/admob/android/banner),
[consent](https://developers.google.com/admob/android/privacy).
