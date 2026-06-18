package com.example.diary_app.ui.pages.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diary_app.R;
import com.example.diary_app.adapter.SearchAdapter;
import com.example.diary_app.viewmodel.SearchViewModel;

public class SearchFragment extends Fragment {

    private SearchViewModel viewModel;
    private SearchAdapter adapter;

    private EditText edtSearch;
    private ImageButton iconClear;
    private ImageButton iconAddFriend;
    private ProgressBar progressBar;
    private RecyclerView rvPosts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ các UI từ code XML của bạn
        edtSearch = view.findViewById(R.id.edtSearch);
        iconClear = view.findViewById(R.id.iconClear);
        iconAddFriend = view.findViewById(R.id.iconAddFriend);
        progressBar = view.findViewById(R.id.progressBar);
        rvPosts = view.findViewById(R.id.rvPosts);

        // Khởi tạo Adapter và gán Grid Layout 2 cột cho RecyclerView
        adapter = new SearchAdapter();
        rvPosts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvPosts.setAdapter(adapter);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        // Theo dõi LiveData danh sách bài viết từ Firebase đổ về
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), posts -> {
            adapter.setData(posts);
        });

        // Theo dõi trạng thái xoay vòng tròn Loading
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Bắt sự kiện gõ chữ trên Search Bar
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();

                // Ẩn/Hiện nút Xóa nhanh chữ
                if (query.length() > 0) {
                    iconClear.setVisibility(View.VISIBLE);
                } else {
                    iconClear.setVisibility(View.GONE);
                }

                // Gửi từ khóa sang ViewModel giải quyết Debounce chặn spam request
                viewModel.setSearchQuery(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Xử lý sự kiện khi ấn nút Clear (Xóa hết chữ trong ô tìm kiếm)
        iconClear.setOnClickListener(v -> edtSearch.setText(""));

        // Click nút Add Friend
        iconAddFriend.setOnClickListener(v -> {
            // Thực hiện chức năng chuyển fragment hoặc mở màn hình thêm bạn bè ở đây
        });
    }
}