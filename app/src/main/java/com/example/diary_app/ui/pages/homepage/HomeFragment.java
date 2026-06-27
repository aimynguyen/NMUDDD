package com.example.diary_app.ui.pages.homepage;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diary_app.Helpers.imageHelper;
import com.example.diary_app.R;
import com.example.diary_app.core.PetConstants;
import com.example.diary_app.data.model.Post;
import com.example.diary_app.repository.AuthRepository;
import com.example.diary_app.repository.NotificationRepository;
import com.example.diary_app.repository.PostRepository;
import com.example.diary_app.repository.UserRepository;
import com.example.diary_app.ui.pages.edit.EditPostBottomSheet;
import com.example.diary_app.ui.pages.post.FeedAdapter;
import com.example.diary_app.ui.pages.statistics.MapFragment;
import com.example.diary_app.viewmodel.PetViewModel;
import com.example.diary_app.viewmodel.PostViewModel;

import java.util.ArrayList;
import java.util.List;
import com.example.diary_app.data.model.User;
public class HomeFragment extends Fragment {

    // 1. DATA & LOGIC
    private PostViewModel postViewModel;
    private PetViewModel petViewModel;
    private AuthRepository authRepository;
    private PostRepository postRepository;
    private UserRepository userRepository;

    // 2. CÁC LỚP GIAO DIỆN CHÍNH (Từ 3 thẻ include)
    private View layoutCamera, layoutPreview, layoutFeed;

