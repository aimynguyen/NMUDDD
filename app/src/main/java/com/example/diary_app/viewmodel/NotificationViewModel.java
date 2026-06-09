package com.example.diary_app.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.diary_app.data.model.Notification;
import com.example.diary_app.repository.NotificationRepository;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationViewModel extends ViewModel {
    private static String TAG = "NotificationViewModel";
    private NotificationRepository notificationRepository;

    private MutableLiveData<List<Notification>> notificationsLiveData;

    private MutableLiveData<Boolean> isLoading;

    private MutableLiveData<String> errorMessage;

    public NotificationViewModel() {
        notificationRepository = new NotificationRepository();
        notificationsLiveData = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
    }

    public MutableLiveData<List<Notification>> getNotifications() {
        return notificationsLiveData;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Hàm gọi để lấy danh sách thông báo
     */
    public void fetchNotifications(String userId) {
        isLoading.setValue(true);

        notificationRepository.getNotificationsForUser(userId)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notification> list = new ArrayList<>();
                    for(DocumentSnapshot document : queryDocumentSnapshots) {
                        Notification notification = document.toObject(Notification.class);
                        if (notification != null) {
                            // Gán ID của document vào object
                            notification.setNotificationId(document.getId());
                            list.add(notification);
                        }
                    }

                    // Đẩy dữ liệu mới vào LiveData
                    notificationsLiveData.setValue(list);
                    isLoading.setValue(false);
                    })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(e.getMessage());
                });

    }

    /**
     * Hàm cập nhật trạng thái đã đọc
     */
    public void markAsRead(String notificationId) {
        notificationRepository.markAsRead(notificationId)
                .addOnSuccessListener(aVoid -> {
                    // TODO: Update UI thành đã đọc
                })
                .addOnFailureListener(e -> {
                    Log.d("NotificationViewModel", "Lỗi đánh dấu đã đọc:", e);
                });
    }
}
