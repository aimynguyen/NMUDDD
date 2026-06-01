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
import com.example.diary_app.model.UserModel;

import java.util.ArrayList;

public class FriendAdapter
        extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private ArrayList<UserModel> friendList;

    public FriendAdapter(ArrayList<UserModel> friendList) {

        this.friendList = friendList;

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

    @Override
    public void onBindViewHolder(
            @NonNull FriendViewHolder holder,
            int position
    ) {

        UserModel user = friendList.get(position);

        holder.txtName.setText(user.getUserName());

        Glide.with(holder.itemView.getContext())
                .load(user.getAvatarUrl())
                .placeholder(R.drawable.human_human)
                .into(holder.imgAvatar);

    }

    @Override
    public int getItemCount() {

        return friendList.size();

    }

    static class FriendViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgAvatar;

        TextView txtName;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);

            imgAvatar =
                    itemView.findViewById(R.id.imgAvatar);

            txtName =
                    itemView.findViewById(R.id.txtName);
        }
    }
}