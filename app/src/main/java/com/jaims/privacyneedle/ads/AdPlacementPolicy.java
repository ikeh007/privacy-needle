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
    public static boolean mayShowTopBanner(int articleCount, boolean isSearch) {
        return mayShowFeedBanner(articleCount, isSearch);
    }

    public static int desiredProductNativeAds(int productCount) {
        // Product discovery also contains explanatory editorial tips. Never
        // display an ad-only or link-only page, and cap ads at two per session.
        if (productCount < 7) return 0;
        return Math.min(2, productCount / 3);
    }

    public static boolean mayShowGameResultsAds(boolean challengeCompleted) {
        return challengeCompleted;
    }

    public static boolean mayShowVideoBanner(int videoCount) {
        return videoCount >= 6;
    }

    public static boolean mayShowVideoNative(int videoCount) {
        // Videos catalogue only; never use mobile ads in the YouTube player.
        return videoCount >= 6;
    }

    public static int desiredVideoNativeAds(int videoCount) {
        if (!mayShowVideoNative(videoCount)) return 0;
        return Math.min(6, videoCount / 3);
    }
}
