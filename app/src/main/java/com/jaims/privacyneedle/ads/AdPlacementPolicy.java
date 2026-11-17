package com.jaims.privacyneedle.ads;

/** Pure policy gate: ad inventory only appears inside an already-populated editorial feed. */
public final class AdPlacementPolicy {
    private AdPlacementPolicy() {}

    public static final int MINIMUM_FEED_ARTICLES = 7;

    public static boolean mayShowFeedBanner(int loadedPublisherArticles, boolean isSearch) {
        return loadedPublisherArticles >= MINIMUM_FEED_ARTICLES && !isSearch;
    }
}
