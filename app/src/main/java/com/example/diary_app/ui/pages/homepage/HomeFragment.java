package com.example.diary_app.ui.pages.homepage;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diary_app.R;
import com.example.diary_app.data.model.Post;
import com.example.diary_app.repository.AuthRepository;
import com.example.diary_app.repository.PostRepository;
import com.example.diary_app.repository.UserRepository;
import com.example.diary_app.ui.pages.post.FeedAdapter;
import com.example.diary_app.viewmodel.PostViewModel;

import java.util.ArrayList;
import java.util.List;
import com.example.diary_app.data.model.User;
public class HomeFragment extends Fragment {

    // 1. DATA & LOGIC
    private PostViewModel postViewModel;
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
    private TextView moodHeart, moodHappy, moodShy, moodCry, moodCalm;
    private View cameraFrame;
    private RecyclerView recyclerFeed;
    private FeedAdapter feedAdapter;
    private Uri selectedImageUri = null;
    private String currentPrivacy = "public"; // Mặc định bài viết là public
    private String currentMood = "😊"; // Mặc định cảm xúc là vui vẻ
    // 4. TRÌNH CHỌN ẢNH VÀ CAMERA
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    // Hiện ảnh lên imgPreview...
                    switchMode(true); // Đổi sang chế độ Preview
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

        // Mặc định lúc mới vào là trạng thái Lướt tin & Chụp ảnh
        switchMode(false);
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
        moodHeart = view.findViewById(R.id.moodHeart);
        moodHappy = view.findViewById(R.id.moodHappy);
        moodShy = view.findViewById(R.id.moodShy);
        moodCry = view.findViewById(R.id.moodCry);
        moodCalm = view.findViewById(R.id.moodCalm);
    }

    private void setupDependencies() {
        authRepository = new AuthRepository();
        postRepository = new PostRepository();
        userRepository = new UserRepository();
        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);
    }

    private void setupListeners() {
        // Nút mở thư viện (Bên lớp Camera)
        btnUpload.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        if (btnCapture != null) {
            btnCapture.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Tính năng chụp ảnh đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }
        // Nút Hủy bài (Bên lớp Preview)
        btnRemoveImage.setOnClickListener(v -> {
            selectedImageUri = null;
            switchMode(false); // Quay về trang chủ
        });

        // Nút Đăng bài (Bên lớp Preview)
        btnPost.setOnClickListener(v -> handlePostAction());
    }

    // HÀM ĐIỀU PHỐI GIAO DIỆN QUAN TRỌNG NHẤT
    private void switchMode(boolean isPreviewing) {
        if (isPreviewing) {
            // Mở màn hình chỉnh sửa bài viết, che lấp Camera và Feed đi
            layoutPreview.setVisibility(View.VISIBLE);
            layoutCamera.setVisibility(View.GONE);
            layoutFeed.setVisibility(View.GONE);
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

        feedAdapter = new FeedAdapter(getContext(), new ArrayList<>(), new FeedAdapter.OnPostInteractionListener() {
            @Override
            public void onReactionClick(Post post, String reactionType) {
                // Đi đường tắt: Gọi trực tiếp Repository để thả cảm xúc
                String realUid = authRepository.getCurrentUserId();
                if (realUid != null) {
                    postRepository.addReaction(post.getPostId(), realUid, reactionType)
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi thả tim: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(getContext(), "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
                }
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
                            currentMood, currentPrivacy, null // Location tạm để null
                    );
                })
                .addOnFailureListener(e -> {
                    // Nếu quá trình lấy thông tin bị lỗi mạng, bật lại nút Post cho user bấm lại
                    btnPost.setEnabled(true);
                    btnPost.setText("Post");
                    Toast.makeText(getContext(), "Lỗi tải thông tin cá nhân: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
