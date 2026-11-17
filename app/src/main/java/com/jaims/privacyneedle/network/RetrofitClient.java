package com.jaims.privacyneedle.network;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {
    private static final String BASE_URL = "https://privacyneedle.com/wp-json/wp/v2/";
    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("User-Agent", "PrivacyNeedleAndroid/1.1")
                    .build()))
            .build();
    private static final Retrofit RETROFIT = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(HTTP)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private RetrofitClient() {}
    public static Retrofit getClient() { return RETROFIT; }
    public static WordPressAPI getAPI() { return RETROFIT.create(WordPressAPI.class); }
}
