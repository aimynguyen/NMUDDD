package com.example.diary_app.ui.pages.post;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
            Glide.with(holder.itemView.getContext())
                    .load(currentPost.getImageUrl())
                    .into(holder.imgPostContent);
            // Đảm bảo khung ảnh hiện ra
            holder.imgPostContent.setVisibility(View.VISIBLE);

        } else {
            // Nếu bài viết không có ảnh, ra lệnh giấu luôn cái khung đi
            holder.imgPostContent.setVisibility(View.GONE);
        }
        // ==========================================
        // 1. HIỂN THỊ AVATAR NGƯỜI DÙNG
        // ==========================================
        if (currentPost.getUserAvatar() != null && !currentPost.getUserAvatar().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(currentPost.getUserAvatar())
                    .circleCrop()
                    .into(holder.imgAvatar);
        } else {
            // Nếu user chưa cài avatar, cho hiển thị cái nền tròn mặc định
            holder.imgAvatar.setImageResource(R.drawable.avatar_circle);
        }
        // ==========================================
        // 2. HIỂN THỊ MOOD CỦA BÀI VIẾT
        // ==========================================
        String mood = currentPost.getEmotion();

        if (mood != null && !mood.isEmpty()) {
            holder.imgMood.setVisibility(View.VISIBLE); // Bật hình tròn lên
            switch (mood) {
                case "😊":
                    holder.imgMood.setText("😊");
                    break;
                case "😳":
                    holder.imgMood.setText("😳");
                    break;
                case "😭":
                    holder.imgMood.setText("😭");
                    break;
                case "😌":
                    holder.imgMood.setText("😌");
                    break;
                case "💗":
                    holder.imgMood.setText("💗");
                    break;
                default:
                    // Nếu là mood lạ, tạm thời ẩn đi
                    holder.imgMood.setVisibility(View.GONE);
                    break;
            }
        } else {
            // Nếu bài viết không gắn Mood, giấu hình tròn góc phải đi cho đỡ trống
            holder.imgMood.setVisibility(View.GONE);
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
        android.widget.ImageView imgAvatar;
        android.widget.TextView imgMood;
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
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            imgMood = itemView.findViewById(R.id.imgMood);
        }
    }
}
