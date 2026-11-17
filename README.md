# Privacy Needle Android

Native Java Android app for [Privacy Needle](https://privacyneedle.com).

## Features

- WordPress news feed, category browsing, searching and paginated article lists
- In-app article viewing with a clear offline fallback
- Daily privacy quiz (three answers per calendar day)
- Firebase Cloud Messaging push notifications and quiz reminders

## Build

1. Open the project in Android Studio with JDK 17.
2. Install Android SDK Platform 36 and sync the Gradle project.
3. Use the checked-in `app/google-services.json` configuration for the existing Firebase project (replace it only if you own a different project).
4. Build with `./gradlew :app:assembleDebug`.

## Push notifications

The app requests `POST_NOTIFICATIONS` on Android 13 or newer. A push payload may supply `title`, `body`, `image`, and an HTTPS `url` on privacyneedle.com. Unknown URLs open the main screen rather than an unrestricted WebView.

## AdMob

**AdMob is not yet integrated.** No production AdMob App ID or ad-unit IDs are present in this repository. Website AdSense codes cannot be substituted for Android AdMob IDs. When adding AdMob, use your own issued IDs and follow Google's testing and consent policies. Do not commit account secrets.

## Testing

Run `./gradlew :app:assembleDebug :app:testDebugUnitTest`. The GitHub Actions workflow runs this on pull requests. Also test on an Android 13+ device or emulator: fresh install, notification permission, several notification taps, tab switching, search and pagination, offline article view, and daily quiz limit.
