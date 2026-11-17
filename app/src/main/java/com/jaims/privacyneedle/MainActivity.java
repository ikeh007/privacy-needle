package com.jaims.privacyneedle;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.view.Menu;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;


import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.jaims.privacyneedle.adapters.PostsAdapter;
import com.jaims.privacyneedle.ads.AdMobManager;
import com.jaims.privacyneedle.videos.VideosActivity;
import android.net.Uri;
import com.jaims.privacyneedle.models.Post;
import com.jaims.privacyneedle.network.RetrofitClient;
import com.jaims.privacyneedle.network.WordPressAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View emptyState;
    private PostsAdapter postsAdapter;
    private AdMobManager adsManager;
    private TabLayout tabLayout;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigation;

    private static final int PAGE_SIZE = 10;
    private final List<Post> postList = new ArrayList<>();
    private Call<List<Post>> activeCall;
    private int currentCategoryId = 0;
    private int nextPage = 1;
    private int totalPages = Integer.MAX_VALUE;
    private String currentSearchQuery;
    private boolean isLoading = false;

    private final List<Integer> categoryIds = new ArrayList<>(Arrays.asList(
            0, 46, 41, 61, 50, 47, 43, 232, 233, 56
    ));

    private final List<String> categoryNames = new ArrayList<>(Arrays.asList(
            "Latest", "Data Protection", "Breach report", "NDPC", "Definition of Terms", "Compliance",
            "Tech & Security", "Digital Lifestyle", "Startups & Innovation", "Resources"
    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        requestNotificationPermission();

        // --- Views ---
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progress);
        emptyState = findViewById(R.id.emptyState);
        findViewById(R.id.retryButton).setOnClickListener(v -> loadNextPage());
        tabLayout = findViewById(R.id.tabLayout);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        // A non-functional Videos button can be treated as deceptive navigation.
        // Enable it only after the verified official YouTube channel ID is configured.
        if (!getString(R.string.youtube_channel_id).matches("UC[A-Za-z0-9_-]{22}")) {
            bottomNavigation.getMenu().findItem(R.id.nav_video).setVisible(false);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupDrawerCategories();


        // Remove title & show logo
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView view, int dx, int dy) {
                super.onScrolled(view, dx, dy);
                if (dy > 0 && !isLoading && nextPage <= totalPages
                        && layoutManager.findLastVisibleItemPosition() >= postList.size() - 3) {
                    loadNextPage();
                }
            }
        });
        postsAdapter = new PostsAdapter(this, postList);
        recyclerView.setAdapter(postsAdapter);

        adsManager = new AdMobManager(this);
        adsManager.requestConsentAndInitialize(() -> {
            if (isFinishing() || isDestroyed()) return;
            updatePrivacyChoicesItem();
            maybeLoadFeedAd();
        });

        // Drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Add category tabs dynamically
        for (String name : categoryNames) {
            tabLayout.addTab(tabLayout.newTab().setText(name));
        }

        selectCategory(0);

        // --- Tab selection ---
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                selectCategory(categoryIds.get(tab.getPosition()));
            }

            @Override
            public void onTabUnselected(@NonNull TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(@NonNull TabLayout.Tab tab) {
            }
        });

        FirebaseMessaging.getInstance().

                subscribeToTopic("all")
                        .

                addOnCompleteListener(task ->

                {
                    if (task.isSuccessful()) {
                        // Successfully subscribed
                    }
                });


        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                tabLayout.getTabAt(0).select();
            } else if (id == R.id.nav_quiz) {
                startActivity(new Intent(this, QuizActivity.class));

            } else if (id == R.id.nav_search) {
                showSearchDialog();


            } else if (id == R.id.nav_share) {
                shareApp();

            } else if (id == R.id.nav_exclusive) {
                tabLayout.getTabAt(9).select(); // Resources tab


            } else if (id == R.id.nav_about) {
                showAboutDialog();


            } else if (id == R.id.nav_video) {
                startActivity(new Intent(this, VideosActivity.class));
            }


            return true;
        });


        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id >= 0 && id < categoryNames.size()) {

                tabLayout.getTabAt(id).select();

            } else if (id == R.id.nav_share) {

                shareApp();

            } else if (id == R.id.nav_privacy_policy) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://privacyneedle.com/privacy-policy/")));

            } else if (id == R.id.nav_privacy_choices) {
                adsManager.showPrivacyChoices(() -> {
                    updatePrivacyChoicesItem();
                    maybeLoadFeedAd();
                });

            } else if (id == R.id.nav_about) {

                showAboutDialog();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });


        // --- Back gesture handling ---
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }
    }

    private void updatePrivacyChoicesItem() {
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_privacy_choices)
                .setVisible(adsManager != null && adsManager.privacyChoicesRequired());
    }

    private void maybeLoadFeedAd() {
        if (adsManager == null || postList.size() < 7 || currentSearchQuery != null) return;
        adsManager.loadFeedBanner(() -> {
            if (!isFinishing() && !isDestroyed()) {
                postsAdapter.setBanner(adsManager.getLoadedBanner());
            }
        });
    }

    private void showAboutDialog() {

        String aboutText =
                "Privacy Needle\n\n" +
                        "Privacy Needle is a digital platform dedicated to privacy awareness, " +
                        "data protection education, cybersecurity news, compliance updates, and digital safety tips.\n\n" +

                        "Our mission is to simplify privacy, data protection, and technology knowledge for everyone.\n\n" +

                        " A product of JAIMS SOLUTIONS, Developed By; Ikeh Ifeanyichukwu James.\n\n" +

                        "📧 Email: info@privacyneedle.com\n" +
                        "📞 Contact: +234 905 748 6076\n\n" +
                        "© 2026 Privacy Needle";

        new AlertDialog.Builder(this)
                .setTitle("About Privacy Needle")
                .setMessage(aboutText)
                .setPositiveButton("Close", null)
                .show();
    }

    private void shareApp() {

        String appLink = "https://play.google.com/store/apps/details?id=" + getPackageName();

        String shareMessage = "Download Privacy Needle – Your trusted source for privacy, data protection & cybersecurity updates.\n\n"
                + "Get it here 👇\n" + appLink;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

        startActivity(Intent.createChooser(shareIntent, "Share Privacy Needle via"));
    }


    // --- Search dialog ---
    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Search Privacy Needle");

        final EditText input = new EditText(MainActivity.this);
        input.setHint("Enter keyword...");
        builder.setView(input);

        builder.setPositiveButton("Search", (dialog, which) -> {
            String query = input.getText().toString().trim();
            if (!query.isEmpty()) searchPosts(query);
            else
                Toast.makeText(MainActivity.this, "Enter a search term", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void searchPosts(String keyword) {
        currentSearchQuery = keyword;
        currentCategoryId = 0;
        resetFeed();
    }

    private void selectCategory(int categoryId) {
        currentCategoryId = categoryId;
        currentSearchQuery = null;
        resetFeed();
    }

    private void resetFeed() {
        if (activeCall != null) {
            activeCall.cancel();
            activeCall = null;
        }
        isLoading = false;
        nextPage = 1;
        totalPages = Integer.MAX_VALUE;
        postList.clear();
        postsAdapter.setAdvertisingEnabled(currentSearchQuery == null);
        emptyState.setVisibility(View.GONE);
        loadNextPage();
    }

    private void loadNextPage() {
        if (isLoading || nextPage > totalPages || isFinishing() || isDestroyed()) {
            return;
        }

        WordPressAPI api = RetrofitClient.getAPI();
        final int requestedPage = nextPage;
        Call<List<Post>> call;
        if (currentSearchQuery != null) {
            call = api.getPostsBySearch(currentSearchQuery, PAGE_SIZE, requestedPage);
        } else if (currentCategoryId == 0) {
            call = api.getPosts(PAGE_SIZE, requestedPage);
        } else {
            call = api.getPostsByCategory(currentCategoryId, PAGE_SIZE, requestedPage);
        }

        activeCall = call;
        isLoading = true;
        emptyState.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(@NonNull Call<List<Post>> request,
                                   @NonNull Response<List<Post>> response) {
                if (request != activeCall) return; // Ignore older tab/search responses.
                activeCall = null;
                isLoading = false;
                progressBar.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {
                    emptyState.setVisibility(postList.isEmpty() ? View.VISIBLE : View.GONE);
                    Toast.makeText(MainActivity.this, "Unable to load articles", Toast.LENGTH_SHORT).show();
                    return;
                }

                String pageHeader = response.headers().get("X-WP-TotalPages");
                if (pageHeader != null) {
                    try {
                        totalPages = Integer.parseInt(pageHeader);
                    } catch (NumberFormatException ignored) {
                        // Fallback to detecting the final short page below.
                    }
                }

                List<Post> incoming = response.body();
                postList.addAll(incoming);
                if (!incoming.isEmpty()) {
                    // Mixed article/ad rows require a full dataset refresh.
                    postsAdapter.notifyDataSetChanged();
                    maybeLoadFeedAd();
                } else if (requestedPage == 1) {
                    emptyState.setVisibility(View.VISIBLE);
                }

                nextPage = requestedPage + 1;
                if (incoming.size() < PAGE_SIZE) {
                    totalPages = Math.min(totalPages, requestedPage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Post>> request, @NonNull Throwable error) {
                if (request != activeCall) return;
                activeCall = null;
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                if (!request.isCanceled()) {
                    emptyState.setVisibility(postList.isEmpty() ? View.VISIBLE : View.GONE);
                    Toast.makeText(MainActivity.this,
                            "Connection problem. Please try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (activeCall != null) activeCall.cancel();
        if (adsManager != null) adsManager.destroy();
        super.onDestroy();
    }

    private void setupDrawerCategories() {
        Menu menu = navigationView.getMenu();
        Menu subMenu = menu.findItem(R.id.nav_categories).getSubMenu();

        subMenu.clear();

        for (int i = 0; i < categoryNames.size(); i++) {
            subMenu.add(0, i, Menu.NONE, categoryNames.get(i))
                    .setIcon(R.drawable.double_arrow_24px);
            ; // optional icon
        }
    }


}


