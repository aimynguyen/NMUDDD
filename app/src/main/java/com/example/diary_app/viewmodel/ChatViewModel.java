package com.example.diary_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.diary_app.data.model.ChatMessage;
import com.example.diary_app.data.model.ChatRoom;
import com.example.diary_app.repository.ChatRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends ViewModel{
    private ChatRepository chatRepository;

    // 1.LiveData cho Front-end
    private MutableLiveData<List<ChatRoom>> chatRooms = new MutableLiveData<>();
    private MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // 2. Các biến giữ kết nối Real-time (Để hủy khi thoát màn hình, chống tràn RAM)
    private ListenerRegistration chatRoomsListener;
    private ListenerRegistration messagesListener;

    public ChatViewModel() {
        chatRepository = new ChatRepository();
    }

    public LiveData<List<ChatRoom>> getChatRooms() { return chatRooms; }
    public LiveData<List<ChatMessage>> getMessages() { return messages; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    /**
     * TẢI VÀ LẮNG NGHE: Danh sách phòng chat (Màn hình ngoài cùng)
     */
    public void loadMyChatRooms(String myUid) {
        // Hủy lắng nghe cũ (nếu có) để tránh bị lặp dữ liệu
        if (chatRoomsListener != null) chatRoomsListener.remove();

        chatRoomsListener = chatRepository.listenToMyChatRoom(myUid, (value, error) -> {
            if (error != null) {
                errorMessage.setValue("Lỗi tải danh sách chat: " + error.getMessage());
                return;
            }

            List<ChatRoom> roomList = new ArrayList<>();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    ChatRoom room = doc.toObject(ChatRoom.class);
                    room.setChatId(doc.getId()); // Gắn khóa chính vào object
                    roomList.add(room);
                }
            }
            // Đẩy danh sách đã xử lý lên LiveData cho FE
            chatRooms.setValue(roomList);
        });
    }

    /**
     * TẢI VÀ LẮNG NGHE: Chi tiết tin nhắn (Màn hình bên trong lúc đang chat)
     */
    public void loadMessagesInRoom(String chatId) {
        if (messagesListener != null) messagesListener.remove();

        messagesListener = chatRepository.listenToMessageInRoom(chatId, (value, error) -> {
            if (error != null) {
                errorMessage.setValue("Lỗi tải tin nhắn: " + error.getMessage());
                return;
            }

            List<ChatMessage> msgList = new ArrayList<>();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    ChatMessage msg = doc.toObject(ChatMessage.class);
                    msgList.add(msg);
                }
            }
            // Cứ có người nhắn tin mới, dòng này sẽ tự động chạy và update UI ngay lập tức
            messages.setValue(msgList);
        });
    }

    /**
     * GỬI TIN NHẮN
     */
    public void sendMessage(String chatId, String myUid, String content, List<String> participants) {
        // Tự tay đóng gói object tin nhắn
        ChatMessage newMessage = new ChatMessage();
        newMessage.setSenderId(myUid);
        newMessage.setContent(content.trim());
        newMessage.setCreatedAt(Timestamp.now());

        chatRepository.sendMessage(chatId, newMessage, participants)
                .addOnFailureListener(e -> {
                    // Chỉ báo lỗi nếu thất bại (rớt mạng).
                    // Nếu thành công, không làm gì vì SnapshotListener ở trên sẽ tự "bắt" được tin nhắn mới và vẽ lên UI.
                    errorMessage.setValue("Không thể gửi tin nhắn: " + e.getMessage());
                });
    }

    /**
     * DỌN DẸP BỘ NHỚ
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        // Khi người dùng thoát hẳn khỏi màn hình Chat, lập tức ngắt kết nối với Firebase
        if (chatRoomsListener != null) chatRoomsListener.remove();
        if (messagesListener != null) messagesListener.remove();
    }
}

































