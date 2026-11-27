package com.jaims.privacyneedle;

import static org.junit.Assert.*;
import com.jaims.privacyneedle.ads.AdPlacementPolicy;
import org.junit.Test;

public class GameAdsPolicyTest {
    @Test public void topBannerRequiresPublisherContentAndNormalFeed() {
        assertFalse(AdPlacementPolicy.mayShowTopBanner(0, false));
        assertFalse(AdPlacementPolicy.mayShowTopBanner(6, false));
        assertTrue(AdPlacementPolicy.mayShowTopBanner(7, false));
        assertFalse(AdPlacementPolicy.mayShowTopBanner(20, true));
    }
    @Test public void gamePlacementsOnlyAfterChallengeCompletes() {
        assertFalse(AdPlacementPolicy.mayShowGameResultsAds(false));
        assertTrue(AdPlacementPolicy.mayShowGameResultsAds(true));
    }
}
