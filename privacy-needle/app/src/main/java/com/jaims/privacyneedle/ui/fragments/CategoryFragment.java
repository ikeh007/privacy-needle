package com.jaims.privacyneedle.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.jaims.privacyneedle.R;
import com.jaims.privacyneedle.adapters.PostsAdapter;
import com.jaims.privacyneedle.models.Post;
import com.jaims.privacyneedle.network.RetrofitClient;
import com.jaims.privacyneedle.network.WordPressAPI;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryFragment extends Fragment {

    private static final String ARG_CATEGORY_ID = "category_id";
    private int categoryId;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;



    public static CategoryFragment newInstance(int categoryId) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CATEGORY_ID, categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progress);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getArguments() != null) {
            categoryId = getArguments().getInt(ARG_CATEGORY_ID);
        }

        loadPosts();
    }

    private void loadPosts() {
        progressBar.setVisibility(View.VISIBLE);
        WordPressAPI api = RetrofitClient.getClient().create(WordPressAPI.class);
        Call<List<Post>> call;

        if (categoryId == 0) {
            // Home tab → all posts
            call = api.getPosts(15, 1);
        } else {
            // Specific category
            call = api.getPostsByCategory(categoryId, 10, 1);
        }

        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    recyclerView.setAdapter(new PostsAdapter(getContext(), response.body()));
                } else {
                    Toast.makeText(getContext(), "No posts found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
