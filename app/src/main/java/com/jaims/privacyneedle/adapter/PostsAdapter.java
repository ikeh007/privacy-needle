package com.jaims.privacyneedle.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdView;
import com.jaims.privacyneedle.R;
import com.jaims.privacyneedle.models.Post;
import com.jaims.privacyneedle.ui.PostDetailsActivity;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ARTICLE = 0;
    private static final int ADVERTISEMENT = 1;
    private static final int AD_POSITION = 5; // After five actual publisher articles.
    private static final int MINIMUM_ARTICLES_FOR_AD = 7;

    private final Context context;
    private final List<Post> postList;
    private AdView banner;
    private boolean advertisingEnabled = true;

    public PostsAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    public void setBanner(AdView loadedBanner) {
        banner = loadedBanner;
        notifyDataSetChanged();
    }

    public void setAdvertisingEnabled(boolean enabled) {
        advertisingEnabled = enabled;
        notifyDataSetChanged();
    }

    private boolean hasAdRow() {
        return advertisingEnabled && banner != null && postList.size() >= MINIMUM_ARTICLES_FOR_AD;
    }

    @Override
    public int getItemViewType(int position) {
        return hasAdRow() && position == AD_POSITION ? ADVERTISEMENT : ARTICLE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        if (type == ADVERTISEMENT) {
            return new AdViewHolder(LayoutInflater.from(context)
                    .inflate(R.layout.item_feed_ad, parent, false));
        }
        return new PostViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_post, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder generic, int position) {
        if (generic instanceof AdViewHolder) {
            AdViewHolder holder = (AdViewHolder) generic;
            if (banner != null) {
                if (banner.getParent() instanceof ViewGroup) {
                    ((ViewGroup) banner.getParent()).removeView(banner);
                }
                holder.container.removeAllViews();
                holder.container.addView(banner);
            }
            return;
        }

        PostViewHolder holder = (PostViewHolder) generic;
        int index = hasAdRow() && position > AD_POSITION ? position - 1 : position;
        Post post = postList.get(index);

        String title = post.getTitle() != null && post.getTitle().rendered != null
                ? post.getTitle().rendered : "Untitled";
        String excerpt = post.getExcerpt() != null && post.getExcerpt().rendered != null
                ? post.getExcerpt().rendered : "";
        holder.title.setText(HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY));
        holder.excerpt.setText(HtmlCompat.fromHtml(excerpt, HtmlCompat.FROM_HTML_MODE_LEGACY));

        String rawDate = post.getDate();
        holder.textDate.setText(rawDate != null && !rawDate.isEmpty()
                ? rawDate.split("T")[0] : "");

        String imageUrl = post.getFeaturedImage();
        if (imageUrl != null) {
            Glide.with(holder.itemView).load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .centerCrop().into(holder.image);
        } else {
            Glide.with(holder.itemView).clear(holder.image);
            holder.image.setImageResource(R.drawable.ic_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailsActivity.class);
            intent.putExtra("url", post.getLink());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return postList.size() + (hasAdRow() ? 1 : 0);
    }

    static class AdViewHolder extends RecyclerView.ViewHolder {
        final FrameLayout container;
        AdViewHolder(@NonNull View v) {
            super(v);
            container = v.findViewById(R.id.adContainer);
        }
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        final TextView title, excerpt, textDate;
        final ImageView image;
        PostViewHolder(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.postTitle);
            excerpt = v.findViewById(R.id.postExcerpt);
            image = v.findViewById(R.id.postImage);
            textDate = v.findViewById(R.id.postDate);
        }
    }
}
