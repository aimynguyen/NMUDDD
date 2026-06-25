package com.example.diary_app.ui.pages.pet;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ImageView;

import com.example.diary_app.R;
import com.example.diary_app.repository.AuthRepository;
import com.example.diary_app.viewmodel.PetViewModel;

import java.util.Locale;

public class fragment_pet_levelup extends Fragment {

    private TextView txtLevel;
    private ImageView imgReward1;
    private Button btnEquip;
    private ImageButton btnClose;
    private PetViewModel petViewModel;
    private AuthRepository authRepository;

    public fragment_pet_levelup() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pet_levelup, container, false);
        
        txtLevel = view.findViewById(R.id.txtLevel);
        imgReward1 = view.findViewById(R.id.imgReward1);
        btnEquip = view.findViewById(R.id.btnEquip);
        btnClose = view.findViewById(R.id.btnClose);

        authRepository = new com.example.diary_app.repository.AuthRepository();
        petViewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(com.example.diary_app.viewmodel.PetViewModel.class);

        petViewModel.getPetInfo().observe(getViewLifecycleOwner(), petInfo -> {
            if (petInfo != null) {
                int newLevel = petInfo.getLevel();
                txtLevel.setText("Mochi đã lên Cấp " + newLevel + "!");
                
                String rewardId = String.format(Locale.getDefault(), "bg_%02d", newLevel);
                int resId = getResources().getIdentifier(rewardId, "drawable", requireContext().getPackageName());
                if (resId != 0) {
                    imgReward1.setImageResource(resId);
                }
                
                btnEquip.setOnClickListener(v -> {
                    String userId = authRepository.getCurrentUserId();
                    if (userId != null) {
                        petViewModel.changeBackground(userId, rewardId);
                    }
                    Navigation.findNavController(v).navigate(R.id.nav_pet);
                });
            }
        });

        btnClose.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_home);
        });

        return view;
    }
}