    // 3. CÁC NÚT BẤM VÀ THÀNH PHẦN CON
    private ImageView btnUpload;
    private View btnCapture;
    private ImageButton btnRemoveImage;
    private ImageView imgPreview;
    private LinearLayout btnPrivate, btnPublic;
    private TextView txtDate;
    private EditText edtCaption;
    private AutoCompleteTextView autoLocation;
    private Button btnPost;
    private TextView moodAngry, moodHappy, moodNeutral, moodSad, moodCalm;
    private View cameraFrame;
    private RecyclerView recyclerFeed;
    private FeedAdapter feedAdapter;
    private Uri selectedImageUri = null;
    private String currentPrivacy = "public"; // Mặc định bài viết là public
    private String currentMood = com.example.diary_app.core.Mood.HAPPY.name(); // Mặc định là HAPPY chuẩn Enum
    private com.example.diary_app.data.model.Location selectedLocation = null;
    // THÊM BIẾN CAMERA X
    private androidx.camera.view.PreviewView viewFinder;
    private androidx.camera.core.ImageCapture imageCapture;
    // THÊM BIẾN LƯU TRẠNG THÁI CAMERA & THỜI GIAN CHẠM
    private int currentLensFacing = androidx.camera.core.CameraSelector.LENS_FACING_BACK; // Mặc định mở cam sau
    private long lastClickTime = 0; // Dùng để đo khoảng cách giữa 2 lần bấm
    // 4. TRÌNH CHỌN ẢNH VÀ CAMERA
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    if (imgPreview != null) {
                        imgPreview.setImageURI(selectedImageUri);
                    }
                    switchMode(true); // Đổi sang chế độ Preview
                }
            });
    // THÊM BỘ XIN QUYỀN CAMERA
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    setupCamera();
                } else {
                    Toast.makeText(getContext(), "Bạn cần cấp quyền Camera!", Toast.LENGTH_SHORT).show();
                }
            });
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupDependencies();
        setupListeners();
        setupFeed();

        // edit lại post thì trên feed cũng được đổi luôn
        getParentFragmentManager()
                .setFragmentResultListener(
                        "post_updated",
                        getViewLifecycleOwner(),
                        (requestKey, bundle) -> {


                            if(bundle.getBoolean("refresh")){

                                fetchFeedData();

                            }

                        });

        // ==========================================
        // PHỤC SINH DỮ LIỆU TỪ CÕI CHẾT (PROCESS DEATH)
        // ==========================================
        if (savedInstanceState != null) {
            // 1. Cứu lại đường dẫn ảnh
            String savedUriStr = savedInstanceState.getString("SAVED_IMAGE_URI");
            if (savedUriStr != null) {
                selectedImageUri = android.net.Uri.parse(savedUriStr);
                if (imgPreview != null) {
                    imgPreview.setImageURI(selectedImageUri); // Gắn lại ảnh lên màn hình
                }
                switchMode(true); // Bật chế độ Preview ngay lập tức
            } else {
                switchMode(false);
                setupCamera();
            }

            // 2. Cứu lại Mood và Privacy
            currentMood = savedInstanceState.getString("SAVED_MOOD", com.example.diary_app.core.Mood.HAPPY.name());
            currentPrivacy = savedInstanceState.getString("SAVED_PRIVACY", "public");

        } else {
            // Nếu khởi động app bình thường (không bị kill)
            switchMode(false);
            setupCamera();
        }

        requireActivity().getSupportFragmentManager().setFragmentResultListener("location_request", getViewLifecycleOwner(), (requestKey, bundle) -> {
            // Lấy dữ liệu do màn hình Map gửi về
            String addressName = bundle.getString("location_name");
            double latitude = bundle.getDouble("latitude");
            double longitude = bundle.getDouble("longitude");
            String cityName = bundle.getString("city_name");

            // 1. Hiển thị tên địa điểm lên ô TextBox cho người dùng thấy
            if (autoLocation != null) {
                autoLocation.setText(addressName);
            }

            // 2. Nạp dữ liệu vào model Location của nhóm bạn
            selectedLocation = new com.example.diary_app.data.model.Location();

            // Tạo đối tượng GeoPoint của Firebase từ Kinh độ & Vĩ độ
            com.google.firebase.firestore.GeoPoint geoPoint = new com.google.firebase.firestore.GeoPoint(latitude, longitude);
            selectedLocation.setCoordinates(geoPoint);

            // Truyền địa chỉ
            selectedLocation.setAddress(addressName);

            // Truyền thành phố (nếu có)
            if (cityName != null) {
                selectedLocation.setCity(cityName);
            }
            switchMode(true);
        });
        // ==========================================
        // 1. LẮNG NGHE TÍN HIỆU ĐĂNG BÀI THÀNH CÔNG
        // ==========================================
        postViewModel.getPostSuccess().observe(getViewLifecycleOwner(), event -> {
            if (event == null) return;
            Boolean isSuccess = event.getContentIfNotHandled();
            if (Boolean.TRUE.equals(isSuccess)) {
                // 1. Mở khóa nút bấm
                if (btnPost != null) {
                    btnPost.setEnabled(true);
                    btnPost.setText("Post");
                }

                // 2. Báo cáo thành công
                Toast.makeText(getContext(), "Đăng nhật ký thành công! 🎉", Toast.LENGTH_SHORT).show();

                //  Tăng EXP cho Pet
                String myUid = authRepository.getCurrentUserId();
                if (myUid != null) {
                    petViewModel.addExp(getContext(), myUid, PetConstants.EXP_PER_POST);
                }

                // 3. Dọn dẹp sạch sẽ form đăng bài để lần sau không bị dính chữ cũ
                selectedImageUri = null;
                selectedLocation = null;
                if (edtCaption != null) edtCaption.setText("");
                if (autoLocation != null) autoLocation.setText("");

                // 4. Trượt màn hình quay về Trang chủ
                switchMode(false);
                setupCamera();
                // 5. Kéo dữ liệu Newsfeed mới nhất về để thấy ngay bài vừa đăng
                fetchFeedData();
            }
        });

        // ==========================================
        // 2. LẮNG NGHE TÍN HIỆU BÁO LỖI (QUAN TRỌNG)
        // ==========================================
        postViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                // Mở khóa nút bấm để user có thể bấm thử lại
                if (btnPost != null) {
                    btnPost.setEnabled(true);
                    btnPost.setText("Post");
                }

                // Bật thông báo lỗi lên màn hình để biết chính xác kẹt ở đâu
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

    private void initViews(View view) {
        // Ánh xạ 3 lớp vỏ
        layoutCamera = view.findViewById(R.id.layoutCamera);
        layoutPreview = view.findViewById(R.id.layoutPreview);
        layoutFeed = view.findViewById(R.id.layoutFeed);

        // Ánh xạ các thành phần con bên trong
        recyclerFeed = view.findViewById(R.id.recyclerFeed);
        btnUpload = view.findViewById(R.id.btnUpload);
        btnCapture = view.findViewById(R.id.btnCapture);
        cameraFrame = view.findViewById(R.id.cameraFrame);
        btnRemoveImage = view.findViewById(R.id.btnRemoveImage);
        imgPreview = view.findViewById(R.id.imgPreview);
        btnPrivate = view.findViewById(R.id.btnPrivate);
        btnPublic = view.findViewById(R.id.btnPublic);
        txtDate = view.findViewById(R.id.txtDate);
        edtCaption = view.findViewById(R.id.edtCaption);
        autoLocation = view.findViewById(R.id.autoLocation);
        btnPost = view.findViewById(R.id.btnPost);
        moodAngry = view.findViewById(R.id.moodAngry);
        moodHappy = view.findViewById(R.id.moodHappy);
        moodNeutral = view.findViewById(R.id.moodNeutral);
        moodSad = view.findViewById(R.id.moodSad);
        moodCalm = view.findViewById(R.id.moodCalm);
        viewFinder = view.findViewById(R.id.viewFinder);
    }

    private void setupDependencies() {
        authRepository = new AuthRepository();
        postRepository = new PostRepository();
        userRepository = new UserRepository();
        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);
        petViewModel = new ViewModelProvider(requireActivity()).get(PetViewModel.class);
    }

    private void setupListeners() {
        // Nút mở thư viện (Bên lớp Camera)
        btnUpload.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        if (btnCapture != null) {
            btnCapture.setOnClickListener(v -> {
                takePhoto();
            });
        }
        // Nút Hủy bài (Bên lớp Preview)
        btnRemoveImage.setOnClickListener(v -> {
            selectedImageUri = null;
            switchMode(false); // Quay về trang chủ
        });

        // Nút Đăng bài (Bên lớp Preview)
        btnPost.setOnClickListener(v -> handlePostAction());
        // Chọn private/public
        if (btnPublic != null && btnPrivate != null) {
            // Mặc định lúc mới vào: Public sáng, Private mờ
            btnPublic.setAlpha(1.0f);
            btnPrivate.setAlpha(0.4f);

            btnPublic.setOnClickListener(v -> {
                currentPrivacy = "public"; // Cập nhật biến để lát nữa đăng bài
                btnPublic.setAlpha(1.0f);  // Làm nút Public sáng rõ
                btnPrivate.setAlpha(0.4f); // Làm nút Private mờ đi
            });

            btnPrivate.setOnClickListener(v -> {
                currentPrivacy = "private";
                btnPrivate.setAlpha(1.0f);
                btnPublic.setAlpha(0.4f);
            });
        }
        // Mood: Sửa lại theo chuẩn Enum
        View.OnClickListener moodClickListener = v -> {
            // Làm mờ tất cả các nút đi
            if (moodAngry != null) moodAngry.setAlpha(0.4f);
            if (moodHappy != null) moodHappy.setAlpha(0.4f);
            if (moodNeutral != null) moodNeutral.setAlpha(0.4f);
            if (moodSad != null) moodSad.setAlpha(0.4f);
            if (moodCalm != null) moodCalm.setAlpha(0.4f);

            // Chỉ làm sáng rõ cái nút vừa được bấm
            v.setAlpha(1.0f);

            // Dựa vào ID của nút để gán tên Enum chuẩn xác đẩy lên Database
            int viewId = v.getId();
            if (viewId == R.id.moodHappy) {
                currentMood = com.example.diary_app.core.Mood.HAPPY.name();
            } else if (viewId == R.id.moodSad) {
                currentMood = com.example.diary_app.core.Mood.SAD.name();
            } else if (viewId == R.id.moodCalm) {
                currentMood = com.example.diary_app.core.Mood.CALM.name();
            } else if (viewId == R.id.moodNeutral) {
                currentMood = com.example.diary_app.core.Mood.NEUTRAL.name();
            } else if (viewId == R.id.moodAngry) {
                currentMood = com.example.diary_app.core.Mood.ANGRY.name();
            }
        };
        // Gắn sự kiện
        if (moodAngry != null) moodAngry.setOnClickListener(moodClickListener);
        if (moodHappy != null) moodHappy.setOnClickListener(moodClickListener);
        if (moodNeutral != null) moodNeutral.setOnClickListener(moodClickListener);
        if (moodSad != null) moodSad.setOnClickListener(moodClickListener);
        if (moodCalm != null) moodCalm.setOnClickListener(moodClickListener);

        // Bấm sẵn nút Happy lúc mới vào
        if (moodHappy != null) moodHappy.performClick();

        if (autoLocation != null) {
            autoLocation.setOnClickListener(v -> {
                // 1. Khởi tạo màn hình bản đồ
                androidx.fragment.app.Fragment mapFragment = new MapFragment();

                // 2. Phủ MapFragment lên thẳng lớp gốc của màn hình (android.R.id.content)
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .add(android.R.id.content, mapFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }
        // ==========================================
        // TÍNH NĂNG CHẠM 2 LẦN LẬT CAMERA
        // ==========================================
        if (viewFinder != null) {
            viewFinder.setOnClickListener(v -> {
                long clickTime = System.currentTimeMillis();
                // Nếu khoảng cách giữa 2 lần chạm nhỏ hơn 400 mili-giây -> Double Click!
                if (clickTime - lastClickTime < 400) {
                    flipCamera(); // Gọi hàm lật ống kính
                    lastClickTime = 0; // Reset lại bộ đếm
                } else {
                    lastClickTime = clickTime; // Lưu lại thời gian chạm lần 1
                }
            });
        }
    }

    // HÀM ĐIỀU PHỐI GIAO DIỆN QUAN TRỌNG NHẤT
    private void switchMode(boolean isPreviewing) {
        if (isPreviewing) {
            // 1. Mở màn hình chỉnh sửa bài viết, che lấp Camera và Feed đi
            layoutPreview.setVisibility(View.VISIBLE);
            layoutCamera.setVisibility(View.GONE);
            layoutFeed.setVisibility(View.GONE);

            // ==========================================
            // 2. CẬP NHẬT NGÀY THÁNG HIỆN TẠI VÀO TEXTVIEW
            // ==========================================
            if (txtDate != null) {
                // Định dạng ngày theo chuẩn Việt Nam
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                String currentDate = sdf.format(new java.util.Date()); // Lấy thời gian thực của hệ thống
                txtDate.setText(currentDate);
            }

        } else {
            // Trở về trang chủ
            layoutPreview.setVisibility(View.GONE);
            layoutCamera.setVisibility(View.VISIBLE);
            layoutFeed.setVisibility(View.VISIBLE);

            // Tự động cuộn ngược mượt mà lên đầu trang camera khi hủy ảnh
            View scrollHome = getView().findViewById(R.id.scrollHome);
            if (scrollHome != null) {
                scrollHome.scrollTo(0, 0);
            }
        }
    }

    private void setupFeed() {
        // 1. Cài đặt giao diện danh sách (RecyclerView)
        recyclerFeed.setLayoutManager(new LinearLayoutManager(getContext()));

        com.example.diary_app.repository.PostRepository postRepository = new com.example.diary_app.repository.PostRepository();

        feedAdapter = new FeedAdapter(getContext(), new ArrayList<>(), new FeedAdapter.OnPostInteractionListener() {
            @Override
            public void onReactionClick(Post post, String reactionType) {
                // 1. Lấy ID của chính mình
                String myUid = authRepository.getCurrentUserId();
                if (myUid == null) {
                    Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID người dùng!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // ==========================================
                // CHỐT CHẶN: KHÔNG CHO TỰ THẢ CẢM XÚC BÀI CỦA MÌNH
                // ==========================================
                if (myUid.equals(post.getUserId())) {
                    Toast.makeText(getContext(), "Bạn không thể thả cảm xúc cho bài viết của chính mình!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 2. Gọi thẳng xuống hàm addReaction của Repository
                postRepository.addReaction(post.getPostId(), myUid, reactionType)
                        .addOnSuccessListener(aVoid -> {
                            String key = (reactionType != null) ? reactionType.trim() : "";
                            String iconToast = "😐";
                            if (key.equalsIgnoreCase("HAPPY")) {
                                iconToast = "😊";
                            } else if (key.equalsIgnoreCase("SAD")) {
                                iconToast = "😭";
                            } else if (key.equalsIgnoreCase("CALM")) {
                                iconToast = "😌";
                            } else if (key.equalsIgnoreCase("ANGRY")) {
                                iconToast = "😡";
                            } else if (key.equalsIgnoreCase("NEUTRAL")) {
                                iconToast = "😳";
                            } else {
                                // Nếu vẫn lọt vào đây thì nghĩa là giá trị cực kỳ lạ, in ra để kiểm tra
                                android.util.Log.e("DEBUG_LOG", "Giá trị lạ không khớp: '" + key + "'");
                            }

                            // Hiện thông báo thả thành công cho vui mắt
                            android.widget.Toast.makeText(getContext(), "Đã thả " + iconToast, android.widget.Toast.LENGTH_SHORT).show();

                            // Tăng EXP cho Pet khi thả reaction
                            petViewModel.addExp(getContext(), myUid, PetConstants.EXP_PER_REACT);

                            // Gửi thông báo cho chủ bài viết
                            if (!myUid.equals(post.getUserId())) {
                                userRepository.getUserProfile(myUid).addOnSuccessListener(documentSnapshot -> {
                                    String myName = "Ai đó";
                                    if (documentSnapshot.exists()) {
                                        User currentUser = documentSnapshot.toObject(User.class);
                                        if (currentUser != null && currentUser.getUserName() != null) {
                                            myName = currentUser.getUserName();
                                        }
                                    }
                                    NotificationRepository notiRepo = new NotificationRepository();
                                    notiRepo.sendNotification(
                                            post.getUserId(),
                                            myUid,
                                            com.example.diary_app.core.NotiType.REACT_POST,
                                            post.getPostId(),
                                            myName + " đã bày tỏ cảm xúc về bài viết của bạn"
                                    );
                                });
                            }

                            // để app tự động tải lại bảng tin nhằm cập nhật giao diện mặt cười ngay lập tức
                            fetchFeedData();
                        });
            }

            @Override
            public void onPostLongClick(Post post, View anchor) {
                showPopup(post, anchor);
            }

            @Override
            public void onMyPostDoubleTap(Post post) {

                String myUid = authRepository.getCurrentUserId();
                if(myUid==null) return;

                // chỉ được sửa post của bản thân
                if (!post.getUserId().equals(myUid)) {
                    Toast.makeText(
                            getContext(),
                            "Bạn chỉ có thể sửa bài của mình",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                EditPostBottomSheet sheet =
                        EditPostBottomSheet.newInstance(post.getPostId());

                sheet.show(
                        getParentFragmentManager(),
                        "edit_post"
                );
            }
        });
        recyclerFeed.setAdapter(feedAdapter);

        // 2. Lắng nghe dữ liệu đổ về từ ViewModel
        postViewModel.getNewsFeedList().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                feedAdapter.setPosts(posts);
            }
        });

        // 3. Kích hoạt kéo dữ liệu từ Firebase
        fetchFeedData();
    }


    private void fetchFeedData() {
        String myUid = authRepository.getCurrentUserId();
        if (myUid != null) {
            // Lấy danh sách bạn bè trước
            userRepository.getUserProfile(myUid).addOnSuccessListener(documentSnapshot -> {
                List<String> myFriendIds = new ArrayList<>();

                if (documentSnapshot.exists() && documentSnapshot.contains("friendIds")) {
                    myFriendIds = (List<String>) documentSnapshot.get("friendIds");
                }

                if (myFriendIds == null) {
                    myFriendIds = new ArrayList<>();
                }

                // Nạp Bảng tin với danh sách bạn bè thật
                postViewModel.loadNewsFeed(myFriendIds, myUid);
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu bạn bè: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                // Nếu lỗi, vẫn cho phép tải bảng tin rỗng (hoặc chỉ bài của chính mình)
                postViewModel.loadNewsFeed(new ArrayList<>(), myUid);
            });
        }
    }

    private void handlePostAction() {
        // 1. Kiểm tra điều kiện bắt buộc
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "Vui lòng chọn hoặc chụp một bức ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        String myUid = authRepository.getCurrentUserId();
        if (myUid == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Thu thập dữ liệu text từ ô nhập liệu
        String caption = edtCaption.getText().toString().trim();

        // 3. Vô hiệu hóa nút Đăng để tránh người dùng bấm spam (double-click)
        btnPost.setEnabled(false);
        btnPost.setText("Đang chuẩn bị...");

        // 4. Lấy thông tin thật từ UserRepository
        userRepository.getUserProfile(myUid)
                .addOnSuccessListener(documentSnapshot -> {
                    String myName = "Người dùng Diary"; // Giá trị mặc định
                    String myAvatar = "";

                    if (documentSnapshot.exists()) {
                        // Ép kiểu toàn bộ dữ liệu trả về thành đối tượng User của nhóm bạn
                        User currentUser = documentSnapshot.toObject(User.class);

                        if (currentUser != null) {
                            // Gọi chính xác hàm Getter từ file User.java bạn vừa gửi
                            if (currentUser.getUserName() != null && !currentUser.getUserName().isEmpty()) {
                                myName = currentUser.getUserName();
                            }

                            if (currentUser.getAvatarUrl() != null) {
                                myAvatar = currentUser.getAvatarUrl();
                            }
                        }
                    }

                    // 5. Ném toàn bộ thông tin cho ViewModel xử lý up ảnh và lưu bài viết
                    postViewModel.createNewPost(
                            myUid, myName, myAvatar,
                            selectedImageUri, caption,
                            new ArrayList<>(), // Không có thẻ tag thì truyền mảng rỗng
                            currentMood, currentPrivacy, selectedLocation
                    );
                })
                .addOnFailureListener(e -> {
                    // Nếu quá trình lấy thông tin bị lỗi mạng, bật lại nút Post cho user bấm lại
                    btnPost.setEnabled(true);
                    btnPost.setText("Post");
                    Toast.makeText(getContext(), "Lỗi tải thông tin cá nhân: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // pop up xóa post khi longclick
    private void showPopup(Post post, View anchor) {

        String myUid = authRepository.getCurrentUserId();

        if (myUid == null || !post.getUserId().equals(myUid)) {
            return;
        }

        PopupMenu popup = new PopupMenu(requireContext(), anchor);

        popup.getMenu().add("Xóa bài viết");

        popup.setOnMenuItemClickListener(item -> {

            new AlertDialog.Builder(requireContext())
                    .setTitle("Xóa bài viết")
                    .setMessage("Bạn có chắc muốn xóa bài viết?")
                    .setPositiveButton("Xóa", (dialog, which) -> {

                        postViewModel.deletePost(post);

                    })
                    .setNegativeButton("Hủy", null)
                    .show();

            return true;
        });

        popup.show();
    }

    // bottomsheetdialog xuất hiện khi double tap post của bản thân


    // =======================================================
    // BỘ HÀM XỬ LÝ CAMERA LOCKET-STYLE
    // =======================================================

    // Hàm 1: Kiểm tra quyền
    private void setupCamera() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        }
    }

    // Hàm 2: Mở ống kính (Đã nâng cấp để nhận diện cam trước/sau)
    private void startCamera() {
        com.google.common.util.concurrent.ListenableFuture<androidx.camera.lifecycle.ProcessCameraProvider> cameraProviderFuture =
                androidx.camera.lifecycle.ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                androidx.camera.lifecycle.ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                androidx.camera.core.Preview preview = new androidx.camera.core.Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                imageCapture = new androidx.camera.core.ImageCapture.Builder().build();

                // THAY ĐỔI Ở ĐÂY: Sử dụng biến currentLensFacing thay vì fix cứng Cam sau
                androidx.camera.core.CameraSelector cameraSelector = new androidx.camera.core.CameraSelector.Builder()
                        .requireLensFacing(currentLensFacing)
                        .build();

                // Gỡ các ống kính đang chạy ngầm và gắn ống kính mới vào
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageCapture);

            } catch (Exception exc) {
                android.util.Log.e("CameraX", "Lỗi khởi động camera", exc);
                android.widget.Toast.makeText(getContext(), "Lỗi lật Camera: " + exc.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        }, androidx.core.content.ContextCompat.getMainExecutor(requireContext()));
    }

    // Hàm bổ sung: Lật Camera
    private void flipCamera() {
        // Nếu đang là cam sau -> đổi thành cam trước, và ngược lại
        if (currentLensFacing == androidx.camera.core.CameraSelector.LENS_FACING_BACK) {
            currentLensFacing = androidx.camera.core.CameraSelector.LENS_FACING_FRONT;
        } else {
            currentLensFacing = androidx.camera.core.CameraSelector.LENS_FACING_BACK;
        }

        // Gọi lại lệnh mở ống kính để áp dụng thay đổi ngay lập tức
        startCamera();
    }
    // Hàm 3: Bấm chụp và xử lý ảnh
    private void takePhoto() {
        if (imageCapture == null) return;

        // Tạo một file ảnh tạm thời trong máy để lưu bức hình vừa chụp
        java.io.File photoFile = new java.io.File(requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES),
                "Diary_" + System.currentTimeMillis() + ".jpg");

        androidx.camera.core.ImageCapture.OutputFileOptions outputOptions =
                new androidx.camera.core.ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Ra lệnh chụp
        imageCapture.takePicture(outputOptions, androidx.core.content.ContextCompat.getMainExecutor(requireContext()),
                new androidx.camera.core.ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull androidx.camera.core.ImageCapture.OutputFileResults outputFileResults) {

                        // THÀNH CÔNG: Lấy Uri của ảnh vừa chụp
                        android.net.Uri savedUri = android.net.Uri.fromFile(photoFile);

                        // 1. Gán vào biến selectedImageUri (Biến cũ của bạn dùng để post bài)
                        selectedImageUri = savedUri;

                        // 2. Gán ảnh vừa chụp lên cái ImageView của màn hình Preview
                        imgPreview.setImageURI(savedUri);

                        // 3. Đóng màn hình Camera, trượt mở màn hình Preview lên (Gọi hàm cũ của bạn)
                        switchMode(true);

                        android.widget.Toast.makeText(getContext(), "Chụp thành công!", android.widget.Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull androidx.camera.core.ImageCaptureException exception) {
                        android.util.Log.e("CameraX", "Chụp ảnh thất bại: " + exception.getMessage(), exception);
                        android.widget.Toast.makeText(getContext(), "Lỗi lưu ảnh", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
    }
    // ==========================================
    // BÍ KÍP KHÁNG TỬ: LƯU ĐƯỜNG DẪN ẢNH TRƯỚC KHI BỊ KILL
    // ==========================================
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Nếu đang có ảnh, lưu đường dẫn của nó lại thành chuỗi (String)
        if (selectedImageUri != null) {
            outState.putString("SAVED_IMAGE_URI", selectedImageUri.toString());
        }

        // Tiện tay lưu luôn trạng thái cảm xúc và quyền riêng tư đang chọn
        outState.putString("SAVED_MOOD", currentMood);
        outState.putString("SAVED_PRIVACY", currentPrivacy);
    }
}
