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
import androidx.navigation.Navigation;

import com.example.diary_app.MainActivity;
import com.example.diary_app.R;
import com.example.diary_app.viewmodel.LoginViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        edtEmail = view.findViewById(R.id.edtEmail);
        edtPassword = view.findViewById(R.id.edtPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnRegister = view.findViewById(R.id.btnRegister);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        btnRegister.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_login_to_nav_signin);
        });

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập email và mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }

            loginViewModel.login(email, password);
        });

        observeViewModel();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // 1. Kiểm tra trạng thái đăng nhập
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // 2. SỬA LỖI TẠI ĐÂY: Dùng Navigation để chuyển màn hình thay vì khởi chạy lại MainActivity
            if (getView() != null) {
                Navigation.findNavController(getView())
                        .navigate(R.id.action_nav_login_to_nav_home);
            }
        }
    }

    private void observeViewModel() {
        loginViewModel.getLoginSuccess().observe(getViewLifecycleOwner(), role -> {
            if (role.equals("admin")) {
                Toast.makeText(requireContext(), "Xin chào Quản trị viên!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                String uid = FirebaseAuth.getInstance().getUid();
                if (uid != null && getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).saveUserIdAndFetchName(uid);
                }

                if (getView() != null) {
                    Navigation.findNavController(getView())
                            .navigate(R.id.action_nav_login_to_nav_home);
                }
            }
        });

        loginViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

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
