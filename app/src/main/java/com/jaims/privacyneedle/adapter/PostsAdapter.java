package com.jaims.privacyneedle.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.nativead.MediaView;
import com.jaims.privacyneedle.R;
import com.jaims.privacyneedle.ads.AdPlacementPolicy;
import com.jaims.privacyneedle.models.Post;
import com.jaims.privacyneedle.ui.PostDetailsActivity;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ARTICLE = 0;
    private static final int TYPE_BANNER = 1;
    private static final int TYPE_NATIVE = 2;

    private static final class Row {
        final int type;
        final int contentIndex;
        Row(int type, int contentIndex) {
            this.type = type;
            this.contentIndex = contentIndex;
        }
    }

    private final Context context;
    private final List<Post> postList;
    private final List<Row> rows = new ArrayList<>();
    private final List<NativeAd> nativeAds = new ArrayList<>();
    private final Map<Integer, NativeAdView> nativeViews = new HashMap<>();
    private AdView banner;
    private boolean advertisingEnabled = true;

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.postList = posts;
        refreshRows();
    }

    public void setBanner(AdView loadedBanner) {
        banner = loadedBanner;
        refreshRows();
    }

    public void setNativeAds(List<NativeAd> ads) {
        nativeAds.clear();
        nativeAds.addAll(ads);
        refreshRows();
    }

    public void setAdvertisingEnabled(boolean enabled) {
        advertisingEnabled = enabled;
        refreshRows();
    }

    public void refreshRows() {
        rows.clear();
        boolean allowed = advertisingEnabled
                && AdPlacementPolicy.mayShowFeedBanner(postList.size(), false);
        int nativeIndex = 0;
        for (int i = 0; i < postList.size(); i++) {
            rows.add(new Row(TYPE_ARTICLE, i));
            if (allowed) {
                int articlesSeen = i + 1;
                if (articlesSeen % AdPlacementPolicy.NATIVE_ARTICLE_INTERVAL == 0
                        && nativeIndex < nativeAds.size()) {
                    rows.add(new Row(TYPE_NATIVE, nativeIndex++));
                }
                // One separate banner after 5 actual publisher articles.
                if (articlesSeen == 5 && banner != null) {
                    rows.add(new Row(TYPE_BANNER, 0));
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        if (type == TYPE_NATIVE || type == TYPE_BANNER) {
            return new AdHolder(LayoutInflater.from(context)
                    .inflate(R.layout.item_feed_ad, parent, false));
        }
        return new PostHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_post, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (row.type == TYPE_BANNER || row.type == TYPE_NATIVE) {
            AdHolder adHolder = (AdHolder) holder;
            adHolder.container.removeAllViews();
            if (row.type == TYPE_BANNER && banner != null) {
                moveIntoContainer(banner, adHolder.container);
            } else if (row.type == TYPE_NATIVE && row.contentIndex < nativeAds.size()) {
                NativeAdView nativeView = nativeViews.get(row.contentIndex);
                if (nativeView == null) {
                    nativeView = createNativeView(nativeAds.get(row.contentIndex));
                    nativeViews.put(row.contentIndex, nativeView);
                }
                moveIntoContainer(nativeView, adHolder.container);
            }
            return;
        }

        PostHolder postHolder = (PostHolder) holder;
        Post post = postList.get(row.contentIndex);
        String title = post.getTitle() != null && post.getTitle().rendered != null
                ? post.getTitle().rendered : "Untitled";
        String excerpt = post.getExcerpt() != null && post.getExcerpt().rendered != null
                ? post.getExcerpt().rendered : "";
        postHolder.title.setText(HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY));
        postHolder.excerpt.setText(HtmlCompat.fromHtml(excerpt, HtmlCompat.FROM_HTML_MODE_LEGACY));

        String date = post.getDate();
        postHolder.date.setText(date == null || date.isEmpty() ? "" : date.split("T")[0]);

        String photo = post.getFeaturedImage();
        if (photo != null && !photo.isEmpty()) {
            Glide.with(postHolder.itemView).load(photo)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .centerCrop().into(postHolder.image);
        } else {
            Glide.with(postHolder.itemView).clear(postHolder.image);
            postHolder.image.setImageResource(R.drawable.ic_placeholder);
        }

        postHolder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, PostDetailsActivity.class);
            i.putExtra("url", post.getLink());
            context.startActivity(i);
        });
    }

    private static void moveIntoContainer(View child, FrameLayout container) {
        if (child.getParent() instanceof ViewGroup) {
            ((ViewGroup) child.getParent()).removeView(child);
        }
        container.addView(child);
    }

    private NativeAdView createNativeView(NativeAd ad) {
        NativeAdView nativeView = (NativeAdView) LayoutInflater.from(context)
                .inflate(R.layout.item_native_ad, null, false);

        TextView headline = nativeView.findViewById(R.id.nativeHeadline);
        TextView body = nativeView.findViewById(R.id.nativeBody);
        TextView advertiser = nativeView.findViewById(R.id.nativeAdvertiser);
        Button cta = nativeView.findViewById(R.id.nativeCta);
        ImageView icon = nativeView.findViewById(R.id.nativeIcon);
        MediaView media = nativeView.findViewById(R.id.nativeMedia);

        nativeView.setHeadlineView(headline);
        headline.setText(ad.getHeadline());

        nativeView.setBodyView(body);
        if (ad.getBody() == null) body.setVisibility(View.GONE);
        else body.setText(ad.getBody());

        nativeView.setAdvertiserView(advertiser);
        if (ad.getAdvertiser() == null) advertiser.setVisibility(View.GONE);
        else advertiser.setText(ad.getAdvertiser());

        nativeView.setCallToActionView(cta);
        if (ad.getCallToAction() == null) cta.setVisibility(View.GONE);
        else cta.setText(ad.getCallToAction());

        nativeView.setIconView(icon);
        if (ad.getIcon() == null) icon.setVisibility(View.GONE);
        else icon.setImageDrawable(ad.getIcon().getDrawable());

        nativeView.setMediaView(media);
        if (ad.getMediaContent() == null) media.setVisibility(View.GONE);
        else media.setMediaContent(ad.getMediaContent());

        // No custom click listeners on ad assets. SDK registers valid click views.
        nativeView.setNativeAd(ad);
        return nativeView;
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static final class AdHolder extends RecyclerView.ViewHolder {
        final FrameLayout container;
        AdHolder(View view) {
            super(view);
            container = view.findViewById(R.id.adContainer);
        }
    }

    static final class PostHolder extends RecyclerView.ViewHolder {
        final TextView title, excerpt, date;
        final ImageView image;
        PostHolder(View view) {
            super(view);
            title = view.findViewById(R.id.postTitle);
            excerpt = view.findViewById(R.id.postExcerpt);
            date = view.findViewById(R.id.postDate);
            image = view.findViewById(R.id.postImage);
        }
    }
}
