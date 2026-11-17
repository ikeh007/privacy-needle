package com.jaims.privacyneedle.videos;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdView;
import com.jaims.privacyneedle.R;
import com.jaims.privacyneedle.ads.AdPlacementPolicy;
import java.util.List;

final class VideosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIDEO = 0;
    private static final int BANNER = 1;
    private static final int AD_POSITION = 5;
    private final Context context;
    private final List<VideoItem> videos;
    private AdView banner;

    VideosAdapter(Context context, List<VideoItem> videos) {
        this.context = context;
        this.videos = videos;
    }
    void setBanner(AdView loaded) {
        banner = loaded;
        notifyDataSetChanged();
    }
    private boolean showAd() {
        return banner != null && AdPlacementPolicy.mayShowVideoBanner(videos.size());
    }
    @Override public int getItemViewType(int position) {
        return showAd() && position == AD_POSITION ? BANNER : VIDEO;
    }
    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup group, int type) {
        if (type == BANNER) {
            return new BannerHolder(LayoutInflater.from(context)
                    .inflate(R.layout.item_feed_ad, group, false));
        }
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_video, group, false));
    }
    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int index) {
        if (getItemViewType(index) == BANNER) {
            BannerHolder adHolder = (BannerHolder) holder;
            if (banner != null) {
                if (banner.getParent() instanceof ViewGroup) {
                    ((ViewGroup) banner.getParent()).removeView(banner);
                }
                adHolder.container.removeAllViews();
                adHolder.container.addView(banner);
            }
            return;
        }
        int itemIndex = showAd() && index > AD_POSITION ? index - 1 : index;
        VideoItem video = videos.get(itemIndex);
        Holder vh = (Holder) holder;
        vh.title.setText(video.title);
        Glide.with(vh.itemView).load("https://i.ytimg.com/vi/" + video.id + "/hqdefault.jpg")
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .centerCrop().into(vh.thumbnail);
        vh.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, VideoPlayerActivity.class);
            intent.putExtra("video_id", video.id);
            intent.putExtra("video_title", video.title);
            context.startActivity(intent);
        });
    }
    @Override public int getItemCount() {
        return videos.size() + (showAd() ? 1 : 0);
    }
    static final class BannerHolder extends RecyclerView.ViewHolder {
        final FrameLayout container;
        BannerHolder(View root) {
            super(root);
            container = root.findViewById(R.id.adContainer);
        }
    }
    static final class Holder extends RecyclerView.ViewHolder {
        final ImageView thumbnail;
        final TextView title;
        Holder(View root) {
            super(root);
            thumbnail = root.findViewById(R.id.videoThumbnail);
            title = root.findViewById(R.id.videoTitle);
        }
    }
}
