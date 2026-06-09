package com.example.diary_app.ui.pages.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.diary_app.R;
import com.example.diary_app.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private TextView txtName;
    private TextView txtEditProfile;
    private ImageView imgAvatar;

    private ProfileViewModel profileViewModel;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Inflate layout cho Fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 2. Ánh xạ các View từ XML (sử dụng biến view)
        txtName = view.findViewById(R.id.txtName);
        txtEditProfile = view.findViewById(R.id.txtEditProfile);
        imgAvatar = view.findViewById(R.id.imgAvatar);

        // 3. Khởi tạo ViewModel gắn với vòng đời của Fragment
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // 4. Load data
        profileViewModel.loadProfile();

        // 5. Observe dữ liệu (Sử dụng getViewLifecycleOwner() tốt hơn dùng 'this' trong Fragment)
        profileViewModel.getUserName().observe(getViewLifecycleOwner(), name -> {
            if (name != null) {
                txtName.setText(name);
            }
        });

        profileViewModel.getAvatarUrl().observe(getViewLifecycleOwner(), url -> {
            // Sử dụng requireContext() cho Glide
            Glide.with(requireContext())
                    .load(url)
                    .placeholder(R.drawable.human_human)
                    .error(R.drawable.human_human)
                    .into(imgAvatar);
        });

        // 6. Xử lý sự kiện click mở màn hình chỉnh sửa
        txtEditProfile.setOnClickListener(v -> {
            NavHostFragment.findNavController(ProfileFragment.this)
                    .navigate(R.id.action_nav_profile_to_nav_edit_profile);
        });

        return view;
    }
}