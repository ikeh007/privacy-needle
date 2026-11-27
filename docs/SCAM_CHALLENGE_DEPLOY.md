# Privacy Needle Scam of the Day: installable app + exact-match friend links

## What is implemented in Android

The daily challenge uses a 30-second monotonic timer and a deterministic `YYYYMMDD` UTC date. The date selects a fixed, fictional phishing/social-engineering scenario from a 16-scenario catalogue. Every friend who opens the **same link**, including later, plays the **same scenario**. No sensitive user messages or contacts are uploaded.

Scoring: correct answer = 100 plus 2 points per whole second remaining (100–160); wrong or timeout = zero. Score comparison is based on the score **claimed in the shared URL**, not a verified leaderboard; users may change URL parameters. The actual first daily attempt is saved locally, with later replays treated as practice.

The share card is a portrait PNG with a live **QR code** containing the exact same dated URL. Android share text includes both the URL and a Google Play install link. Some social apps strip image captions, which is why the QR matters.

The challenge is never blocked by an advertisement. A labelled adaptive AdMob banner is eligible only after the full result and detailed safety explanation; an optional rewarded ad can unlock one additional practice scenario per day. Debug builds use Google's sample ads. Google may serve no ads due to policy or inventory; earnings are not guaranteed.

## CRITICAL: Deploy the web landing page and verify Android App Links

**Android repo only does not publish website files.** The friend link will only work end-to-end once the site is configured:

1. Copy `web/challenge/index.html` to `https://play.privacyneedle.com/challenge/index.html` via the hosting/web deployment system serving the **play** subdomain. Ensure a GET to `https://play.privacyneedle.com/challenge/?day=20261127&score=130` returns the web game (HTTP 200). The web game shares the exact ordered scenario catalogue and algorithm as Android.
2. In Google Play Console → **App integrity** → App signing, find the **App signing certificate SHA-256 fingerprint**. This is **NOT** an AdMob App ID, package name, Firebase SHA or secret key.
3. Replace the placeholder in `web/assetlinks.json.template` with the real signing certificate SHA-256. Publish the file as `https://play.privacyneedle.com/.well-known/assetlinks.json`, with an HTTP 200 JSON response, no redirect and correct `application/json` content type. **Do not publish the template placeholder**.
4. Install the production-signed Android app and verify links on the device with `adb shell pm get-app-links com.jaims.privacyneedle`. Verify by tapping a link in a messaging app, and by trying it on a device without the app. Debug builds have a different certificate SHA-256 and may not verify without an additional debug fingerprint, so use a direct ADB intent to test the activity until verified:
   `adb shell am start -a android.intent.action.VIEW -d "https://play.privacyneedle.com/challenge/?day=20261127&score=130" -n com.jaims.privacyneedle/com.jaims.privacyneedle.scam.ScamChallengeActivity`
5. Test links forwarded via WhatsApp, Instagram, SMS and browsers on Android, iOS and desktop. When the app is **not** installed, the website lets the recipient play the same scenario and optionally install Privacy Needle; installation does not automatically carry the web game state across devices.

The verified App Link requirement is documented by Android: https://developer.android.com/training/app-links/about

## QA checklist

- One day maps to the same scenario in native app and website, including leap days and cross-time-zone shares
- Correct answer with 30 s left earns 160; incorrect or expired timer earns 0
- Leaving app or rotating screen cannot restart an active timed question
- Friend comparison labels shared scores as user-claimed (not globally verified)
- Native result details and full educational explanation display even when AdMob cannot fill
- QR code scans and opens the EXACT dated challenge link; forwarded image remains legible on WhatsApp / Instagram
- Bonus practice requires an explicit completed rewarded-ad callback and does not gate daily play
- No ads over answer buttons or the timed question; banner only after result
- No user authentication, analytics PII, contact upload, or leaderboard required
- Splash motion is short, reduces when Android animations are off, and never shows AdMob content
- Device startup, first-run onboarding, logged-in/out websites, text scaling, screen rotation and Back behaviour should be tested before release

## Maintenance

To add or change scam scenarios, update `ScamScenarioCatalog.java` **and** the matching ordered `scenarios` array in `web/challenge/index.html` in the same release. **Do not reorder or delete existing entries**: date-to-scenario mapping uses modulo the list size, so changing its length changes old challenge links. Long term, pin a catalogue version in URLs before adding entries.

If a real company or government body is mentioned in a scenario, independently verify its current policy; all current examples are fictional and educational.
