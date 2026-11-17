package com.jaims.privacyneedle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.jaims.privacyneedle.ads.DailyQuizPolicy;

import org.junit.Test;

public class DailyQuizPolicyTest {
    @Test public void allUsersReceiveThreeFreeQuestions() {
        assertEquals(3, DailyQuizPolicy.allowedQuestions(false));
    }
    @Test public void oneEarnedRewardUnlocksExactlyOneQuestion() {
        assertEquals(4, DailyQuizPolicy.allowedQuestions(true));
    }
    @Test public void bonusOnlyOfferedAfterThreeFreeQuestions() {
        assertFalse(DailyQuizPolicy.canOfferBonus(0, false));
        assertFalse(DailyQuizPolicy.canOfferBonus(2, false));
        assertTrue(DailyQuizPolicy.canOfferBonus(3, false));
        assertFalse(DailyQuizPolicy.canOfferBonus(3, true));
        assertFalse(DailyQuizPolicy.canOfferBonus(4, true));
    }
}
