package com.example.diary_app.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.example.diary_app.R;
import com.example.diary_app.data.model.Post;
import java.util.List;

public class AdminPostAdapter extends RecyclerView.Adapter<AdminPostAdapter.AdminPostViewHolder> {

    private List<Post> postList;
    private OnPostDeleteListener deleteListener;

    public interface OnPostDeleteListener {
        void onDeleteClick(Post post, int position);
    }

    public AdminPostAdapter(List<Post> postList, OnPostDeleteListener deleteListener) {
        this.postList = postList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public AdminPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_adminreview, parent, false);
        return new AdminPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminPostViewHolder holder, int position) {
        Post post = postList.get(position);
        Context context = holder.itemView.getContext();

        // 1. Đổ dữ liệu chữ (Username, Caption, Location)
        holder.tvUsername.setText(post.getUserName() != null ? post.getUserName() : "Ẩn danh");
        String caption = post.getCaption();

        if (caption == null || caption.trim().isEmpty()) {
            holder.tvCaption.setVisibility(View.GONE);
        } else {
            holder.tvCaption.setVisibility(View.VISIBLE);
            holder.tvCaption.setText(caption);
        }

        if (post.getLocation() != null && post.getLocation().getCity() != null) {
            holder.tvLocation.setText(post.getLocation().getCity());
        } else {
            holder.tvLocation.setText("Không rõ vị trí");
        }

        // 2. Load ảnh Avatar người dùng bằng Glide vào Background của View (Thẻ <View> trong XML)
        if (post.getUserAvatar() != null && !post.getUserAvatar().isEmpty()) {
            Glide.with(context)
                    .load(post.getUserAvatar())
                    .placeholder(R.drawable.avatar_circle)
                    .into(new com.bumptech.glide.request.target.CustomViewTarget<View, android.graphics.drawable.Drawable>(holder.imgAvatar) {
                        @Override
                        public void onLoadFailed(@Nullable android.graphics.drawable.Drawable errorDrawable) {
                            holder.imgAvatar.setBackgroundResource(R.drawable.avatar_circle);
                        }

                        @Override
                        public void onResourceReady(@NonNull android.graphics.drawable.Drawable resource, @Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.drawable.Drawable> transition) {
                            holder.imgAvatar.setBackground(resource);
                        }

                        @Override
                        protected void onResourceCleared(@Nullable android.graphics.drawable.Drawable placeholder) {
                            holder.imgAvatar.setBackground(placeholder);
                        }
                    });
        } else {
            holder.imgAvatar.setBackgroundResource(R.drawable.avatar_circle);
        }

        // 3. Load hình ảnh bài viết (imgDiary) kết hợp bo góc 24dp bằng code để tránh lỗi hiển thị
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.imgDiary.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(post.getImageUrl())
                    .transform(new CenterCrop(), new GranularRoundedCorners(24, 24, 24, 24)) // Bo góc 24dp khớp với XML của bạn
                    .into(holder.imgDiary);
        } else {
            // Nếu không có hình từ database, đổi thành ảnh mặc định hoặc ẩn hẳn view đi
            holder.imgDiary.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // 4. XỬ LÝ SỰ KIỆN CLICK NÚT REMOVE (Đã sửa lỗi cảnh báo vị trí an toàn)
        holder.btnRemove.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                Post currentPost = postList.get(currentPosition);
                showConfirmationDialog(context, currentPost, currentPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList != null ? postList.size() : 0;
    }

    private void showConfirmationDialog(Context context, Post post, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa bài")
                .setMessage("Bạn có chắc muốn xóa bài viết này của '" + post.getUserName() + "' không?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    if (deleteListener != null) {
                        deleteListener.onDeleteClick(post, position);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    public static class AdminPostViewHolder extends RecyclerView.ViewHolder {
        Button btnRemove;
        View imgAvatar;
        ImageView imgDiary;
        TextView tvUsername, tvLocation, tvCaption;

        public AdminPostViewHolder(@NonNull View itemView) {
            super(itemView);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            imgDiary = itemView.findViewById(R.id.imgDiary);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvCaption = itemView.findViewById(R.id.tvCaption);
        }
    }
}