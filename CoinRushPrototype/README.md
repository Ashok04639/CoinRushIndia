# Coin Rush India — Android prototype

A simple playable 30-second tap game with AdMob Rewarded and Interstitial integration.

## AdMob IDs configured
- App ID: ca-app-pub-459015901387755~6817704062
- Rewarded: ca-app-pub-459015901387755/5227139421
- Interstitial: ca-app-pub-459015901387755/9228973931

## Important for testing
`AdConfig.java` currently has `USE_TEST_ADS = true` so the prototype uses Google's test ad units while you test the game. Do not repeatedly click your own live ads. Change it to `false` only when you are ready for a properly configured release build.

## Build
Open this folder in Android Studio. Let Gradle sync, then Run on an Android device/emulator.

Package/application ID: `com.coinrushindia.game`
