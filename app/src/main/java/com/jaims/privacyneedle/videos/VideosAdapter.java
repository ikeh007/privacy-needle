package com.jaims.privacyneedle.videos;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.jaims.privacyneedle.R;
import com.jaims.privacyneedle.ads.AdPlacementPolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The video catalogue shows genuine publisher content between every ad:
 * one native ad after every third video, and one banner after five videos.
 * Ads are never inserted into or drawn over the actual YouTube player.
 */
final class VideosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_VIDEO = 0;
    private static final int TYPE_BANNER = 1;
    private static final int TYPE_NATIVE = 2;
    private static final int BANNER_AFTER_VIDEOS = 5;

    private static final class Row {
        final int type;
        final int index;
        Row(int type, int index) {
            this.type = type;
            this.index = index;
        }
    }

    private final Context context;
    private final List<VideoItem> videos;
    private final List<Row> rows = new ArrayList<>();
    private final List<NativeAd> nativeAds = new ArrayList<>();
    private final Map<Integer, NativeAdView> nativeViews = new HashMap<>();
    private AdView banner;

    VideosAdapter(Context context, List<VideoItem> videos) {
        this.context = context;
        this.videos = videos;
        refreshRows();
    }

    void setBanner(AdView loaded) {
        banner = loaded;
        refreshRows();
    }

    void setNativeAds(List<NativeAd> ads) {
        nativeAds.clear();
        nativeAds.addAll(ads);
        refreshRows();
    }

    void refreshRows() {
        rows.clear();
        boolean mayShowNative = AdPlacementPolicy.mayShowVideoNative(videos.size());
        boolean mayShowBanner = AdPlacementPolicy.mayShowVideoBanner(videos.size());
        int nativeIndex = 0;
        for (int i = 0; i < videos.size(); i++) {
            rows.add(new Row(TYPE_VIDEO, i));
            int watched = i + 1;
            if (mayShowNative && watched % 3 == 0 && nativeIndex < nativeAds.size()) {
                rows.add(new Row(TYPE_NATIVE, nativeIndex++));
            }
            if (mayShowBanner && watched == BANNER_AFTER_VIDEOS && banner != null) {
                rows.add(new Row(TYPE_BANNER, 0));
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup group, int type) {
        if (type != TYPE_VIDEO) {
            return new AdHolder(LayoutInflater.from(context).inflate(
                    type == TYPE_NATIVE ? R.layout.item_native_container
                            : R.layout.item_feed_ad, group, false));
        }
        return new VideoHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_video, group, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder base, int position) {
        Row row = rows.get(position);
        if (row.type != TYPE_VIDEO) {
            AdHolder holder = (AdHolder) base;
            holder.container.removeAllViews();
            if (row.type == TYPE_BANNER && banner != null) {
                attach(banner, holder.container);
            } else if (row.type == TYPE_NATIVE && row.index < nativeAds.size()) {
                NativeAdView adView = nativeViews.get(row.index);
                if (adView == null) {
                    adView = createNativeView(nativeAds.get(row.index));
                    nativeViews.put(row.index, adView);
                }
                attach(adView, holder.container);
            }
            return;
        }
        VideoHolder holder = (VideoHolder) base;
        VideoItem video = videos.get(row.index);
        holder.title.setText(video.title);
        Glide.with(holder.itemView)
                .load("https://i.ytimg.com/vi/" + video.id + "/hqdefault.jpg")
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .centerCrop()
                .into(holder.thumbnail);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, VideoPlayerActivity.class);
            intent.putExtra("video_id", video.id);
            intent.putExtra("video_title", video.title);
            context.startActivity(intent);
        });
    }

    private static void attach(View child, FrameLayout parent) {
        if (child.getParent() instanceof ViewGroup) {
            ((ViewGroup) child.getParent()).removeView(child);
        }
        parent.addView(child);
    }

    private NativeAdView createNativeView(NativeAd ad) {
        NativeAdView view = (NativeAdView) LayoutInflater.from(context)
                .inflate(R.layout.item_native_ad, null, false);
        TextView headline = view.findViewById(R.id.nativeHeadline);
        TextView body = view.findViewById(R.id.nativeBody);
        TextView advertiser = view.findViewById(R.id.nativeAdvertiser);
        android.widget.Button cta = view.findViewById(R.id.nativeCta);
        ImageView icon = view.findViewById(R.id.nativeIcon);
        MediaView media = view.findViewById(R.id.nativeMedia);

        headline.setText(ad.getHeadline());
        view.setHeadlineView(headline);
        if (ad.getBody() != null) body.setText(ad.getBody());
        else body.setVisibility(View.GONE);
        view.setBodyView(body);
        if (ad.getAdvertiser() != null) advertiser.setText(ad.getAdvertiser());
        else advertiser.setVisibility(View.GONE);
        view.setAdvertiserView(advertiser);
        if (ad.getCallToAction() != null) cta.setText(ad.getCallToAction());
        else cta.setVisibility(View.GONE);
        view.setCallToActionView(cta);
        if (ad.getIcon() != null) icon.setImageDrawable(ad.getIcon().getDrawable());
        else icon.setVisibility(View.GONE);
        view.setIconView(icon);
        if (ad.getMediaContent() != null) media.setMediaContent(ad.getMediaContent());
        else media.setVisibility(View.GONE);
        view.setMediaView(media);
        // The Google SDK owns the clicks. No custom click handlers on ad assets.
        view.setNativeAd(ad);
        return view;
    }

    @Override public int getItemCount() {
        return rows.size();
    }

    static final class AdHolder extends RecyclerView.ViewHolder {
        final FrameLayout container;
        AdHolder(View root) {
            super(root);
            container = root.findViewById(R.id.adContainer);
        }
    }

    static final class VideoHolder extends RecyclerView.ViewHolder {
        final ImageView thumbnail;
        final TextView title;
        VideoHolder(View root) {
            super(root);
            thumbnail = root.findViewById(R.id.videoThumbnail);
            title = root.findViewById(R.id.videoTitle);
        }
    }
}
