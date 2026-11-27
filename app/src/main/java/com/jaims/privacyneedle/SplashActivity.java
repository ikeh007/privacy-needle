package com.jaims.privacyneedle;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

/**
 * Native Android 12-compatible launch splash followed by a short branded
 * cinematic reveal. Respects reduced animations, avoids forced ads/delays,
 * and routes into the existing first-run welcome logic.
 */
public class SplashActivity extends AppCompatActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean navigated;
    private final Runnable goNext = () -> {
        if (navigated || isFinishing() || isDestroyed()) return;
        navigated = true;
        startActivity(new Intent(this, WelcomeActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    };

    @Override
    protected void onCreate(Bundle state) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(state);
        setContentView(R.layout.activity_splash);

        View ring = findViewById(R.id.splashRing);
        View core = findViewById(R.id.splashCore);
        View logo = findViewById(R.id.splashLogo);
        View brand = findViewById(R.id.splashBrand);
        View tagline = findViewById(R.id.splashTagline);
        View line = findViewById(R.id.splashLine);

        boolean animationsEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.O
                || ValueAnimator.areAnimatorsEnabled();
        if (!animationsEnabled) {
            handler.postDelayed(goNext, 300L);
            return;
        }

        logo.setAlpha(0f);
        logo.setScaleX(0.70f);
        logo.setScaleY(0.70f);
        core.setAlpha(0f);
        core.setScaleX(0.58f);
        core.setScaleY(0.58f);
        ring.setAlpha(0f);
        ring.setRotation(-38f);
        brand.setAlpha(0f);
        brand.setTranslationY(22f);
        tagline.setAlpha(0f);
        tagline.setTranslationY(17f);
        line.setScaleX(0.08f);
        line.setAlpha(0f);

        core.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(900L)
                .setInterpolator(new DecelerateInterpolator()).start();
        ring.animate().alpha(0.88f).rotation(0f).scaleX(1.04f).scaleY(1.04f)
                .setStartDelay(90L).setDuration(1500L)
                .setInterpolator(new DecelerateInterpolator()).start();
        logo.animate().alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(260L)
                .setDuration(850L).setInterpolator(new OvershootInterpolator(0.7f)).start();
        brand.animate().alpha(1f).translationY(0f).setStartDelay(600L)
                .setDuration(580L).start();
        tagline.animate().alpha(1f).translationY(0f).setStartDelay(930L)
                .setDuration(560L).start();
        line.animate().alpha(1f).scaleX(1f).setStartDelay(970L)
                .setDuration(750L).start();

        handler.postDelayed(goNext, 1950L);
    }

    @Override protected void onDestroy() {
        handler.removeCallbacks(goNext);
        super.onDestroy();
    }
}
