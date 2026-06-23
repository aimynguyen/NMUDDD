package com.example.diary_app.viewmodel;

import android.app.Application;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.diary_app.data.model.Post;
import com.example.diary_app.repository.PostRepository;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticticsViewModel extends AndroidViewModel {
    private PostRepository repo;

    // LiveData lưu danh sách cảm xúc thô (Dùng cho Biểu đồ ở Fragment)
    private MutableLiveData<List<String>> _reactionList = new MutableLiveData<>();
    public LiveData<List<String>> getReactionList() {
        return _reactionList;
    }

    // LiveData lưu danh sách Top 3 cảm xúc (Dùng cho UI hiển thị Top Emotion)
    private MutableLiveData<List<Pair<String, Integer>>> _topEmotions = new MutableLiveData<>();
    public LiveData<List<Pair<String, Integer>>> getTopEmotions() {
        return _topEmotions;
    }

    private MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    public StaticticsViewModel(@NonNull Application application) {
        super(application);
        this.repo = new PostRepository();
    }

    /**
     * Hàm duy nhất dùng để kéo data từ Firebase về và phân phối cho toàn bộ màn hình Thống kê
     */
    public void loadData(String UId, Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            _errorMessage.setValue("Invalid date range");
            return;
        }

        repo.getPostByTimeRange(UId, startDate, endDate)
                .addOnSuccessListener(querySnapshot -> {
                    List<String> allMoods = new ArrayList<>();
                    Map<String, Integer> emotionCountMap = new HashMap<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            // Lấy cảm xúc lúc đăng bài của chính User
                            String emotion = post.getEmotion();

                            if (emotion != null && !emotion.isEmpty()) {
                                allMoods.add(emotion);
                                // Đếm số lần xuất hiện để làm Top 3
                                emotionCountMap.put(emotion, emotionCountMap.getOrDefault(emotion, 0) + 1);
                            }
                        }
                    }

                    // 1. Cập nhật danh sách thô cho Biểu đồ ở Fragment
                    _reactionList.setValue(allMoods);

                    // 2. Xử lý tính toán Top 3 cảm xúc nhiều nhất
                    List<Pair<String, Integer>> emotionList = new ArrayList<>();
                    for (Map.Entry<String, Integer> entry : emotionCountMap.entrySet()) {
                        emotionList.add(new Pair<>(entry.getKey(), entry.getValue()));
                    }

                    // Sắp xếp giảm dần theo số lượng bài viết
                    Collections.sort(emotionList, (p1, p2) -> p2.second.compareTo(p1.second));

                    // Lấy tối đa Top 3 phần tử nhiều nhất
                    List<Pair<String, Integer>> top3 = new ArrayList<>();
                    for (int i = 0; i < Math.min(3, emotionList.size()); i++) {
                        top3.add(emotionList.get(i));
                    }
                    _topEmotions.setValue(top3);

                })
                .addOnFailureListener(e -> _errorMessage.setValue(e.getMessage()));
    }
}