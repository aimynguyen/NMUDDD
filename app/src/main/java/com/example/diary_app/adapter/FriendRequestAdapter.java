package com.example.diary_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.diary_app.R;
import com.example.diary_app.data.model.User;

import java.util.List;

public class FriendRequestAdapter
        extends RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder> {

    private List<User> requestList;
    private OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAccept(User user);
        void onReject(User user);
    }

    public FriendRequestAdapter(List<User> requestList, OnRequestActionListener listener) {
        this.requestList = requestList;
        this.listener = listener;
    }

    public void updateList(List<User> newList) {
        this.requestList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_friend_request,
                        parent,
                        false
                );

        return new RequestViewHolder(view);

    }

    @Override
    public void onBindViewHolder(
            @NonNull RequestViewHolder holder,
            int position
    ) {
        User user = requestList.get(position);
        
        if (holder.txtName != null && user.getUserName() != null) {
            holder.txtName.setText(user.getUserName());
        }

        if (holder.imgAvatar != null) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.human_human)
                    .circleCrop()
                    .into(holder.imgAvatar);
        }

            holder.btnAccept.setOnClickListener(v -> {
                if (listener != null) listener.onAccept(user);
            });

            holder.btnReject.setOnClickListener(v -> {
                if (listener != null) listener.onReject(user);
            });
    }

    @Override
    public int getItemCount() {
        return requestList != null ? requestList.size() : 0;
    }

    static class RequestViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgAvatar;
        TextView txtName;
        Button btnAccept, btnReject;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtName = itemView.findViewById(R.id.txtName);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}