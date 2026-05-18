package com.example.diary_app.ui.pages.statistics;

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
import java.util.Date;
import java.util.List;
import java.util.Map;

public class StaticticsViewModel extends AndroidViewModel{
    private PostRepository repo = new PostRepository();
    private MutableLiveData<List<String>> _reactionList = new MutableLiveData<>();
    public LiveData<List<String>> getReactionList(){
        return _reactionList;
    }

    private MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage(){
        return _errorMessage;
    }

    public StaticticsViewModel(@NonNull Application application){
        super(application);
        this.repo = new PostRepository();
    }

    public void getReactionList(String UId, Date startDate, Date endDate){
        if (startDate == null || endDate == null){
            String tempE="Invalid date range";
            _errorMessage.setValue(tempE);
            return;
        }

        repo.getPostByTimeRange(UId, startDate, endDate)
                .addOnSuccessListener(querySnapshot -> {
                    List<String> allMoods=new ArrayList<>();

                    for(DocumentSnapshot doc: querySnapshot.getDocuments()){
                        Post post = doc.toObject(Post.class);

                        if(post != null && post.getReactions() != null){
                            Map <String,String> reactions;
                            reactions = post.getReactions();
                            allMoods.addAll(reactions.values());
                        }
                        _reactionList.setValue(allMoods);
                    }

                })
                .addOnFailureListener(  (Exception e)-> {
                    _errorMessage.setValue(e.getMessage());
                });

    }

}
//
//public class StaticticsViewModel extends AndroidViewModel {
//
//    private MutableLiveData<List<Pair<String, Integer>>> moodData = new MutableLiveData<>();
//    private MutableLiveData<List<Pair<String, Integer>>> emotionData = new MutableLiveData<>();
//    private PostRepository postRepository = new PostRepository();
//
//
//    public StaticticsViewModel(@NonNull Application application) {
//        super(application);
//
//        Date startDate = new Date();
//        Date endDate = new Date();
//
//        private void getDatas= postRepository.getP
//
//        List<Pair<>>
//        // Giả lập dữ liệu, sau này thay bằng API hoặc Firestore
//        List<Pair<String, Integer>> moods = new ArrayList<>();
//        moods.add(new Pair<>("Happy", 10));
//        moods.add(new Pair<>("Sad", 5));
//        moods.add(new Pair<>("Excited", 8));
//        moods.add(new Pair<>("Calm", 6));
//        moods.add(new Pair<>("Angry", 3));
//        moodData.setValue(moods);
//
//        List<Pair<String, Integer>> emotions = new ArrayList<>();
//        emotions.add(new Pair<>("Mon", 5));
//        emotions.add(new Pair<>("Tue", 8));
//        emotions.add(new Pair<>("Wed", 6));
//        emotions.add(new Pair<>("Thu", 10));
//        emotions.add(new Pair<>("Fri", 7));
//        emotionData.setValue(emotions);
//    }
//
//    public LiveData<List<Pair<String, Integer>>> getMoodData() {
//        return moodData;
//    }
//
//    public LiveData<List<Pair<String, Integer>>> getEmotionData() {
//        return emotionData;
//    }
//}
