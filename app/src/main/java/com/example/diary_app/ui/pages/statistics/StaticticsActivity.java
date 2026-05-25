//region code cũ
//package com.example.diary_app.ui.pages.statistics;
//
//import android.os.Bundle;
//import android.util.Pair;
//import android.view.Gravity;
//import android.view.View;
//import android.widget.GridLayout;
//import android.widget.ImageButton;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.anychart.AnyChart;
//import com.anychart.AnyChartView;
//import com.anychart.chart.common.dataentry.DataEntry;
//import com.anychart.chart.common.dataentry.ValueDataEntry;
//import com.anychart.charts.Cartesian;
//import com.anychart.charts.Pie;
//import com.anychart.APIlib;
//import com.example.diary_app.R;
//import com.google.firebase.auth.FirebaseAuth;
//
//import java.text.DateFormatSymbols;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//
//public class StaticticsActivity extends AppCompatActivity {
//
//    //region khai báo biến
//    private StaticticsViewModel viewModel;
//    private AnyChartView pieChartView;
//    private AnyChartView barChartView;
//
//    private GridLayout gridCalendar;
//    private TextView month;
//    private TextView year;
//    private ImageButton btnDropDown;
//    private ImageButton btnPre;
//    private ImageButton btnNext;
//    private Calendar calendar;
//    private Date startDate;
//    private Date endDate;
//    //endregion
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState){
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.fragment_statictics);
//
//        //region khởi tạo
//        pieChartView = findViewById(R.id.pieChart);
//        barChartView = findViewById(R.id.barChart);
//        gridCalendar = findViewById(R.id.gridCalendar);
//        month = findViewById(R.id.month);
//        year = findViewById(R.id.year);
//        btnDropDown = findViewById(R.id.btnDropDown);
//        btnPre = findViewById(R.id.btnPre);
//        btnNext = findViewById(R.id.btnNext);
//        startDate = new Date();
//        endDate = new Date();
//        //vm
//        viewModel = new ViewModelProvider(this).get(StaticticsViewModel.class);
//        //endregion
//
//        String Uid ="";
//        if(FirebaseAuth.getInstance().getCurrentUser() != null){
//            Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        }
//
//        viewModel.getReactionList().observe(this, (List<String> reactions) -> {
//            if(reactions != null && !reactions.isEmpty()){
//                Map<String,Integer> emotionCounts = countReactions(reactions);
//                showPieChart(emotionCounts);
//                showBarChart(emotionCounts);
//            }
//            else{
//                Map<String, Integer> emptyData = new HashMap<>();
//                emptyData.put("Chưa có dữ liệu", 1);
//                showPieChart(new HashMap<>());
//                showBarChart(new HashMap<>());
//            }
//        });
//
//        viewModel.getErrorMessage().observe(this, (String errorMessage) -> {
//            if(errorMessage != null){
//                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
//            }
//        });
//
////        viewModel.getMoodData().observe(this, moodData -> {
////            if (moodData != null) showPieChart(moodData);
////        });
////
////        viewModel.getEmotionData().observe(this, emotionData -> {
////            if (emotionData != null) showBarChart(emotionData);
////        });
////
//        calendar = Calendar.getInstance();
//        calendar.set(Calendar.DAY_OF_MONTH, 1);
//        updateCalendar();
//
//        btnPre.setOnClickListener(v -> {
//           calendar.add(Calendar.MONTH, -1);
//           updateCalendar();
//        });
//
//        btnNext.setOnClickListener(v -> {
//            calendar.add(Calendar.MONTH, 1);
//            updateCalendar();
//        });
//    }
//
//    // region show pie chat cũ
////    private void showPieChart(List<Pair<String, Integer>> moodData){
////        APIlib.getInstance().setActiveAnyChartView(pieChartView);
////        Pie pie = AnyChart.pie();
////        List<DataEntry> datas = new ArrayList<>();
////        for(Pair<String, Integer> item : moodData){
////            datas.add(new ValueDataEntry(item.first, item.second));
////        }
////        pie.data(datas);
////        pie.title("Thống kê tâm trạng");
////        pie.title().fontSize("12");
////        pie.legend().enabled(false);
////        pie.palette(new String[]{"#FEFACA", "#CBE5C2", "#D6C7DE", "#CDEBF3", "#F7BDB1"});
////        pieChartView.setChart(pie);
////    }
//    //endregion
//
//    private void showPieChart(Map<String, Integer> emotionDatas) {
//        APIlib.getInstance().setActiveAnyChartView(pieChartView);
//        Pie pie = AnyChart.pie();
//
//        List<DataEntry>datas = new ArrayList<>();
//
//        for(Map.Entry<String,Integer> item: emotionDatas.entrySet()){
//            datas.add(new ValueDataEntry(item.getKey(), item.getValue()));
//        }
//
//        pie.data(datas);
//        pie.title("Thống kê tâm trạng");
//        pie.title().fontSize("12");
//        pie.legend().enabled(false);
//        pie.palette(new String[]{"#FEFACA", "#CBE5C2", "#D6C7DE", "#CDEBF3", "#F7BDB1"});
//        pieChartView.setChart(pie);
//    }
//    //region show bar chat cũ
////    private void showBarChart(List<Pair<String, Integer>> emotionData){
////        APIlib.getInstance().setActiveAnyChartView(barChartView);
////        Cartesian bar = AnyChart.cartesian();
////        List<DataEntry> datas = new ArrayList<>();
////        for(Pair<String, Integer> item : emotionData){
////            datas.add(new ValueDataEntry(item.first, item.second));
////        }
////        bar.column(datas);
////        bar.title("Emotion trends");
////        bar.title().fontSize("12");
////        bar.palette(new String[]{"#FEFACA", "#CBE5C2", "#D6C7DE", "#CDEBF3", "#F7BDB1"});
////        barChartView.setChart(bar);
////    }
//    //endregion
//
//    private void showBarChart(Map<String, Integer> emotionDatas){
//        APIlib.getInstance().setActiveAnyChartView(barChartView);
//        Cartesian bar = AnyChart.cartesian();
//        List<DataEntry> datas = new ArrayList<>();
//        for(Map.Entry<String,Integer> item: emotionDatas.entrySet()){
//            datas.add(new ValueDataEntry(item.getKey(), item.getValue()));
//        }
//
//        bar.column(datas);
//        bar.title("Emotion trends");
//        bar.title().fontSize("12");
//        bar.palette(new String[]{"#FEFACA", "#CBE5C2", "#D6C7DE", "#CDEBF3", "#F7BDB1"});
//        barChartView.setChart(bar);
//    }
//
//    private void updateCalendar(){
//        //lấy thứ của ngày đầu tháng
//        calendar.set(Calendar.DAY_OF_MONTH, 1);
//
//        month.setText(new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)]);
//        year.setText(String.valueOf(calendar.get(Calendar.YEAR)));
//
//        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
//        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // Sunday = 1
//
//        gridCalendar.removeAllViews();
//
//        // 42 ô tất cả
//        int totalCells = 42;
//        int dayCounter = 1;
//       // 2. Thêm các ngày trong tháng
//
//        for (int cell = 1; cell <= totalCells; cell++) {
//            TextView dayView = new TextView(this);
//            dayView.setGravity(Gravity.CENTER);
//            dayView.setTextSize(14);
//
//            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
//            params.width = 0;
//            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
//            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
//            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
//            params.setMargins(4, 12, 4, 12);
//            dayView.setLayoutParams(params);
//
//            // Nếu ô này là ngày hợp lệ trong tháng
//            if (cell >= dayOfWeek && dayCounter <= daysInMonth) {
//                dayView.setText(String.valueOf(dayCounter));
//                dayView.setTextColor(getResources().getColor(android.R.color.black));
//                dayCounter++;
//            } else {
//                dayView.setText(""); // ô trống
//            }
//
//            gridCalendar.addView(dayView);
//        }
//        //TÍNH TOÁN THỜI GIAN VÀ KÍCH HOẠT VÀO VIEWMODEL ---
//        Calendar startCal = (Calendar) calendar.clone();
//        startCal.set(Calendar.DAY_OF_MONTH, 1);
//        startCal.set(Calendar.HOUR_OF_DAY, 0);
//        startCal.set(Calendar.MINUTE, 0);
//        startCal.set(Calendar.SECOND, 0);
//        startDate = startCal.getTime();
//
//        Calendar endCal = (Calendar) calendar.clone();
//        endCal.set(Calendar.DAY_OF_MONTH, daysInMonth);
//        endCal.set(Calendar.HOUR_OF_DAY, 23);
//        endCal.set(Calendar.MINUTE, 59);
//        endCal.set(Calendar.SECOND, 59);
//        endDate = endCal.getTime();
//
//        String Uid ="";
//        if(FirebaseAuth.getInstance().getCurrentUser() != null){
//            Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        }
//
//        if (!Uid.isEmpty()) {
//            viewModel.getReactionList(Uid, startDate, endDate);
//        }
//    }
//
//    private Map<String, Integer> countReactions(List<String> reactions) {
//        Map<String, Integer> counts = new HashMap<>();
//        for (String r : reactions) {
//            if (counts.containsKey(r)) {
//                counts.put(r, counts.get(r) + 1);
//            } else {
//                counts.put(r, 1);
//            }
//        }
//        return counts;
//    }
//}
//endregion

