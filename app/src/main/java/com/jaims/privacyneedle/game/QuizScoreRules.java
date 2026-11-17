package com.jaims.privacyneedle.game;

/** Deterministic, testable quiz scoring and share labels. */
public final class QuizScoreRules {
    private QuizScoreRules() {}

    public static boolean canShare(int answered) {
        return answered >= 3;
    }

    public static String displayScore(int correct, int answered, boolean known) {
        if (!known) return answered + " QUESTIONS";
        int bounded = Math.max(0, Math.min(correct, Math.max(0, answered)));
        return bounded + " / " + answered;
    }

    public static String scoreSubtitle(boolean known) {
        return known ? "CORRECT ANSWERS" : "CHALLENGE COMPLETED";
    }

    public static String socialCaption(int correct, int answered,
                                       boolean known, String playStoreLink) {
        String outcome = known
                ? "I got " + displayScore(correct, answered, true)
                        + " correct on today's Privacy Needle challenge!"
                : "I completed " + answered
                        + " questions on today's Privacy Needle challenge!";
        return outcome + "\nCan you beat my result? Try the daily privacy quiz.\n"
                + playStoreLink;
    }
}
