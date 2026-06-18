package com.example.diary_app.viewmodel;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.diary_app.DTOs.DiaryListItemDTO;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
public class SearchViewModel extends ViewModel {
    // Lưu trữ từ khóa tìm kiếm hiện tại
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();

    // Trạng thái Loading để xoay vòng tròn trên UI
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // Kết quả tìm kiếm đầu ra
    private final LiveData<List<DiaryListItemDTO>> searchResults;

    // Các công cụ để xử lý Debounce (Trì hoãn)
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    public SearchViewModel() {
        searchResults = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                isLoading.setValue(false);
                return new MutableLiveData<>(new ArrayList<>());
            }
            return performSearchFromDB(query);
        });
    }

    // CÁC HÀM GETTER CHO ACTIVITY
    public LiveData<List<DiaryListItemDTO>> getSearchResults() { return searchResults; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    // HÀM XỬ LÝ SỰ KIỆN GÕ PHÍM (CÓ DEBOUNCE)
    public void setSearchQuery(String query) {
        // Nếu người dùng gõ phím mới, hủy ngay lệnh tìm kiếm đang chờ trước đó
        if (searchRunnable != null) {
            handler.removeCallbacks(searchRunnable);
        }

        searchRunnable = () -> {
            if (!query.equals(searchQuery.getValue())) {
                searchQuery.setValue(query.trim());
            }
        };

        handler.postDelayed(searchRunnable, 300); // Delay 300ms
    }

    // HÀM LOGIC GỌI DATABASE
    private LiveData<List<DiaryListItemDTO>> performSearchFromDB(String keyword) {
        MutableLiveData<List<DiaryListItemDTO>> result = new MutableLiveData<>();
        isLoading.setValue(true); // Bật loading
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("diaries")
                .orderBy("title")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DiaryListItemDTO> list = new ArrayList<>();

                    // Duyệt qua từng kết quả Firebase trả về
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            int id = doc.getLong("id").intValue();
                            String title = doc.getString("title");
                            String date = doc.getString("date");
                            String moodEmoji = doc.getString("moodEmoji");
                            double latitude = doc.getDouble("latitude");
                            double longitude = doc.getDouble("longitude");

                            // Đóng gói vào DTO
                            DiaryListItemDTO item = new DiaryListItemDTO(
                                    id, title, date, null, moodEmoji, latitude, longitude
                            );

                            list.add(item);
                        } catch (Exception e) {
                            e.printStackTrace();
                            // Bỏ qua document bị lỗi cấu trúc để không làm chết app
                        }
                    }

                    // Đẩy danh sách lên cho UI và TẮT loading
                    result.setValue(list);
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    // Nếu lỗi (mất mạng, sai tên bảng...), vẫn phải tắt loading và trả về mảng rỗng
                    result.setValue(new ArrayList<>());
                    isLoading.setValue(false);
                });
        return result;
    }
}
