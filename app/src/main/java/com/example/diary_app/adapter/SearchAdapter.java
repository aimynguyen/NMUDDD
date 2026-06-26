package com.example.diary_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.diary_app.R;
import com.example.diary_app.data.model.Post;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<Post> postList = new ArrayList<>();

    public void setData(List<Post> newList) {
        this.postList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);

        // 1. Gán text dữ liệu cơ bản
        holder.tvUsername.setText(post.getUserName());
        String caption = post.getCaption(); //ẩn ô caption khi caption rỗng

        if (caption == null || caption.trim().isEmpty()) {
            holder.tvCaption.setVisibility(View.GONE);
        } else {
            holder.tvCaption.setVisibility(View.VISIBLE);
            holder.tvCaption.setText(caption);
        }

        // 2. Xử lý hiển thị Location nếu có
        if (post.getLocation() != null && post.getLocation().getCity() != null && !post.getLocation().getCity().isEmpty()) {
            holder.layoutLocation.setVisibility(View.VISIBLE);
            holder.tvLocation.setText(post.getLocation().getCity());
        } else {
            holder.layoutLocation.setVisibility(View.GONE);
        }

        // 3. Load ảnh Nhật ký bằng Glide
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(post.getImageUrl())
                    .placeholder(android.R.color.darker_gray)
                    .into(holder.imgDiary);
        } else {
            holder.imgDiary.setImageResource(android.R.color.darker_gray);
        }

        // 4. Load ảnh User Avatar bằng Glide
        if (post.getUserAvatar() != null && !post.getUserAvatar().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(post.getUserAvatar())
                    .circleCrop() // Biến ảnh avatar thành hình tròn mượt mà
                    .into(holder.imgAvatar);
        }

        // 5. Hiển thị Emotion (Mood) của người đăng bài
        String mood = post.getEmotion();
        if (mood != null && !mood.trim().isEmpty()) {
            holder.imgMood.setVisibility(View.VISIBLE);
            holder.imgMood.setText(getMoodIcon(mood));
        } else {
            holder.imgMood.setVisibility(View.GONE);
        }
    }

    private String getMoodIcon(String moodName) {
        if (moodName == null) return "😐";
        switch (moodName.toUpperCase().trim()) {
            case "HAPPY":   return "😊";
            case "SAD":     return "😭";
            case "CALM":    return "😌";
            case "ANGRY":   return "😡";
            case "NEUTRAL": return "😳";
            default:        return "😐";
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgDiary, imgAvatar;
        TextView imgMood;
        TextView tvUsername, tvLocation, tvCaption;
        LinearLayout layoutLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDiary = itemView.findViewById(R.id.imgDiary);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            imgMood = itemView.findViewById(R.id.imgMood);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            layoutLocation = itemView.findViewById(R.id.layoutLocation);
        }
    }
}