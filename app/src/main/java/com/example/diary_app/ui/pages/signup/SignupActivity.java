package com.example.diary_app.ui.pages.signup;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.diary_app.R;
import com.example.diary_app.viewmodel.SignupViewModel;

public class SignupActivity extends AppCompatActivity {

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

            signupViewModel.signup(email, password);

        });

        signupViewModel.getSignupSuccess()
                .observe(this, success -> {

                    if (success) {

                        Toast.makeText(this,
                                "Signup success",
                                Toast.LENGTH_SHORT).show();
                    }

                });

        signupViewModel.getErrorMessage()
                .observe(this, error -> {

                    Toast.makeText(this,
                            error,
                            Toast.LENGTH_SHORT).show();

                });
    }
}