package com.example.diary_app.ui.pages.statistics;

import android.os.Bundle;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.charts.Pie;
import com.anychart.APIlib;
import com.example.diary_app.R;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StaticticsActivity extends AppCompatActivity {
    private StaticticsViewModel viewModel;
    private AnyChartView pieChartView;
    private AnyChartView barChartView;

    private GridLayout gridCalendar;
    private TextView month;
    private TextView year;
    private ImageButton btnDropDown;
    private ImageButton btnPre;
    private ImageButton btnNext;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_statictics);

        pieChartView = findViewById(R.id.pieChart);
        barChartView = findViewById(R.id.barChart);
        gridCalendar = findViewById(R.id.gridCalendar);
        month = findViewById(R.id.month);
        year = findViewById(R.id.year);
        btnDropDown = findViewById(R.id.btnDropDown);
        btnPre = findViewById(R.id.btnPre);
        btnNext = findViewById(R.id.btnNext);

        viewModel = new ViewModelProvider(this).get(StaticticsViewModel.class);

        viewModel.getMoodData().observe(this, moodData -> {
            if (moodData != null) showPieChart(moodData);
        });
        
        viewModel.getEmotionData().observe(this, emotionData -> {
            if (emotionData != null) showBarChart(emotionData);
        });

        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        updateCalendar();

        btnPre.setOnClickListener(v -> {
           calendar.add(Calendar.MONTH, -1);
           updateCalendar();
        });

        btnNext.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });
    }

    private void showPieChart(List<Pair<String, Integer>> moodData){
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
        pie.palette(new String[]{"#FEFACA", "#CBE5C2", "#D6C7DE", "#CDEBF3", "#F7BDB1"});
        pieChartView.setChart(pie);
    }

    private void showBarChart(List<Pair<String, Integer>> emotionData){
        APIlib.getInstance().setActiveAnyChartView(barChartView);
        Cartesian bar = AnyChart.cartesian();
        List<DataEntry> datas = new ArrayList<>();
        for(Pair<String, Integer> item : emotionData){
            datas.add(new ValueDataEntry(item.first, item.second));
        }
        bar.column(datas);
        bar.title("Emotion trends");
        bar.title().fontSize("12");
        bar.palette(new String[]{"#FEFACA", "#CBE5C2", "#D6C7DE", "#CDEBF3", "#F7BDB1"});
        barChartView.setChart(bar);
    }

    private void updateCalendar(){
        // Luôn đặt về ngày 1 để lấy thứ của ngày đầu tháng
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        
        month.setText(new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)]);
        year.setText(String.valueOf(calendar.get(Calendar.YEAR)));

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // Sunday = 1

        gridCalendar.removeAllViews();


        // 42 ô tất cả
        int totalCells = 42;
        int dayCounter = 1;

//        // 1. Thêm các ô trống (offset)
//        int offset = dayOfWeek - 1; // Số ô trống cần bỏ qua để ngày 1 đúng thứ
//        for(int i = 0; i < offset; i++){
//            TextView emptyView = new TextView(this);
//            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
//            params.width = 0;
//            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
//            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
//            emptyView.setLayoutParams(params);
//            gridCalendar.addView(emptyView);
//        }
//
//        // 2. Thêm các ngày trong tháng
//        for (int day = 1; day <= daysInMonth; day++){
//            TextView dayView = new TextView(this);
//            dayView.setText(String.valueOf(day));
//            dayView.setTextSize(14);
//            dayView.setTextColor(getResources().getColor(android.R.color.black));
//            dayView.setGravity(Gravity.CENTER);
//
//            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
//            params.width = 0;
//            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
//            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
//            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
//            params.setMargins(4, 12, 4, 12);
//            dayView.setLayoutParams(params);
//
//            gridCalendar.addView(dayView);
//        }
        for (int cell = 1; cell <= totalCells; cell++) {
            TextView dayView = new TextView(this);
            dayView.setGravity(Gravity.CENTER);
            dayView.setTextSize(14);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
            params.setMargins(4, 12, 4, 12);
            dayView.setLayoutParams(params);

            // Nếu ô này là ngày hợp lệ trong tháng
            if (cell >= dayOfWeek && dayCounter <= daysInMonth) {
                dayView.setText(String.valueOf(dayCounter));
                dayView.setTextColor(getResources().getColor(android.R.color.black));
                dayCounter++;
            } else {
                dayView.setText(""); // ô trống
            }

            gridCalendar.addView(dayView);
        }
    }
}
