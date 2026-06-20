package com.example.diary_app.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.diary_app.R;
import com.example.diary_app.data.model.ChatRoom;
import java.util.ArrayList;
import java.util.List;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private List<ChatRoom> chatRoomList;
    private List<ChatRoom> chatRoomListFull;
    private OnChatRoomClickListener listener;
    private String currentUserId;

    public interface OnChatRoomClickListener {
        void onChatRoomClick(ChatRoom chatRoom, String friendId);
    }

    public ChatRoomAdapter(List<ChatRoom> chatRoomList, String currentUserId, OnChatRoomClickListener listener) {
        this.chatRoomList = chatRoomList != null ? chatRoomList : new ArrayList<>();
        this.chatRoomListFull = new ArrayList<>(this.chatRoomList);
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void updateData(List<ChatRoom> newList) {
        this.chatRoomList = newList;
        this.chatRoomListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        if (chatRoomListFull == null) return;

        chatRoomList.clear();
        if (text == null || text.isEmpty()) {
            chatRoomList.addAll(chatRoomListFull);
        } else {
            String filterPattern = text.toLowerCase().trim();
            for (ChatRoom item : chatRoomListFull) {
                if (item.getRoomName() != null && item.getRoomName().toLowerCase().contains(filterPattern)) {
                    chatRoomList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom room = chatRoomList.get(position);

        holder.tvFriendName.setText(room.getRoomName());
        holder.tvLastMessage.setText(room.getLastMessage());

        // Load avatar
        if (!TextUtils.isEmpty(room.getAvatarUrl())) {
            Glide.with(holder.itemView.getContext())
                    .load(room.getAvatarUrl())
                    .placeholder(R.drawable.avatar_circle)
                    .circleCrop()
                    .into(holder.imgFriendAvatar);
        } else {
            holder.imgFriendAvatar.setImageResource(R.drawable.avatar_circle);
        }

        String friendId = "";
        if (room.getParticipants() != null) {
            for (String uid : room.getParticipants()) {
                if (!uid.equals(currentUserId)) {
                    friendId = uid;
                    break;
                }
            }
        }

        String finalFriendId = friendId;
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatRoomClick(room, finalFriendId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatRoomList != null ? chatRoomList.size() : 0;
    }

    static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvFriendName, tvLastMessage, tvTime;
        ImageView imgFriendAvatar;

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFriendName = itemView.findViewById(R.id.tvFriendName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            imgFriendAvatar = itemView.findViewById(R.id.imgFriendAvatar);
        }
    }
}
