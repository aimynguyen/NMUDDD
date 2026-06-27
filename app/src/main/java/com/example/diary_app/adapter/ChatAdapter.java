package com.example.diary_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diary_app.R;
import com.example.diary_app.data.model.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private static final int TYPE_SENDER = 1;
    private static final int TYPE_RECEIVER = 2;
    private String currentUserId; // Bỏ static để tránh lỗi dữ liệu khi có nhiều instance

    private List<ChatMessage> messageList;

    public ChatAdapter(List<ChatMessage> messageList, String userId) {
        this.messageList = messageList;
        this.currentUserId = userId;
    }

    @Override
    public int getItemViewType(int position) {
        // So sánh với currentUserId được truyền vào thay vì hardcode "user_123"
        if (messageList.get(position).getSenderId() != null && 
            messageList.get(position).getSenderId().equals(currentUserId)) {
            return TYPE_SENDER;
        } else {
            return TYPE_RECEIVER;
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng item_chat_ai chứa cả 2 phía trái/phải
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    public void updateList(List<ChatMessage> newList) {
        this.messageList = newList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        float density = holder.itemView.getContext().getResources().getDisplayMetrics().density;

        if (getItemViewType(position) == TYPE_SENDER) {
            // Xử lý Tin nhắn Gửi
            holder.itemView.findViewById(R.id.right_chat_layout).setVisibility(View.VISIBLE);
            holder.itemView.findViewById(R.id.left_chat_layout).setVisibility(View.GONE);

            holder.txtMessageRight.setText(message.getContent());
            // Đảm bảo background và padding được set đúng
            holder.txtMessageRight.setBackgroundResource(R.drawable.bg_message_sent);
            holder.txtMessageRight.setPadding(16,8,16,8);

            // Check xem tin nhắn này có đính kèm ảnh bài viết không
            if (message.getPostImageUrl() != null && !message.getPostImageUrl().isEmpty()) {
                holder.imgMessageRight.setVisibility(View.VISIBLE);
                com.bumptech.glide.Glide.with(holder.itemView.getContext())
                        .load(message.getPostImageUrl())
                        .into(holder.imgMessageRight);
            } else {
                holder.imgMessageRight.setVisibility(View.GONE); // Nếu không có ảnh thì giấu khung đi
            }

        } else {
            // Xử lý Tin nhắn Nhận
            holder.itemView.findViewById(R.id.left_chat_layout).setVisibility(View.VISIBLE);
            holder.itemView.findViewById(R.id.right_chat_layout).setVisibility(View.GONE);

            holder.txtMessageLeft.setText(message.getContent());
            holder.txtMessageLeft.setBackgroundResource(R.drawable.bg_message_recieved);
            holder.txtMessageLeft.setPadding(16,8,16,8);

            if (message.getPostImageUrl() != null && !message.getPostImageUrl().isEmpty()) {
                holder.imgMessageLeft.setVisibility(View.VISIBLE);
                com.bumptech.glide.Glide.with(holder.itemView.getContext())
                        .load(message.getPostImageUrl())
                        .into(holder.imgMessageLeft);
            } else {
                holder.imgMessageLeft.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessageLeft;
        TextView txtMessageRight;
        android.widget.ImageView imgMessageLeft, imgMessageRight;
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessageLeft = itemView.findViewById(R.id.left_chat_textview);
            txtMessageRight = itemView.findViewById(R.id.right_chat_textview);
            imgMessageLeft = itemView.findViewById(R.id.left_chat_image);
            imgMessageRight = itemView.findViewById(R.id.right_chat_image);
        }
    }
}
