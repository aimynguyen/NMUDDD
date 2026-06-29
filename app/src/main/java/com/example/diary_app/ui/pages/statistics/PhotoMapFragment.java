package com.example.diary_app.ui.pages.statistics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.diary_app.R;
import com.example.diary_app.data.model.Post;
import com.example.diary_app.viewmodel.StaticticsViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotoMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private StaticticsViewModel viewModel;
    private List<Post> mPosts = new ArrayList<>();
    
    private Map<String, List<Post>> markerDataMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        viewModel = new ViewModelProvider(this).get(StaticticsViewModel.class);

        if (getArguments() != null) {
            long startMs = getArguments().getLong("startDate", 0);
            long endMs = getArguments().getLong("endDate", 0);
            String userId = getArguments().getString("userId", "");

            if (startMs > 0 && endMs > 0 && !userId.isEmpty()) {
                viewModel.loadData(userId, new Date(startMs), new Date(endMs));
            }
        }

        viewModel.getPostsWithLocation().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                mPosts = posts;
                plotMarkers();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        mMap.setOnMarkerClickListener(marker -> false);

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {
                List<Post> posts = markerDataMap.get(marker.getId());
                if (posts != null) {
                    String[] options = {"Xem danh sách ảnh", "Xuất video slideshow"};
                    new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Tuỳ chọn")
                        .setItems(options, (dialog, which) -> {
                            if (which == 0) {
                                showPhotoDetailsBottomSheet(posts);
                            } else if (which == 1) {
                                showExportVideoOption(posts);
                            }
                        })
                        .show();
                }
            }

            @Override
            public void onMarkerDrag(@NonNull Marker marker) {}

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                List<Post> posts = markerDataMap.get(marker.getId());
                if (posts != null && !posts.isEmpty()) {
                    Post p = posts.get(0);
                    marker.setPosition(new LatLng(
                        p.getLocation().getCoordinates().getLatitude(),
                        p.getLocation().getCoordinates().getLongitude()
                    ));
                }
            }
        });

        plotMarkers();
    }

    private void plotMarkers() {
        if (mMap == null || mPosts == null || mPosts.isEmpty() || !isAdded() || getContext() == null) {
            return;
        }

        mMap.clear();

        // Group by coordinates (approximate to 4 decimal places to group nearby)
        Map<String, List<Post>> groupedPosts = new HashMap<>();
        for (Post post : mPosts) {
            if (post.getLocation() != null && post.getLocation().getCoordinates() != null) {
                double lat = post.getLocation().getCoordinates().getLatitude();
                double lng = post.getLocation().getCoordinates().getLongitude();
                String key = String.format("%.4f,%.4f", lat, lng);
                if (!groupedPosts.containsKey(key)) {
                    groupedPosts.put(key, new ArrayList<>());
                }
                groupedPosts.get(key).add(post);
            }
        }

        boolean isFirst = true;

        for (Map.Entry<String, List<Post>> entry : groupedPosts.entrySet()) {
            List<Post> locPosts = entry.getValue();
            // Sort to get latest image
            locPosts.sort((p1, p2) -> {
                if (p1.getCreateAt() == null || p2.getCreateAt() == null) return 0;
                return p2.getCreateAt().compareTo(p1.getCreateAt());
            });

            Post latestPost = locPosts.get(0);
            LatLng position = new LatLng(
                    latestPost.getLocation().getCoordinates().getLatitude(),
                    latestPost.getLocation().getCoordinates().getLongitude()
            );

            if (isFirst) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 5f));
                isFirst = false;
            }

            createCustomMarker(locPosts, position);
        }
    }

    private void createCustomMarker(List<Post> locPosts, LatLng position) {
        if (locPosts == null || locPosts.isEmpty()) return;
        Post latestPost = locPosts.get(0);
        String imageUrl = latestPost.getImageUrl();
        int count = locPosts.size();
        View markerView = LayoutInflater.from(requireContext()).inflate(R.layout.map_marker_photo, null);
        ImageView markerImage = markerView.findViewById(R.id.markerImage);
        TextView markerCount = markerView.findViewById(R.id.markerCount);

        if (count > 1) {
            markerCount.setVisibility(View.VISIBLE);
            markerCount.setText(String.valueOf(count));
        }

        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .centerCrop()
            .into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    markerImage.setImageBitmap(resource);
                    Bitmap icon = createDrawableFromView(markerView);
                    if (mMap != null) {
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(position)
                                .draggable(true)
                                .icon(BitmapDescriptorFactory.fromBitmap(icon)));
                        markerDataMap.put(marker.getId(), locPosts);
                    }
                }

                @Override
                public void onLoadCleared(@Nullable android.graphics.drawable.Drawable placeholder) {}
            });
    }

    private Bitmap createDrawableFromView(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void showPhotoDetailsBottomSheet(List<Post> posts) {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = 
            new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
            
        android.widget.ScrollView scrollView = new android.widget.ScrollView(requireContext());
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 48);
        
        TextView title = new TextView(requireContext());
        title.setText("Chi tiết ảnh tại địa điểm này");
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 32);
        layout.addView(title);
        
        for (Post post : posts) {
            ImageView iv = new ImageView(requireContext());
            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 800);
            params.setMargins(0, 0, 0, 16);
            iv.setLayoutParams(params);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setBackgroundColor(0xFFDDDDDD);
            
            TextView tvDate = new TextView(requireContext());
            if (post.getCreateAt() != null) {
                tvDate.setText(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(post.getCreateAt().toDate()));
            } else {
                tvDate.setText("Không có ngày");
            }
            tvDate.setPadding(0, 0, 0, 32);
            
            Glide.with(this).load(post.getImageUrl()).into(iv);
            
            layout.addView(iv);
            layout.addView(tvDate);
        }
        
        scrollView.addView(layout);
        bottomSheetDialog.setContentView(scrollView);
        bottomSheetDialog.show();
    }

    private void showExportVideoOption(List<Post> posts) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_video_config, null);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();

        android.widget.Spinner spinnerMusic = dialogView.findViewById(R.id.spinnerMusic);
        android.widget.SeekBar seekBarDuration = dialogView.findViewById(R.id.seekBarDuration);
        android.widget.TextView tvDurationValue = dialogView.findViewById(R.id.tvDurationValue);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        android.widget.Button btnStartExport = dialogView.findViewById(R.id.btnStartExport);
        btnStartExport.setText("Tiếp tục");

        String[] musicNames = {
                "Bật nhạc lên", "Đi giữa trời rực rỡ", "Lớn rồi còn khóc nhè",
                "Matchanah", "Mới hôm qua", "Nơi pháo hoa rực rỡ",
                "Thức giấc", "Vạn sự như ý", "Về đi thôi", "Xin đừng nhấc máy"
        };
        int[] musicResIds = {
                R.raw.bat_nhac_len, R.raw.di_giua_troi_ruc_ro, R.raw.lon_roi_con_khoc_nhe,
                R.raw.matchanah, R.raw.moi_hom_qua, R.raw.noi_phao_hoa_ruc_ro,
                R.raw.thuc_giac, R.raw.van_su_nhu_y, R.raw.ve_di_thoi, R.raw.xin_dung_nhac_may
        };

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, musicNames);
        spinnerMusic.setAdapter(adapter);

        seekBarDuration.setMax(9);
        seekBarDuration.setProgress(2);
        tvDurationValue.setText("3 giây");

        seekBarDuration.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                tvDurationValue.setText((progress + 1) + " giây");
            }
            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });

        btnCancel.setOnClickListener(v2 -> dialog.dismiss());

        btnStartExport.setOnClickListener(v2 -> {
            int selectedMusicResId = musicResIds[spinnerMusic.getSelectedItemPosition()];
            int selectedDuration = seekBarDuration.getProgress() + 1;
            dialog.dismiss();

            Bundle args = new Bundle();
            ArrayList<String> imageUrls = new ArrayList<>();
            for (Post p : posts) {
                imageUrls.add(p.getImageUrl());
            }
            args.putStringArrayList("imageUrls", imageUrls);
            args.putInt("musicResId", selectedMusicResId);
            args.putInt("duration", selectedDuration);

            if (getArguments() != null) {
                args.putLong("startDate", getArguments().getLong("startDate", 0));
                args.putLong("endDate", getArguments().getLong("endDate", 0));
                args.putString("userId", getArguments().getString("userId", ""));
            }

            try {
                Navigation.findNavController(requireView()).navigate(R.id.nav_video, args);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Không thể chuyển đến tạo video", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}
