package com.example.diary_app.repository;

import android.net.Uri;

import com.example.diary_app.data.model.Post;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PostRepository {
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    public PostRepository(){
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    /**
     * 1. Đẩy ảnh lên Firebase Storage
     * Trả về UploadTask để ViewModel biết khi nào upload xong và lấy URL
     */
    public UploadTask uploadImageToStorage(byte[] imageBytes) {
        String fileName = "post_images/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference().child(fileName);

        return ref.putBytes(imageBytes);
    }

    /**
     * 2. Lấy link Download URL sau khi Upload xong
     * (Cần lấy link này để vào biến imageUrl của class Post)
     */
    public Task<Uri> getDownloadUrl(StorageReference imageRef){
        return imageRef.getDownloadUrl();
    }

    // 3. Lưu thông tin Bài viết vào Firestore
    public Task<DocumentReference> createPost(Post post){
        return db.collection("posts").add(post);
    }

    /**
     * 4. Lấy danh sách Newsfeed
     * Lấy bài viết của chính mình và của bạn bè, sắp xếp từ mới nhất đến cũ nhất.
     */
    public Task<QuerySnapshot> getNewsFeed(List<String> friendIds, String myUid){
        List<String> queryIds = new ArrayList<>(friendIds);
        queryIds.add(myUid);

        // Firestore giới hạn mảng trong lệnh whereIn tối đa 10 phần tử
        // Tạm thời lấy 10 người đầu tiên
        if(queryIds.size() > 10)
            queryIds = queryIds.subList(0, 10);

        return db.collection("posts")
                .whereIn("userId", queryIds)
                .orderBy("createAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get();

        /* LƯU Ý KHI XỬ LÝ KẾT QUẢ NÀY TRONG VIEWMODEL:
         * Vì Firestore không hỗ trợ lệnh OR phức tạp, query này sẽ lấy CẢ bài private của bạn bè.
         * Cần dùng vòng lặp FOR để lọc lại danh sách trả về:
         * Nếu (bài viết đó KHÔNG PHẢI của mình) VÀ (privacy == "private") -> Xóa khỏi danh sách hiển thị.
         */
    }

    // 5. Lấy danh sách ảnh của chính mình
    public Task<QuerySnapshot> getMyAlbum(String myUid){
        return db.collection("posts")
                .whereEqualTo("userId", myUid)
                .orderBy("createAt", Query.Direction.DESCENDING)
                .get();
    }

    // 6. Tìm kiếm bài viết theo Tag
    public Task<QuerySnapshot> searchPostByTag(String keyword){
        String searchTag = keyword.toLowerCase().trim();

        return db.collection("posts")
                .whereArrayContains("tag", searchTag)
                .orderBy("createAt", Query.Direction.DESCENDING)
                .get();
    }

    // 7. Tìm kiếm bài viết theo Vị trí (City)
    public Task<QuerySnapshot> getPostsByCity(String myUid, String city) {
        return db.collection("posts")
                .whereEqualTo("userId", myUid)
                .whereEqualTo("location.city", city)
                .orderBy("createAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get();
    }

    // 8. Thả React vào bài viết
    public Task<Void> addReaction(String postId, String myUid, String reactionType) {
        return db.collection("posts")
                .document(postId)
                .update("reactions." + myUid, reactionType);
    }

    // 9. Lấy toàn bộ bài viết trong một khoảng thời gian (vd 1 thang)
    // Hàm này cung cấp data cho: Calendar, Biểu đồ Cảm xúc, và Danh sách Tag
    public Task<QuerySnapshot> getPostByTimeRange(String myUid, Date startDate, Date endDate){
        Timestamp start = new Timestamp(startDate);
        Timestamp end = new Timestamp(endDate);

        return db.collection("posts")
                .whereEqualTo("userId", myUid)
                .whereGreaterThanOrEqualTo("createAt", start)
                .whereLessThanOrEqualTo("createAt", end)
                .orderBy("createAt", Query.Direction.DESCENDING)
                .get();

        // ViewModel DÙNG HÀM NÀY ĐỂ XỬ LÝ MÀN HÌNH THỐNG KÊ
    }

    // 10. Xóa bài viết
    public Task<Void> deletePost(String postId, String imageUrl){
        if(imageUrl != null && !imageUrl.isEmpty()){
            StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);

            return imageRef.delete().continueWithTask(task -> {
                return db.collection("posts").document(postId).delete();
            });
        }
        else {
            return db.collection("posts").document(postId).delete();
        }
    }

    // 11. Cập nhật nội dung bài viết (Sửa Caption, đổi Privacy, thêm Tag...)
    // KHÔNG SỬA ẢNH
    public Task<Void> updatePost(String postId, Map<String, Object> updates){
        return db.collection("posts").document(postId).update(updates);
    }

}
