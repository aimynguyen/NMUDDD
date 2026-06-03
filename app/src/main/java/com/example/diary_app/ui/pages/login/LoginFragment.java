package com.example.diary_app.ui.pages.login;
import android.content.Intent;

import com.example.diary_app.ui.pages.profile.ProfileFragment;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.diary_app.R;
import com.example.diary_app.ui.pages.signup.SignupFragment;
import com.example.diary_app.viewmodel.LoginViewModel;

public class LoginFragment extends AppCompatActivity {

    EditText edtEmail, edtPassword;

    Button btnLogin, btnRegister;

    LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        // findViewById
        edtEmail = findViewById(R.id.edtEmail);

        edtPassword = findViewById(R.id.edtPassword);

        btnLogin = findViewById(R.id.btnLogin);

        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginFragment.this, SignupFragment.class);
            startActivity(intent);
        });

        // ViewModel
        loginViewModel =
                new ViewModelProvider(this)
                        .get(LoginViewModel.class);

        // Button login
        btnLogin.setOnClickListener(v -> {

            String email =
                    edtEmail.getText().toString().trim();

            String password =
                    edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }

            loginViewModel.login(email, password);

        });

        // Observe success
        loginViewModel.getLoginSuccess().observe(this, role -> {

            if(role.equals("admin")){
                Toast.makeText(this, "Xin chào Quản trị viên!", Toast.LENGTH_SHORT).show();
                ///// TODO: intent sang màn hình admin
            }
            else {
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                Intent intent =
                        new Intent(LoginFragment.this,
                                ProfileFragment.class);

                startActivity(intent);
            }
            finish();

        });

        // Observe error
        loginViewModel.getErrorMessage()
                .observe(this, error -> {

                    Toast.makeText(this,
                            error,
                            Toast.LENGTH_SHORT).show();

                });

        // Observe loading
        loginViewModel.getIsLoading().observe(this, isLoading -> {
            btnLogin.setEnabled(!isLoading); // Khóa nút khi đang load
            if (isLoading) {
                btnLogin.setText("Đang đăng nhập...");
            } else {
                btnLogin.setText("Đăng nhập");
            }
        });
    }
}