package com.jaims.privacyneedle.ads;

/** Testable limits: everyone gets 3 free questions; one opt-in reward unlocks 1 more. */
public final class DailyQuizPolicy {
    private DailyQuizPolicy() { }

    public static final int FREE_DAILY_QUESTIONS = 3;

    public static int allowedQuestions(boolean bonusUnlocked) {
        return FREE_DAILY_QUESTIONS + (bonusUnlocked ? 1 : 0);
    }

    public static boolean canOfferBonus(int alreadyAnsweredToday, boolean bonusUnlocked) {
        return alreadyAnsweredToday == FREE_DAILY_QUESTIONS && !bonusUnlocked;
    }
}
