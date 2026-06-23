package com.example.diary_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diary_app.adapter.AdminPostAdapter;
import com.example.diary_app.data.model.Post;
import com.example.diary_app.viewmodel.AdminReviewViewModel;

import java.util.ArrayList;
import java.util.List;

public class fragment_adminreview extends Fragment {

    private AdminReviewViewModel viewModel;
    private AdminPostAdapter adapter;
    private List<Post> postList = new ArrayList<>();

    private RecyclerView rvPostList;
    private ImageButton btnBack;

    public fragment_adminreview() {
        // Required empty public constructor
    }

    public static fragment_adminreview newInstance() {
        return new fragment_adminreview();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo ViewModel gắn với scope của Fragment này
        viewModel = new ViewModelProvider(this).get(AdminReviewViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout của fragment kiểm duyệt bài viết
        return inflater.inflate(R.layout.fragment_adminreview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ các View từ Layout của Fragment
        rvPostList = view.findViewById(R.id.rvPostList);
        btnBack = view.findViewById(R.id.btnBack);

        // 2. Xử lý nút quay lại (Bởi vì là Fragment nên ta pop BackStack của FragmentManager)
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        // 3. Cấu hình RecyclerView với LayoutManager và Adapter
        rvPostList.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AdminPostAdapter(postList, new AdminPostAdapter.OnPostDeleteListener() {
            @Override
            public void onDeleteClick(Post post, int position) {
                // Gọi ViewModel thực hiện xóa bài viết khi admin đồng ý xóa trên Dialog
                viewModel.deletePost(post, position);
            }
        });
        rvPostList.setAdapter(adapter);

        // 4. Lắng nghe (Observe) các thay đổi từ ViewModel
        observeViewModel();

        // 5. Bắt đầu kích hoạt lấy dữ liệu bài viết từ Firebase Firestore lên
        viewModel.fetchAllPosts();
    }

    private void observeViewModel() {
        // Lắng nghe khi dữ liệu toàn bộ bài viết đổ về thành công
        viewModel.getPostsLiveData().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                postList.clear();
                postList.addAll(posts);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Không thể tải danh sách bài viết kiểm duyệt!", Toast.LENGTH_SHORT).show();
            }
        });

        // Lắng nghe phản hồi trạng thái xóa từ Firebase
        viewModel.getDeleteStatusLiveData().observe(getViewLifecycleOwner(), status -> {
            if ("SUCCESS".equals(status)) {
                Toast.makeText(getContext(), "Đã xóa bài viết vi phạm thành công", Toast.LENGTH_SHORT).show();
                // Đồng bộ cập nhật lại giao diện danh sách sau khi xóa phần tử
                adapter.notifyDataSetChanged();
            } else if ("FAILED".equals(status)) {
                Toast.makeText(getContext(), "Xóa thất bại, vui lòng kiểm tra kết nối mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}