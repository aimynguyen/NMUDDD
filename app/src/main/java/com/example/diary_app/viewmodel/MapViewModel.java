package com.example.diary_app.viewmodel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.example.diary_app.DTOs.DiaryListItemDTO;
import java.util.ArrayList;
import java.util.List;
public class MapViewModel extends ViewModel {
    // Lưu trữ vị trí hiện tại của người dùng
    private final MutableLiveData<LatLng> currentLocation = new MutableLiveData<>();

    // Lưu trữ vị trí người dùng vừa "chạm" (Ghim) trên bản đồ
    private final MutableLiveData<LatLng> selectedLocation = new MutableLiveData<>();

    // Danh sách các bài nhật ký để vẽ Marker lên bản đồ
    private final MutableLiveData<List<DiaryListItemDTO>> diaryMarkers = new MutableLiveData<>();

    // Các hàm Getter để Activity hóng dữ liệu
    public LiveData<LatLng> getCurrentLocation() { return currentLocation; }
    public LiveData<LatLng> getSelectedLocation() { return selectedLocation; }
    public LiveData<List<DiaryListItemDTO>> getDiaryMarkers() { return diaryMarkers; }

    // Các hàm xử lý Logic (Activity sẽ gọi)

    // Gọi khi GPS của máy quét được vị trí mới
    public void updateLocation(double lat, double lng) {
        currentLocation.setValue(new LatLng(lat, lng));
    }

    // Gọi khi người dùng nhấn giữ trên bản đồ để chọn chỗ viết nhật ký
    public void selectLocation(LatLng latLng) {
        selectedLocation.setValue(latLng);
    }

    // Lấy danh sách nhật ký từ DB để hiển thị lên Map
    public void fetchDiaryMarkers() {
        List<DiaryListItemDTO> mockData = new ArrayList<>();

        diaryMarkers.setValue(mockData);
    }
}
