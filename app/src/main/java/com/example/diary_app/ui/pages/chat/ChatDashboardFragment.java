package com.example.diary_app.ui.pages.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

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
import java.util.Calendar;
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

    // Khai báo thêm một biến toàn cục để quản lý danh sách các Listener phòng chat nhằm tránh rò rỉ bộ nhớ
    private List<ListenerRegistration> roomListeners = new ArrayList<>();
    private com.example.diary_app.repository.UserRepository userRepository = new com.example.diary_app.repository.UserRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chathub, container, false);

        TextView tvGreeting = view.findViewById(R.id.tvGreeting);
        TextView tvQuestion = view.findViewById(R.id.tvQuestion);

        updateGreeting(tvGreeting);
        updateQuestion(tvQuestion);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            currentUid = "user_123";
        }

        btnAiAssistant = view.findViewById(R.id.btnAiAssistant);
        rvFriendChats = view.findViewById(R.id.rvFriendChats);
        etSearchFriends = view.findViewById(R.id.edtSearchChat);

        setupRecyclerView();

        // Gọi hàm lắng nghe hỗn hợp: Vừa lấy đủ bạn bè, vừa cập nhật tin nhắn và thứ tự realtime
        listenToAllFriendsAndChats();

        // Logic ô tìm kiếm tìm kiếm
        if (etSearchFriends != null) {
            etSearchFriends.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) adapter.filter(s.toString());
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }

        // Click chuyển đổi AI
        btnAiAssistant.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("MY_UID", currentUid);
            androidx.navigation.Navigation.findNavController(v).navigate(R.id.nav_chatAI, args);
        });

        return view;
    }

    private void listenToAllFriendsAndChats() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Lắng nghe Realtime danh sách bạn bè của User hiện tại
        db.collection("users").document(currentUid)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null || documentSnapshot == null || !documentSnapshot.exists()) return;

                    List<String> friendIds = (List<String>) documentSnapshot.get("friendIds");
                    if (friendIds == null || friendIds.isEmpty()) {
                        chatRoomList.clear();
                        adapter.updateData(new ArrayList<>());
                        return;
                    }

                    // Hủy các listener cũ của các phòng chat trước đó để tránh trùng lặp dữ liệu
                    for (ListenerRegistration lr : roomListeners) {
                        if (lr != null) lr.remove();
                    }
                    roomListeners.clear();

                    // 2. Lấy thông tin chi tiết (Tên, avatar) của tất cả bạn bè
                    userRepository.getUsersByIds(friendIds).addOnSuccessListener(users -> {
                        chatRoomList.clear();

                        // Tạo sẵn danh sách khung chứa thông tin bạn bè ban đầu
                        for (com.example.diary_app.data.model.User friend : users) {
                            ChatRoom room = new ChatRoom();
                            room.setRoomName(friend.getUserName());
                            room.setAvatarUrl(friend.getAvatarUrl());

                            List<String> participants = new ArrayList<>();
                            participants.add(currentUid);
                            participants.add(friend.getUid());
                            room.setParticipants(participants);

                            String uniqueChatId = generateChatId(currentUid, friend.getUid());
                            room.setChatId(uniqueChatId);

                            // Giá trị mặc định nếu chưa từng chat
                            room.setLastMessage("Bấm để bắt đầu trò chuyện");
                            room.setLastUpdated(null);

                            chatRoomList.add(room);
                        }

                        // Cập nhật giao diện tạm thời bằng danh sách bạn bè
                        adapter.updateData(new ArrayList<>(chatRoomList));

                        // 3. Với mỗi người bạn, tiến hành lắng nghe Realtime chính phòng chat đó trên Firestore
                        // Sửa tại Bước 3 trong hàm listenToAllFriendsAndChats() của ChatDashboardFragment:
                        for (int i = 0; i < chatRoomList.size(); i++) {
                            final int index = i;
                            ChatRoom currentRoom = chatRoomList.get(index);

                            // ĐỔI THÀNH "chats" CHO KHỚP VỚI REPOSITORY
                            ListenerRegistration lr = db.collection("chats").document(currentRoom.getChatId())
                                    .addSnapshotListener((roomSnap, roomErr) -> {
                                        if (roomErr != null || roomSnap == null || !roomSnap.exists()) return;

                                        String lastMsg = roomSnap.getString("lastMessage");
                                        com.google.firebase.Timestamp timestamp = roomSnap.getTimestamp("lastUpdated");
                                        String lastSenderId = roomSnap.getString("lastSenderId"); // Đọc trường vừa thêm ở trên

                                        if (lastMsg != null) {
                                            if (lastSenderId != null) {
                                                // Nếu ID người nhắn cuối là mình -> Hiện "Bạn: ...", nếu là bạn mình -> Hiện "Tên bạn: ..."
                                                String prefix = lastSenderId.equals(currentUid) ? "Bạn" : currentRoom.getRoomName();
                                                currentRoom.setLastMessage(prefix + ": " + lastMsg);
                                            } else {
                                                currentRoom.setLastMessage(lastMsg);
                                            }
                                        }

                                        if (timestamp != null) {
                                            currentRoom.setLastUpdated(timestamp);
                                        }

                                        adapter.updateData(new ArrayList<>(chatRoomList));
                                    });

                            roomListeners.add(lr);
                        }
                    });
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Giải phóng bộ nhớ khi thoát màn hình
        for (ListenerRegistration lr : roomListeners) {
            if (lr != null) lr.remove();
        }
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

    // Hàm update lời chào
    private void updateGreeting(TextView tvGreeting) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;

        if (hour < 12) {
            greeting = "Chào buổi sáng ☀";
        } else if (hour < 18) {
            greeting = "Chào buổi chiều ⛅";
        } else {
            greeting = "Chào buổi tối ✨";
        }

        tvGreeting.setText(greeting);
    }

    // Hàm update câu hỏi
    private void updateQuestion (TextView tvQuestion) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        // sau 10h đêm luôn hiện câu này
        if (hour >= 22) {
            tvQuestion.setText("Đừng thức quá khuya nhé.");
            return;
        }

        String[] questions;

        if (hour >= 5 && hour < 11) {
            // sáng
            questions = new String[] {
                    "Bạn ngủ ngon chứ?",
                    "Bạn đã ăn sáng chưa?",
                    "Hôm nay bạn mong chờ điều gì?",
                    "Chúc bạn một ngày thật dễ chịu nhé!",
                    "Bạn đã sẵn sàng cho hôm nay chưa?"
            };
        } else if (hour >= 11 && hour <18) {
            // trưa - chiều
            questions = new String[] {
                    "Bạn đã ăn trưa chưa?",
                    "Mọi thứ hôm nay vẫn ổn chứ?",
                    "Có chuyện gì thứ vị xảy ra chưa?",
                    "Bạn có đang hơi mệt không?",
                    "Nhớ nghỉ ngơi một chút nhé.",
                    "Nhớ uống nước nhé."
            };
        } else {
            // tối
            questions = new String[] {
                    "Hôm nay bạn cảm thấy thế nào?",
                    "Có điều gì làm bạn vui không?",
                    "Bạn có tâm sự gì muốn kể không?",
                    "Ngày hôm nay của bạn ra sao?",
                    "Bạn có điều gì muốn chia sẻ không?",
                    "Hôm nay có khoảnh khắc nào đáng nhớ không?"
            };
        }
        // random câu
        int index = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % questions.length;

        tvQuestion.setText(questions[index]);
    }

}
