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
import com.google.firebase.firestore.FirebaseFirestore;

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
            if (query == null || query.trim().isEmpty()) {
                isLoading.setValue(false);
                MutableLiveData<List<Post>> liveDataResults = new MutableLiveData<>();
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
                isLoading.setValue(true);
                searchQuery.setValue(query.trim());
            }
        };

        // Delay 400ms to debounce search requests
        handler.postDelayed(searchRunnable, 400);
    }

    // Logic to search for Posts in Firestore
    private LiveData<List<Post>> performSearchFromDB(String keyword) {
        MutableLiveData<List<Post>> result = new MutableLiveData<>();
        isLoading.setValue(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Searching by caption in the "posts" collection to match the Post model
        db.collection("posts")
                .orderBy("caption")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> list = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
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

                    result.setValue(list);
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    result.setValue(new ArrayList<>());
                    isLoading.setValue(false);
                });
        return result;
    }
}
