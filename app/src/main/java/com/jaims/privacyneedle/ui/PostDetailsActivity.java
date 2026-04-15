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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

        // ===== HANDLE DATA FROM NOTIFICATION =====
        if (getIntent() != null) {

            String postId = getIntent().getStringExtra("post_id");
            String url = getIntent().getStringExtra("url");

            // Prefer URL if available
            if (url != null && !url.isEmpty()) {
                postUrl = url;
            }
            // If URL is missing but post_id exists (future support)
            else if (postId != null && !postId.isEmpty()) {
                // TODO: In future, fetch post using API with postId
                // For now, just show error
                postUrl = null;
            }
        }



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

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(view, request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(view, url);
            }

            private boolean handleUrl(WebView view, String url) {

                // Play Store handling
                if (url.startsWith("https://play.google.com/store/apps/details?id=") ||
                        url.startsWith("market://details?id=")) {

                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(url.replace("market://", "https://play.google.com/store/apps/details?id=")));
                        startActivity(intent);
                    }
                    return true;
                }

                view.loadUrl(url);
                return true;
            }

            // ✅ OFFLINE MODE HANDLER
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, android.webkit.WebResourceError error) {
                if (!isOnline()) {
                    view.loadUrl("file:///android_asset/offline.html");
                }
            }
        });

        postWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
            }
        });

        // ✅ OFFLINE CHECK BEFORE LOADING
        if (isOnline()) {
            postWebView.loadUrl(postUrl);
        } else {
            postWebView.loadUrl("file:///android_asset/offline.html");
        }

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_backArrow) {
                if (postWebView.canGoBack()) {
                    postWebView.goBack();
                } else {
                    Toast.makeText(this, "Returning to Home", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                return true;
            }


            if (id == R.id.nav_forwardArrow) {
                if (postWebView.canGoForward()) {
                    postWebView.goForward();
                } else {
                    Toast.makeText(this, "No next page", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            }

            if (id == R.id.nav_share) {
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

    // ✅ INTERNET CHECK METHOD
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (postWebView != null) {
            postWebView.loadUrl("about:blank");
            postWebView.stopLoading();
            postWebView.setWebViewClient(null);
            postWebView.setWebChromeClient(null);
            postWebView.destroy();
            postWebView = null;
        }
    }
}