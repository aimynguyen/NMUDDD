package com.example.diary_app.viewmodel;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.diary_app.data.model.Post;
import com.example.diary_app.repository.PostRepository;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchViewModel extends ViewModel {
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final LiveData<List<Post>> searchResults;
    private final PostRepository postRepository;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    public SearchViewModel() {
        postRepository = new PostRepository();

        searchResults = Transformations.switchMap(searchQuery, query -> {
            MutableLiveData<List<Post>> liveDataResults = new MutableLiveData<>();
            if (query == null || query.trim().isEmpty()) {
                isLoading.setValue(false);
                liveDataResults.setValue(new ArrayList<>());
                return liveDataResults;
            }
            return performSearchFromDB(query);
        });
    }

    public LiveData<List<Post>> getSearchResults() { return searchResults; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void setSearchQuery(String query) {
        if (searchRunnable != null) {
            handler.removeCallbacks(searchRunnable);
        }

        searchRunnable = () -> {
            if (!query.equals(searchQuery.getValue())) {
                isLoading.setValue(true); // Kích hoạt hiệu ứng quay vòng tròn chờ dữ liệu
                searchQuery.setValue(query.trim());
            }
        };

        // Delay 400ms để người dùng gõ xong chữ mới gọi Firebase (Tiết kiệm băng thông)
        handler.postDelayed(searchRunnable, 400);
    }

    private LiveData<List<Post>> performSearchFromDB(String keyword) {
        MutableLiveData<List<Post>> result = new MutableLiveData<>();

        // Sử dụng hàm searchPostByTag có sẵn trong PostRepository của bạn
        postRepository.searchPostByTag(keyword).addOnCompleteListener(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful() && task.getResult() != null) {
                List<Post> posts = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    Post post = doc.toObject(Post.class);
                    if (post != null) {
                        post.setPostId(doc.getId());
                        posts.add(post);
                    }
                }
                result.setValue(posts);
            } else {
                result.setValue(new ArrayList<>());
            }
        });

        return result;
    }
}