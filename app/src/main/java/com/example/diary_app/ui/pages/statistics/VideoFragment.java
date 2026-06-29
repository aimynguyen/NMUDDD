package com.example.diary_app.ui.pages.statistics;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.diary_app.R;
import com.example.diary_app.adapter.VideoAdapter;
import com.example.diary_app.utils.VideoExporter;
import com.example.diary_app.viewmodel.StaticticsViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoFragment extends Fragment {

    private ViewPager2 viewPager2;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private MediaPlayer mediaPlayer;
    private final List<String> imageUrls = new ArrayList<>();
    private VideoAdapter adapter;
    private StaticticsViewModel viewModel;
    private ImageButton btnSave;

    private int musicResId = R.raw.bat_nhac_len;
    private int duration = 3;

    // Xin quyền WRITE_EXTERNAL_STORAGE (chỉ cần Android ≤ 9)
    private final ActivityResultLauncher<String> permLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startExportVideo(musicResId, duration);
                else Toast.makeText(getContext(), "Cần cấp quyền để lưu video", Toast.LENGTH_SHORT).show();
            });

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_videoslideshow, container, false);

        Bundle args = getArguments();
        if (args != null) {
            musicResId = args.getInt("musicResId", R.raw.bat_nhac_len);
            duration = args.getInt("duration", 3);
        }

        viewPager2 = view.findViewById(R.id.viewPagerVideo);
        btnSave    = view.findViewById(R.id.btnSave);

        adapter = new VideoAdapter(imageUrls);
        viewPager2.setAdapter(adapter);

        // Nhạc nền
        mediaPlayer = MediaPlayer.create(getContext(), musicResId);
        mediaPlayer.setLooping(true);

        // Auto-scroll
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, duration * 1000L);
            }
        });

        // Nút Lưu Video
        btnSave.setOnClickListener(v -> {
            if (imageUrls.isEmpty()) {
                Toast.makeText(getContext(), "Không có ảnh nào để xuất video", Toast.LENGTH_SHORT).show();
                return;
            }
            checkPermissionAndExport(musicResId, duration);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(StaticticsViewModel.class);

        Bundle args = getArguments();
        if (args != null) {
            long startMs = args.getLong("startDate", 0L);
            long endMs   = args.getLong("endDate", 0L);
            String uid   = args.getString("userId", "");
            if (startMs > 0 && endMs > 0 && !uid.isEmpty())
                viewModel.loadData(uid, new Date(startMs), new Date(endMs));
            else
                Toast.makeText(getContext(), "Không xác định được tháng", Toast.LENGTH_SHORT).show();
        }

        viewModel.getPostImageUrls().observe(getViewLifecycleOwner(), urls -> {
            if (urls == null || urls.isEmpty()) {
                Toast.makeText(getContext(), "Tháng này chưa có ảnh nào", Toast.LENGTH_SHORT).show();
                return;
            }
            imageUrls.clear();
            imageUrls.addAll(urls);
            adapter.notifyDataSetChanged();
            sliderHandler.postDelayed(sliderRunnable, duration * 1000L);
        });
    }

    private void checkPermissionAndExport(int musicResId, int duration) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            startExportVideo(musicResId, duration);
        }
    }

    // ── Xuất Video ────────────────────────────────────────────────────────────

    @SuppressWarnings("deprecation")
    private void startExportVideo(int audioResId, int secondsPerImage) {
        ProgressDialog progress = new ProgressDialog(requireContext());
        progress.setTitle("Đang tạo video...");
        progress.setMessage("Chuẩn bị...");
        progress.setCancelable(false);
        progress.show();

        // Tải tất cả ảnh bằng Glide trên background thread trước
        ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.execute(() -> {
            List<Bitmap> bitmaps = new ArrayList<>();
            for (String url : imageUrls) {
                try {
                    Bitmap bmp = Glide.with(requireContext())
                            .asBitmap().load(url).submit(
                                    VideoExporter.WIDTH, VideoExporter.HEIGHT).get();
                    bitmaps.add(bmp);
                } catch (Exception e) {
                    // Bỏ qua ảnh lỗi
                }
            }

            if (bitmaps.isEmpty()) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    progress.dismiss();
                    Toast.makeText(getContext(), "Không tải được ảnh nào", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            // Bắt đầu xuất video
            VideoExporter.export(requireContext(), bitmaps, audioResId, secondsPerImage,
                    new VideoExporter.ExportListener() {

                        @Override
                        public void onProgress(String message) {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    progress.setMessage(message));
                        }

                        @Override
                        public void onSuccess(Uri savedUri) {
                            progress.dismiss();
                            Toast.makeText(getContext(),
                                    "✅ Đã lưu video vào Album ảnh (DCIM/DiaryApp)!", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(String message) {
                            progress.dismiss();
                            Toast.makeText(getContext(),
                                    "❌ Lỗi: " + message, Toast.LENGTH_LONG).show();
                        }
                    });
        });
        exec.shutdown();
    }

    // ── Auto-scroll ───────────────────────────────────────────────────────────

    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (imageUrls.isEmpty()) return;
            int cur = viewPager2.getCurrentItem();
            viewPager2.setCurrentItem(cur < imageUrls.size() - 1 ? cur + 1 : 0);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (!imageUrls.isEmpty()) sliderHandler.postDelayed(sliderRunnable, duration * 1000L);
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) mediaPlayer.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
        if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sliderHandler.removeCallbacks(sliderRunnable);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}