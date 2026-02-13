package com.jaims.privacyneedle.ui;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

import com.jaims.privacyneedle.MainActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jaims.privacyneedle.R;

public class PostDetailsActivity extends AppCompatActivity {

    private WebView postWebView;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigation;
    private String postUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        postWebView = findViewById(R.id.postWebView);
        progressBar = findViewById(R.id.progressBar);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.getMenu().clear();
        bottomNavigation.inflateMenu(R.menu.bottom_post_details);

        postUrl = getIntent().getStringExtra("url");

        if (postUrl == null || postUrl.isEmpty()) {
            Toast.makeText(this, "Unable to load post", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        WebSettings webSettings = postWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        postWebView.setWebViewClient(new WebViewClient() {

            // For API 24+ (new method)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(view, request.getUrl().toString());
            }

            // For older APIs
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(view, url);
            }

            // Handle Play Store links here
            private boolean handleUrl(WebView view, String url) {
                // If it's a Play Store link
                if (url.startsWith("https://play.google.com/store/apps/details?id=") ||
                        url.startsWith("market://details?id=")) {

                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        // If Play Store is not installed, open in browser
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(url.replace("market://", "https://play.google.com/store/apps/details?id=")));
                        startActivity(intent);
                    }
                    return true; // we handled it
                }

                // Otherwise load URL normally inside WebView
                view.loadUrl(url);
                return true;
            }
        });

        postWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
            }
        });

        postWebView.loadUrl(postUrl);

        bottomNavigation.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            }

            if (item.getItemId() == R.id.nav_share) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, postUrl);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
                return true;
            }

            return false;
        });

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (postWebView.canGoBack()) {
                            postWebView.goBack();
                        } else {
                            finish();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (postWebView != null) {
            postWebView.destroy();
        }
    }
}
