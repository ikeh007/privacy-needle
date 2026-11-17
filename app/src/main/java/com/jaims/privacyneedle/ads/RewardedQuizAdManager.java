package com.jaims.privacyneedle.ads;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.jaims.privacyneedle.BuildConfig;

/**
 * One user-initiated, rewarded ad for one bonus quiz question.
 * No interstitials on navigation, and no rewards for mere clicks or incomplete views.
 */
public final class RewardedQuizAdManager {
    private static final String TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917";
    private static final String LIVE_REWARDED = "ca-app-pub-7008705915557363/8974043868";

    public interface ReadyCallback {
        void onReady(boolean ready);
    }

    private final Activity activity;
    private final AdMobManager consentGate;
    private RewardedAd rewardedAd;
    private boolean destroyed;
    private boolean consumed;

    public RewardedQuizAdManager(Activity activity) {
        this.activity = activity;
        consentGate = new AdMobManager(activity);
    }

    public boolean isReady() {
        return !destroyed && !consumed && rewardedAd != null;
    }

    public void prepare(ReadyCallback callback) {
        if (destroyed || !BuildConfig.ADMOB_REWARDED_ENABLED) {
            callback.onReady(false);
            return;
        }
        consentGate.requestConsentAndInitialize(() -> {
            if (destroyed || activity.isFinishing() || activity.isDestroyed()
                    || !consentGate.canLoadAds()) {
                callback.onReady(false);
                return;
            }
            RewardedAd.load(activity,
                    BuildConfig.DEBUG ? TEST_REWARDED : LIVE_REWARDED,
                    new AdRequest.Builder().build(),
                    new RewardedAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull RewardedAd ad) {
                            if (destroyed) return;
                            rewardedAd = ad;
                            callback.onReady(true);
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError error) {
                            rewardedAd = null;
                            if (!destroyed) callback.onReady(false);
                        }
                    });
        });
    }

    public void showReward(Runnable grantReward, Runnable afterDismissal) {
        if (!isReady() || activity.isFinishing() || activity.isDestroyed()) {
            afterDismissal.run();
            return;
        }

        RewardedAd ad = rewardedAd;
        rewardedAd = null; // Prevent accidental replay of the same ad.
        consumed = true;
        final boolean[] earned = {false};
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                if (!destroyed && !earned[0]) afterDismissal.run();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError error) {
                if (!destroyed) afterDismissal.run();
            }
        });

        ad.show(activity, rewardItem -> {
            if (!destroyed && !earned[0]) {
                earned[0] = true;
                grantReward.run();
            }
        });
    }

    public void destroy() {
        destroyed = true;
        rewardedAd = null;
        consentGate.destroy();
    }
}
