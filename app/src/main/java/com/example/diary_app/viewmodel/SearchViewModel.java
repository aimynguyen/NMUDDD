package com.example.diary_app.viewmodel;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.diary_app.DTOs.DiaryListItemDTO;

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
        return result;
    }
}
