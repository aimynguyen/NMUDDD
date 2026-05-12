package com.example.diary_app.ui.pages.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diary_app.R;
import com.example.diary_app.data.model.ChatMessage;

import java.util.List;
// 1. Thay đổi ở đây: Chỉ định rõ ChatViewHolder trong dấu ngoặc nhọn
public class ChatAIAdapter extends RecyclerView.Adapter<ChatAIAdapter.ChatViewHolder> {

    private static final int TYPE_USER = 1;
    private static final int TYPE_BOT = 2;
    private List<ChatMessage> messageList;

    public ChatAIAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getSenderId().equals("user_123")) {
            return TYPE_USER;
        } else {
            return TYPE_BOT;
        }
    }

    @NonNull
    @Override
    // 2. Thay đổi ở đây: Trả về ChatViewHolder thay vì RecyclerView.ViewHolder
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_ai, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    // 3. Thay đổi ở đây: Ép kiểu sẵn ChatViewHolder giúp code gọn hơn
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        if (getItemViewType(position) == TYPE_USER) {
            holder.txtMessageRight.setVisibility(View.VISIBLE);
            holder.txtMessageLeft.setVisibility(View.GONE);
            holder.txtMessageRight.setText(message.getContent());
            holder.txtMessageRight.setBackgroundResource(R.drawable.bg_message_sent);
            holder.txtMessageRight.setPadding(24, 24, 24, 24);
        } else {
            holder.txtMessageLeft.setVisibility(View.VISIBLE);
            holder.txtMessageRight.setVisibility(View.GONE);
            holder.txtMessageLeft.setText(message.getContent());
            holder.txtMessageLeft.setBackgroundResource(R.drawable.bg_message_recieved);
            holder.txtMessageLeft.setPadding(24, 24, 24, 24);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
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