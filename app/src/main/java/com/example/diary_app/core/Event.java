package com.example.diary_app.core;

/**
 * Event Wrapper - Đảm bảo mỗi sự kiện one-shot chỉ được xử lý MỘT LẦN duy nhất.
 * Giải quyết vấn đề "Sticky LiveData" - khi observer mới đăng ký, LiveData thông thường
 * sẽ phát lại giá trị cũ, gây ra bug re-trigger
 */
public class Event<T> {
    private final T content;
    private boolean hasBeenHandled = false;

    public Event(T content) {
        this.content = content;
    }

    /**
     * Lấy nội dung nếu chưa được xử lý.
     * Sau lần gọi đầu tiên, trả về null cho các lần gọi tiếp theo.
     */
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    /**
     * Lấy nội dung mà không đánh dấu là đã xử lý.
     */
    public T peekContent() {
        return content;
    }
}
