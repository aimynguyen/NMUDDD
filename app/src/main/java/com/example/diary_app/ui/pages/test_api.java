package com.example.diary_app.ui.pages;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.diary_app.R;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class test_api extends AppCompatActivity {

    // THAY CÁI KEY LẤY TỪ GOOGLE AI STUDIO VÀO ĐÂY (KHÔNG PHẢI KEY TRONG FILE JSON)
    private static final String MY_API_KEY = "AIzaSyASW_qgTgbCWqboIFOD8RZ7l6juzLG0zo8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_api_chatbot);

        TextView statusTextView = findViewById(R.id.txtStatus);
        statusTextView.setText("Đang kết nối với Gemini...");

        try {
            // BẮT BUỘC có tiền tố models/
            // Trong file test_api.java
// 1. Dùng model flash (bản nhanh và ít lỗi Not found nhất)
            GenerativeModel gm = new GenerativeModel("gemini-1.5-flash-001", MY_API_KEY);

// 2. Chỉnh lại phần gọi API một chút cho chuẩn bản 0.9.0
            GenerativeModelFutures model = GenerativeModelFutures.from(gm);

            Content content = new Content.Builder()
                    .addText("Chào Bot, hãy trả lời ngắn gọn: Kết nối OK!")
                    .build();

            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String botReply = result.getText();
                    runOnUiThread(() -> statusTextView.setText("Bot trả lời: " + botReply));
                }

                @Override
                public void onFailure(Throwable t) {
                    // Nếu vẫn lỗi, nó sẽ hiện lý do thực sự ở đây
                    runOnUiThread(() -> statusTextView.setText("Lỗi: " + t.getMessage()));
                }
            }, ContextCompat.getMainExecutor(this));

        } catch (Exception e) {
            statusTextView.setText("Lỗi khởi tạo: " + e.getMessage());
        }
    }
}