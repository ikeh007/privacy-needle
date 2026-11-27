package com.jaims.privacyneedle.scam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Pure, deterministic rules shared by daily and friend challenges. */
public final class ScamChallengeRules {
    public static final int SECONDS = 30;

    private ScamChallengeRules() {}

    /** UTC is intentional: a dated challenge is identical across time zones. */
    public static String todayUtc() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(new Date());
    }

    public static boolean isValidDay(String day) {
        if (day == null || !day.matches("20[0-9]{6}")) return false;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        formatter.setLenient(false);
        try {
            Date parsed = formatter.parse(day);
            return parsed != null && day.equals(formatter.format(parsed));
        } catch (ParseException ex) {
            return false;
        }
    }

    public static int scenarioIndex(String day, int catalogSize) {
        if (!isValidDay(day) || catalogSize <= 0) throw new IllegalArgumentException("Invalid challenge");
        return Integer.parseInt(day) % catalogSize;
    }

    public static int calculateScore(boolean correct, int secondsRemaining) {
        if (!correct) return 0;
        return 100 + 2 * Math.max(0, Math.min(SECONDS, secondsRemaining));
    }

    public static String comparison(int yourScore, int friendScore) {
        if (friendScore < 0) return "Your result";
        if (yourScore > friendScore) return "You beat the shared score!";
        if (yourScore == friendScore) return "It's a tie!";
        return "Your friend is ahead this round";
    }

    public static String shareLink(String day, int score) {
        return shareLink(day, score, false);
    }

    public static String shareLink(String day, int score, boolean bonus) {
        if (!isValidDay(day)) throw new IllegalArgumentException("Invalid challenge day");
        int safeScore = Math.max(0, Math.min(160, score));
        return "https://play.privacyneedle.com/challenge/?day=" + day
                + "&score=" + safeScore + (bonus ? "&mode=bonus" : "");
    }

    public static int parseSharedScore(String score) {
        if (score == null || !score.matches("[0-9]{1,3}")) return -1;
        int parsed = Integer.parseInt(score);
        return parsed <= 160 ? parsed : -1;
    }
}
