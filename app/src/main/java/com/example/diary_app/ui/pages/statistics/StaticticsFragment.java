package com.example.diary_app.ui.pages.statistics;

import android.os.Bundle;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

    // Các thành phần UI cho Top 3 Emotion
    private LinearLayout layoutTop1, layoutTop2, layoutTop3;
    private View imgTop1, imgTop2, imgTop3;
    private TextView txtTop1, txtTop2, txtTop3;
    //endregion

    public StaticticsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

        // Ánh xạ các View của Top Emotion từ XML đã đặt ID
        layoutTop1 = view.findViewById(R.id.layout_top1);
        layoutTop2 = view.findViewById(R.id.layout_top2);
        layoutTop3 = view.findViewById(R.id.layout_top3);

        imgTop1 = view.findViewById(R.id.img_top1);
        imgTop2 = view.findViewById(R.id.img_top2);
        imgTop3 = view.findViewById(R.id.img_top3);

        txtTop1 = view.findViewById(R.id.txt_top1);
        txtTop2 = view.findViewById(R.id.txt_top2);
        txtTop3 = view.findViewById(R.id.txt_top3);

        // Gắn ViewModel với vòng đời của Fragment hiện tại
        viewModel = new ViewModelProvider(this).get(StaticticsViewModel.class);
        //endregion

        // Lắng nghe danh sách cảm xúc thô để vẽ biểu đồ
        viewModel.getReactionList().observe(getViewLifecycleOwner(), (List<String> reactions) -> {
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

        // Lắng nghe Top 3 Emotion từ ViewModel truyền về để cập nhật UI Top Emotion
        viewModel.getTopEmotions().observe(getViewLifecycleOwner(), (List<Pair<String, Integer>> topEmotions) -> {
            if (isAdded() && getContext() != null) {
                updateTopEmotionsUI(topEmotions);
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

    /**
     * Nhận danh sách Top 3 đã được xử lý từ ViewModel và đổ lên UI ô màu
     */
    private void updateTopEmotionsUI(List<Pair<String, Integer>> topEmotions) {
        // Mặc định ẩn tất cả các ô đi để dọn sạch dữ liệu cũ của tháng trước
        if (layoutTop1 != null) layoutTop1.setVisibility(View.INVISIBLE);
        if (layoutTop2 != null) layoutTop2.setVisibility(View.INVISIBLE);
        if (layoutTop3 != null) layoutTop3.setVisibility(View.INVISIBLE);

        if (topEmotions == null || topEmotions.isEmpty()) return;

        // Đổ dữ liệu động vào các ô tương ứng nếu có phần tử tương thích
        if (topEmotions.size() >= 1 && layoutTop1 != null) {
            setSingleEmotionItem(layoutTop1, imgTop1, txtTop1, topEmotions.get(0));
        }
        if (topEmotions.size() >= 2 && layoutTop2 != null) {
            setSingleEmotionItem(layoutTop2, imgTop2, txtTop2, topEmotions.get(1));
        }
        if (topEmotions.size() >= 3 && layoutTop3 != null) {
            setSingleEmotionItem(layoutTop3, imgTop3, txtTop3, topEmotions.get(2));
        }
    }

    private void setSingleEmotionItem(LinearLayout layout, View dotView, TextView textView, Pair<String, Integer> data) {
        layout.setVisibility(View.VISIBLE);

        // Gán text hiển thị, ví dụ: "Happy (5)"
        textView.setText(data.first + " (" + data.second + ")");

        // Đổi màu nền chấm tròn tương ứng với tên
        int drawableId = getEmotionDrawable(data.first);
        dotView.setBackgroundResource(drawableId);
    }

    private int getEmotionDrawable(String emotionName) {
        if (emotionName == null) return R.drawable.bg_circle_neutral;

        switch (emotionName.toLowerCase().trim()) {
            case "happy":
                return R.drawable.bg_circle_happy;
            case "angry":
                return R.drawable.bg_circle_angry;
            case "calm":
                return R.drawable.bg_circle_calm;
            case "sad":
                return R.drawable.bg_circle_sad;
            default:
                return R.drawable.bg_circle_neutral;
        }
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
            // Thay đổi từ hàm cũ sang hàm loadData hợp nhất mới của ViewModel
            viewModel.loadData(Uid, startDate, endDate);
        }
    }

    private Map<String, Integer> countReactions(List<String> reactions) {
        Map<String, Integer> counts = new HashMap<>();
        for (String r : reactions) {
            counts.put(r, counts.getOrDefault(r, 0) + 1);
        }
        return counts;
    }

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