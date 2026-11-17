package com.jaims.privacyneedle.ads;

/** Testable content-first AdMob placement policy. */
public final class AdPlacementPolicy {
    private AdPlacementPolicy() {}

    public static final int MINIMUM_FEED_ARTICLES = 7;
    public static final int MINIMUM_ARTICLES = MINIMUM_FEED_ARTICLES;
    public static final int NATIVE_ARTICLE_INTERVAL = 3;
    public static final int MAX_NATIVE_ADS_PER_FEED = 8;

    public static boolean mayShowFeedBanner(int articleCount, boolean isSearch) {
        return articleCount >= MINIMUM_ARTICLES && !isSearch;
    }
    public static boolean mayShowFeedNative(int articleCount, boolean isSearch) {
        return articleCount >= MINIMUM_ARTICLES && !isSearch;
    }
    public static int desiredNativeAds(int articleCount, boolean isSearch) {
        if (!mayShowFeedNative(articleCount, isSearch)) return 0;
        return Math.min(MAX_NATIVE_ADS_PER_FEED, articleCount / NATIVE_ARTICLE_INTERVAL);
    }
    public static boolean mayShowVideoBanner(int videoCount) {
        return videoCount >= 6;
    }
}
