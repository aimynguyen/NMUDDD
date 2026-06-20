package com.example.diary_app.ui.pages.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.diary_app.R;
import com.example.diary_app.adapter.ChatRoomAdapter;
import com.example.diary_app.data.model.ChatRoom;
import com.example.diary_app.repository.ChatRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.example.diary_app.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatDashboardFragment extends Fragment {

    private RecyclerView rvFriendChats;
    private View btnAiAssistant;
    private EditText etSearchFriends;
    private ChatRoomAdapter adapter;
    private List<ChatRoom> chatRoomList;

    private ChatRepository chatRepository;
    private ListenerRegistration chatListener;
    private String currentUid;

    public ChatDashboardFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chathub, container, false);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            currentUid = "user_123";
        }

        chatRepository = new ChatRepository();

        btnAiAssistant = view.findViewById(R.id.btnAiAssistant);
        rvFriendChats = view.findViewById(R.id.rvFriendChats);

        // 2. Ánh xạ ô tìm kiếm (Nhớ bổ sung ID này vào file XML fragment_chathub của bạn)
        etSearchFriends = view.findViewById(R.id.edtSearchChat);

        setupRecyclerView();
        listenToChatRooms();

        // 3. THIẾT LẬP TÍNH NĂNG TÌM KIẾM REALTIME KHI GÕ CHỮ
        if (etSearchFriends != null) {
            etSearchFriends.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Gọi hàm lọc từ adapter khi người dùng đang gõ chữ
                    if (adapter != null) {
                        adapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }

        // CLICK CHUYỂN SANG CHAT BOT AI
        btnAiAssistant.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("MY_UID", currentUid);

            // Sử dụng Navigation để chuyển sang ChatAIFragment
            androidx.navigation.Navigation.findNavController(v)
                    .navigate(R.id.nav_chatAI, args);
        });

        return view;
    }

    private void setupRecyclerView() {
        chatRoomList = new ArrayList<>();
        adapter = new ChatRoomAdapter(chatRoomList, currentUid, (chatRoom, friendId) -> {
            // Tạo Bundle để truyền dữ liệu
            Bundle bundle = new Bundle();
            bundle.putString("CHAT_ID", chatRoom.getChatId());
            bundle.putString("MY_UID", currentUid);
            bundle.putString("RECEIVER_UID", friendId);

            // Sử dụng Navigation Controller để chuyển sang ChatFragment
            androidx.navigation.Navigation.findNavController(requireView())
                    .navigate(R.id.nav_chat, bundle);
        });

        rvFriendChats.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFriendChats.setAdapter(adapter);
    }

    private void listenToChatRooms() {
        UserRepository userRepository = new UserRepository();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Lắng nghe realtime chính tài khoản của bạn để luôn cập nhật danh sách friendIds mới nhất
        db.collection("users").document(currentUid)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null || documentSnapshot == null || !documentSnapshot.exists()) return;

                    // Lấy mảng friendIds của bạn
                    List<String> friendIds = (List<String>) documentSnapshot.get("friendIds");

                    if (friendIds == null || friendIds.isEmpty()) {
                        // Nếu chưa có bạn bè nào, xóa sạch danh sách hiển thị và dừng lại
                        chatRoomList.clear();
                        adapter.updateData(new ArrayList<>());
                        return;
                    }

                    // 2. Lấy chi tiết thông tin (Tên, avatar...) của tất cả bạn bè dựa vào list ID vừa lấy
                    userRepository.getUsersByIds(friendIds).addOnSuccessListener(users -> {
                        List<ChatRoom> finalRooms = new ArrayList<>();

                        // Duyệt qua từng người bạn để tạo object ChatRoom hiển thị lên giao diện
                        for (com.example.diary_app.data.model.User friend : users) {
                            ChatRoom room = new ChatRoom();
                            room.setRoomName(friend.getUserName()); // Gán tên bạn bè vào tên phòng
                            room.setAvatarUrl(friend.getAvatarUrl()); // Gán avatar bạn bè

                            // Thiết lập danh sách participants (gồm bạn và người bạn này)
                            List<String> participants = new ArrayList<>();
                            participants.add(currentUid);
                            participants.add(friend.getUid());
                            room.setParticipants(participants);

                            // 3. Tìm xem 2 người đã từng có chatId chưa bằng cách so sánh chuỗi ID ghép đôi cố định
                            // Cách này giúp tạo ra 1 ChatID duy nhất giữa 2 người mà không cần tạo trước trên DB
                            String uniqueChatId = generateChatId(currentUid, friend.getUid());
                            room.setChatId(uniqueChatId);

                            // Tạm thời lấy tin nhắn cuối cùng (nếu có hệ thống lưu, bạn có thể bổ sung sau)
                            room.setLastMessage("Bấm để bắt đầu trò chuyện");

                            finalRooms.add(room);
                        }

                        // 4. Đổ toàn bộ danh sách bạn bè đã kết bạn lên RecyclerView
                        adapter.updateData(finalRooms);
                    });
                });
    }

    // Hàm bổ trợ tạo Chat ID duy nhất giữa 2 người dựa trên UID (sắp xếp theo bảng chữ cái)
    private String generateChatId(String uid1, String uid2) {
        if (uid1.compareTo(uid2) < 0) {
            return uid1 + "_" + uid2;
        } else {
            return uid2 + "_" + uid1;
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatListener != null) {
            chatListener.remove(); // Hủy lắng nghe Firebase để tránh tràn RAM
        }
    }
}
