package com.jaims.privacyneedle.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jaims.privacyneedle.R;
import com.jaims.privacyneedle.models.Post;
import com.jaims.privacyneedle.ui.PostDetailsActivity;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    private final Context context;
    private final List<Post> postList;

    public PostsAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        // WordPress plugins can return missing or null rendered fields.
        String title = post.getTitle() != null && post.getTitle().rendered != null
                ? post.getTitle().rendered : "Untitled";
        String excerpt = post.getExcerpt() != null && post.getExcerpt().rendered != null
                ? post.getExcerpt().rendered : "";
        holder.title.setText(androidx.core.text.HtmlCompat.fromHtml(
                title, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY));
        holder.excerpt.setText(androidx.core.text.HtmlCompat.fromHtml(
                excerpt, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY));

        // Format date
        String rawDate = post.getDate();
        if (rawDate != null && !rawDate.isEmpty()) {
            String formattedDate = rawDate.split("T")[0];
            holder.textDate.setText(formattedDate);
        } else {
            holder.textDate.setText("");
        }

        // Load featured image
        String imageUrl = post.getFeaturedImage();
        if (imageUrl != null) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.ic_placeholder);
        }

        // Handle click → open PostDetailsActivity with URL
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), PostDetailsActivity.class);
            intent.putExtra("url", post.getLink()); // only pass the URL
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return postList == null ? 0 : postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView title, excerpt, textDate;
        ImageView image;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.postTitle);
            excerpt = itemView.findViewById(R.id.postExcerpt);
            image = itemView.findViewById(R.id.postImage);
            textDate = itemView.findViewById(R.id.postDate);
        }
    }
}
