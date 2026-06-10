package com.example.diary_app.ui.pages.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diary_app.data.model.Post;
import com.example.diary_app.R;
import com.example.diary_app.viewmodel.PostViewModel;

import java.util.ArrayList;
import java.util.List;

import com.example.diary_app.repository.AuthRepository;
import com.example.diary_app.repository.PostRepository;
import com.example.diary_app.repository.UserRepository;
public class FeedFragment extends Fragment {
    private PostViewModel postViewModel;
    private RecyclerView recyclerFeed;
    private FeedAdapter feedAdapter;

    private PostRepository postRepository;
    private AuthRepository authRepository;
    private UserRepository userRepository;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRepositories();
        setupRecyclerView();
        setupViewModel();

        fetchData();
    }

    private void initViews(View view) {
        recyclerFeed = view.findViewById(R.id.recyclerFeed);
        view.findViewById(R.id.layoutFeed).setVisibility(View.VISIBLE);
    }

    private void setupRepositories() {
        postRepository = new PostRepository();
        authRepository = new AuthRepository();
        userRepository = new UserRepository();
    }

    private void setupRecyclerView() {
        recyclerFeed.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo Adapter và chỉ lắng nghe duy nhất hành động Thả cảm xúc
        feedAdapter = new FeedAdapter(getContext(), new ArrayList<>(), new FeedAdapter.OnPostInteractionListener() {
            @Override
            public void onReactionClick(Post post, String reactionType) {
                // 1. Lấy UID thật từ AuthRepository
                String realUid = authRepository.getCurrentUserId();

                // 2. Nếu đã đăng nhập thành công thì tiến hành update Firestore
                if (realUid != null) {
                    postRepository.addReaction(post.getPostId(), realUid, reactionType)
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Lỗi thả cảm xúc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(getContext(), "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerFeed.setAdapter(feedAdapter);
    }

    private void setupViewModel() {
        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);

        postViewModel.getNewsFeedList().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null && !posts.isEmpty()) {
                feedAdapter.setPosts(posts);
            }
        });

        postViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchData() {
        // 1. Lấy UID của chính mình
        String myUid = authRepository.getCurrentUserId();

        if (myUid != null) {
            // 2. Gọi UserRepository để lấy thông tin Document của mình trên Firestore
            userRepository.getUserProfile(myUid).addOnSuccessListener(documentSnapshot -> {
                        List<String> myFriends = new ArrayList<>();

                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.contains("friendIds")) {
                                myFriends = (List<String>) documentSnapshot.get("friendIds");
                            }
                        }

                        // Phòng trường hợp danh sách bạn bè bị null thì khởi tạo mảng rỗng
                        if (myFriends == null) {
                            myFriends = new ArrayList<>();
                        }

                        // 3. Đã có danh sách bạn bè thật, tiến hành nạp Bảng tin (News Feed)
                        postViewModel.loadNewsFeed(myFriends, myUid);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Lỗi tải danh sách bạn bè: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                        // Nếu lỗi không lấy được bạn bè, vẫn gọi loadNewsFeed với mảng rỗng
                        // để người dùng ít nhất vẫn xem được các bài viết của CHÍNH HỌ.
                        postViewModel.loadNewsFeed(new ArrayList<>(), myUid);
                    });
        } else {
            Toast.makeText(getContext(), "Không tìm thấy thông tin tài khoản!", Toast.LENGTH_SHORT).show();
        }
    }
}
