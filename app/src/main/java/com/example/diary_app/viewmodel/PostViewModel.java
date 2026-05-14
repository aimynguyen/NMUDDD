package com.example.diary_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.diary_app.data.model.Post;
import com.example.diary_app.repository.PostRepository;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PostViewModel extends ViewModel {
    private PostRepository postRepository;

    // Các LiveData để UI lắng nghe
    private MutableLiveData<List<Post>> newsFeedList = new MutableLiveData<>();
    private MutableLiveData<List<Post>> myAlbumList = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /**
     * 1. Tải danh sách Newsfeed (Trang chủ)
     * Lấy bài của mình và bạn bè, đồng thời xử lý logic ẩn bài Private của người khác.
     */
    public void loadNewsFeed(List<String> friendIds, String myUid) {
        isLoading.setValue(true);

        postRepository.getNewsFeed(friendIds, myUid)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    isLoading.setValue(false);
                    List<Post> posts = new ArrayList<>();

                    for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                        Post post = doc.toObject(Post.class);
                        post.setPostId(doc.getId()); // Gắn ID của Document vào Object

                        // Nếu bài viết này KHÔNG phải của mình, VÀ đang để chế độ "private" -> Bỏ qua không hiển thị
                        if (!post.getUserId().equals(myUid) && "private".equals(post.getPrivacy())) {
                            continue;
                        }

                        posts.add(post);
                    }
                    newsFeedList.setValue(posts);
                });
    }

    /**
     * 2. Tải danh sách ảnh của chính mình (Màn hình Album cá nhân)
     */
    public void loadMyAlbum(String myUid) {
        isLoading.setValue(true);

        postRepository.getMyAlbum(myUid)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    isLoading.setValue(false);
                    List<Post> posts = new ArrayList<>();

                    for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                        Post post = doc.toObject(Post.class);
                        post.setPostId(doc.getId()); // Gắn ID của Document vào Object
                        posts.add(post);
                    }
                    myAlbumList.setValue(posts);
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Lỗi tải danh sách ảnh: " + e.getMessage());
                });
    }





















}
