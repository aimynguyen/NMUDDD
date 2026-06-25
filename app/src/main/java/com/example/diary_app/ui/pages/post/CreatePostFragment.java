package com.example.diary_app.ui.pages.post;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.diary_app.R;
import com.example.diary_app.core.PetConstants;
import com.example.diary_app.data.model.Location;
import com.example.diary_app.repository.AuthRepository;
import com.example.diary_app.viewmodel.PetViewModel;
import com.example.diary_app.viewmodel.PostViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
public class CreatePostFragment extends Fragment {
    private PostViewModel postViewModel;
    private PetViewModel petViewModel;
    private AuthRepository authRepository;

    // Các khối giao diện chính
    private View viewCamera, viewPreview;

    // View của màn hình Camera
    private ImageView btnUpload;
    private View btnCapture;

    // View của màn hình Preview
    private ImageButton btnRemoveImage;
    private ImageView imgPreview;
    private LinearLayout btnPrivate, btnPublic;
    private TextView txtDate;
    private EditText edtCaption;
    private AutoCompleteTextView autoLocation;
    private Button btnPost;

    // Biến lưu trạng thái dữ liệu
    private Uri selectedImageUri = null;
    private String currentPrivacy = "public"; // Mặc định là public
    // LƯU Ý MỚI: Không lưu icon nữa, mà lấy tên Enum ("NORMAL", "HAPPY"...) để đẩy lên Firebase
    private String currentMood = com.example.diary_app.core.Mood.HAPPY.name();
    private com.example.diary_app.repository.UserRepository userRepository;

