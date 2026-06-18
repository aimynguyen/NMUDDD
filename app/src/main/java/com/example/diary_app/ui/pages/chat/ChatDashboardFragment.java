package com.example.diary_app.ui.pages.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

public class ChatDashboardFragment extends Fragment {

    private RecyclerView rvFriendChats;
    private View btnAiAssistant;
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

        // Giả lập lấy UID hiện tại từ FirebaseAuth
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            currentUid = "user_123"; // ID test trùng với mẫu của bạn
        }

        chatRepository = new ChatRepository();

        btnAiAssistant = view.findViewById(R.id.btnAiAssistant);
        rvFriendChats = view.findViewById(R.id.rvFriendChats);

        setupRecyclerView();

        // 1. CLICK CHUYỂN SANG CHAT BOT AI
        btnAiAssistant.setOnClickListener(v -> {
            Fragment chatAiFragment = new ChatAIFragment();
            // Truyền ID sang nếu cần lưu trữ vào repository ai_chats
            Bundle args = new Bundle();
            args.putString("MY_UID", currentUid);
            chatAiFragment.setArguments(args);

            openFragment(chatAiFragment);
        });

        // 2. LẮNG NGHE REALTIME PHÒNG CHAT BẠN BÈ TỪ FIREBASE
        listenToChatRooms();

        return view;
    }

    private void setupRecyclerView() {
        chatRoomList = new ArrayList<>();
        // Định nghĩa sự kiện khi click vào item bạn bè
        adapter = new ChatRoomAdapter(chatRoomList, currentUid, (chatRoom, friendId) -> {

            // CLICK CHUYỂN SANG CHAT BẠN BÈ (ChatFragment của bạn)
            Fragment chatFragment = new ChatFragment();
            Bundle bundle = new Bundle();
            bundle.putString("CHAT_ID", chatRoom.getChatId());
            bundle.putString("MY_UID", currentUid);
            bundle.putString("RECEIVER_UID", friendId);
            chatFragment.setArguments(bundle);

            openFragment(chatFragment);
        });

        rvFriendChats.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFriendChats.setAdapter(adapter);
    }

    private void listenToChatRooms() {
        chatListener = chatRepository.listenToMyChatRoom(currentUid, (value, error) -> {
            if (error != null || value == null) return;

            List<ChatRoom> rooms = new ArrayList<>();
            for (DocumentSnapshot doc : value.getDocuments()) {
                ChatRoom room = doc.toObject(ChatRoom.class);
                if (room != null) {
                    room.setChatId(doc.getId());
                    rooms.add(room);
                }
            }
            adapter.updateData(rooms);
        });
    }

    private void openFragment(Fragment fragment) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_chatroom, fragment) // Thay R.id.fragment_container bằng ID khung chứa Fragment của Activity bạn
                    .addToBackStack(null)
                    .commit();
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