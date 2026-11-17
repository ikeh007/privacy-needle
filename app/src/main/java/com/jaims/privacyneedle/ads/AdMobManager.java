package com.jaims.privacyneedle.ads;

import android.app.Activity;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.jaims.privacyneedle.BuildConfig;

/**
 * Consent-gated, content-first advertising. No ads on splash, onboarding, empty feeds,
 * menu-only screens, the quiz, video player, or inside a WordPress WebView.
 * No interstitial or rewarded ad is requested while the policy restriction is unresolved.
 */
public final class AdMobManager {
    private static final String TEST_BANNER = "ca-app-pub-3940256099942544/9214589741";
    private static final String LIVE_BANNER = "ca-app-pub-7008705915557363/5695932384";

    // Verified from AdMob, reserved for future compliant features and NOT loaded here:
    // Interstitial: ca-app-pub-7008705915557363/8130524035
    // Native:       ca-app-pub-7008705915557363/5999184710
    // Rewarded:     ca-app-pub-7008705915557363/8974043868

    private final Activity activity;
    private final ConsentInformation consentInfo;
    private AdView banner;
    private boolean sdkReady;
    private boolean bannerLoading;
    private boolean closed;

    public AdMobManager(Activity activity) {
        this.activity = activity;
        this.consentInfo = UserMessagingPlatform.getConsentInformation(activity);
    }

    public static boolean isConfiguredForThisBuild() {
        return BuildConfig.DEBUG || BuildConfig.ADMOB_LIVE_ENABLED;
    }

    public boolean privacyChoicesRequired() {
        return consentInfo.getPrivacyOptionsRequirementStatus()
                == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
    }

    public void showPrivacyChoices(Runnable onFinished) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, error -> {
            if (!closed) onFinished.run();
        });
    }

    public void requestConsentAndInitialize(Runnable onFinished) {
        if (!isConfiguredForThisBuild()) {
            onFinished.run();
            return;
        }
        ConsentRequestParameters parameters = new ConsentRequestParameters.Builder().build();
        // Do not rely on old cached consent strings: ask UMP on every app launch.
        consentInfo.requestConsentInfoUpdate(activity, parameters,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity,
                        error -> prepareIfAllowed(onFinished)),
                error -> prepareIfAllowed(onFinished));
    }

    private void prepareIfAllowed(Runnable onFinished) {
        if (closed) return;
        if (!consentInfo.canRequestAds()) {
            onFinished.run();
            return;
        }
        if (sdkReady) {
            onFinished.run();
            return;
        }
        new Thread(() -> MobileAds.initialize(activity.getApplicationContext(), status -> {
            activity.runOnUiThread(() -> {
                if (closed) return;
                sdkReady = true;
                onFinished.run();
            });
        })).start();
    }

    public void loadFeedBanner(Runnable onLoaded) {
        if (!sdkReady || bannerLoading || banner != null || closed) {
            if (banner != null && !closed) onLoaded.run();
            return;
        }
        bannerLoading = true;
        AdView adView = new AdView(activity);
        adView.setAdUnitId(BuildConfig.DEBUG ? TEST_BANNER : LIVE_BANNER);
        float density = activity.getResources().getDisplayMetrics().density;
        int screenWidthDp = (int) (activity.getResources().getDisplayMetrics().widthPixels / density);
        adView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                activity, Math.max(1, screenWidthDp - 32)));
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                bannerLoading = false;
                if (closed) {
                    adView.destroy();
                    return;
                }
                banner = adView;
                onLoaded.run();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError error) {
                bannerLoading = false;
                adView.destroy();
                // No ad placeholder is displayed when inventory is unavailable.
            }
        });
        adView.loadAd(new AdRequest.Builder().build());
    }

    public boolean canLoadAds() {
        return sdkReady && !closed && consentInfo.canRequestAds();
    }

    public AdView getLoadedBanner() {
        return banner;
    }

    public void destroy() {
        closed = true;
        if (banner != null) {
            if (banner.getParent() instanceof ViewGroup) {
                ((ViewGroup) banner.getParent()).removeView(banner);
            }
            banner.destroy();
            banner = null;
        }
    }
}
