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

## AdMob policy-remediation integration (November 2026)

Current AdMob policy centre reports **Restricted ad serving**, with “Google-served ads on screens without publisher-content” and “Site Behavior: Navigation”. An app update and approved AdMob review are required; a successful build alone does not remove a restriction.

Production ad units confirmed from the account screenshots:

- Banner: `ca-app-pub-7008705915557363/5695932384`
- Interstitial: `ca-app-pub-7008705915557363/8130524035` (NOT REQUESTED during remediation)
- Native: `ca-app-pub-7008705915557363/5999184710` (NOT REQUESTED during remediation)
- Rewarded: `ca-app-pub-7008705915557363/8974043868` (NOT REQUESTED during remediation)

The **App ID** (tilde `~`, not slash `/`) is missing and MUST come from AdMob → Apps → Privacy Needle → App settings. To enable production ads add to your **local** `~/.gradle/gradle.properties`:

```properties
PRIVACY_NEEDLE_ADMOB_APP_ID=ca-app-pub-7008705915557363~YOUR_ACTUAL_10_DIGIT_APP_SUFFIX
```

Replace the example with the exact App ID from Google. This value isn't committed. With no valid App ID, the release build shows **no ads**. Debug builds always use Google's sample App ID and test banner unit. Never test by clicking live ads.

The feed only adds **one clearly labelled adaptive banner between real articles** if seven or more WordPress posts loaded and UMP permits advertising. Ads never cover navigation or appear on empty/loading screens, splash, onboarding, article WebViews, quiz or video player. We intentionally keep interstitial/native/rewarded requests off until the policy restriction is resolved and a separate placement audit is completed. Google controls approval and fill; no revenue is guaranteed.

From AdMob → Privacy & messaging, create/verify the applicable GDPR and US-state consent messages; UMP refreshes consent status on each launch and exposes a Privacy choices menu item when required. Update the website/app privacy notice to reflect current SDKs and consent management and complete Google Play Data safety accurately.

## YouTube videos

The Videos screen reads the **public YouTube channel RSS** without requiring a YouTube Data API key, and plays selected videos through the official embedded YouTube player. The publisher supplied the channel ID `UCo03u7ZOc9bYFUhPAJGf8TQ` and it is configured in `app/src/main/res/values/strings.xml`, enabling the Videos tab. The feed loads public videos from `https://www.youtube.com/feeds/videos.xml?channel_id=UCo03u7ZOc9bYFUhPAJGf8TQ`. Channel content and real-device playback must still be verified before release. No AdMob ads are shown over YouTube playback. Videos can also be opened on YouTube using the official link.


## All-format advertising configuration

The full Android ad stack now includes:
- Native (production `ca-app-pub-7008705915557363/5999184710`): one unique native ad after every **3 published WordPress articles**, up to **8 native ads** in a reading session. Each has visible "ADVERTISEMENT" labelling and SDK-managed click targets. Never on search or blank feeds; seven genuine posts must load first.
- Adaptive banner (`ca-app-pub-7008705915557363/5695932384`): one in the news feed after five articles and one in the video library after five public video cards (a different Activity).
- Interstitial (`ca-app-pub-7008705915557363/8130524035`): preloaded, displayed only between completing the quiz and choosing "See today's quiz summary"; one per QuizActivity and a **20-minute cooldown** across sessions. No interruption when tapping Back, Home, Search, or an article.
- Rewarded (`ca-app-pub-7008705915557363/8974043868`): explicit opt-in after three free quiz questions, granting precisely one additional question on the reward callback.

Debug builds use only Google's demonstration ad IDs. Production builds use the captured real AdMob App ID. All four formats are enabled when compiled as a release, but individual formats can be disabled in `~/.gradle/gradle.properties` with `PRIVACY_NEEDLE_DISABLE_NATIVE_ADS=true`, `PRIVACY_NEEDLE_DISABLE_INTERSTITIAL_ADS=true` or `PRIVACY_NEEDLE_DISABLE_REWARDED_ADS=true` (as needed).

Ad fill and revenue depend on inventory, policy status, traffic quality, consent, device country and competition. **Existing account restrictions are not fixed by code changes alone.** Finish compliance checks, update the Google Play bundle version code, verify the AdMob app-ads.txt listing, and request a review in AdMob when eligible.

The YouTube player now embeds videos directly within the app (with a proper referrer context and fullscreen support). Individual videos may block embedding; the "Watch on YouTube" button remains available.
