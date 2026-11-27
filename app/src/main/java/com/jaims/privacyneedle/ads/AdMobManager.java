package com.jaims.privacyneedle.ads;

import android.app.Activity;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.nativead.NativeAd;
import java.util.List;
import java.util.ArrayList;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.jaims.privacyneedle.BuildConfig;

/**
 * Consent-gated, content-first advertising. No ads on splash, onboarding, empty feeds,
 * menu-only screens, the video player, or inside a WordPress WebView.
 * Full-screen formats are controlled separately by explicit quiz transitions.
 */
public final class AdMobManager {
    private static final String TEST_BANNER = "ca-app-pub-3940256099942544/9214589741";
    private static final String LIVE_BANNER = "ca-app-pub-7008705915557363/5695932384";

    private static final String TEST_NATIVE = "ca-app-pub-3940256099942544/2247696110";
    private static final String LIVE_NATIVE = "ca-app-pub-7008705915557363/5999184710";

    private final Activity activity;
    private final ConsentInformation consentInfo;
    private AdView banner;
    private AdView topBanner;
    private boolean topBannerLoading;
    private final List<NativeAd> nativeAds = new ArrayList<>();
    private AdLoader nativeLoader;
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

    /** Dedicated top-of-news banner, separate from the in-feed banner AdView. */
    public void loadTopBanner(Runnable onLoaded) {
        if (!canLoadAds() || topBannerLoading || topBanner != null) {
            if (!closed && topBanner != null) onLoaded.run();
            return;
        }
        topBannerLoading = true;
        AdView ad = new AdView(activity);
        ad.setAdUnitId(BuildConfig.DEBUG ? TEST_BANNER : LIVE_BANNER);
        float density = activity.getResources().getDisplayMetrics().density;
        int width = (int) (activity.getResources().getDisplayMetrics().widthPixels / density);
        ad.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                activity, Math.max(1, width - 32)));
        ad.setAdListener(new AdListener() {
            @Override public void onAdLoaded() {
                topBannerLoading = false;
                if (closed) {
                    ad.destroy();
                    return;
                }
                topBanner = ad;
                onLoaded.run();
            }
            @Override public void onAdFailedToLoad(LoadAdError error) {
                topBannerLoading = false;
                ad.destroy();
            }
        });
        ad.loadAd(new AdRequest.Builder().build());
    }

    public AdView getLoadedTopBanner() {
        return topBanner;
    }

    /** Two distinct native ads in a sufficiently populated, explanatory product guide. */
    public void loadProductNativeAds(int productCount, Runnable onUpdated) {
        loadNativeAds(AdPlacementPolicy.desiredProductNativeAds(productCount), onUpdated);
    }

    /** One additional native placement on a completed game-result page. */
    public void loadGameResultNativeAd(Runnable onUpdated) {
        loadNativeAds(1, onUpdated);
    }

    public void loadFeedNativeAds(int articleCount, Runnable onUpdated) {
        loadNativeAds(AdPlacementPolicy.desiredNativeAds(articleCount, false), onUpdated);
    }

    /** Load distinct native ads for a catalogue with at least six genuine videos. */
    public void loadVideoNativeAds(int videoCount, Runnable onUpdated) {
        loadNativeAds(AdPlacementPolicy.desiredVideoNativeAds(videoCount), onUpdated);
    }

    private void loadNativeAds(int desired, Runnable onUpdated) {
        if (!BuildConfig.ADMOB_NATIVE_ENABLED || !canLoadAds() || closed || desired <= 0) return;
        if (nativeAds.size() >= desired || (nativeLoader != null && nativeLoader.isLoading())) {
            return;
        }
        int count = Math.min(5, desired - nativeAds.size());
        nativeLoader = new AdLoader.Builder(activity,
                BuildConfig.DEBUG ? TEST_NATIVE : LIVE_NATIVE)
                .forNativeAd(ad -> {
                    if (closed) {
                        ad.destroy();
                        return;
                    }
                    nativeAds.add(ad);
                    activity.runOnUiThread(() -> {
                        if (!closed) onUpdated.run();
                    });
                })
                .withAdListener(new AdListener() {
                    @Override public void onAdFailedToLoad(LoadAdError error) {
                        // Do not insert empty placeholders; allow a retry on later content loading.
                    }
                })
                .build();
        nativeLoader.loadAds(new AdRequest.Builder().build(), count);
    }

    public List<NativeAd> getLoadedNativeAds() {
        return new ArrayList<>(nativeAds);
    }

    public boolean canLoadAds() {
        return sdkReady && !closed && consentInfo.canRequestAds();
    }

    public AdView getLoadedBanner() {
        return banner;
    }

    public void destroy() {
        closed = true;
        for (NativeAd ad : nativeAds) ad.destroy();
        nativeAds.clear();
        if (topBanner != null) {
            if (topBanner.getParent() instanceof ViewGroup) {
                ((ViewGroup) topBanner.getParent()).removeView(topBanner);
            }
            topBanner.destroy();
            topBanner = null;
        }
        if (banner != null) {
            if (banner.getParent() instanceof ViewGroup) {
                ((ViewGroup) banner.getParent()).removeView(banner);
            }
            banner.destroy();
            banner = null;
        }
    }
}
