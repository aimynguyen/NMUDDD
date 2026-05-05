package com.example.diary_app.ui.pages.statistics;

import android.os.Bundle;
import android.util.Pair;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.charts.Pie;
import com.anychart.APIlib; // Import bắt buộc để xử lý nhiều biểu đồ
import com.example.diary_app.R;

import java.util.ArrayList;
import java.util.List;

public class StaticticsActivity extends AppCompatActivity {
    private StaticticsViewModel viewModel;
    private AnyChartView pieChartView;
    private AnyChartView barChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_statictics);

        pieChartView = findViewById(R.id.pieChart);
        barChartView = findViewById(R.id.barChart);

        viewModel = new ViewModelProvider(this).get(StaticticsViewModel.class);

        // Quan sát dữ liệu và hiển thị
        viewModel.getMoodData().observe(this, moodData -> {
            if (moodData != null) showPieChart(moodData);
        });
        
        viewModel.getEmotionData().observe(this, emotionData -> {
            if (emotionData != null) showBarChart(emotionData);
        });
    }

    private void showPieChart(List<Pair<String, Integer>> moodData){
        // BẮT BUỘC: Thông báo cho AnyChart biết ta đang thao tác với pieChartView
        APIlib.getInstance().setActiveAnyChartView(pieChartView);

        Pie pie = AnyChart.pie();
        List<DataEntry> datas = new ArrayList<>();

        for(Pair<String, Integer> item : moodData){
            datas.add(new ValueDataEntry(item.first, item.second));
        }

        pie.data(datas);
        pie.title("Thống kê tâm trạng");
        pie.title().fontSize("12");
        pie.legend().enabled(false);
        pie.palette(new String[]{
                "#FEFACA", // mood_happy
                "#CBE5C2", // mood_calm
                "#D6C7DE", // mood_neutral
                "#CDEBF3", // mood_sad
                "#F7BDB1"  // mood_angry
        });

        pieChartView.setChart(pie);
    }

    private void showBarChart(List<Pair<String, Integer>> emotionData){
        // BẮT BUỘC: Thông báo cho thư viện biết ta đang thao tác với barChartView
        APIlib.getInstance().setActiveAnyChartView(barChartView);

        Cartesian bar = AnyChart.cartesian();
        List<DataEntry> datas = new ArrayList<>();

        for(Pair<String, Integer> item : emotionData){
            datas.add(new ValueDataEntry(item.first, item.second));
        }

        bar.column(datas);
        bar.title("Emotion trends");
        bar.title().fontSize("12");
        bar.labels().fontSize("4");
        bar.legend().fontSize("10");
        bar.palette(new String[]{
                "#FEFACA",
                "#CBE5C2",
                "#D6C7DE",
                "#CDEBF3",
                "#F7BDB1"
        });
        barChartView.setChart(bar);
    }
}
