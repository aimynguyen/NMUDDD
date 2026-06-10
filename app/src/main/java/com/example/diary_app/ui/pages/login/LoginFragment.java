package com.example.diary_app.ui.pages.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.diary_app.MainActivity;
import com.example.diary_app.R;
import com.example.diary_app.ui.pages.profile.ProfileFragment;
import com.example.diary_app.ui.pages.signup.SignupFragment;
import com.example.diary_app.viewmodel.LoginViewModel;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    private EditText edtEmail, edtPassword;
    private Button btnLogin, btnRegister;
    private LoginViewModel loginViewModel;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Inflate layout cho Fragment này
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // 2. Ánh xạ các View từ đối tượng 'view' vừa inflate
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPassword = view.findViewById(R.id.edtPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnRegister = view.findViewById(R.id.btnRegister);

        // 3. Khởi tạo ViewModel gắn với Lifecycle của Fragment
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // 4. Sự kiện khi ấn nút Đăng ký (Chuyển sang SignupFragment)
        btnRegister.setOnClickListener(v -> {
            androidx.navigation.Navigation.findNavController(v)
                    .navigate(R.id.action_nav_login_to_nav_signin);
        });

        // 5. Sự kiện khi ấn nút Đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập email và mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }

            loginViewModel.login(email, password);
        });

        // 6. Lắng nghe dữ liệu (Observe) từ ViewModel
        observeViewModel();

        return view;
    }

    private void observeViewModel() {
        // Sử dụng getViewLifecycleOwner() để đảm bảo an toàn cho vòng đời UI trong Fragment
        loginViewModel.getLoginSuccess().observe(getViewLifecycleOwner(), role -> {
            if (role.equals("admin")) {
                Toast.makeText(requireContext(), "Xin chào Quản trị viên!", Toast.LENGTH_SHORT).show();
                // TODO: Thực hiện chuyển sang màn hình admin nếu có
            } else {
                Toast.makeText(requireContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                String uid = FirebaseAuth.getInstance().getUid();
                if (uid != null && getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).saveUserIdAndFetchName(uid);
                }

                if (getView() != null) {
                    androidx.navigation.Navigation.findNavController(getView())
                            .navigate(R.id.action_nav_login_to_nav_home);
                }
            }
        });

        // Lắng nghe lỗi đăng nhập
        loginViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Lắng nghe trạng thái loading để đóng/mở nút tránh spam click
        loginViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnLogin.setEnabled(!isLoading);
            if (isLoading) {
                btnLogin.setText("Đang đăng nhập...");
            } else {
                btnLogin.setText("Đăng nhập");
            }
        });
    }
}