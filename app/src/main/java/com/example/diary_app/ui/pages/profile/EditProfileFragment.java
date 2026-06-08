package com.example.diary_app.ui.pages.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.diary_app.R;
import com.example.diary_app.model.UserModel;
import com.example.diary_app.viewmodel.EditProfileViewModel;

public class EditProfileFragment extends Fragment {

    private ImageView imgAvatar;
    private EditText edtName;
    private EditText edtAvatarUrl;
    private Button btnFinish;

    private EditProfileViewModel viewModel;

    // old data
    private String oldName = "";
    private String oldAvatar = "";

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Inflate layout cho Fragment
        View view = inflater.inflate(R.layout.layout_editprofile, container, false);

        // 2. Tìm view từ object 'view' vừa inflate
        imgAvatar = view.findViewById(R.id.imgAvatar);
        edtName = view.findViewById(R.id.edtName);
        edtAvatarUrl = view.findViewById(R.id.edtAvatarUrl);
        btnFinish = view.findViewById(R.id.btnFinish);

        // 3. Khởi tạo VIEWMODEL gắn liền với Fragment
        viewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);

        // 4. Load profile
        viewModel.loadProfile();

        // 5. Observe dữ liệu với LifecycleOwner của Fragment
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                loadUserData(user);
            }
        });

        // Observe message thông báo
        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });

        // 6. Xử lý sự kiện click button finish
        btnFinish.setOnClickListener(v -> {
            String newName = edtName.getText().toString().trim();
            String newAvatar = edtAvatarUrl.getText().toString().trim();

            // Validate dữ liệu đầu vào
            if (newName.isEmpty()) {
                edtName.setError("Name cannot be empty");
                return;
            }

            // Gọi hàm update trong ViewModel
            viewModel.updateProfile(
                    oldName,
                    oldAvatar,
                    newName,
                    newAvatar
            );
        });

        return view;
    }

    private void loadUserData(UserModel user) {
        // Lưu lại dữ liệu cũ
        oldName = user.getUserName();
        oldAvatar = user.getAvatarUrl();

        // Set text lên các ô nhập liệu
        edtName.setText(oldName);
        edtAvatarUrl.setText(oldAvatar);

        // Load ảnh đại diện bằng Glide (Dùng requireContext() thay vì 'this')
        Glide.with(requireContext())
                .load(oldAvatar)
                .placeholder(R.drawable.human_human)
                .into(imgAvatar);
    }
}