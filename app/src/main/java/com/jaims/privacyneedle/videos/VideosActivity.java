package com.jaims.privacyneedle.videos;

import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import com.jaims.privacyneedle.QuizActivity;
import com.jaims.privacyneedle.products.ProductsActivity;
import com.jaims.privacyneedle.ads.DailyQuizPolicy;
import com.jaims.privacyneedle.ads.RewardedQuizAdManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jaims.privacyneedle.R;
import com.jaims.privacyneedle.ads.AdMobManager;
import com.jaims.privacyneedle.ads.AdPlacementPolicy;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads uploads from the actual publisher channel's YouTube RSS.
 * Uses public RSS; a YouTube API key is not needed.
 */
public class VideosActivity extends AppCompatActivity {
    private ProgressBar progress;
    private View empty;
    private TextView message;
    private VideosAdapter adapter;
    private AdMobManager adsManager;
    private RewardedQuizAdManager rewardedManager;
    private Button bonusButton;
    private boolean bonusLoadStarted;
    private final List<VideoItem> items = new ArrayList<>();
    private String channelId;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_videos);

        channelId = getString(R.string.youtube_channel_id).trim();
        progress = findViewById(R.id.videosProgress);
        empty = findViewById(R.id.videosEmpty);
        message = findViewById(R.id.videosMessage);

        RecyclerView list = findViewById(R.id.videosList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideosAdapter(this, items);
        list.setAdapter(adapter);

        bonusButton = findViewById(R.id.videoBonusAd);
        bonusButton.setVisibility(View.GONE);
        bonusButton.setOnClickListener(v -> confirmBonusReward());
        findViewById(R.id.openPrivacyProducts)
                .setOnClickListener(v -> startActivity(new Intent(this, ProductsActivity.class)));

        adsManager = new AdMobManager(this);
        rewardedManager = new RewardedQuizAdManager(this);
        adsManager.requestConsentAndInitialize(this::maybeLoadGalleryAds);

        findViewById(R.id.videoBack).setOnClickListener(v -> finish());
        findViewById(R.id.videosRetry).setOnClickListener(v -> refresh());
        findViewById(R.id.openYouTubeChannel).setOnClickListener(v -> {
            if (isConfigured()) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/channel/" + channelId)));
            }
        });
        if (isConfigured()) refresh();
        else {
            findViewById(R.id.openYouTubeChannel).setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
            message.setText("This video channel has not been connected yet.");
        }
    }

    private void maybeLoadGalleryAds() {
        if (adsManager == null || isFinishing() || isDestroyed()) return;
        if (AdPlacementPolicy.mayShowVideoBanner(items.size())) {
            adsManager.loadFeedBanner(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    adapter.setBanner(adsManager.getLoadedBanner());
                }
            });
        }
        if (AdPlacementPolicy.mayShowVideoNative(items.size())) {
            adsManager.loadVideoNativeAds(items.size(), () -> {
                if (!isFinishing() && !isDestroyed()) {
                    adapter.setNativeAds(adsManager.getLoadedNativeAds());
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        maybePrepareBonusAd();
    }

    private boolean canEarnVideoQuizBonus() {
        SharedPreferences prefs = getSharedPreferences("daily_quiz", MODE_PRIVATE);
        // The fourth quiz question can be unlocked in Videos, even before the
        // first three are answered. Playback and the daily three stay free.
        return prefs.getInt("bonus_unlock_day", -1) != DailyQuizPolicy.todayKey();
    }

    private void maybePrepareBonusAd() {
        if (rewardedManager == null || bonusButton == null) return;
        if (items.isEmpty() || !canEarnVideoQuizBonus()) {
            bonusButton.setVisibility(View.GONE);
            return;
        }
        if (rewardedManager.isReady()) {
            bonusButton.setVisibility(View.VISIBLE);
            return;
        }
        if (bonusLoadStarted) return;
        bonusLoadStarted = true;
        rewardedManager.prepare(ready -> {
            if (isFinishing() || isDestroyed()) return;
            bonusButton.setVisibility(ready && canEarnVideoQuizBonus()
                    ? View.VISIBLE : View.GONE);
        });
    }

    private void confirmBonusReward() {
        if (!canEarnVideoQuizBonus() || !rewardedManager.isReady()) {
            bonusButton.setVisibility(View.GONE);
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Unlock one bonus quiz question")
                .setMessage("Watch one optional AdMob rewarded ad to unlock a fourth "
                        + "privacy quiz question today. All videos and the first "
                        + "three quiz questions remain free without watching an ad.")
                .setNegativeButton("Not now", null)
                .setPositiveButton("Watch optional ad", (d, which) -> {
                    bonusButton.setVisibility(View.GONE);
                    rewardedManager.showReward(() -> {
                        if (isFinishing() || isDestroyed() || !canEarnVideoQuizBonus()) return;
                        getSharedPreferences("daily_quiz", MODE_PRIVATE).edit()
                                .putInt("bonus_unlock_day", DailyQuizPolicy.todayKey()).apply();
                        new AlertDialog.Builder(this)
                                .setTitle("Bonus question unlocked")
                                .setMessage("Your fourth daily privacy quiz question is available.")
                                .setPositiveButton("Open quiz", (dialog, w) ->
                                        startActivity(new Intent(this, QuizActivity.class)))
                                .setNegativeButton("Keep watching", null)
                                .show();
                    }, () -> {
                        if (!isFinishing() && !isDestroyed()) maybePrepareBonusAd();
                    });
                }).show();
    }

    @Override
    protected void onDestroy() {
        if (adsManager != null) adsManager.destroy();
        if (rewardedManager != null) rewardedManager.destroy();
        super.onDestroy();
    }

    private boolean isConfigured() {
        return channelId.matches("UC[A-Za-z0-9_-]{22}");
    }

    private void refresh() {
        if (!isConfigured()) return;
        progress.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);
        // Network work never runs on Android's UI thread.
        new Thread(() -> {
            List<VideoItem> loaded = new ArrayList<>();
            String error = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL("https://www.youtube.com/feeds/videos.xml?channel_id=" + channelId);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("User-Agent", "PrivacyNeedleAndroid/1.0");
                if (connection.getResponseCode() != 200) throw new Exception("Feed unavailable");
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(connection.getInputStream(), "UTF-8");

                boolean entry = false;
                String id = null;
                String title = null;
                int event = parser.getEventType();
                while (event != XmlPullParser.END_DOCUMENT && loaded.size() < 30) {
                    String tag = parser.getName();
                    if (event == XmlPullParser.START_TAG) {
                        if ("entry".equals(tag)) {
                            entry = true;
                            id = null;
                            title = null;
                        } else if (entry && "videoId".equals(tag)) {
                            id = parser.nextText();
                        } else if (entry && "title".equals(tag)) {
                            title = parser.nextText();
                        }
                    } else if (event == XmlPullParser.END_TAG && "entry".equals(tag)) {
                        if (id != null && id.matches("[A-Za-z0-9_-]{11}")) {
                            loaded.add(new VideoItem(id,
                                    title == null || title.trim().isEmpty() ? "Privacy Needle video" : title));
                        }
                        entry = false;
                    }
                    event = parser.next();
                }
            } catch (Exception ex) {
                error = "Unable to load videos. Check your connection and try again.";
            } finally {
                if (connection != null) connection.disconnect();
            }
            String finalError = error;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                progress.setVisibility(View.GONE);
                items.clear();
                items.addAll(loaded);
                adapter.refreshRows();
                maybeLoadGalleryAds();
                maybePrepareBonusAd();
                if (items.isEmpty()) {
                    empty.setVisibility(View.VISIBLE);
                    message.setText(finalError != null ? finalError : "This channel has no public videos yet.");
                }
            });
        }).start();
    }
}
