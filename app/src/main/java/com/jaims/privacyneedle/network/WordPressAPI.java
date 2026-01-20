package com.jaims.privacyneedle.network;

import com.jaims.privacyneedle.models.Post;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WordPressAPI {
    // Get latest posts
    @GET("posts?_embed")
    Call<List<Post>> getPosts(
            @Query("per_page") int perPage,
            @Query("page") int page
    );

    @GET("posts?_embed")
    Call<List<Post>> getPostsByCategory(
            @Query("categories") int categoryId,
            @Query("per_page") int perPage,
            @Query("page") int page
    );
    @GET("posts")
    Call<List<Post>> getPostsBySearch(@Query("search") String keyword);


}
