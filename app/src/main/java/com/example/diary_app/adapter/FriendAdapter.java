package com.example.diary_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.diary_app.R;
import com.example.diary_app.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class FriendAdapter
        extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private List<User> friendList;

    public interface OnFriendActionListener {
        void onUnfriend(User user);
    }

    private OnFriendActionListener listener;

    public FriendAdapter(List<User> friendList, OnFriendActionListener listener) {
        this.friendList = friendList;
        this.listener = listener;
    }

    public void updateList(List<User> newList) {
        this.friendList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_friend,
                        parent,
                        false
                );
        return new FriendViewHolder(view);
    }

    public void onBindViewHolder(
            @NonNull FriendViewHolder holder,
            int position
    ) {
        User user = friendList.get(position);
        holder.txtName.setText(user.getUserName());

        Glide.with(holder.itemView.getContext())
                .load(user.getAvatarUrl())
                .placeholder(R.drawable.human_human)
                .circleCrop()
                .into(holder.imgAvatar);

        holder.btnDeleteFriend.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUnfriend(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendList != null ? friendList.size() : 0;
    }

    static class FriendViewHolder
            extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtName;
        ImageView btnDeleteFriend;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtName = itemView.findViewById(R.id.txtName);
            btnDeleteFriend = itemView.findViewById(R.id.btnDeleteFriend);
        }
    }
}
