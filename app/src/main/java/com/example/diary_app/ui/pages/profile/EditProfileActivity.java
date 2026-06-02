package com.example.diary_app.ui.pages.profile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.diary_app.R;
import com.example.diary_app.model.UserModel;
import com.example.diary_app.viewmodel.EditProfileViewModel;

public class EditProfileActivity extends AppCompatActivity {

    ImageView imgAvatar;

    EditText edtName;
    EditText edtAvatarUrl;

    Button btnFinish;

    EditProfileViewModel viewModel;

    // old data
    String oldName = "";
    String oldAvatar = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_editprofile);

        // find view
        imgAvatar = findViewById(R.id.imgAvatar);

        edtName = findViewById(R.id.edtName);
        edtAvatarUrl = findViewById(R.id.edtAvatarUrl);

        btnFinish = findViewById(R.id.btnFinish);

        // VIEWMODEL
        viewModel =
                new ViewModelProvider(this)
                        .get(EditProfileViewModel.class);

        // load profile
        viewModel.loadProfile();

        // observe user
        viewModel.getUser()
                .observe(this, user -> {

                    if (user != null) {

                        loadUserData(user);

                    }

                });

        // observe message
        viewModel.getMessage()
                .observe(this, message -> {

                    Toast.makeText(
                            this,
                            message,
                            Toast.LENGTH_SHORT
                    ).show();

                });

        // button finish
        btnFinish.setOnClickListener(v -> {

            String newName =
                    edtName.getText()
                            .toString()
                            .trim();

            String newAvatar =
                    edtAvatarUrl.getText()
                            .toString()
                            .trim();

            // validate
            if (newName.isEmpty()) {

                edtName.setError("Name cannot be empty");
                return;

            }

            // update
            viewModel.updateProfile(
                    oldName,
                    oldAvatar,
                    newName,
                    newAvatar
            );

        });

    }

    private void loadUserData(UserModel user) {

        // save old data
        oldName = user.getUserName();
        oldAvatar = user.getAvatarUrl();

        // set text
        edtName.setText(oldName);

        edtAvatarUrl.setText(oldAvatar);

        // load avatar
        Glide.with(this)
                .load(oldAvatar)
                .placeholder(R.drawable.human_human)
                .into(imgAvatar);

    }

}