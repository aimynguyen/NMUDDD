package com.example.diary_app.ui.pages.statistics;

import android.os.Bundle;
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
import com.example.diary_app.viewmodel.StaticticsViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticticsFragment extends Fragment {

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

    public StaticticsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp layout XML fragment_statictics
        return inflater.inflate(R.layout.fragment_statictics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //region khởi tạo (Tìm ID qua view)
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

        // Gắn ViewModel với vòng đời của Fragment hiện tại
        viewModel = new ViewModelProvider(this).get(StaticticsViewModel.class);
        //endregion

        String Uid = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Lắng nghe danh sách cảm xúc từ LiveData
        viewModel.getReactionList().observe(getViewLifecycleOwner(), (List<String> reactions) -> {
            // An toàn giao diện: Chỉ cập nhật khi Fragment còn tồn tại và đã được đính kèm vào Activity
            if (isAdded() && getContext() != null) {
                if (reactions != null && !reactions.isEmpty()) {
                    Map<String, Integer> emotionCounts = countReactions(reactions);
                    showPieChart(emotionCounts);
                    showBarChart(emotionCounts);
                } else {
                    showPieChart(new HashMap<>());
                    showBarChart(new HashMap<>());
                }
            }
        });

        // Lắng nghe thông báo lỗi
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), (String errorMessage) -> {
            if (isAdded() && errorMessage != null) {
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
        if (pieChartView == null) return;

        APIlib.getInstance().setActiveAnyChartView(pieChartView);
        Pie pie = AnyChart.pie();

        List<DataEntry> datas = new ArrayList<>();
        for (Map.Entry<String, Integer> item : emotionDatas.entrySet()) {
            datas.add(new ValueDataEntry(item.getKey(), item.getValue()));
        }

        pie.data(datas);
        pie.title("Thống kê tâm trạng");
        pie.title().fontSize("12");
        pie.legend().enabled(false);
        pie.palette(new String[]{"#FEFACA", "#CBE5C2", "#D6C7DE", "#CDEBF3", "#F7BDB1"});
        pieChartView.setChart(pie);
    }

    private void showBarChart(Map<String, Integer> emotionDatas) {
        if (barChartView == null) return;

        APIlib.getInstance().setActiveAnyChartView(barChartView);
        Cartesian bar = AnyChart.cartesian();
        List<DataEntry> datas = new ArrayList<>();
        for (Map.Entry<String, Integer> item : emotionDatas.entrySet()) {
            datas.add(new ValueDataEntry(item.getKey(), item.getValue()));
        }

        bar.column(datas);
        bar.title("Emotion trends");
        bar.title().fontSize("12");
        bar.palette(new String[]{"#FEFACA", "#CBE5C2", "#D6C7DE", "#CDEBF3", "#F7BDB1"});
        barChartView.setChart(bar);
    }

    private void updateCalendar() {
        if (!isAdded() || getContext() == null) return;

        calendar.set(Calendar.DAY_OF_MONTH, 1);

        month.setText(new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)]);
        year.setText(String.valueOf(calendar.get(Calendar.YEAR)));

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        gridCalendar.removeAllViews();

        int totalCells = 42;
        int dayCounter = 1;

        for (int cell = 1; cell <= totalCells; cell++) {
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

        String Uid = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if (!Uid.isEmpty()) {
            viewModel.getReactionList(Uid, startDate, endDate);
        }
    }

    private Map<String, Integer> countReactions(List<String> reactions) {
        Map<String, Integer> counts = new HashMap<>();
        for (String r : reactions) {
            counts.put(r, counts.getOrDefault(r, 0) + 1);
        }
        return counts;
    }

    // TỐI ƯU BỘ NHỚ: Giải phóng tài nguyên đồ thị WebView khi Fragment bị huỷ giao diện
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pieChartView != null) {
            pieChartView.removeAllViews();
            pieChartView = null;
        }
        if (barChartView != null) {
            barChartView.removeAllViews();
            barChartView = null;
        }
    }
}