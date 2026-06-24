package com.example.diary_app.ui.pages.pet;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diary_app.R;
import com.example.diary_app.core.PetConstants;
import com.example.diary_app.repository.AuthRepository;
import com.example.diary_app.viewmodel.PetViewModel;

public class PetFragment extends Fragment {

    private ImageView imgBackground, btnSetting, imgPet, btnInventory;
    private TextView txtPetName, txtPetLevel, txtXP, txtEnergy, txtStreak, txtQuote;
    
    private PetViewModel petViewModel;
    private AuthRepository authRepository;

    public PetFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pet_home, container, false);

        // Ánh xạ UI
        imgBackground = view.findViewById(R.id.imgBackground);
        btnSetting = view.findViewById(R.id.btnSetting);
        imgPet = view.findViewById(R.id.imgPet);
        btnInventory = view.findViewById(R.id.btnInventory);
        txtPetName = view.findViewById(R.id.txtPetName);
        txtPetLevel = view.findViewById(R.id.txtPetLevel);
        txtXP = view.findViewById(R.id.txtXP);
        txtEnergy = view.findViewById(R.id.txtEnergy);
        txtStreak = view.findViewById(R.id.txtStreak);
        txtQuote = view.findViewById(R.id.txtQuote);

        petViewModel = new ViewModelProvider(this).get(PetViewModel.class);
        authRepository = new AuthRepository();
        String userId = authRepository.getCurrentUserId();

        // Lắng nghe thông tin Pet
        petViewModel.getPetInfo().observe(getViewLifecycleOwner(), petInfo -> {
            if (petInfo != null) {
                txtPetName.setText("Mochi"); 
                txtPetLevel.setText("Lv." + petInfo.getLevel());
                
                int nextLevelExp = petInfo.getLevel() < PetConstants.MAX_LEVEL 
                        ? PetConstants.EXP_THRESHOLDS[petInfo.getLevel() + 1] 
                        : petInfo.getCurrentExp();
                txtXP.setText(petInfo.getCurrentExp() + " / " + nextLevelExp + " XP");
                txtStreak.setText("🔥" + petInfo.getStreakDays());
                
                int energyPercent = (int) ((petInfo.getDailyExp() / (float) PetConstants.MAX_DAILY_EXP) * 100);
                txtEnergy.setText("⚡" + energyPercent + "%");
            }
        });

        // Lắng nghe cảm xúc để đổi Animation
        petViewModel.getPetEmotion().observe(getViewLifecycleOwner(), emotion -> {
            // Xóa src mặc định để dùng background làm animation
            imgPet.setImageDrawable(null); 

            if (PetConstants.EMOTION_HAPPY.equals(emotion)) {
                imgPet.setBackgroundResource(R.drawable.pet_happy);
            } else if (PetConstants.EMOTION_SAD.equals(emotion)) {
                imgPet.setBackgroundResource(R.drawable.pet_sad);
            } else if (PetConstants.EMOTION_ANGRY.equals(emotion)) {
                imgPet.setBackgroundResource(R.drawable.pet_angry);
            } else {
                imgPet.setBackgroundResource(R.drawable.pet_sleep);
            }

            AnimationDrawable animation = (AnimationDrawable) imgPet.getBackground();
            
            if (animation != null) {
                animation.start();
            }
        });

        // Lắng nghe câu Quote
        petViewModel.getPetQuote().observe(getViewLifecycleOwner(), quote -> {
            txtQuote.setText(quote);
        });

        petViewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        if (userId != null) {
            petViewModel.loadPetData(userId);
            petViewModel.checkTodayEmotion(userId);
        }

        return view;
    }
}