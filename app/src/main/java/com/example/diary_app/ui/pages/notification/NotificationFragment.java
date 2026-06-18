package com.example.diary_app.ui.pages.notification;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.diary_app.R;
import com.example.diary_app.adapter.NotificationAdapter;
import com.example.diary_app.core.NotiType;
import com.example.diary_app.data.model.Notification;
import com.example.diary_app.viewmodel.NotificationViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class NotificationFragment extends Fragment {

    private ImageButton btnBack, btnSetting;
    private RecyclerView rvNewest, rvPrevious;
    private TextView txtNewest, txtPrevious;

    private NotificationAdapter newestAdapter;
    private NotificationAdapter previousAdapter;
    private NotificationViewModel viewModel;

    private ArrayList<Notification> newestList = new ArrayList<>();
    private ArrayList<Notification> previousList = new ArrayList<>();

    public NotificationFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_noti, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        initViews(view);

        // 2. Cài đặt RecyclerView và Adapter
        setupRecyclerViews();

        // 3. Khởi tạo ViewModel và lắng nghe dữ liệu
        setupViewModel();

        // 4. Xử lý các sự kiện click nút bấm
        handleClicks();
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        btnSetting = view.findViewById(R.id.btnSetting);
        rvNewest = view.findViewById(R.id.rvNewest);
        rvPrevious = view.findViewById(R.id.rvPrevious);
        txtNewest = view.findViewById(R.id.txtNewest);
        txtPrevious = view.findViewById(R.id.txtPrevious);
    }

    private void setupRecyclerViews() {
        // Setup cho list "Mới nhất"
        rvNewest.setLayoutManager(new LinearLayoutManager(getContext()));
        newestAdapter = new NotificationAdapter(getContext(), newestList);
        rvNewest.setAdapter(newestAdapter);

        // Setup cho list "Trước đó"
        rvPrevious.setLayoutManager(new LinearLayoutManager(getContext()));
        previousAdapter = new NotificationAdapter(getContext(), previousList);
        rvPrevious.setAdapter(previousAdapter);

        // Bắt sự kiện click vào item thông báo
        NotificationAdapter.OnItemClickListener listener = notification -> {
            // Đánh dấu đã đọc
            if (!notification.isRead()) {
                viewModel.markAsRead(notification.getNotificationId());
            }

            // Chuyển hướng dựa theo Type của thông báo
            handleNotificationClick(notification);
        };

        newestAdapter.setOnItemClickListener(listener);
        previousAdapter.setOnItemClickListener(listener);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        // Lấy UID người dùng hiện tại
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Lắng nghe dữ liệu thay đổi
        viewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            newestList.clear();
            previousList.clear();

            for (Notification notif : notifications) {
                // Phân loại: Thông báo chưa đọc -> Cho vào "Mới nhất", đã đọc -> "Trước đó"
                if (!notif.isRead()) {
                    newestList.add(notif);
                } else {
                    previousList.add(notif);
                }
            }

            // Ẩn/Hiện tiêu đề "Mới nhất" nếu danh sách trống
            txtNewest.setVisibility(newestList.isEmpty() ? View.GONE : View.VISIBLE);
            txtPrevious.setVisibility(previousList.isEmpty() ? View.GONE : View.VISIBLE);

            // Cập nhật giao diện
            newestAdapter.setData(newestList);
            previousAdapter.setData(previousList);
        });

        // Gọi lệnh lấy dữ liệu
        viewModel.fetchNotifications(currentUserId);
    }

    private void handleClicks() {
        btnBack.setOnClickListener(v -> {
            // Quay lại màn hình trước đó
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
        
    }

    private void handleNotificationClick(Notification notification) {
        NotiType type = notification.getType();

        switch (type) {
            case DELETE_POST:
                // TODO: Hiện dialog cảnh báo vi phạm
                break;
            case REACT_POST:
                // TODO: Mở màn hình chi tiết bài viết (Truyền targetId sang)
                // String targetPostId = notification.getTargetId();
                break;
            case FRIEND_REQUEST:
                NavHostFragment.findNavController(NotificationFragment.this)
                        .navigate(R.id.action_nav_notification_to_nav_profile);
                break;
            case FRIEND_ACCEPT:
                NavHostFragment.findNavController(NotificationFragment.this)
                        .navigate(R.id.action_nav_notification_to_nav_profile);
                break;
            case PET_FEED:
                // TODO: Chuyển sang Fragment nuôi Pet
            case PET_LEVEL_UP:
                // TODO: Chuyển sang Fragment nuôi Pet
                break;
        }
    }
}