//region code mới
package com.example.diary_app.ui.pages.statistics;

import android.os.Bundle;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.charts.Pie;
import com.anychart.APIlib;

import com.example.diary_app.R;

import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// 1. Đổi thành extends Fragment
public class StaticticsActivity extends Fragment {

    //region khai báo biến
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
    private Date startDate;
    private Date endDate;
    //endregion

    // 2. Fragment dùng onCreateView để nạp layout XML
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statictics, container, false);
    }

    // 3. Toàn bộ logic khởi tạo và bắt sự kiện chuyển sang onViewCreated
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //region khởi tạo (Tìm ID thông qua biến view)
        pieChartView = view.findViewById(R.id.pieChart);
        barChartView = view.findViewById(R.id.barChart);
        gridCalendar = view.findViewById(R.id.gridCalendar);
        month = view.findViewById(R.id.month);
        year = view.findViewById(R.id.year);
        btnDropDown = view.findViewById(R.id.btnDropDown);
        btnPre = view.findViewById(R.id.btnPre);
        btnNext = view.findViewById(R.id.btnNext);
        startDate = new Date();
        endDate = new Date();

        // Khởi tạo ViewModel gắn với Fragment này
        viewModel = new ViewModelProvider(this).get(StaticticsViewModel.class);
        //endregion

        String Uid ="";
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Thay 'this' bằng 'getViewLifecycleOwner()' để quản lý LiveData chuẩn theo Fragment
        viewModel.getReactionList().observe(getViewLifecycleOwner(), (List<String> reactions) -> {
            if(reactions != null && !reactions.isEmpty()){
                Map<String,Integer> emotionCounts = countReactions(reactions);
                showPieChart(emotionCounts);
                showBarChart(emotionCounts);
            }
            else{
                Map<String, Integer> emptyData = new HashMap<>();
                emptyData.put("Chưa có dữ liệu", 1);
                showPieChart(new HashMap<>());
                showBarChart(new HashMap<>());
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), (String errorMessage) -> {
            if(errorMessage != null){
                // Fragment không phải là Context, nên dùng requireContext() thay cho 'this'
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
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

    private void showPieChart(Map<String, Integer> emotionDatas) {
        APIlib.getInstance().setActiveAnyChartView(pieChartView);
        Pie pie = AnyChart.pie();

        List<DataEntry> datas = new ArrayList<>();
        for(Map.Entry<String,Integer> item: emotionDatas.entrySet()){
            datas.add(new ValueDataEntry(item.getKey(), item.getValue()));
        }

        pie.data(datas);
        pie.title("Thống kê tâm trạng");
        pie.title().fontSize("12");
        pie.legend().enabled(false);
        pie.palette(new String[]{"#FEFACA", "#CBE5C2", "#D6C7DE", "#CDEBF3", "#F7BDB1"});
        pieChartView.setChart(pie);
    }

    private void showBarChart(Map<String, Integer> emotionDatas){
        APIlib.getInstance().setActiveAnyChartView(barChartView);
        Cartesian bar = AnyChart.cartesian();
        List<DataEntry> datas = new ArrayList<>();
        for(Map.Entry<String,Integer> item: emotionDatas.entrySet()){
            datas.add(new ValueDataEntry(item.getKey(), item.getValue()));
        }

        bar.column(datas);
        bar.title("Emotion trends");
        bar.title().fontSize("12");
        bar.palette(new String[]{"#FEFACA", "#CBE5C2", "#D6C7DE", "#CDEBF3", "#F7BDB1"});
        barChartView.setChart(bar);
    }

    private void updateCalendar(){
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        month.setText(new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)]);
        year.setText(String.valueOf(calendar.get(Calendar.YEAR)));

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        gridCalendar.removeAllViews();

        int totalCells = 42;
        int dayCounter = 1;

        for (int cell = 1; cell <= totalCells; cell++) {
            // Dùng requireContext() để khởi tạo View trong Fragment
            TextView dayView = new TextView(requireContext());
            dayView.setGravity(Gravity.CENTER);
            dayView.setTextSize(14);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
            params.setMargins(4, 12, 4, 12);
            dayView.setLayoutParams(params);

            if (cell >= dayOfWeek && dayCounter <= daysInMonth) {
                dayView.setText(String.valueOf(dayCounter));
                // Dùng ContextCompat để lấy màu (M3 tương thích tốt hơn)
                dayView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
                dayCounter++;
            } else {
                dayView.setText("");
            }

            gridCalendar.addView(dayView);
        }

        Calendar startCal = (Calendar) calendar.clone();
        startCal.set(Calendar.DAY_OF_MONTH, 1);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startDate = startCal.getTime();

        Calendar endCal = (Calendar) calendar.clone();
        endCal.set(Calendar.DAY_OF_MONTH, daysInMonth);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endDate = endCal.getTime();

        String Uid ="";
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if (!Uid.isEmpty()) {
            viewModel.getReactionList(Uid, startDate, endDate);
        }
    }

    private Map<String, Integer> countReactions(List<String> reactions) {
        Map<String, Integer> counts = new HashMap<>();
        for (String r : reactions) {
            if (counts.containsKey(r)) {
                counts.put(r, counts.get(r) + 1);
            } else {
                counts.put(r, 1);
            }
        }
        return counts;
    }
}
//endregion