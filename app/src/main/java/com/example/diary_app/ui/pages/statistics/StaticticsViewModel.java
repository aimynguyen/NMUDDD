package com.example.diary_app.ui.pages.statistics;

import android.app.Application;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class StaticticsViewModel extends AndroidViewModel {

    private MutableLiveData<List<Pair<String, Integer>>> moodData = new MutableLiveData<>();
    private MutableLiveData<List<Pair<String, Integer>>> emotionData = new MutableLiveData<>();

    public StaticticsViewModel(@NonNull Application application) {
        super(application);

        // Giả lập dữ liệu, sau này thay bằng API hoặc Firestore
        List<Pair<String, Integer>> moods = new ArrayList<>();
        moods.add(new Pair<>("Happy", 10));
        moods.add(new Pair<>("Sad", 5));
        moods.add(new Pair<>("Excited", 8));
        moods.add(new Pair<>("Calm", 6));
        moods.add(new Pair<>("Angry", 3));
        moodData.setValue(moods);

        List<Pair<String, Integer>> emotions = new ArrayList<>();
        emotions.add(new Pair<>("Mon", 5));
        emotions.add(new Pair<>("Tue", 8));
        emotions.add(new Pair<>("Wed", 6));
        emotions.add(new Pair<>("Thu", 10));
        emotions.add(new Pair<>("Fri", 7));
        emotionData.setValue(emotions);
    }

    public LiveData<List<Pair<String, Integer>>> getMoodData() {
        return moodData;
    }

    public LiveData<List<Pair<String, Integer>>> getEmotionData() {
        return emotionData;
    }
}
