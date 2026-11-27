package com.jaims.privacyneedle;

import static org.junit.Assert.*;
import com.jaims.privacyneedle.scam.ScamChallengeRules;
import com.jaims.privacyneedle.scam.ScamScenarioCatalog;
import org.junit.Test;

public class ScamChallengeRulesTest {
    @Test public void utcDayAndScenarioAreDeterministic() {
        assertTrue(ScamChallengeRules.isValidDay("20261127"));
        assertFalse(ScamChallengeRules.isValidDay("20260230"));
        assertFalse(ScamChallengeRules.isValidDay("202611270"));
        assertEquals(ScamScenarioCatalog.forDay("20261127").id,
                ScamScenarioCatalog.forDay("20261127").id);
        assertEquals(ScamChallengeRules.scenarioIndex("20261127", 16),
                ScamChallengeRules.scenarioIndex("20261127", 16));
    }
    @Test public void scoreRequiresCorrectAnswerAndIsCapped() {
        assertEquals(160, ScamChallengeRules.calculateScore(true, 30));
        assertEquals(100, ScamChallengeRules.calculateScore(true, 0));
        assertEquals(160, ScamChallengeRules.calculateScore(true, 900));
        assertEquals(0, ScamChallengeRules.calculateScore(false, 30));
    }
    @Test public void friendLinkReplaysIdenticalDate() {
        assertEquals("https://play.privacyneedle.com/challenge/?day=20261127&score=134",
                ScamChallengeRules.shareLink("20261127", 134));
        assertEquals(-1, ScamChallengeRules.parseSharedScore("-5"));
        assertEquals(-1, ScamChallengeRules.parseSharedScore("999"));
        assertEquals(0, ScamChallengeRules.parseSharedScore("0"));
    }
    @Test public void comparisonIsClear() {
        assertEquals("You beat the shared score!", ScamChallengeRules.comparison(140, 110));
        assertEquals("It's a tie!", ScamChallengeRules.comparison(110, 110));
        assertEquals("Your friend is ahead this round", ScamChallengeRules.comparison(0, 110));
    }
}
