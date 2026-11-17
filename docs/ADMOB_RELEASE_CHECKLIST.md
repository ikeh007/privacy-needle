# Privacy Needle Android | AdMob remediation and production release checklist

## Findings from the AdMob screenshots (17 November 2026)
- App: Privacy Needle: GDPR & NDPA, Android (`com.jaims.privacyneedle`)
- Ad serving status: **Restricted ad serving**
- Google-reported issues dated **1 November 2026**:
  - **Google-served ads on screens without publisher-content**
  - **Site Behavior: Navigation**
- The reviewed screenshot showed a blank content area and a test banner close to bottom navigation. The then-published Play build is **not** the current source on this feature branch.

## Remediation in the app code
- A loaded WordPress feed must contain **7+ genuine articles** before the one labelled inline adaptive banner is eligible.
- Banner appears after the first **5** articles. It never overlaps navigation, quiz buttons, or article controls.
- No ads in empty/error/loading states, splash, welcome, search, quiz, YouTube/video player, or WordPress WebView.
- Dummy **Videos** and **Settings** navigation are removed/replaced: videos require a real YouTube channel; settings links to the actual privacy policy and UMP privacy options where required.
- Consent: UMP requests fresh consent status every app launch and loads ads only if `canRequestAds()` permits.
- Interstitial, Native and Rewarded units are **not requested** during restriction remediation.

## Your production AdMob ad units (verified from screenshot)
| Placement | ID | This release |
|---|---|---|
| Banner | `ca-app-pub-7008705915557363/5695932384` | Enabled only after App ID + consent + actual content |
| Interstitial | `ca-app-pub-7008705915557363/8130524035` | Disabled |
| Native advanced | `ca-app-pub-7008705915557363/5999184710` | Disabled |
| Rewarded | `ca-app-pub-7008705915557363/8974043868` | Disabled |

## Required production App ID
Get the App ID from **AdMob → Apps → Privacy Needle → App settings**. It looks like
`ca-app-pub-7008705915557363~NNNNNNNNNN`. The suffix is **not** contained in the screenshots.

In your Mac terminal, add the actual full App ID to `~/.gradle/gradle.properties`, e.g.:

```properties
PRIVACY_NEEDLE_ADMOB_APP_ID=ca-app-pub-7008705915557363~REPLACE_WITH_ACTUAL_10_DIGITS
```

Build a release only after replacing this with the real numeric value. The repository deliberately contains only the sample App ID for test builds; without a valid production App ID, release ads are disabled.

## App-ads.txt (separate from AdSense ads.txt)
Check that your verified **developer website** in the Google Play listing points to `https://privacyneedle.com`, then make a publicly reachable text file at **https://privacyneedle.com/app-ads.txt** that contains your own AdMob seller entry, usually:

```text
google.com, pub-7008705915557363, DIRECT, f08c47fec0942fa0
```

Use the **personalized snippet** from AdMob if it differs. The Android GitHub repo does not publish WordPress website files, so this step cannot be completed by an Android code change.

## Before requesting a Google review
1. Build and install the Android release with the right App ID and a new Play `versionCode` (check current Play Console highest version first).
2. Test offline/error/retry screens: **zero ad requests** when there are no articles.
3. Test a populated feed: one clearly labelled banner appears only after the first five publisher articles; no accidental tap zone next to navigation.
4. Test the article reader, back navigation, search, quiz, and video player (no AdMob overlay).
5. Use test devices/ad units during development. Do **not** click live ads.
6. In AdMob → Privacy & messaging, verify GDPR and US-state messages where applicable; check privacy options entry and update the website privacy policy and Play Data safety details as appropriate.
7. Ensure your YouTube channel ID has been configured and the Videos tab displays real videos before enabling the tab.
8. Publish the corrected AAB to the Play Store. In AdMob Policy centre, select **Start review process** once the compliant version is live. Google alone decides whether to lift restrictions.

## Revenue optimization after policy resolution
- Improve real content quality and reader engagement first. Policy compliance and genuine traffic drive durable earnings.
- Monitor matched requests, impressions, fill rate, eCPM, CTR, country mix and retention in AdMob reports.
- Once approved, separately audit **native ads within long editorial feeds** and infrequent **interstitials at explicit natural breaks**. Never place forced rewarded ads, ads on empty screens, ads disguised as navigation, or app banners atop YouTube playback.
- No ad frequency, code change or optimization can guarantee revenue or an account approval.
