package com.jaims.privacyneedle.ui;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jaims.privacyneedle.MainActivity;
import com.jaims.privacyneedle.R;

public class PostDetailsActivity extends AppCompatActivity {
    private static final String OFFLINE_PAGE = "file:///android_asset/offline.html";

    private WebView postWebView;
    private ProgressBar progressBar;
    private String postUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        postWebView = findViewById(R.id.postWebView);
        progressBar = findViewById(R.id.progressBar);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);

        String requestedUrl = getIntent().getStringExtra("url");
        if (!isTrustedArticleUrl(requestedUrl)) {
            Toast.makeText(this, "Unable to open this article", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        postUrl = requestedUrl;

        WebSettings settings = postWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);

        postWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (!request.isForMainFrame()) return false;
                return handleNavigation(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleNavigation(Uri.parse(url));
            }

            private boolean handleNavigation(Uri uri) {
                if (isTrustedArticleUrl(uri.toString())) {
                    return false; // Let WebView load internal website links itself.
                }
                try {
                    Intent external = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(external);
                } catch (Exception ignored) {
                    Toast.makeText(PostDetailsActivity.this,
                            "No app can open this link", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        android.webkit.WebResourceError error) {
                if (request.isForMainFrame() && !isOnline()) {
                    view.loadUrl(OFFLINE_PAGE);
                }
            }
        });

        postWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                progressBar.setVisibility(progress < 100 ? View.VISIBLE : View.GONE);
            }
        });

        postWebView.loadUrl(isOnline() ? postUrl : OFFLINE_PAGE);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_backArrow) {
                if (postWebView.canGoBack()) postWebView.goBack();
                else finish();
                return true;
            }
            if (id == R.id.nav_forwardArrow) {
                if (postWebView.canGoForward()) postWebView.goForward();
                else Toast.makeText(this, "No next page", Toast.LENGTH_SHORT).show();
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
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, postUrl);
                startActivity(Intent.createChooser(share, "Share via"));
                return true;
            }
            return false;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (postWebView.canGoBack()) postWebView.goBack();
                else finish();
            }
        });
    }

    private static boolean isTrustedArticleUrl(String url) {
        if (url == null || url.trim().isEmpty()) return false;
        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        return "https".equalsIgnoreCase(uri.getScheme()) && host != null
                && (host.equalsIgnoreCase("privacyneedle.com")
                || host.equalsIgnoreCase("www.privacyneedle.com"));
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm == null || cm.getActiveNetwork() == null) return false;
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    @Override
    protected void onDestroy() {
        if (postWebView != null) {
            postWebView.stopLoading();
            postWebView.loadUrl("about:blank");
            postWebView.setWebViewClient(null);
            postWebView.setWebChromeClient(null);
            postWebView.destroy();
            postWebView = null;
        }
        super.onDestroy();
    }
}
