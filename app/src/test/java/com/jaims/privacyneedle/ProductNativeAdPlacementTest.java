package com.jaims.privacyneedle;

import static org.junit.Assert.assertEquals;

import com.jaims.privacyneedle.ads.AdPlacementPolicy;
import org.junit.Test;

public class ProductNativeAdPlacementTest {
    @Test public void productNativeRequiresEnoughPublisherContent() {
        assertEquals(0, AdPlacementPolicy.desiredProductNativeAds(0));
        assertEquals(0, AdPlacementPolicy.desiredProductNativeAds(3));
        assertEquals(0, AdPlacementPolicy.desiredProductNativeAds(6));
        assertEquals(2, AdPlacementPolicy.desiredProductNativeAds(7));
        assertEquals(2, AdPlacementPolicy.desiredProductNativeAds(8));
        assertEquals(2, AdPlacementPolicy.desiredProductNativeAds(30));
    }
}
