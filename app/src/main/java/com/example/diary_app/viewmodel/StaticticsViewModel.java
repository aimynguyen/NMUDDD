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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticticsViewModel extends AndroidViewModel {
    private PostRepository repo;

    // Dùng để chống race condition: chỉ xử lý kết quả của request mới nhất
    private int currentRequestId = 0;

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

    // LiveData lưu cảm xúc trung bình theo ngày (Dùng cho Calendar)
    private MutableLiveData<Map<Integer, String>> _dailyEmotions = new MutableLiveData<>();
    public LiveData<Map<Integer, String>> getDailyEmotions() {
        return _dailyEmotions;
    }

    // LiveData lưu danh sách URL ảnh của các bài viết trong tháng (Dùng cho VideoFragment)
    private MutableLiveData<List<String>> _postImageUrls = new MutableLiveData<>();
    public LiveData<List<String>> getPostImageUrls() {
        return _postImageUrls;
    }

    // LiveData lưu danh sách các bài viết có vị trí và hình ảnh (Dùng cho PhotoMapFragment)
    private MutableLiveData<List<Post>> _postsWithLocation = new MutableLiveData<>();
    public LiveData<List<Post>> getPostsWithLocation() {
        return _postsWithLocation;
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

        // Tăng requestId mỗi lần load để chống race condition
        final int thisRequestId = ++currentRequestId;

        repo.getPostByTimeRange(UId, startDate, endDate)
                .addOnSuccessListener(querySnapshot -> {
                    // Bỏ qua kết quả nếu đây không phải request mới nhất (race condition)
                    if (thisRequestId != currentRequestId) return;

                    List<String> allMoods = new ArrayList<>();
                    List<String> imageUrls = new ArrayList<>();
                    List<Post> postsWithLoc = new ArrayList<>();
                    Map<String, Integer> emotionCountMap = new HashMap<>();
                    Map<Integer, List<String>> dailyEmotionsRaw = new HashMap<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            // Lấy cảm xúc lúc đăng bài của chính User
                            String emotion = post.getEmotion();

                            // Thu thập URL ảnh (chỉ lấy bài có ảnh)
                            String imgUrl = post.getImageUrl();
                            if (imgUrl != null && !imgUrl.isEmpty()) {
                                imageUrls.add(imgUrl);
                            }

                            if (post.getLocation() != null && post.getLocation().getCoordinates() != null && imgUrl != null && !imgUrl.isEmpty()) {
                                postsWithLoc.add(post);
                            }

                            if (emotion != null && !emotion.isEmpty()) {
                                allMoods.add(emotion);
                                // Đếm số lần xuất hiện để làm Top 3
                                emotionCountMap.put(emotion, emotionCountMap.getOrDefault(emotion, 0) + 1);

                                // Tính ngày để hiển thị lên lịch
                                if (post.getCreateAt() != null) {
                                    Date date = post.getCreateAt().toDate();
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(date);
                                    int day = cal.get(Calendar.DAY_OF_MONTH);

                                    if (!dailyEmotionsRaw.containsKey(day)) {
                                        dailyEmotionsRaw.put(day, new ArrayList<>());
                                    }
                                    dailyEmotionsRaw.get(day).add(emotion);
                                }
                            }
                        }
                    }

                    // 1. Cập nhật danh sách thô cho Biểu đồ ở Fragment
                    _reactionList.setValue(allMoods);

                    // 1b. Cập nhật danh sách URL ảnh cho VideoFragment
                    _postImageUrls.setValue(imageUrls);

                    _postsWithLocation.setValue(postsWithLoc);

                    // Tính cảm xúc chủ đạo mỗi ngày
                    Map<Integer, String> dailyEmotions = new HashMap<>();
                    for (Map.Entry<Integer, List<String>> entry : dailyEmotionsRaw.entrySet()) {
                        dailyEmotions.put(entry.getKey(), calculateDominantEmotion(entry.getValue()));
                    }
                    _dailyEmotions.setValue(dailyEmotions);

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
                .addOnFailureListener(e -> {
                    if (thisRequestId == currentRequestId) {
                        _errorMessage.setValue(e.getMessage());
                    }
                });
    }

    private String calculateDominantEmotion(List<String> emotions) {
        if (emotions == null || emotions.isEmpty()) return "neutral";
        
        Map<String, Integer> counts = new HashMap<>();
        for (String emotion : emotions) {
            String e = emotion.toLowerCase();
            String mapped = "neutral";
            if (e.contains("happy") || e.contains("😁") || e.contains("😀")) mapped = "happy";
            else if (e.contains("calm") || e.contains("😌") || e.contains("😊")) mapped = "calm";
            else if (e.contains("neutral") || e.contains("😳") || e.contains("😐")) mapped = "neutral";
            else if (e.contains("sad") || e.contains("😭") || e.contains("😢")) mapped = "sad";
            else if (e.contains("angry") || e.contains("😡") || e.contains("❤️")) mapped = "angry";
            
            counts.put(mapped, counts.getOrDefault(mapped, 0) + 1);
        }
        
        String dominant = "neutral";
        int max = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                dominant = entry.getKey();
            }
        }
        
        return dominant;
    }
}