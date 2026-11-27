package com.jaims.privacyneedle.ads;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.jaims.privacyneedle.R;

import java.util.List;

/**
 * Native and adaptive banner placements on COMPLETED quiz/scam-game result
 * sections only. Both are hidden until an ad genuinely loads. No ad requests
 * during timed questions, splash, menus or transitions.
 */
public final class GameResultAds {
    private final Activity activity;
    private final AdMobManager manager;
    private final FrameLayout nativeSlot;
    private final View bannerWrapper;
    private final FrameLayout bannerSlot;
    private boolean started;
    private boolean destroyed;

    public GameResultAds(Activity activity, int nativeSlotId,
                         int bannerWrapperId, int bannerSlotId) {
        this.activity = activity;
        this.nativeSlot = activity.findViewById(nativeSlotId);
        this.bannerWrapper = activity.findViewById(bannerWrapperId);
        this.bannerSlot = activity.findViewById(bannerSlotId);
        this.manager = new AdMobManager(activity);
        nativeSlot.setVisibility(View.GONE);
        bannerWrapper.setVisibility(View.GONE);
    }

    /** Only call after game state confirms the result is fully available. */
    public void showCompletedResult() {
        if (started || destroyed || activity.isFinishing() || activity.isDestroyed()
                || !AdPlacementPolicy.mayShowGameResultsAds(true)) return;
        started = true;
        manager.requestConsentAndInitialize(() -> {
            if (!isActive() || !manager.canLoadAds()) return;
            manager.loadGameResultNativeAd(this::renderNative);
            manager.loadFeedBanner(this::renderBanner);
        });
    }

    private boolean isActive() {
        return !destroyed && !activity.isFinishing() && !activity.isDestroyed();
    }

    private void renderBanner() {
        if (!isActive()) return;
        AdView banner = manager.getLoadedBanner();
        if (banner == null) return;
        if (banner.getParent() instanceof ViewGroup) {
            ((ViewGroup) banner.getParent()).removeView(banner);
        }
        bannerSlot.removeAllViews();
        bannerSlot.addView(banner);
        bannerWrapper.setVisibility(View.VISIBLE);
    }

    private void renderNative() {
        if (!isActive()) return;
        List<NativeAd> loaded = manager.getLoadedNativeAds();
        if (loaded.isEmpty()) return;
        nativeSlot.removeAllViews();
        NativeAdView view = createNativeView(loaded.get(0));
        nativeSlot.addView(view);
        nativeSlot.setVisibility(View.VISIBLE);
    }

    private NativeAdView createNativeView(NativeAd ad) {
        NativeAdView view = (NativeAdView) LayoutInflater.from(activity)
                .inflate(R.layout.item_native_ad, nativeSlot, false);
        TextView headline = view.findViewById(R.id.nativeHeadline);
        TextView body = view.findViewById(R.id.nativeBody);
        TextView advertiser = view.findViewById(R.id.nativeAdvertiser);
        ImageView icon = view.findViewById(R.id.nativeIcon);
        MediaView media = view.findViewById(R.id.nativeMedia);
        Button callToAction = view.findViewById(R.id.nativeCta);

        headline.setText(ad.getHeadline());
        view.setHeadlineView(headline);

        if (ad.getBody() == null) body.setVisibility(View.GONE);
        else body.setText(ad.getBody());
        view.setBodyView(body);

        if (ad.getAdvertiser() == null) advertiser.setVisibility(View.GONE);
        else advertiser.setText(ad.getAdvertiser());
        view.setAdvertiserView(advertiser);

        if (ad.getIcon() == null) icon.setVisibility(View.GONE);
        else icon.setImageDrawable(ad.getIcon().getDrawable());
        view.setIconView(icon);

        if (ad.getMediaContent() == null) media.setVisibility(View.GONE);
        else media.setMediaContent(ad.getMediaContent());
        view.setMediaView(media);

        if (ad.getCallToAction() == null) callToAction.setVisibility(View.GONE);
        else callToAction.setText(ad.getCallToAction());
        view.setCallToActionView(callToAction);

        // Do not install custom click handlers on native ad assets.
        view.setNativeAd(ad);
        return view;
    }

    public void destroy() {
        destroyed = true;
        manager.destroy();
        nativeSlot.removeAllViews();
        bannerSlot.removeAllViews();
    }
}
