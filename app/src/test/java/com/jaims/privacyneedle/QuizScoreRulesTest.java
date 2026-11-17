package com.jaims.privacyneedle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.jaims.privacyneedle.game.QuizScoreRules;
import org.junit.Test;

public class QuizScoreRulesTest {
    @Test public void shareOnlyAfterThreeQuestions() {
        assertFalse(QuizScoreRules.canShare(0));
        assertFalse(QuizScoreRules.canShare(2));
        assertTrue(QuizScoreRules.canShare(3));
        assertTrue(QuizScoreRules.canShare(4));
    }

    @Test public void realScoreIsBoundedToActualAttempts() {
        assertEquals("2 / 3", QuizScoreRules.displayScore(2, 3, true));
        assertEquals("3 / 3", QuizScoreRules.displayScore(5, 3, true));
        assertEquals("0 / 4", QuizScoreRules.displayScore(-1, 4, true));
    }

    @Test public void unknownLegacyResultsNeverClaimIncorrectScore() {
        assertEquals("3 QUESTIONS", QuizScoreRules.displayScore(0, 3, false));
        String caption = QuizScoreRules.socialCaption(
                0, 3, false, "https://play.google.com/store/apps/details?id=example");
        assertTrue(caption.contains("completed 3 questions"));
        assertFalse(caption.contains("got 0"));
    }

    @Test public void socialShareContainsStoreLink() {
        String caption = QuizScoreRules.socialCaption(3, 3, true,
                "https://play.google.com/store/apps/details?id=com.jaims.privacyneedle");
        assertTrue(caption.contains("3 / 3"));
        assertTrue(caption.contains("com.jaims.privacyneedle"));
    }
}
