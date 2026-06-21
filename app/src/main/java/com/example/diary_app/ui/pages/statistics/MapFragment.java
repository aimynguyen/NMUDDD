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
    private Spinner spinner;
    private View rootView;

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
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case 1:
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case 2:
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    case 3:
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void addControls() {
        spinner = rootView.findViewById(R.id.spinner);
        ArrayList<String> ds_Style = new ArrayList<>();
        ds_Style.add("Style 1");
        ds_Style.add("Style 2");
        ds_Style.add("Style 3");
        ds_Style.add("Style 4");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, ds_Style);
        spinner.setAdapter(arrayAdapter);
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
            // Bước A: Xóa các dấu ghim cũ và cắm một cái ghim mới vào vị trí vừa chạm
            mMap.clear();
            mMap.addMarker(new com.google.android.gms.maps.model.MarkerOptions()
                    .position(latLng)
                    .title("Vị trí được chọn"));

            // Bước B: Dùng Geocoder để dịch Tọa độ (Kinh/Vĩ độ) thành địa chỉ chữ (Tên đường, phường, quận...)
            String addressName = "Vị trí chọn từ bản đồ";
            String cityName = "";

            try {
                android.location.Geocoder geocoder = new android.location.Geocoder(requireContext(), java.util.Locale.getDefault());
                // Lấy tối đa 1 kết quả chính xác nhất
                java.util.List<android.location.Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    android.location.Address address = addresses.get(0);
                    addressName = address.getAddressLine(0); // Trích xuất địa chỉ đầy đủ (Ví dụ: 123 Đường ABC, Quận 9...)
                    cityName = address.getAdminArea(); // Lấy tên tỉnh/thành phố (Ví dụ: TP. Hồ Chí Minh)
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }

            // Bước C: Đóng gói dữ liệu vào Bundle để gửi ngược về HomeFragment
            android.os.Bundle result = new android.os.Bundle();
            result.putString("location_name", addressName);
            result.putString("city_name", cityName);
            result.putDouble("latitude", latLng.latitude);
            result.putDouble("longitude", latLng.longitude);

            // Bắn dữ liệu về qua Request Key đã hẹn trước ở HomeFragment
            getParentFragmentManager().setFragmentResult("location_request", result);

            // Bước D: Lệnh "thần thánh" để tự động đóng màn hình Map, quay về màn hình Preview
            getParentFragmentManager().popBackStack();
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
}