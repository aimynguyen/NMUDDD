package com.example.diary_app.viewmodel;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.diary_app.Helpers.imageHelper;
import com.example.diary_app.data.model.Location;
import com.example.diary_app.data.model.Post;
import com.example.diary_app.repository.PostRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostViewModel extends ViewModel {
    private PostRepository postRepository;

    // Các LiveData để UI lắng nghe
    private MutableLiveData<List<Post>> newsFeedList = new MutableLiveData<>();
    private MutableLiveData<List<Post>> myAlbumList = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    // các LiveData để theo dõi trạng thái Đăng bài
    private MutableLiveData<Boolean> postSuccess = new MutableLiveData<>();
    private MutableLiveData<Integer> uploadProgress = new MutableLiveData<>(); // Để FE làm thanh tiến trình (Progress Bar)

    public LiveData<Boolean> getPostSuccess() { return postSuccess; }
    public LiveData<Integer> getUploadProgress() { return uploadProgress; }
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

    /**
     * 3. Chức năng ĐĂNG BÀI MỚI
     */
    public void createNewPost(Context context, String userId, String userName, String userAvatar,
                              Uri imageUri, String caption, List<String> tags,
                              String emotion, String privacy, Location location) {
        isLoading.setValue(true);
        postSuccess.setValue(false);
        uploadProgress.setValue(0);

        // --- XỬ LÝ ẢNH TRƯỚC KHI UPLOAD ---
        try {
            // Biến Uri thành Bitmap
            Bitmap originalBitmap = imageHelper.uriToBitmap(context, imageUri);
            if (originalBitmap == null)
                throw new Exception("Không thể đọc ảnh");

            // Resize ảnh (Giới hạn tối đa HD 1080x1080 để chống vỡ khung hình)
            Bitmap resizedBitmap = imageHelper.resizeBitmap(originalBitmap, 1080, 1080);

            // Nén ảnh với chất lượng 70%
            byte[] compressedImageData = imageHelper.compressBitmap(resizedBitmap, 70);

            //1: Đẩy ảnh lên storage
            postRepository.uploadImageToStorage(compressedImageData)
                    .addOnProgressListener(taskSnapshot -> {
                        // Tính toán % để báo cho FE cập nhật thanh Progress Bar
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        uploadProgress.setValue((int) progress);
                    })
                    .addOnSuccessListener(taskSnapshot -> {
                        // 2: Upload ảnh xong, lấy link Download URL
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();

                            // 3: Có link ảnh rồi, đóng gói Object Post và đẩy lên Firestore
                            Post newPost = new Post();
                            newPost.setUserId(userId);
                            newPost.setUserName(userName);
                            newPost.setUserAvatar(userAvatar);
                            newPost.setImageUrl(downloadUrl);
                            newPost.setCaption(caption);
                            newPost.setTags(tags);
                            newPost.setEmotion(emotion);
                            newPost.setPrivacy(privacy);
                            newPost.setLocation(location);
                            newPost.setCreateAt(Timestamp.now());
                            newPost.setReactions(new HashMap<>());

                            postRepository.createPost(newPost)
                                    .addOnSuccessListener(documentReference -> {
                                        isLoading.setValue(false);
                                        postSuccess.setValue(true); // Báo FE đóng màn hình đăng bài, chuyển về Feed
                                    })
                                    .addOnFailureListener(e -> {
                                        isLoading.setValue(false);
                                        errorMessage.setValue("Lỗi lưu bài viết: " + e.getMessage());
                                    });
                        }).addOnFailureListener(e -> {
                            isLoading.setValue(false);
                            errorMessage.setValue("Lỗi lấy link ảnh: " + e.getMessage());
                        });
                    }).addOnFailureListener(e -> {
                        isLoading.setValue(false);
                        errorMessage.setValue("Lỗi tải ảnh lên server: " + e.getMessage());
                    });
        } catch(Exception e){
            isLoading.setValue(false);
            errorMessage.setValue("Lỗi xử lý ảnh: " + e.getMessage());
        }
    }



















}
