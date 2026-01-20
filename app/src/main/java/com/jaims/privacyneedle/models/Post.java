package com.jaims.privacyneedle.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Post {
    private int id;
    private String date;

    private Title title;

    @SerializedName("excerpt")
    private Excerpt excerpt;

    @SerializedName("content")
    private Content content;

    @SerializedName("link")
    private String link;

    // Embedded media (featured image)
    @SerializedName("_embedded")
    private Embedded embedded;




    // Nested classes
    public static class Title {
        @SerializedName("rendered")
        public String rendered;
    }

    public static class Excerpt {
        @SerializedName("rendered")
        public String rendered;
    }

    public static class Content {
        @SerializedName("rendered")
        public String rendered;
    }

    public static class Embedded {
        @SerializedName("wp:featuredmedia")
        public List<FeaturedMedia> featuredMedia;
    }

    public static class FeaturedMedia {
        @SerializedName("source_url")
        public String sourceUrl;
    }

    // Getters
    public int getId() { return id; }
    public String getDate() { return date; }
    public Title getTitle() { return title; }
    public Excerpt getExcerpt() { return excerpt; }
    public Content getContent() { return content; }
    public String getLink() { return link; }

    // Get featured image safely
    public String getFeaturedImage() {
        if (embedded != null && embedded.featuredMedia != null && !embedded.featuredMedia.isEmpty()) {
            return embedded.featuredMedia.get(0).sourceUrl;
        }
        return null; // fallback if no image
    }

}
