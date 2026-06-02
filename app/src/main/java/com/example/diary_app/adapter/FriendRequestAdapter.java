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
import com.example.diary_app.model.UserModel;

import java.util.ArrayList;

public class FriendRequestAdapter
        extends RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder> {

    private ArrayList<UserModel> requestList;

    public FriendRequestAdapter(ArrayList<UserModel> requestList) {

        this.requestList = requestList;

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

        UserModel user = requestList.get(position);

        holder.txtName.setText(user.getUserName());

        Glide.with(holder.itemView.getContext())
                .load(user.getAvatarUrl())
                .placeholder(R.drawable.human_human)
                .into(holder.imgAvatar);

        // temporary button
        holder.btnAccept.setOnClickListener(v -> {

        });

        holder.btnReject.setOnClickListener(v -> {

        });

    }

    @Override
    public int getItemCount() {

        return requestList.size();

    }

    static class RequestViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgAvatar;

        TextView txtName;

        Button btnAccept, btnReject;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            imgAvatar =
                    itemView.findViewById(R.id.imgAvatar);

            txtName =
                    itemView.findViewById(R.id.txtName);

            btnAccept =
                    itemView.findViewById(R.id.btnAccept);


        }
    }
}