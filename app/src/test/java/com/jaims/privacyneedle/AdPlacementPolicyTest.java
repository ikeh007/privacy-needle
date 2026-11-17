package com.jaims.privacyneedle;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.jaims.privacyneedle.ads.AdPlacementPolicy;

import org.junit.Test;

public class AdPlacementPolicyTest {
    @Test
    public void neverShowBannerOnEmptyOrShortFeeds() {
        for (int count = 0; count < 7; count++) {
            assertFalse(AdPlacementPolicy.mayShowFeedBanner(count, false));
        }
    }

    @Test
    public void allowBannerOnlyAfterSubstantialPublisherContent() {
        assertTrue(AdPlacementPolicy.mayShowFeedBanner(7, false));
        assertTrue(AdPlacementPolicy.mayShowFeedBanner(50, false));
    }

    @Test
    public void neverShowBannerInSearchMode() {
        assertFalse(AdPlacementPolicy.mayShowFeedBanner(7, true));
        assertFalse(AdPlacementPolicy.mayShowFeedBanner(100, true));
    }
}
