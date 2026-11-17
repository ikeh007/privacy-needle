package com.jaims.privacyneedle.ads;

import android.app.Activity;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.jaims.privacyneedle.BuildConfig;

/**
 * Preloaded full-screen ad only between a completed quiz and its results summary.
 * 20-minute cooldown across sessions, once per QuizActivity, no navigation-triggered ads.
 */
public final class InterstitialQuizAdManager {
    private static final String TEST_ID = "ca-app-pub-3940256099942544/1033173712";
    private static final String PRODUCTION_ID = "ca-app-pub-7008705915557363/8130524035";
    private static final String PREFS = "privacy_needle_ad_frequency";
    private static final String KEY_LAST = "last_interstitial_millis";
    private static final long COOLDOWN_MS = 20L * 60 * 1000;

    private final Activity activity;
    private final AdMobManager consentManager;
    private InterstitialAd loadedAd;
    private boolean closed;
    private boolean shownThisSession;

    public InterstitialQuizAdManager(Activity activity) {
        this.activity = activity;
        this.consentManager = new AdMobManager(activity);
    }

    private boolean cooledDown() {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
        long last = prefs.getLong(KEY_LAST, 0L);
        return last == 0L || System.currentTimeMillis() - last >= COOLDOWN_MS;
    }

    public void prepare() {
        if (closed || !BuildConfig.ADMOB_INTERSTITIAL_ENABLED || !cooledDown()) return;
        consentManager.requestConsentAndInitialize(() -> {
            if (closed || activity.isFinishing() || activity.isDestroyed()
                    || !consentManager.canLoadAds()) return;
            InterstitialAd.load(activity,
                    BuildConfig.DEBUG ? TEST_ID : PRODUCTION_ID,
                    new AdRequest.Builder().build(),
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd ad) {
                            if (!closed) loadedAd = ad;
                        }
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError error) {
                            loadedAd = null;
                        }
                    });
        });
    }

    public void showAtNaturalBreak(Runnable afterAd) {
        if (closed || loadedAd == null || !cooledDown() || shownThisSession
                || activity.isFinishing() || activity.isDestroyed()) {
            afterAd.run();
            return;
        }

        InterstitialAd ad = loadedAd;
        loadedAd = null;
        shownThisSession = true;
        final boolean[] completed = {false};
        Runnable finish = () -> {
            if (!completed[0]) {
                completed[0] = true;
                afterAd.run();
            }
        };
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE).edit()
                        .putLong(KEY_LAST, System.currentTimeMillis()).apply();
            }
            @Override
            public void onAdDismissedFullScreenContent() {
                finish.run();
            }
            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError error) {
                finish.run();
            }
        });
        ad.show(activity);
    }

    public void destroy() {
        closed = true;
        loadedAd = null;
        consentManager.destroy();
    }
}
