package com.example.diary_app.ui.pages.statistics;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.diary_app.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private View rootView;
    // Biến cho thanh tìm kiếm
    private android.widget.EditText edtSearch;
    private android.widget.ImageView iconSearch, iconClear;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_map, container, false);

        // 1. Ánh xạ Spinner và thiết lập dữ liệu
        addControls();
        addEvents();
        // 2. Khởi tạo bản đồ
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return rootView;
    }

    private void addEvents() {
        // 3.1. Nút X (Xóa chữ)
        iconClear.setOnClickListener(v -> edtSearch.setText(""));

        // 3.2. Ẩn/Hiện nút X khi gõ chữ
        edtSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                iconClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // 3.3. Bắt sự kiện bấm nút "Kính lúp" trên thanh công cụ
        iconSearch.setOnClickListener(v -> performSearch());

        // 3.4. Bắt sự kiện bấm phím "Enter/Search" trên bàn phím ảo của điện thoại
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void addControls() {
        edtSearch = rootView.findViewById(R.id.edtSearch);
        iconSearch = rootView.findViewById(R.id.iconSearch);
        iconClear = rootView.findViewById(R.id.iconClear);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Cài đặt các tính năng tương tác
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Kiểm tra quyền và bật vị trí
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            // Tự động zoom vào vị trí người dùng khi bản đồ sẵn sàng
            zoomToUserLocation();
        } else {
            // Xin quyền nếu chưa có
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        mMap.setOnMapClickListener(latLng -> {
            // Bước A: Cắm cờ tại nơi user vừa chạm
            mMap.clear();
            mMap.addMarker(new com.google.android.gms.maps.model.MarkerOptions()
                    .position(latLng)
                    .title("Vị trí được chọn"));

            // Bước B: Dùng Geocoder dịch Tọa độ thành Tên đường
            String addressName = "Vị trí chọn từ bản đồ";
            String cityName = "";

            try {
                android.location.Geocoder geocoder = new android.location.Geocoder(requireContext(), java.util.Locale.getDefault());
                java.util.List<android.location.Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    android.location.Address address = addresses.get(0);
                    addressName = address.getAddressLine(0);
                    cityName = address.getAdminArea();
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }

            // Gắn final để dùng an toàn bên trong sự kiện click của Hộp thoại
            final String finalAddressName = addressName;
            final String finalCityName = cityName;

            // Bước C: HIỆN HỘP THOẠI XÁC NHẬN TRƯỚC KHI QUAY VỀ
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận địa điểm")
                    .setMessage("Bạn có muốn chọn địa điểm này không?\n\n📍 " + finalAddressName)
                    .setPositiveButton("Chọn", (dialog, which) -> {

                        // 1. Đóng gói dữ liệu
                        android.os.Bundle result = new android.os.Bundle();
                        result.putString("location_name", finalAddressName);
                        result.putString("city_name", finalCityName);
                        result.putDouble("latitude", latLng.latitude);
                        result.putDouble("longitude", latLng.longitude);

                        // 2. Bắn dữ liệu về HomeFragment
                        requireActivity().getSupportFragmentManager().setFragmentResult("location_request", result);

                        // 3. ÉP BUỘC TẮT DIALOG TRƯỚC (Giải phóng luồng giao diện)
                        dialog.dismiss();

                        // 4. Lùi trang và dọn dẹp sạch sẽ MapFragment khỏi bộ nhớ
                        requireActivity().getSupportFragmentManager().popBackStack();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> {
                        // Nếu bấm Hủy: Đóng hộp thoại và xóa cái cờ vừa cắm đi cho sạch
                        dialog.dismiss();
                        mMap.clear();
                    })
                    .show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Người dùng VỪA BẤM "Cho phép" xong -> Bật map và zoom luôn
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    zoomToUserLocation();
                }
            } else {
                Toast.makeText(requireContext(), "Chưa cấp quyền thì không xem được vị trí đâu nhé!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void zoomToUserLocation() {
        // 1. Khởi tạo trình lấy vị trí
        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireContext());

        // 2. Kiểm tra lại quyền
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Đã có quyền (Dù là chính xác hay tương đối) -> Bật vị trí
            mMap.setMyLocationEnabled(true);
            // 3. Lấy vị trí cuối cùng được ghi nhận
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    // 4. Tạo tọa độ LatLng từ vị trí lấy được
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    // 5. Zoom mượt mà vào vị trí đó
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
                } else {
                    Toast.makeText(requireActivity(), "Không tìm thấy vị trí. Hãy bật GPS!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void performSearch() {
        String query = edtSearch.getText().toString().trim();
        if (query.isEmpty() || mMap == null) {
            Toast.makeText(requireContext(), "Vui lòng nhập địa điểm!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tự động thu gọn bàn phím xuống
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);

        try {
            android.location.Geocoder geocoder = new android.location.Geocoder(requireContext(), java.util.Locale.getDefault());

            // SỬA TẠI ĐÂY: Thay vì lấy 1, chúng ta lấy tối đa 5 kết quả (có thể chỉnh lên 10 nếu muốn)
            java.util.List<android.location.Address> addresses = geocoder.getFromLocationName(query, 5);

            if (addresses != null && !addresses.isEmpty()) {
                mMap.clear(); // Xóa các cờ cũ đi
                com.google.android.gms.maps.model.LatLngBounds.Builder builder = new com.google.android.gms.maps.model.LatLngBounds.Builder();

                // Dùng vòng lặp để cắm cờ cho TẤT CẢ các địa điểm tìm được
                for (android.location.Address address : addresses) {
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new com.google.android.gms.maps.model.MarkerOptions()
                            .position(latLng)
                            .title(address.getAddressLine(0))); // Hiện tên đường khi bấm vào cờ
                    builder.include(latLng); // Đưa tọa độ này vào khung ngắm camera
                }

                // Di chuyển camera sao cho nhìn thấy tất cả các cờ
                try {
                    if (addresses.size() == 1) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()), 16f));
                    } else {
                        // padding 150px để cờ không bị sát mép màn hình
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
                    }
                } catch (Exception e) {
                    // Phòng hờ lỗi nếu API trả về 2 điểm trùng nhau 100% tọa độ
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()), 16f));
                }

                Toast.makeText(requireContext(), "Đã tìm thấy " + addresses.size() + " vị trí! Chạm vào một vị trí để chọn.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireContext(), "Không tìm thấy địa điểm này!", Toast.LENGTH_SHORT).show();
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Lỗi mạng khi tìm kiếm, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }
}