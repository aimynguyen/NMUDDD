package com.example.diary_app.ui.pages.login;
import android.content.Intent;

import com.example.diary_app.ui.pages.profile.ProfileActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.diary_app.R;
import com.example.diary_app.ui.pages.homepage.HomeActivity;
import com.example.diary_app.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

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

            loginViewModel.login(email, password);

        });

        // Observe success
        loginViewModel.getLoginSuccess()
                .observe(this, success -> {

                    if (success) {

                        Toast.makeText(this,
                                "Login success",
                                Toast.LENGTH_SHORT).show();

                        Intent intent =
                                new Intent(LoginActivity.this,
                                        ProfileActivity.class);

                        startActivity(intent);

                        finish();

                    }

                });

        // Observe error
        loginViewModel.getErrorMessage()
                .observe(this, error -> {

                    Toast.makeText(this,
                            error,
                            Toast.LENGTH_SHORT).show();

                });
    }
}