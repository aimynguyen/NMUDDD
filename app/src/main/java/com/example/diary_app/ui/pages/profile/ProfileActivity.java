package com.example.diary_app.ui.pages.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.diary_app.R;
import com.example.diary_app.viewmodel.ProfileViewModel;

import com.bumptech.glide.Glide;
import android.widget.ImageView;

public class ProfileActivity extends AppCompatActivity {

    TextView txtName;
    TextView txtEditProfile;
    ImageView imgAvatar;

    ProfileViewModel profileViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_profile);

        // findViewById
        txtName = findViewById(R.id.txtName);
        txtEditProfile = findViewById(R.id.txtEditProfile);
        imgAvatar = findViewById(R.id.imgAvatar);

        // ViewModel
        profileViewModel =
                new ViewModelProvider(this)
                        .get(ProfileViewModel.class);

        // load data
        profileViewModel.loadProfile();

        // observe data
        profileViewModel.getUserName()
                .observe(this, name -> {

                    if (name != null) {
                        txtName.setText(name);
                    }

                });

        profileViewModel.getAvatarUrl()
                .observe(this, url -> {

                    Glide.with(this)
                            .load(url)
                            .placeholder(R.drawable.human_human)
                            .error(R.drawable.human_human)
                            .into(imgAvatar);

                });

        // open edit profile
        txtEditProfile.setOnClickListener(v -> {
            Intent intent =
                    new Intent(
                            ProfileActivity.this,
                            EditProfileActivity.class
                    );

            startActivity(intent);
        });

    }
}