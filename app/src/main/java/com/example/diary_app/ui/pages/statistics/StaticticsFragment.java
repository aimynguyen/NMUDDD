package com.example.diary_app.ui.pages.statistics;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

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
    private android.widget.FrameLayout pieChartContainer;
    private android.widget.FrameLayout barChartContainer;

    private GridLayout gridCalendar;
    private TextView month;
    private TextView year;
    private ImageButton btnDropDown;
    private ImageButton btnPre;
    private ImageButton btnNext;
    private AppCompatButton btnVideo;
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
        pieChartContainer = view.findViewById(R.id.pieChartContainer);
        barChartContainer = view.findViewById(R.id.barChartContainer);
        gridCalendar = view.findViewById(R.id.gridCalendar);
        month = view.findViewById(R.id.month);
        year = view.findViewById(R.id.year);
        btnDropDown = view.findViewById(R.id.btnDropDown);
        btnPre = view.findViewById(R.id.btnPre);
        btnNext = view.findViewById(R.id.btnNext);
        btnVideo = view.findViewById(R.id.btnVideo);
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
        
        // Lắng nghe cảm xúc theo ngày để cập nhật Lịch
        viewModel.getDailyEmotions().observe(getViewLifecycleOwner(), (Map<Integer, String> dailyEmotions) -> {
            if (isAdded() && getContext() != null) {
                updateCalendarUIWithEmotions(dailyEmotions);
            }
        });

        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        updateCalendar();

        btnDropDown.setOnClickListener(v -> showMonthYearPickerDialog());

        btnPre.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        btnNext.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        btnVideo.setOnClickListener(v -> {
            String uid = "";
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }

            Bundle args = new Bundle();
            args.putLong("startDate", startDate.getTime());
            args.putLong("endDate", endDate.getTime());
            args.putString("userId", uid);

            Navigation.findNavController(v)
                    .navigate(R.id.action_nav_dashboard_to_nav_video, args);
        });
    }
    private void showMonthYearPickerDialog() {
        if (!isAdded() || getContext() == null) return;

        // Tạo container layout chứa 2 NumberPicker
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(48, 32, 48, 16);

        // NumberPicker chọn Tháng (1 - 12)
        NumberPicker monthPicker = new NumberPicker(requireContext());
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(calendar.get(Calendar.MONTH) + 1);
        monthPicker.setDisplayedValues(new String[]{
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        });

        // Spacer giữa 2 picker
        TextView spacer = new TextView(requireContext());
        spacer.setText("/");
        spacer.setTextSize(20);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        spacerParams.setMargins(16, 0, 16, 0);
        spacer.setLayoutParams(spacerParams);

        // NumberPicker chọn Năm (5 năm về trước đến 5 năm sau)
        NumberPicker yearPicker = new NumberPicker(requireContext());
        int currentYear = calendar.get(Calendar.YEAR);
        yearPicker.setMinValue(currentYear - 5);
        yearPicker.setMaxValue(currentYear + 5);
        yearPicker.setValue(currentYear);

        container.addView(monthPicker);
        container.addView(spacer);
        container.addView(yearPicker);

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn tháng / năm")
                .setView(container)
                .setPositiveButton("OK", (dialog, which) -> {
                    calendar.set(Calendar.MONTH, monthPicker.getValue() - 1);
                    calendar.set(Calendar.YEAR, yearPicker.getValue());
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    updateCalendar();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void showPieChart(Map<String, Integer> emotionDatas) {
        if (pieChartContainer == null || !isAdded() || getContext() == null) return;

        // Xóa chart cũ và tạo mới AnyChartView để tránh lỗi re-render của WebView
        pieChartContainer.removeAllViews();
        AnyChartView pieChartView = new AnyChartView(requireContext());
        pieChartView.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT));
        pieChartContainer.addView(pieChartView);

        APIlib.getInstance().setActiveAnyChartView(pieChartView);
        Pie pie = AnyChart.pie();

        List<DataEntry> datas = new ArrayList<>();
        List<String> colorPalette = new ArrayList<>();

        // Lặp qua dữ liệu và map chuẩn màu cho từng cột/miếng bánh
        for (Map.Entry<String, Integer> item : emotionDatas.entrySet()) {
            datas.add(new ValueDataEntry(item.getKey(), item.getValue()));
            // Lấy mã màu HEX chính xác dựa trên tên cảm xúc
            colorPalette.add(getEmotionHexColor(item.getKey()));
        }

        pie.data(datas);
        pie.title("Thống kê tâm trạng");
        pie.title().fontSize("14");
        pie.legend().enabled(false);

        // Đặt bảng màu động dựa theo thứ tự dữ liệu truyền vào
        if (!colorPalette.isEmpty()) {
            pie.palette(colorPalette.toArray(new String[0]));
        }

        pieChartView.setChart(pie);
    }

    private void showBarChart(Map<String, Integer> emotionDatas) {
        if (barChartContainer == null || !isAdded() || getContext() == null) return;

        // Xóa chart cũ và tạo mới AnyChartView để tránh lỗi re-render của WebView
        barChartContainer.removeAllViews();
        AnyChartView barChartView = new AnyChartView(requireContext());
        barChartView.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT));
        barChartContainer.addView(barChartView);

        APIlib.getInstance().setActiveAnyChartView(barChartView);
        Cartesian bar = AnyChart.cartesian();

        // Dùng anonymous subclass của ValueDataEntry (kế thừa setValue() từ DataEntry)
        // để truyền thuộc tính 'fill' và 'stroke' trực tiếp vào từng điểm dữ liệu
        List<DataEntry> datas = new ArrayList<>();
        for (Map.Entry<String, Integer> item : emotionDatas.entrySet()) {
            final String color = getEmotionHexColor(item.getKey());
            final String label = item.getKey();
            final int value = item.getValue();
            ValueDataEntry entry = new ValueDataEntry(label, value) {{
                setValue("fill", color);
                setValue("stroke", color);
            }};
            datas.add(entry);
        }

        com.anychart.core.cartesian.series.Column column = bar.column(datas);
        // Cho phép mỗi điểm trong series tự xác định màu từ trường 'fill'
        column.fill("function() { return this.getData('fill'); }");
        column.stroke("function() { return this.getData('stroke'); }");

        bar.title("Emotion trends");
        bar.title().fontSize("14");

        barChartView.setChart(bar);
    }

    /**
     * Hàm lấy mã màu HEX khớp 100% với ảnh colors.xml của bạn
     */
    /**
     * Hàm lấy mã màu HEX cho Biểu đồ - Đã cập nhật nhận diện cả Emoji từ Firebase
     */
    private String getEmotionHexColor(String emotionName) {
        if (emotionName == null) return "#FBD1DD"; // Mặc định là màu hồng nhạt

        String name = emotionName.toLowerCase().trim();

        // Kiểm tra nếu chuỗi chứa ký tự Emoji hoặc chữ tương ứng
        if (name.contains("happy") || name.contains("😁") || name.contains("😀")) {
            return "#FEFACA"; // Màu vàng
        } else if (name.contains("calm") || name.contains("😌") || name.contains("😊")) {
            return "#CBE5C2"; // Màu xanh lá
        } else if (name.contains("neutral") || name.contains("😳") || name.contains("😐")) {
            return "#FBD1DD"; // Màu hồng
        } else if (name.contains("sad") || name.contains("😭") || name.contains("😢")) {
            return "#CDEBF3"; // Màu xanh dương
        } else if (name.contains("angry") || name.contains("😡") || name.contains("❤️")) {
            return "#F7BDB1"; // Màu cam đỏ (Angry)
        }

        return "#FBD1DD"; // Mặc định lọt lưới thì trả về màu hồng nhạt
    }

    /**
     * Hàm lấy hình tròn màu cho mục Top Emotion - Đã cập nhật nhận diện cả Emoji từ Firebase
     */
    private int getEmotionDrawable(String emotionName) {
        if (emotionName == null) return R.drawable.bg_circle_neutral;

        String name = emotionName.toLowerCase().trim();

        if (name.contains("happy") || name.contains("😁") || name.contains("😀")) {
            return R.drawable.bg_circle_happy;
        } else if (name.contains("calm") || name.contains("😌") || name.contains("😊")) {
            return R.drawable.bg_circle_calm;
        } else if (name.contains("neutral") || name.contains("😳") || name.contains("😐")) {
            return R.drawable.bg_circle_neutral;
        } else if (name.contains("sad") || name.contains("😭") || name.contains("😢")) {
            return R.drawable.bg_circle_sad;
        } else if (name.contains("angry") || name.contains("😡") || name.contains("❤️")) {
            return R.drawable.bg_circle_angry;
        }

        return R.drawable.bg_circle_neutral;
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

    private void updateCalendar() {
        if (!isAdded() || getContext() == null) return;

        calendar.set(Calendar.DAY_OF_MONTH, 1);

        month.setText(new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)]);
        year.setText(String.valueOf(calendar.get(Calendar.YEAR)));

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        gridCalendar.removeAllViews();

        // Tính tổng số ô cần thiết để không bị dư khoảng trắng phía dưới
        int emptyCells = dayOfWeek - 1; 
        int requiredCells = emptyCells + daysInMonth;
        int totalCells = (int) Math.ceil(requiredCells / 7.0) * 7;
        
        int dayCounter = 1;

        // Kích thước cố định cho mỗi ô ngày (40dp) để hình tròn không bị méo
        int sizeInPx = (int) (30 * requireContext().getResources().getDisplayMetrics().density + 0.5f);

        for (int cell = 1; cell <= totalCells; cell++) {
            android.widget.FrameLayout cellLayout = new android.widget.FrameLayout(requireContext());
            GridLayout.LayoutParams cellParams = new GridLayout.LayoutParams();
            cellParams.width = 0; // Để chia đều 7 cột
            cellParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
            cellParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            cellParams.rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
            cellLayout.setLayoutParams(cellParams);

            TextView dayView = new TextView(requireContext());
            dayView.setGravity(Gravity.CENTER);
            dayView.setTextSize(14);

            android.widget.FrameLayout.LayoutParams dayParams = new android.widget.FrameLayout.LayoutParams(sizeInPx, sizeInPx);
            dayParams.gravity = Gravity.CENTER; // Căn giữa TextView trong ô lưới
            dayParams.setMargins(0, 8, 0, 8);
            dayView.setLayoutParams(dayParams);

            if (cell >= dayOfWeek && dayCounter <= daysInMonth) {
                dayView.setText(String.valueOf(dayCounter));
                dayView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
                dayView.setTag(dayCounter);
                dayCounter++;
            } else {
                dayView.setText("");
                dayView.setTag(null);
            }

            cellLayout.addView(dayView);
            gridCalendar.addView(cellLayout);
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
    
    private void updateCalendarUIWithEmotions(Map<Integer, String> dailyEmotions) {
        if (gridCalendar == null || dailyEmotions == null) return;
        for (int i = 0; i < gridCalendar.getChildCount(); i++) {
            View child = gridCalendar.getChildAt(i);
            if (child instanceof android.widget.FrameLayout) {
                android.widget.FrameLayout cellLayout = (android.widget.FrameLayout) child;
                if (cellLayout.getChildCount() > 0 && cellLayout.getChildAt(0) instanceof TextView) {
                    TextView dayView = (TextView) cellLayout.getChildAt(0);
                    Object tag = dayView.getTag();
                    if (tag instanceof Integer) {
                        int day = (Integer) tag;
                        if (dailyEmotions.containsKey(day)) {
                            String emotion = dailyEmotions.get(day);
                            dayView.setBackgroundResource(getEmotionDrawable(emotion));
                        } else {
                            dayView.setBackgroundResource(0);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pieChartContainer != null) {
            pieChartContainer.removeAllViews();
            pieChartContainer = null;
        }
        if (barChartContainer != null) {
            barChartContainer.removeAllViews();
            barChartContainer = null;
        }
    }
}