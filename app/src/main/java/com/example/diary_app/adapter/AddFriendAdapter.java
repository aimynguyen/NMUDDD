package com.example.diary_app.adapter;

import static java.lang.Long.parseLong;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.diary_app.R;
import com.example.diary_app.model.FriendRequestModel;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.ViewHolder> {
    private List<FriendRequestModel> list;
    private OnAddClickListener onAddClickListener;

    // Interface để xử lý sự kiện click nút Add từ Fragment
    public interface OnAddClickListener {
        void onAddClick(FriendRequestModel friendRequest, int position);
    }

    public AddFriendAdapter(List<FriendRequestModel> list, OnAddClickListener onAddClickListener) {
        this.list = list;
        this.onAddClickListener = onAddClickListener;
    }

    @NonNull
    @Override
    public AddFriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_add_friend, parent, false);
        return new AddFriendAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddFriendAdapter.ViewHolder holder, int position) {
        FriendRequestModel friendRequestModel = list.get(position);

        if (friendRequestModel != null) {
            holder.tvName.setText("ID: " + friendRequestModel.getReceiverId());

            // Xử lý sự kiện khi click vào nút "Add"
            holder.btnAdd.setOnClickListener(v -> {
                if (onAddClickListener != null) {
                    onAddClickListener.onAddClick(friendRequestModel, position);
                }
            });

            //Glide.with(holder.itemView.getContext()).load(link_anh).into(holder.imgAvatar);
        }
    }

    @Override
    public long getItemId(int position) {
        if (list != null && list.get(position) != null && list.get(position).getReceiverId() != null) {
            try {
                return parseLong(list.get(position).getReceiverId());
            } catch (NumberFormatException e) {
                return position;
            }
        }
        return position;
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    // ViewHolder lưu trữ các View thành phần bên trong item_add_friend.xml
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