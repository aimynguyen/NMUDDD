package com.example.diary_app.ui.pages.signup;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.diary_app.R;
import com.example.diary_app.viewmodel.SignupViewModel;

public class SignupFragment extends AppCompatActivity {

    EditText edtUsername, edtEmail,
            edtDob, edtPassword;

    Button btnSignup;

    SignupViewModel signupViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_signup);

        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtDob = findViewById(R.id.edtDob);
        edtPassword = findViewById(R.id.edtPassword);

        btnSignup = findViewById(R.id.btnSignup);

        signupViewModel =
                new ViewModelProvider(this)
                        .get(SignupViewModel.class);

        btnSignup.setOnClickListener(v -> {

            String email =
                    edtEmail.getText().toString().trim();

            String password =
                    edtPassword.getText().toString().trim();
            String dob = edtDob.getText().toString().trim();
            String username = edtUsername.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            signupViewModel.signup(email, password, username, dob);

        });

        signupViewModel.getSignupSuccess()
                .observe(this, success -> {

                    if (success) {

                        Toast.makeText(this,
                                "Đăng ký thành công! Hãy kiểm tra email để xác nhận tài khoản!",
                                Toast.LENGTH_SHORT).show();
                    }

                    //// TODO: intent sang màn hình chính
                    finish();
                });

        signupViewModel.getErrorMessage()
                .observe(this, error -> {

                    Toast.makeText(this,
                            error,
                            Toast.LENGTH_SHORT).show();

                });

        // Lắng nghe trạng thái loading để vô hiệu hóa nút bấm tránh spam
        signupViewModel.getIsLoading().observe(this, isLoading -> {
            btnSignup.setEnabled(!isLoading);
            if(isLoading) {
                btnSignup.setText("Đang đăng ký...");
            } else {
                btnSignup.setText("Đăng ký");
            }
        });
    }
}