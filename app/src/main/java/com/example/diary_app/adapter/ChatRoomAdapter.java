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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        this.chatRoomListFull = new ArrayList<>(this.chatRoomList); // Khởi tạo danh sách full rỗng thay vì để null
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void updateData(List<ChatRoom> newList) {
        this.chatRoomList = newList;
        this.chatRoomListFull = new ArrayList<>(newList); // Tạo một bản sao độc lập phục vụ tìm kiếm
        notifyDataSetChanged();
    }
    public void filter(String text) {
        if (chatRoomListFull == null) return; // Bảo vệ nếu chưa có dữ liệu

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

        // Hiển thị tên bạn bè và tin nhắn cuối
        holder.tvFriendName.setText(room.getRoomName());
        holder.tvLastMessage.setText(room.getLastMessage());

        // Tìm friendId từ participants
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
        // Bắt sự kiện click để mở khung chat
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Khi bấm vào, truyền cả room (đã có chatId cố định) và friendId sang fragment chat
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

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFriendName = itemView.findViewById(R.id.tvFriendName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}