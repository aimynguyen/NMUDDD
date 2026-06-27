package com.example.diary_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.diary_app.core.NotiType;
import com.example.diary_app.data.model.Post;
import com.example.diary_app.repository.NotificationRepository;
import com.example.diary_app.repository.PostRepository;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminReviewViewModel extends ViewModel {

    private final PostRepository repository;

    // LiveData chứa danh sách bài viết để Fragment quan sát (Observe)
    private final MutableLiveData<List<Post>> postsLiveData = new MutableLiveData<>();

    // LiveData thông báo trạng thái xóa (Thành công hay Thất bại)
    private final MutableLiveData<String> deleteStatusLiveData = new MutableLiveData<>();

    public AdminReviewViewModel() {
        repository = new PostRepository();
    }

    // Getter để Fragment lấy dữ liệu ra theo dõi
    public LiveData<List<Post>> getPostsLiveData() {
        return postsLiveData;
    }

    public LiveData<String> getDeleteStatusLiveData() {
        return deleteStatusLiveData;
    }

    /**
     * 1. Lấy toàn bộ bài viết từ Firestore về cho Admin duyệt
     */
    public void fetchAllPosts() {
        repository.getAllPostsForAdmin().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Post> list = new ArrayList<>();

                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    Post post = doc.toObject(Post.class);
                    if (post != null) {
                        // Cực kỳ quan trọng: Lấy cái Document ID trên Firestore
                        // ném vào biến postId của model để tí nữa biết đường mà xóa!
                        post.setPostId(doc.getId());
                        list.add(post);
                    }
                }
                // Bắn danh sách về cho Fragment cập nhật lên RecyclerView
                postsLiveData.setValue(list);
            } else {
                postsLiveData.setValue(null);
            }
        });
    }

    /**
     * 2. Xóa bài viết vi phạm (Xóa ảnh trên Storage trước, sau đó xóa Document trên Firestore)
     */
    public void deletePost(Post post, int position) {
        repository.deletePost(post.getPostId(), post.getImageUrl()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Bước 1: Xóa thành công trên Firebase, ta tiến hành xóa phần tử đó trong list local luôn
                List<Post> currentList = postsLiveData.getValue();
                if (currentList != null && position < currentList.size()) {
                    currentList.remove(position);
                // Cập nhật lại LiveData để RecyclerView tự động mất đi item đó mà không cần load lại cả trang
                postsLiveData.setValue(currentList);
            }

            // gửi thông báo
            if (post.getUserId() != null && !post.getUserId().isEmpty()) {
                NotificationRepository notiRepo = new NotificationRepository();
                notiRepo.sendNotification(
                        post.getUserId(),
                        "admin",
                        NotiType.DELETE_POST,
                        post.getPostId(),
                        "Bài viết của bạn đã bị quản trị viên xóa do vi phạm tiêu chuẩn cộng đồng."
                );
            }

            // Bước 2: Báo cho Fragment biết là "Xóa thành công rồi, hiện Toast đi!"
                deleteStatusLiveData.setValue("SUCCESS");
            } else {
                // Báo xóa thất bại
                deleteStatusLiveData.setValue("FAILED");
            }
        });
    }
}