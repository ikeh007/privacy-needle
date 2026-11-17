package com.jaims.privacyneedle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.jaims.privacyneedle.ads.AdPlacementPolicy;
import org.junit.Test;

public class AdPlacementPolicyTest {
    @Test public void hideAdsOnEmptyOrShortFeed() {
        for (int i = 0; i < 7; i++) {
            assertFalse(AdPlacementPolicy.mayShowFeedBanner(i, false));
            assertEquals(0, AdPlacementPolicy.desiredNativeAds(i, false));
        }
    }

    @Test public void threeArticleNativeIntervalWithEightAdCap() {
        assertEquals(2, AdPlacementPolicy.desiredNativeAds(7, false));
        assertEquals(3, AdPlacementPolicy.desiredNativeAds(10, false));
        assertEquals(6, AdPlacementPolicy.desiredNativeAds(18, false));
        assertEquals(8, AdPlacementPolicy.desiredNativeAds(80, false));
    }

    @Test public void noAdsOnSearchOrEmptyVideos() {
        assertFalse(AdPlacementPolicy.mayShowFeedBanner(100, true));
        assertEquals(0, AdPlacementPolicy.desiredNativeAds(100, true));
        assertFalse(AdPlacementPolicy.mayShowVideoBanner(5));
        assertTrue(AdPlacementPolicy.mayShowVideoBanner(6));
    }
}
