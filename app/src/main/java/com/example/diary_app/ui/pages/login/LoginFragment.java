package com.example.diary_app.ui.pages.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

        TextView txtForgot = view.findViewById(R.id.txtForgot);
        txtForgot.setOnClickListener(v -> {
            showForgotPasswordDialog();
        });

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
            // Khi đã đăng nhập, thông thường ta sẽ muốn kiểm tra Role trước khi điều hướng.
            // Ở đây tạm thời để LoginViewModel xử lý hoặc để user tự chuyển.
            // Để đơn giản và sửa lỗi nhanh, ta có thể gọi login profile check nếu cần, 
            // nhưng hiện tại onStart đang mặc định về Home.
            if (getView() != null) {
                 // Navigation.findNavController(getView()).navigate(R.id.action_nav_login_to_nav_home);
            }
        }
    }

    private void observeViewModel() {
        loginViewModel.getLoginSuccess().observe(getViewLifecycleOwner(), role -> {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid != null && getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).saveUserIdAndFetchName(uid);
            }

            if (role.equals("admin")) {
                Toast.makeText(requireContext(), "Xin chào Quản trị viên!", Toast.LENGTH_SHORT).show();
                if (getView() != null) {
                    Navigation.findNavController(getView())
                            .navigate(R.id.action_nav_login_to_nav_admin);
                }
            } else {
                Toast.makeText(requireContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
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

    private void showForgotPasswordDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_forgot_password, null);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText edtEmail = dialogView.findViewById(R.id.edtForgotEmail);
        Button btnSend = dialogView.findViewById(R.id.btnSendForgot);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelForgot);
        android.widget.ProgressBar progressBar = dialogView.findViewById(R.id.progressBarForgot);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSend.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập email!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "Email không đúng định dạng!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            loginViewModel.forgotPassword(email);
        });

        loginViewModel.getIsForgotPasswordLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                if (isLoading) {
                    progressBar.setVisibility(View.VISIBLE);
                    btnSend.setEnabled(false);
                    btnSend.setText("Đang gửi...");
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnSend.setEnabled(true);
                    btnSend.setText("Gửi yêu cầu");
                }
            }
        });

        loginViewModel.getForgotPasswordSuccess().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                loginViewModel.clearForgotPasswordStatus();
                dialog.dismiss();
            }
        });

        loginViewModel.getForgotPasswordError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                loginViewModel.clearForgotPasswordStatus();
            }
        });

        dialog.show();
    }
}