    // --- BIẾN CAMERA ---
    private androidx.camera.view.PreviewView viewFinder;
    private androidx.camera.core.ImageCapture imageCapture;
    // Trình chọn ảnh từ thư viện
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgPreview.setImageURI(uri);
                    switchMode(true); // Có ảnh -> Chuyển sang màn hình Preview
                }
            });

    // Hàm hỗ trợ lưu ảnh Camera vào bộ nhớ tạm để lấy đường dẫn Uri
    private Uri getImageUriFromBitmap(android.graphics.Bitmap bitmap) {
        try {
            java.io.File cachePath = new java.io.File(requireContext().getCacheDir(), "images");
            cachePath.mkdirs();
            java.io.File tempFile = new java.io.File(cachePath, "temp_camera.png");
            java.io.FileOutputStream stream = new java.io.FileOutputStream(tempFile);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
            return Uri.fromFile(tempFile);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        android.util.Log.e("KIET_DEBUG", "0. ĐÃ MỞ ĐÚNG FILE CREATE POST FRAGMENT!!!!!");
        // Nhớ sử dụng file wrapper mới tạo
        return inflater.inflate(R.layout.fragment_create_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupDependencies();
        setupListeners();
        observeViewModel();

        // Tự động điền ngày hôm nay vào txtDate
        setCurrentDate();
    }

    private void initViews(View view) {
        // 1. Ánh xạ 2 khối màn hình
        viewCamera = view.findViewById(R.id.layoutCamera);
        viewPreview = view.findViewById(R.id.layoutPreview);

        // 2. Ánh xạ nút Camera
        btnUpload = view.findViewById(R.id.btnUpload);
        btnCapture = view.findViewById(R.id.btnCapture); // Cần đặt ID trong XML

        // 3. Ánh xạ nút Preview
        btnRemoveImage = view.findViewById(R.id.btnRemoveImage);
        imgPreview = view.findViewById(R.id.imgPreview);
        btnPrivate = view.findViewById(R.id.btnPrivate);
        btnPublic = view.findViewById(R.id.btnPublic);
        txtDate = view.findViewById(R.id.txtDate);
        edtCaption = view.findViewById(R.id.edtCaption);
        autoLocation = view.findViewById(R.id.autoLocation);
        btnPost = view.findViewById(R.id.btnPost);
    }

    private void setupDependencies() {
        authRepository = new AuthRepository();
        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);
        petViewModel = new ViewModelProvider(requireActivity()).get(PetViewModel.class);
        userRepository = new com.example.diary_app.repository.UserRepository();
    }

    private void setupListeners() {
        // --- CHỨC NĂNG BÊN CAMERA ---
        btnUpload.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        // --- CHỨC NĂNG BÊN PREVIEW ---
        btnRemoveImage.setOnClickListener(v -> {
            selectedImageUri = null;
            switchMode(false); // Xóa ảnh -> Quay lại màn hình Camera
        });

        // Xử lý nút Privacy (Công khai / Riêng tư)
        btnPrivate.setOnClickListener(v -> {
            currentPrivacy = "private";
            btnPrivate.setAlpha(1.0f); // Làm nổi bật nút Private
            btnPublic.setAlpha(0.5f);  // Làm mờ nút Public
        });

        btnPublic.setOnClickListener(v -> {
            currentPrivacy = "public";
            btnPublic.setAlpha(1.0f);
            btnPrivate.setAlpha(0.5f);
        });

        // Sự kiện đăng bài
        btnPost.setOnClickListener(v -> handlePostAction());
    }

    /**
     * Hàm điều hướng giữa 2 màn hình
     * @param isPreviewMode true = Bật Preview, false = Bật Camera
     */
    private void switchMode(boolean isPreviewMode) {
        if (isPreviewMode) {
            viewCamera.setVisibility(View.GONE);
            viewPreview.setVisibility(View.VISIBLE);
        } else {
            viewCamera.setVisibility(View.VISIBLE);
            viewPreview.setVisibility(View.GONE);
        }
    }

    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        txtDate.setText(currentDate);
    }

    private void handlePostAction() {
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "Vui lòng chọn hoặc chụp một bức ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        String myUid = authRepository.getCurrentUserId();
        if (myUid == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        String caption = edtCaption.getText().toString().trim();

        // 1. Vô hiệu hóa nút Đăng để tránh người dùng bấm 2 lần liên tục
        btnPost.setEnabled(false);
        btnPost.setText("Đang chuẩn bị...");

        // 2. Gọi Database lấy thông tin Tên và Avatar
        userRepository.getUserProfile(myUid)
                .addOnSuccessListener(documentSnapshot -> {
                    // Đặt giá trị mặc định phòng khi user chưa cập nhật profile
                    String myName = "Người dùng Diary";
                    String myAvatar = "";

                    if (documentSnapshot.exists()) {
                        // LƯU Ý: Ép kiểu dữ liệu trả về thành Object User để lấy chính xác các trường
                        com.example.diary_app.data.model.User currentUser =
                                documentSnapshot.toObject(com.example.diary_app.data.model.User.class);

                        if (currentUser != null) {
                            if (currentUser.getUserName() != null && !currentUser.getUserName().isEmpty()) {
                                myName = currentUser.getUserName();
                            }
                            if (currentUser.getAvatarUrl() != null) {
                                myAvatar = currentUser.getAvatarUrl();
                            }
                        }
                    }

                    // 3. Đã có đủ thông tin thật, gọi ViewModel để đẩy bài viết lên mạng
                    postViewModel.createNewPost(
                            myUid, myName, myAvatar,
                            selectedImageUri, caption, new ArrayList<>(),
                            currentMood, currentPrivacy, null
                    );
                })
                .addOnFailureListener(e -> {
                    // Nếu lỗi mạng không lấy được profile, nhả nút Đăng ra cho bấm lại
                    btnPost.setEnabled(true);
                    btnPost.setText("Post");
                    Toast.makeText(getContext(), "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void observeViewModel() {
        postViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnPost.setEnabled(!isLoading);
            btnPost.setText(isLoading ? "Posting..." : "Post");
        });

        postViewModel.getPostSuccess().observe(getViewLifecycleOwner(), event -> {
            if (event == null) return;
            // getContentIfNotHandled() đảm bảo chỉ xử lý MỘT LẦN - tránh trigger lại khi Fragment mới tạo ra
            Boolean isSuccess = event.getContentIfNotHandled();
            if (Boolean.TRUE.equals(isSuccess)) {
                Toast.makeText(getContext(), "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                
                // Tăng EXP cho Pet
                String myUid = authRepository.getCurrentUserId();
                if (myUid != null) {
                    petViewModel.addExp(myUid, PetConstants.EXP_PER_POST);
                }
                
                Navigation.findNavController(requireView()).popBackStack();
            }
        });

        postViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        });

        petViewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                String message = event.getContentIfNotHandled();
                if (message != null && !message.isEmpty()) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
