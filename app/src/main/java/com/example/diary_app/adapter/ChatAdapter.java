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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_ai, parent, false);
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
        int paddingLeftRight = (int) (16 * density + 0.5f);
        int paddingTopBottom = (int) (8 * density + 0.5f);

        if (getItemViewType(position) == TYPE_SENDER) {
            holder.txtMessageRight.setVisibility(View.VISIBLE);
            holder.txtMessageLeft.setVisibility(View.GONE);
            holder.txtMessageRight.setText(message.getContent());
            // Đảm bảo background và padding được set đúng
            holder.txtMessageRight.setBackgroundResource(R.drawable.bg_message_sent);
            holder.txtMessageRight.setPadding(paddingLeftRight, paddingTopBottom, paddingLeftRight, paddingTopBottom);
        } else {
            holder.txtMessageLeft.setVisibility(View.VISIBLE);
            holder.txtMessageRight.setVisibility(View.GONE);
            holder.txtMessageLeft.setText(message.getContent());
            holder.txtMessageLeft.setBackgroundResource(R.drawable.bg_message_recieved);
            holder.txtMessageLeft.setPadding(paddingLeftRight, paddingTopBottom, paddingLeftRight, paddingTopBottom);
        }
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessageLeft;
        TextView txtMessageRight;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessageLeft = itemView.findViewById(R.id.left_chat_textview);
            txtMessageRight = itemView.findViewById(R.id.right_chat_textview);
        }
    }
}
