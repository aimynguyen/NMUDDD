package com.example.diary_app.ui.pages.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diary_app.R;
import com.example.diary_app.adapter.SearchAdapter;
import com.example.diary_app.data.model.Post;
import com.example.diary_app.model.UserModel;
import com.example.diary_app.viewmodel.FriendViewModel;
import com.example.diary_app.viewmodel.SearchViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private SearchViewModel viewModel;
    private FriendViewModel friendViewModel;
    private SearchAdapter adapter;

    private EditText edtSearch;
    private ImageButton iconClear;
    private ImageButton iconAddFriend;
    private RecyclerView rvPosts;
    private Spinner spinnerFilter;

    private static final String FILTER_ALL = "__ALL__";
    private String selectedUserId = "";
    private List<Post> currentPosts = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ các UI
        edtSearch = view.findViewById(R.id.edtSearch);
        iconClear = view.findViewById(R.id.iconClear);
        iconAddFriend = view.findViewById(R.id.iconAddFriend);
        rvPosts = view.findViewById(R.id.rvPosts);
        spinnerFilter = view.findViewById(R.id.spinnerFilter);

        // Khởi tạo Adapter
        adapter = new SearchAdapter();
        rvPosts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvPosts.setAdapter(adapter);

        // Khởi tạo ViewModels
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class);

        // Setup Friend Filter Spinner
        setupFriendFilter();

        // Theo dõi LiveData danh sách bài viết từ Firebase đổ về
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), posts -> {
            currentPosts = posts;
            filterPosts();
        });

        // Bắt sự kiện gõ chữ trên Search Bar
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();

                if (query.length() > 0) {
                    iconClear.setVisibility(View.VISIBLE);
                    viewModel.setSearchQuery(query);
                } else {
                    iconClear.setVisibility(View.GONE);
                    // Query is empty, load all posts for the selected user
                    loadAllPostsForUser(selectedUserId);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        iconClear.setOnClickListener(v -> edtSearch.setText(""));

        iconAddFriend.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_addfriend);
        });
    }

    private void setupFriendFilter() {
        friendViewModel.loadFriends();
        
        List<String> spinnerNames = new ArrayList<>();
        List<String> spinnerIds = new ArrayList<>();

        String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        friendViewModel.getFriendList().observe(getViewLifecycleOwner(), friends -> {
            spinnerNames.clear();
            spinnerIds.clear();

            spinnerNames.add("Tất cả"); // Show posts from everyone
            spinnerIds.add(FILTER_ALL);

            spinnerNames.add("Bài viết của tôi"); // My posts only
            spinnerIds.add(currentUid);

            for (UserModel friend : friends) {
                spinnerNames.add(friend.getUserName() != null ? friend.getUserName() : "Unknown");
                spinnerIds.add(friend.getUid());
            }

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, spinnerNames);
            spinnerFilter.setAdapter(spinnerAdapter);
        });

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < spinnerIds.size()) {
                    selectedUserId = spinnerIds.get(position);
                    
                    // If search query is empty, load all posts of the selected user directly
                    if (edtSearch.getText().toString().trim().isEmpty()) {
                        loadAllPostsForUser(selectedUserId);
                    } else {
                        // Otherwise, re-filter the current search results
                        filterPosts();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadAllPostsForUser(String userId) {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

        if (FILTER_ALL.equals(userId)) {
            // Load tất cả bài viết (không lọc theo userId)
            db.collection("posts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> list = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                list.add(post);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    currentPosts = list;
                    adapter.setData(currentPosts);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    adapter.setData(new ArrayList<>());
                });
        } else {
            if (userId == null || userId.isEmpty()) return;

            // Load bài viết của một user cụ thể
            db.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> list = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                list.add(post);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    currentPosts = list;
                    adapter.setData(currentPosts);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    adapter.setData(new ArrayList<>());
                });
        }
    }

    private void filterPosts() {
        // Nếu chọn "Tất cả" hoặc không có filter, hiển thị tất cả
        if (FILTER_ALL.equals(selectedUserId) || selectedUserId == null || selectedUserId.isEmpty()) {
            adapter.setData(currentPosts);
            return;
        }

        List<Post> filtered = new ArrayList<>();
        for (Post p : currentPosts) {
            if (p.getUserId() != null && p.getUserId().equals(selectedUserId)) {
                filtered.add(p);
            }
        }
        adapter.setData(filtered);
    }
}