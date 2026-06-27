package com.example.diary_app.ui.pages.pet;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diary_app.R;
import com.example.diary_app.core.Mood;
import com.example.diary_app.core.PetConstants;
import com.example.diary_app.repository.AuthRepository;
import com.example.diary_app.viewmodel.PetViewModel;

import java.util.Date;
import java.util.Locale;

public class PetFragment extends Fragment {

    private ImageView imgBackground, btnSetting, imgPet, btnInventory;
    private TextView txtPetName, txtPetLevel, txtXP, txtEnergy, txtStreak, txtQuote;
    private android.widget.ProgressBar pbXP;
    
    private PetViewModel petViewModel;
    private AuthRepository authRepository;

    public PetFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pet_home, container, false);

        // Ánh xạ UI
        imgBackground = view.findViewById(R.id.imgBackground);
        imgPet = view.findViewById(R.id.imgPet);
        btnInventory = view.findViewById(R.id.btnInventory);
        txtPetName = view.findViewById(R.id.txtPetName);
        txtPetLevel = view.findViewById(R.id.txtPetLevel);
        txtXP = view.findViewById(R.id.txtXP);
        pbXP = view.findViewById(R.id.pbXP);
        txtEnergy = view.findViewById(R.id.txtEnergy);
        txtStreak = view.findViewById(R.id.txtStreak);
        txtQuote = view.findViewById(R.id.txtQuote);

        petViewModel = new ViewModelProvider(requireActivity()).get(PetViewModel.class);
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
                if (pbXP != null) {
                    pbXP.setMax(nextLevelExp);
                    pbXP.setProgress(petInfo.getCurrentExp());
                }
                txtStreak.setText("🔥" + petInfo.getStreakDays());
                
                int displayDailyExp = petInfo.getDailyExp();
                String todayStr = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                if (!todayStr.equals(petInfo.getLastExpDate())) {
                    displayDailyExp = 0;
                }
                
                int energyPercent = (int) ((displayDailyExp / (float) PetConstants.MAX_DAILY_EXP) * 100);
                txtEnergy.setText("⚡" + energyPercent + "%");

                // Cập nhật background
                if (petInfo.getEquippedItems() != null) {
                    String bgId = petInfo.getEquippedItems().get(PetConstants.ITEM_TYPE_BACKGROUND);
                    if (bgId != null) {
                        int resId = getResources().getIdentifier(bgId, "drawable", requireContext().getPackageName());
                        if (resId != 0) {
                            imgBackground.setImageResource(resId);
                        }
                    }
                }
            }
        });

        // Lắng nghe cảm xúc để đổi Animation
        petViewModel.getPetEmotion().observe(getViewLifecycleOwner(), emotion -> {
            // Xóa src mặc định để dùng background làm animation
            imgPet.setImageDrawable(null); 

            try {
                Mood mood = Mood.valueOf(emotion.toUpperCase(java.util.Locale.ROOT));
                
                if (mood == Mood.HAPPY || mood == Mood.NEUTRAL || mood == Mood.CALM) {
                    imgPet.setBackgroundResource(R.drawable.pet_happy);
                } else if (mood == Mood.SAD) {
                    imgPet.setBackgroundResource(R.drawable.pet_sad);
                } else if (mood == Mood.ANGRY) {
                    imgPet.setBackgroundResource(R.drawable.pet_angry);
                } else {
                    imgPet.setBackgroundResource(R.drawable.pet_sleep);
                }
            } catch (Exception e) {
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

        petViewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                String message = event.getContentIfNotHandled();
                if (message != null && !message.isEmpty()) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (userId != null) {
            petViewModel.loadPetData(userId);
            petViewModel.checkTodayEmotion(userId);
        }

        btnInventory.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_pet_to_nav_inventory);
        });

        final long[] lastClickTime = {0};
        imgPet.setOnClickListener(v -> {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime[0] < 300) {
                petViewModel.changeQuote();
            }
            lastClickTime[0] = clickTime;
        });

        return view;
    }
}