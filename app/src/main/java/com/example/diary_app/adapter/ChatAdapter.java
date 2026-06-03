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
    private static String userId = "";

    public List<ChatMessage> messageList;
    public ChatAdapter(List<ChatMessage> messageList, String userId) {
        this.messageList = messageList;
        this.userId = userId;
    }

    @Override
    public int getItemViewType(int position) {
        if(messageList.get(position).getSenderId().equals("user_123"))
            return TYPE_SENDER;
        else
            return TYPE_RECEIVER;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_ai, parent, false);
        return new ChatViewHolder(view);
    }

    public void updateList(List<ChatMessage> newList) {
        this.messageList = newList; // 'messageList' là biến chứa danh sách tin nhắn trong Adapter của bạn
        notifyDataSetChanged();     // Lệnh báo cho RecyclerView vẽ lại toàn bộ tin nhắn mới
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        if(getItemViewType(position) == TYPE_SENDER) {
            holder.txtMessageRight.setVisibility(View.VISIBLE);
            holder.txtMessageLeft.setVisibility(View.GONE);
            holder.txtMessageRight.setText(message.getContent());
            holder.txtMessageRight.setBackgroundResource(R.drawable.bg_message_sent);
            holder.txtMessageRight.setPadding(32, 24, 32, 24);
        } else {
            holder.txtMessageLeft.setVisibility(View.VISIBLE);
            holder.txtMessageRight.setVisibility(View.GONE);
            holder.txtMessageLeft.setText(message.getContent());
            holder.txtMessageLeft.setBackgroundResource(R.drawable.bg_message_recieved);
            holder.txtMessageLeft.setPadding(32, 24, 32, 24);
        }
    }

    public int getItemCount() {return messageList.size();}
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
