package com.example.diary_app.Helpers;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
public class locationHelper {
    /**
     * Hàm chuyển tọa độ (Lat/Lng) thành tên địa chỉ cụ thể.
     * Trả về chuỗi địa chỉ hoặc thông báo lỗi nếu không tìm thấy.
     */
    public static String getAddressFromLocation(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            // maxResults = 1 vì chúng ta chỉ cần địa chỉ chính xác nhất
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                // address.getAddressLine(0) trả về địa chỉ đầy đủ: số nhà, tên đường, phường, quận...
                return address.getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Không thể lấy địa chỉ (Lỗi kết nối)";
        }
        return "Không tìm thấy địa chỉ";
    }
}
//cách dùng trong Activity
//String diaChi = LocationHelper.getAddressFromLocation(context, item.getLat(), item.getLng());
//txtLocation.setText(diaChi);
