package com.jaims.privacyneedle.videos;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.jaims.privacyneedle.R;

import java.util.List;

final class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.Holder> {
    private final Context context;
    private final List<VideoItem> videos;

    VideosAdapter(Context context, List<VideoItem> videos) {
        this.context = context;
        this.videos = videos;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup group, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_video, group, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int index) {
        VideoItem video = videos.get(index);
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

    @Override
    public int getItemCount() {
        return videos.size();
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
