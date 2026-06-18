package com.example.diary_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.diary_app.R;
import com.example.diary_app.data.model.Notification;
import com.example.diary_app.model.NotificationModel;
import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Notification> list;
    private OnItemClickListener listener;

    public NotificationAdapter(Context context, ArrayList<Notification> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_notification, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Notification notification = list.get(position);

        holder.txtContent.setText(notification.getMessage());
        holder.txtTime.setText(getTimeAgo(notification.getCreatedAt()));

        // Reset trạng thái
        holder.viewUnread.setVisibility(View.GONE);
        holder.viewWarning.setVisibility(View.GONE);

        // Chưa đọc
        if (!notification.isRead()) {
            holder.viewUnread.setVisibility(View.VISIBLE);
        }

        // Phân loại thông báo
        if (notification.getType() != null) {
            switch (notification.getType()) {
                case DELETE_POST:
                    holder.imgAvatar.setImageResource(R.drawable.warning);
                    holder.viewWarning.setVisibility(View.VISIBLE);
                    break;

                case PET_FEED:
                    holder.imgAvatar.setImageResource(R.drawable.cute_treat);
                    break;

                case PET_LEVEL_UP:
                    holder.imgAvatar.setImageResource(R.drawable.auralog_logo);
                    break;

                case REACT_POST:
                    loadSenderAvatar(holder, notification);
                    break;
                case FRIEND_REQUEST:
                    loadSenderAvatar(holder, notification);
                    break;
                case FRIEND_ACCEPT:
                    loadSenderAvatar(holder, notification);
                    break;

                default:
                    holder.imgAvatar.setImageResource(R.drawable.avatar_circle);
                    break;
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(notification);
            }
        });
    }

    private void loadSenderAvatar(ViewHolder holder, Notification notification) {
        String avatarUrl = notification.getSenderAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.avatar_circle)
                    .error(R.drawable.avatar_circle)
                    .circleCrop()
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.avatar_circle);
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public void setData(ArrayList<Notification> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Notification notification);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgAvatar;
        TextView txtContent, txtTime;
        View viewUnread, viewWarning;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtContent = itemView.findViewById(R.id.txtContent);
            txtTime = itemView.findViewById(R.id.txtTime);

            viewUnread = itemView.findViewById(R.id.viewUnread);
            viewWarning = itemView.findViewById(R.id.viewWarning);
        }
    }

    private String getTimeAgo(Timestamp timestamp) {

        if (timestamp == null) return "";

        long diff = System.currentTimeMillis()
                - timestamp.toDate().getTime();

        long minute = 60 * 1000;
        long hour = 60 * minute;
        long day = 24 * hour;

        if (diff < minute) {
            return "Vừa xong";
        } else if (diff < hour) {
            return (diff / minute) + " phút trước";
        } else if (diff < day) {
            return (diff / hour) + " giờ trước";
        } else {
            return (diff / day) + " ngày trước";
        }
    }
}