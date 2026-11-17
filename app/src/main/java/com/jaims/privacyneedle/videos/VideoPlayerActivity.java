package com.jaims.privacyneedle.videos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jaims.privacyneedle.R;
import com.jaims.privacyneedle.products.ProductsActivity;

/**
 * In-app YouTube player: preserve YouTube's own embed controls and origin/referrer
 * context. YouTube may disable third-party embeds per video; the direct watch
 * link remains available. Never layer AdMob units over YouTube video playback.
 */
public class VideoPlayerActivity extends AppCompatActivity {
    private WebView player;
    private View fullScreenView;
    private WebChromeClient.CustomViewCallback fullScreenCallback;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        String videoId = getIntent().getStringExtra("video_id");
        if (videoId == null || !videoId.matches("[A-Za-z0-9_-]{11}")) {
            finish();
            return;
        }
        setContentView(R.layout.activity_video_player);

        String title = getIntent().getStringExtra("video_title");
        ((TextView) findViewById(R.id.videoPlayerTitle)).setText(title == null ? "Video" : title);
        findViewById(R.id.playerBack).setOnClickListener(v -> finish());
        findViewById(R.id.playerExploreProducts).setOnClickListener(v ->
                startActivity(new Intent(this, ProductsActivity.class)));
        findViewById(R.id.watchOnYouTube).setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/watch?v=" + videoId))));

        player = findViewById(R.id.youtubePlayer);
        WebSettings settings = player.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        player.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (!request.isForMainFrame()) return false;
                Uri uri = request.getUrl();
                if ("https".equalsIgnoreCase(uri.getScheme())
                        && ("www.youtube-nocookie.com".equalsIgnoreCase(uri.getHost())
                            || "www.youtube.com".equalsIgnoreCase(uri.getHost()))) {
                    return false;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception ignored) {
                    // A video can still be played using the separate YouTube button.
                }
                return true;
            }
        });

        player.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (fullScreenView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                fullScreenView = view;
                fullScreenCallback = callback;
                ViewGroup root = findViewById(android.R.id.content);
                root.addView(fullScreenView, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                player.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onHideCustomView() {
                hideFullScreen();
            }
        });

        String embed = "https://www.youtube-nocookie.com/embed/" + videoId
                + "?playsinline=1&autoplay=0&rel=0";
        String html = "<!doctype html><html lang='en'><head><meta charset='utf-8'>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<meta name='referrer' content='strict-origin-when-cross-origin'>"
                + "<style>html,body{width:100%;height:100%;margin:0;background:#000}"
                + "iframe{border:0;width:100%;height:100%}</style></head>"
                + "<body><iframe src='" + embed + "' title='YouTube video player' "
                + "referrerpolicy='strict-origin-when-cross-origin' allowfullscreen "
                + "allow='autoplay;encrypted-media;gyroscope;picture-in-picture;fullscreen'>"
                + "</iframe></body></html>";
        player.loadDataWithBaseURL("https://privacyneedle.com/", html,
                "text/html", "UTF-8", null);
    }

    private void hideFullScreen() {
        if (fullScreenView == null) return;
        ViewGroup root = findViewById(android.R.id.content);
        root.removeView(fullScreenView);
        fullScreenView = null;
        player.setVisibility(View.VISIBLE);
        if (fullScreenCallback != null) {
            fullScreenCallback.onCustomViewHidden();
            fullScreenCallback = null;
        }
    }

    @Override
    protected void onDestroy() {
        hideFullScreen();
        if (player != null) {
            player.stopLoading();
            player.loadUrl("about:blank");
            player.destroy();
            player = null;
        }
        super.onDestroy();
    }
}
