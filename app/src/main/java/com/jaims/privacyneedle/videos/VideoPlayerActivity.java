package com.jaims.privacyneedle.videos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jaims.privacyneedle.R;

/**
 * YouTube official iframe embed. No AdMob ads are placed next to playback.
 * Preserve YouTube controls and offer a fallback link to the official watch page.
 */
public class VideoPlayerActivity extends AppCompatActivity {
    private WebView player;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        String videoId = getIntent().getStringExtra("video_id");
        if (videoId == null || !videoId.matches("[A-Za-z0-9_-]{11}")) {
            finish();
            return;
        }
        setContentView(R.layout.activity_video_player);
        ((TextView) findViewById(R.id.videoPlayerTitle)).setText(
                getIntent().getStringExtra("video_title"));
        findViewById(R.id.playerBack).setOnClickListener(v -> finish());
        findViewById(R.id.watchOnYouTube).setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/watch?v=" + videoId))));

        player = findViewById(R.id.youtubePlayer);
        WebSettings settings = player.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(true);
        player.setWebChromeClient(new WebChromeClient());

        String embed = "https://www.youtube-nocookie.com/embed/" + videoId
                + "?playsinline=1&rel=0";
        String html = "<!doctype html><html><head><meta name='viewport' "
                + "content='width=device-width, initial-scale=1'></head>"
                + "<body style='margin:0;background:#000'>"
                + "<iframe style='border:0;width:100%;height:100%;position:absolute' "
                + "src='" + embed + "' title='YouTube video player' "
                + "allow='accelerometer;encrypted-media;gyroscope;picture-in-picture;fullscreen' "
                + "allowfullscreen></iframe></body></html>";
        player.loadDataWithBaseURL("https://privacyneedle.com", html, "text/html", "UTF-8", null);
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.stopLoading();
            player.destroy();
            player = null;
        }
        super.onDestroy();
    }
}
