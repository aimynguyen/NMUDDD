package com.example.diary_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Đảm bảo bạn đã thêm thư viện Glide vào build.gradle
import com.example.diary_app.R;
import com.example.diary_app.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.ViewHolder> {
    private List<User> userList;
    private OnAddClickListener onAddClickListener;

    public interface OnAddClickListener {
        void onAddClick(User targetUser, int position);
    }

    public SearchUserAdapter(List<User> userList, OnAddClickListener onAddClickListener) {
        this.userList = userList;
        this.onAddClickListener = onAddClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_add_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        if (user != null) {
            // 1. Hiển thị UserName từ Model của bạn
            holder.tvName.setText(user.getUserName() != null ? user.getUserName() : user.getEmail());

            // 2. Load ảnh đại diện bằng Glide nếu có avatarUrl
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(user.getAvatarUrl())
                        .placeholder(R.drawable.ic_launcher_foreground) // Ảnh mặc định khi đang load
                        .into(holder.imgAvatar);
            } else {
                holder.imgAvatar.setImageResource(R.drawable.ic_launcher_foreground);
            }

            // 3. Lấy ID của bạn (người đang dùng app)
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // 4. Kiểm tra xem bạn đã nằm trong danh sách friendIds của người này chưa
            List<String> targetFriendIds = user.getFriendIds();

            if (targetFriendIds != null && targetFriendIds.contains(currentUserId)) {
                // 1. Nếu đã là bạn
                holder.btnAdd.setText("Đã là bạn");
                holder.btnAdd.setEnabled(false);
                holder.btnAdd.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY));
            } else if (sentRequestIds != null && sentRequestIds.contains(user.getUid())) {
                // 2. Nếu ĐÃ GỬI lời mời và đang chờ chấp nhận
                holder.btnAdd.setText("Đã gửi");
                holder.btnAdd.setEnabled(false);
                holder.btnAdd.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFA500"))); // Màu cam chờ đợi
            } else {
                // 3. Nếu chưa có mối quan hệ nào
                holder.btnAdd.setText("Add");
                holder.btnAdd.setEnabled(true);
                holder.btnAdd.setBackgroundTintList(null);
            }

            // 5. Xử lý sự kiện click nút Add
            holder.btnAdd.setOnClickListener(v -> {
                if (onAddClickListener != null) {
                    onAddClickListener.onAddClick(user, position);

                    // Sau khi bấm, tạm thời đổi giao diện thành "Đã gửi" để tránh bấm liên tục
                    holder.btnAdd.setText("Đã gửi");
                    holder.btnAdd.setEnabled(false);
                    holder.btnAdd.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#888888")));
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    private List<String> sentRequestIds = new ArrayList<>();

    // Sửa lại hàm updateList
    public void updateList(List<User> newList, List<String> sentIds) {
        this.userList = newList;
        this.sentRequestIds = sentIds;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName;
        MaterialButton btnAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}