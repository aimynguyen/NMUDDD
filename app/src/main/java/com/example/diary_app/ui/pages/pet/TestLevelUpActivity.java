package com.example.diary_app.ui.pages.pet;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diary_app.R;

public class TestLevelUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_pet_levelup);

        ImageView imgPet = findViewById(R.id.imgPet);

        imgPet.setBackgroundResource(R.drawable.pet_levelup);

        AnimationDrawable animation =
                (AnimationDrawable) imgPet.getBackground();

        animation.start();
    }
}