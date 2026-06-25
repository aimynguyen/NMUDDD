package com.example.diary_app.ui.pages.pet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diary_app.R;
import com.example.diary_app.adapter.InventoryAdapter;
import com.example.diary_app.core.PetConstants;
import com.example.diary_app.data.model.PetInfo;
import com.example.diary_app.repository.AuthRepository;
import com.example.diary_app.viewmodel.PetViewModel;

import java.util.ArrayList;

public class InventoryFragment extends Fragment {

    private RecyclerView rvInventory;
    private ImageButton btnBack;
    private InventoryAdapter adapter;
    private PetViewModel petViewModel;
    private AuthRepository authRepository;

    public InventoryFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);

        rvInventory = view.findViewById(R.id.rvInventory);
        btnBack = view.findViewById(R.id.btnBack);

        rvInventory.setLayoutManager(new GridLayoutManager(getContext(), 2));

        authRepository = new AuthRepository();
        String userId = authRepository.getCurrentUserId();

        petViewModel = new ViewModelProvider(requireActivity()).get(PetViewModel.class);

        adapter = new InventoryAdapter(new ArrayList<>(), "", item -> {
            if (item.isUnlocked()) {
                petViewModel.changeBackground(userId, item.getId());
                //Toast.makeText(getContext(), "Đã trang bị", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            } else {
                Toast.makeText(getContext(), "Cần đạt " + item.getName() + " để mở khóa!", Toast.LENGTH_SHORT).show();
            }
        });
        rvInventory.setAdapter(adapter);

        petViewModel.getPetInfo().observe(getViewLifecycleOwner(), petInfo -> {
            if (petInfo != null) {
                petViewModel.generateInventoryList(requireContext(), petInfo.getUnlockedItems());
            }
        });

        petViewModel.getInventoryItems().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                PetInfo petInfo = petViewModel.getPetInfo().getValue();
                String equippedBg = "";
                if (petInfo != null && petInfo.getEquippedItems() != null && petInfo.getEquippedItems().containsKey(PetConstants.ITEM_TYPE_BACKGROUND)) {
                    equippedBg = petInfo.getEquippedItems().get(PetConstants.ITEM_TYPE_BACKGROUND);
                }
                adapter.updateData(items, equippedBg);
            }
        });

        if (userId != null) {
            petViewModel.loadPetData(userId);
        }

        btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        return view;
    }
}
