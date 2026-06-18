package com.example.diary_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.diary_app.R;
import com.example.diary_app.data.model.ChatRoom;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private List<ChatRoom> chatRoomList;
    private OnChatRoomClickListener listener;
    private String currentUserId;

    public interface OnChatRoomClickListener {
        void onChatRoomClick(ChatRoom chatRoom, String friendId);
    }

    public ChatRoomAdapter(List<ChatRoom> chatRoomList, String currentUserId, OnChatRoomClickListener listener) {
        this.chatRoomList = chatRoomList;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void updateData(List<ChatRoom> newList) {
        this.chatRoomList = newList;
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

        holder.tvLastMessage.setText(room.getLastMessage());

        if (room.getLastUpdated() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            holder.tvTime.setText(sdf.format(room.getLastUpdated().toDate()));
        }

        // Lấy ID của người kia (friendId) từ danh sách participants
        String friendId = "";
        if (room.getParticipants() != null) {
            for (String uid : room.getParticipants()) {
                if (!uid.equals(currentUserId)) {
                    friendId = uid;
                    break;
                }
            }
        }

        // Tạm thời set cứng tên vì class ChatRoom chưa có thuộc tính tên bạn bè
        // (Trong thực tế bạn nên fetch profile của friendId từ bảng Users)
        holder.tvFriendName.setText("User " + friendId.substring(0, Math.min(friendId.length(), 5)));

        String finalFriendId = friendId;
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onChatRoomClick(room, finalFriendId);
        });
    }

    @Override
    public int getItemCount() {
        return chatRoomList != null ? chatRoomList.size() : 0;
    }

    static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvFriendName, tvLastMessage, tvTime;

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFriendName = itemView.findViewById(R.id.tvFriendName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}