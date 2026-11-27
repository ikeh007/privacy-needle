package com.jaims.privacyneedle.scam;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdView;
import com.google.android.material.button.MaterialButton;
import com.jaims.privacyneedle.R;
import com.jaims.privacyneedle.ads.AdMobManager;
import com.jaims.privacyneedle.ads.RewardedQuizAdManager;
import com.jaims.privacyneedle.products.ProductsActivity;

/**
 * Thirty-second daily challenge. A shared HTTPS URL pins the UTC date, which
 * selects the same fictional scenario on every device, regardless of time zone.
 */
public final class ScamChallengeActivity extends AppCompatActivity {
    public static final String EXTRA_DAY = "challenge_day";
    public static final String EXTRA_BONUS = "bonus_mode";

    private String challengeDay;
    private ScamScenario scenario;
    private boolean bonus;
    private int friendScore = -1;
    private long deadline;
    private int score;
    private boolean started;
    private boolean resolved;
    private boolean correct;
    private boolean timedOut;

    private CountDownTimer countdown;
    private TextView timerView;
    private ProgressBar timerProgress;
    private View introPanel, answerPanel, resultPanel;
    private View adWrapper;
    private MaterialButton bonusButton;
    private AdMobManager ads;
    private RewardedQuizAdManager rewarded;
    private boolean rewardLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scam_challenge);

        Uri deepLink = getIntent().getData();
        String requestedDay = deepLink != null
                ? deepLink.getQueryParameter("day") : getIntent().getStringExtra(EXTRA_DAY);
        challengeDay = ScamChallengeRules.isValidDay(requestedDay)
                ? requestedDay : ScamChallengeRules.todayUtc();
        bonus = (deepLink != null && "bonus".equals(deepLink.getQueryParameter("mode")))
                || getIntent().getBooleanExtra(EXTRA_BONUS, false);
        friendScore = deepLink != null
                ? ScamChallengeRules.parseSharedScore(deepLink.getQueryParameter("score")) : -1;
        scenario = bonus ? ScamScenarioCatalog.forBonusDay(challengeDay)
                : ScamScenarioCatalog.forDay(challengeDay);

        timerView = findViewById(R.id.scamTimer);
        timerProgress = findViewById(R.id.scamTimerProgress);
        introPanel = findViewById(R.id.scamIntroPanel);
        answerPanel = findViewById(R.id.scamAnswerPanel);
        resultPanel = findViewById(R.id.scamResultPanel);
        adWrapper = findViewById(R.id.scamAdWrapper);
        bonusButton = findViewById(R.id.scamBonus);

        ((TextView) findViewById(R.id.scamCategory)).setText(scenario.category);
        ((TextView) findViewById(R.id.scamSender)).setText(scenario.sender);
        ((TextView) findViewById(R.id.scamMessage)).setText(scenario.message);
        ((TextView) findViewById(R.id.scamSubtitle)).setText(
                bonus ? "BONUS PRACTICE · 30 seconds" : "30 seconds. One message. Trust your instincts.");
        if (friendScore >= 0) {
            ((TextView) findViewById(R.id.scamFriendPrompt)).setText(
                    "A friend shared " + friendScore + " points. Can you beat that score?");
        }

        findViewById(R.id.scamBack).setOnClickListener(v -> finish());
        findViewById(R.id.scamStart).setOnClickListener(v -> beginChallenge());
        findViewById(R.id.scamAnswerScam).setOnClickListener(v -> submitAnswer(true));
        findViewById(R.id.scamAnswerLegit).setOnClickListener(v -> submitAnswer(false));
        findViewById(R.id.scamShare).setOnClickListener(v -> shareChallenge());
        findViewById(R.id.scamMore).setOnClickListener(v ->
                startActivity(new Intent(this, ProductsActivity.class)));
        bonusButton.setVisibility(View.GONE);
        bonusButton.setOnClickListener(v -> confirmBonusAd());

        if (savedInstanceState != null
                && challengeDay.equals(savedInstanceState.getString("day"))
                && bonus == savedInstanceState.getBoolean("bonus")) {
            started = savedInstanceState.getBoolean("started");
            resolved = savedInstanceState.getBoolean("resolved");
            deadline = savedInstanceState.getLong("deadline");
            score = savedInstanceState.getInt("score");
            correct = savedInstanceState.getBoolean("correct");
            timedOut = savedInstanceState.getBoolean("timedOut");
        }
        render();
        ads = new AdMobManager(this);
        rewarded = new RewardedQuizAdManager(this);
        ads.requestConsentAndInitialize(() -> {
            if (resolved && !isFinishing() && !isDestroyed()) loadResultBanner();
        });
        if (resolved) loadBonusAdIfEligible();
    }

    private void render() {
        introPanel.setVisibility(!started ? View.VISIBLE : View.GONE);
        answerPanel.setVisibility(started && !resolved ? View.VISIBLE : View.GONE);
        resultPanel.setVisibility(resolved ? View.VISIBLE : View.GONE);
        if (!started) {
            timerView.setText("00:30");
            timerProgress.setProgress(30);
        } else if (resolved) {
            timerView.setText("DONE");
            timerProgress.setProgress(0);
            renderResult();
        } else {
            updateRemaining();
        }
    }

    private void beginChallenge() {
        if (started) return;
        started = true;
        deadline = SystemClock.elapsedRealtime() + ScamChallengeRules.SECONDS * 1000L;
        render();
        startTimer();
    }

    private void startTimer() {
        if (!started || resolved) return;
        if (countdown != null) countdown.cancel();
        long remaining = deadline - SystemClock.elapsedRealtime();
        if (remaining <= 0) {
            complete(false, true);
            return;
        }
        countdown = new CountDownTimer(remaining, 120L) {
            @Override public void onTick(long ignored) {
                updateRemaining();
            }
            @Override public void onFinish() {
                complete(false, true);
            }
        }.start();
        updateRemaining();
    }

    private int secondsRemaining() {
        long remaining = Math.max(0L, deadline - SystemClock.elapsedRealtime());
        return (int) Math.min(ScamChallengeRules.SECONDS, (remaining + 999L) / 1000L);
    }

    private void updateRemaining() {
        int left = secondsRemaining();
        timerView.setText(String.format(java.util.Locale.US, "00:%02d", left));
        timerProgress.setProgress(left);
        if (left == 0 && !resolved) complete(false, true);
    }

    private void submitAnswer(boolean selectedScam) {
        if (!started || resolved) return;
        if (secondsRemaining() == 0) {
            complete(false, true);
            return;
        }
        complete(selectedScam == scenario.scam, false);
    }

    private void complete(boolean wasCorrect, boolean timeExpired) {
        if (resolved) return;
        resolved = true;
        timedOut = timeExpired;
        correct = wasCorrect && !timeExpired;
        score = ScamChallengeRules.calculateScore(correct, timeExpired ? 0 : secondsRemaining());
        if (countdown != null) {
            countdown.cancel();
            countdown = null;
        }
        // The first attempt is preserved as the daily result. Later replays are practice.
        if (!bonus && challengeDay.equals(ScamChallengeRules.todayUtc())) {
            SharedPreferences p = getSharedPreferences("scam_daily", MODE_PRIVATE);
            if (!challengeDay.equals(p.getString("first_day", ""))) {
                p.edit().putString("first_day", challengeDay).putInt("first_score", score).apply();
            }
        }
        render();
        loadResultBanner();
        loadBonusAdIfEligible();
    }

    private void renderResult() {
        ((TextView) findViewById(R.id.scamResultTitle)).setText(
                timedOut ? "Time's up. Learn the clues."
                        : correct ? "Excellent instincts." : "A useful lesson learned.");
        ((TextView) findViewById(R.id.scamResultScore)).setText(score + " POINTS");
        ((TextView) findViewById(R.id.scamVerdict)).setText(
                scenario.scam ? "This message is suspicious" : "Likely legitimate, but verify");
        ((TextView) findViewById(R.id.scamExplanation)).setText(scenario.explanation);
        ((TextView) findViewById(R.id.scamTip)).setText("YOUR SAFETY TAKEAWAY\n" + scenario.safetyTip);

        String compare = friendScore >= 0
                ? ScamChallengeRules.comparison(score, friendScore)
                    + "\nFriend's shared score: " + friendScore + " points"
                : "Send this exact challenge to a friend and compare scores.";
        ((TextView) findViewById(R.id.scamFriendCompare)).setText(compare);
    }

    private void loadResultBanner() {
        if (ads == null || !resolved || isFinishing() || isDestroyed()) return;
        ads.loadFeedBanner(() -> {
            if (isFinishing() || isDestroyed()) return;
            AdView banner = ads.getLoadedBanner();
            if (banner == null) return;
            FrameLayout container = findViewById(R.id.scamAdContainer);
            if (banner.getParent() instanceof ViewGroup) {
                ((ViewGroup) banner.getParent()).removeView(banner);
            }
            container.removeAllViews();
            container.addView(banner);
            adWrapper.setVisibility(View.VISIBLE);
        });
    }

    private boolean bonusAvailable() {
        if (bonus || !resolved || !challengeDay.equals(ScamChallengeRules.todayUtc())) {
            return false;
        }
        return !challengeDay.equals(getSharedPreferences("scam_daily", MODE_PRIVATE)
                .getString("bonus_day", ""));
    }

    private void loadBonusAdIfEligible() {
        if (!bonusAvailable() || rewarded == null || rewardLoading) return;
        rewardLoading = true;
        rewarded.prepare(ready -> {
            if (isFinishing() || isDestroyed()) return;
            bonusButton.setVisibility(ready && bonusAvailable() ? View.VISIBLE : View.GONE);
        });
    }

    private void confirmBonusAd() {
        if (!bonusAvailable() || !rewarded.isReady()) return;
        new AlertDialog.Builder(this)
                .setTitle("Optional bonus practice")
                .setMessage("Watch one optional AdMob rewarded advertisement to unlock "
                        + "another 30-second scam scenario. Today's challenge and sharing "
                        + "are free without ads.")
                .setNegativeButton("Not now", null)
                .setPositiveButton("Watch ad", (dialog, which) -> {
                    bonusButton.setVisibility(View.GONE);
                    rewarded.showReward(() -> {
                        if (isFinishing() || isDestroyed() || !bonusAvailable()) return;
                        getSharedPreferences("scam_daily", MODE_PRIVATE).edit()
                                .putString("bonus_day", challengeDay).apply();
                        Intent next = new Intent(this, ScamChallengeActivity.class);
                        next.putExtra(EXTRA_DAY, challengeDay);
                        next.putExtra(EXTRA_BONUS, true);
                        startActivity(next);
                    }, () -> Toast.makeText(this,
                            "Bonus practice unlocks only after the ad reward.",
                            Toast.LENGTH_SHORT).show());
                }).show();
    }

    private void shareChallenge() {
        if (!resolved) return;
        String link = ScamChallengeRules.shareLink(challengeDay, score, bonus);
        try {
            Intent share = ScamShareCard.createShareIntent(this, link, challengeDay, score, correct);
            startActivity(Intent.createChooser(share, "Challenge a friend"));
        } catch (Exception ex) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, "I scored " + score + " in Privacy Needle's "
                    + "30-second Scam Challenge. Can you beat me?\n" + link
                    + "\nGet Privacy Needle on Google Play.");
            startActivity(Intent.createChooser(share, "Challenge a friend"));
        }
    }

    @Override protected void onStart() {
        super.onStart();
        if (started && !resolved) startTimer();
    }

    @Override protected void onStop() {
        if (countdown != null) {
            countdown.cancel();
            countdown = null;
        }
        super.onStop();
    }

    @Override protected void onSaveInstanceState(Bundle saved) {
        super.onSaveInstanceState(saved);
        saved.putString("day", challengeDay);
        saved.putBoolean("bonus", bonus);
        saved.putBoolean("started", started);
        saved.putBoolean("resolved", resolved);
        saved.putLong("deadline", deadline);
        saved.putInt("score", score);
        saved.putBoolean("correct", correct);
        saved.putBoolean("timedOut", timedOut);
    }

    @Override protected void onDestroy() {
        if (countdown != null) countdown.cancel();
        if (ads != null) ads.destroy();
        if (rewarded != null) rewarded.destroy();
        super.onDestroy();
    }
}
