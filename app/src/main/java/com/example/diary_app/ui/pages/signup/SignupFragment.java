package com.example.diary_app.ui.pages.signup;

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
import androidx.navigation.Navigation;

import com.example.diary_app.R;
import com.example.diary_app.viewmodel.SignupViewModel;

public class SignupFragment extends Fragment {

    private EditText edtUsername, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnSignup;
    private SignupViewModel signupViewModel;

    public SignupFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Inflate layout cho Fragment
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        // 2. Ánh xạ các View từ đối tượng 'view'
        edtUsername = view.findViewById(R.id.edtUsername);
        edtEmail = view.findViewById(R.id.edtEmail);

        edtPassword = view.findViewById(R.id.edtPassword);
        edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword);
        btnSignup = view.findViewById(R.id.btnSignup);

        // 3. Khởi tạo ViewModel gắn với vòng đời của Fragment
        signupViewModel = new ViewModelProvider(this).get(SignupViewModel.class);

        // 4. Xử lý sự kiện click nút Đăng ký
        btnSignup.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();
            String dob = "";
            String username = edtUsername.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(requireContext(), "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
                return;
            }

            signupViewModel.signup(email, password, username, dob);
        });

        // 5. Lắng nghe (Observe) các trạng thái từ ViewModel
        observeViewModel();

        return view;
    }

    private void observeViewModel() {
        // Sử dụng getViewLifecycleOwner() thay vì 'this' để lắng nghe LiveData an toàn hơn trong Fragment
        signupViewModel.getSignupSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                // Hiển thị dialog thông báo xác nhận email
                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Đăng ký thành công!")
                        .setMessage("📧 Chúng tôi đã gửi email xác nhận đến hộp thư của bạn.\n\nVui lòng kiểm tra email và nhấn vào liên kết xác nhận, sau đó quay lại đăng nhập.")
                        .setCancelable(false)
                        .setPositiveButton("Đẹn, tôi hiểu!", (dialog, which) -> {
                            // Điều hướng về màn hình Login
                            if (getView() != null) {
                                Navigation.findNavController(getView())
                                        .navigate(R.id.action_nav_signin_to_nav_login);
                            }
                        })
                        .show();
            }
        });

        signupViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Lắng nghe trạng thái loading để vô hiệu hóa nút bấm tránh spam click
        signupViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnSignup.setEnabled(!isLoading);
            if (isLoading) {
                btnSignup.setText("Đang đăng ký...");
            } else {
                btnSignup.setText("Đăng ký");
            }
        });
    }
}