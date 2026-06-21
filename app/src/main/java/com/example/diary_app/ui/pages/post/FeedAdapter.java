package com.example.diary_app.ui.pages.post;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.diary_app.R;
import com.example.diary_app.data.model.Post;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    private Context context;
    private List<Post> postList;
    private OnPostInteractionListener listener;

    // 1. TẠO INTERFACE: Bộ đàm liên lạc giữa Adapter và Fragment
    public interface OnPostInteractionListener {
        void onReactionClick(Post post, String reactionType);
    }

    // 2. CONSTRUCTOR: Ép buộc phải truyền vào bộ đàm (listener)
    public FeedAdapter(Context context, List<Post> postList, OnPostInteractionListener listener) {
        this.context = context;
        this.postList = postList;
        this.listener = listener;
    }

    public void setPosts(List<Post> posts) {
        this.postList = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feed, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        Post currentPost = postList.get(position);

        // --- A. HIỂN THỊ DỮ LIỆU BÀI VIẾT ---
        holder.tvUsername.setText(currentPost.getUserName());
        holder.tvCaption.setText(currentPost.getCaption());

        // --- B. XỬ LÝ SỰ KIỆN THẢ CẢM XÚC ---
        // Khi bấm vào cảm xúc nào, Adapter sẽ dùng bộ đàm gọi về Fragment kèm theo ID bài viết
        holder.reactHeart.setOnClickListener(v -> listener.onReactionClick(currentPost, "heart"));
        holder.reactHappy.setOnClickListener(v -> listener.onReactionClick(currentPost, "happy"));
        holder.reactShy.setOnClickListener(v -> listener.onReactionClick(currentPost, "shy"));
        holder.reactCry.setOnClickListener(v -> listener.onReactionClick(currentPost, "cry"));
        holder.reactCalm.setOnClickListener(v -> listener.onReactionClick(currentPost, "calm"));
        holder.reactFire.setOnClickListener(v -> listener.onReactionClick(currentPost, "fire"));
        //Load ảnh
        if (currentPost.getImageUrl() != null && !currentPost.getImageUrl().isEmpty()) {

            // Dùng thư viện Glide để tải ảnh
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(currentPost.getImageUrl())
                    .into(holder.imgPostContent);
            // Đảm bảo khung ảnh hiện ra
            holder.imgPostContent.setVisibility(android.view.View.VISIBLE);

        } else {
            // Nếu bài viết không có ảnh, ra lệnh giấu luôn cái khung đi
            holder.imgPostContent.setVisibility(android.view.View.GONE);
        }
    }
    @Override
    public int getItemCount() {
        return postList != null ? postList.size() : 0;
    }

    // =========================================================
    // LỚP VIEWHOLDER: Tìm và lưu trữ ID của các View
    // =========================================================
    public static class FeedViewHolder extends RecyclerView.ViewHolder {

        TextView reactHeart, reactHappy, reactShy, reactCry, reactCalm, reactFire;

        TextView tvUsername, tvCaption;
        android.widget.ImageView imgPostContent;
        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);

            reactHeart = itemView.findViewById(R.id.reactHeart);
            reactHappy = itemView.findViewById(R.id.reactHappy);
            reactShy = itemView.findViewById(R.id.reactShy);
            reactCry = itemView.findViewById(R.id.reactCry);
            reactCalm = itemView.findViewById(R.id.reactCalm);
            reactFire = itemView.findViewById(R.id.reactFire);

            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            imgPostContent = itemView.findViewById(R.id.imgDiary);
        }
    }
}
