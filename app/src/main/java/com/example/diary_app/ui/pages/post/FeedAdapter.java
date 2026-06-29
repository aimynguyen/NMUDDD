package com.example.diary_app.ui.pages.post;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.diary_app.R;
import com.example.diary_app.data.model.Post;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    private Context context;
    private List<Post> postList;
    private OnPostInteractionListener listener;

    // 1. TẠO INTERFACE: Bộ đàm liên lạc giữa Adapter và Fragment
    public interface OnPostInteractionListener {
        void onReactionClick(Post post, String reactionType);
        void onPostLongClick(Post post, View anchor);
        void onMyPostDoubleTap(Post post);
    }

    // 2. CONSTRUCTOR: Ép buộc phải truyền vào bộ đàm (listener)
    public FeedAdapter(Context context, List<Post> postList, OnPostInteractionListener listener) {
        this.context = context;
        this.postList = postList;
        this.listener = listener;
    }

    public void setPosts(List<Post> posts) {
        this.postList = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feed, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        Post currentPost = postList.get(position);
        // Khởi tạo Repo để lấy ID của người đang lướt App
        com.example.diary_app.repository.AuthRepository authRepo = new com.example.diary_app.repository.AuthRepository();
        String myUid = authRepo.getCurrentUserId();

        // Kiểm tra xem bài viết này có phải của chính mình không
        if (myUid != null && myUid.equals(currentPost.getUserId())) {
            // LÀ BÀI CỦA MÌNH -> Xóa sổ thanh React và Comment
            holder.layoutReactionBar.setVisibility(View.GONE);
            holder.layoutCommentBar.setVisibility(View.GONE);
        } else {
            // LÀ BÀI NGƯỜI KHÁC -> Hiện lên bình thường
            holder.layoutReactionBar.setVisibility(View.VISIBLE);
            holder.layoutCommentBar.setVisibility(View.VISIBLE);
        }

        // --- A. HIỂN THỊ DỮ LIỆU BÀI VIẾT ---
        holder.tvUsername.setText(currentPost.getUserName());
        // ==========================================
        // HIỂN THỊ NGÀY THÁNG ĐĂNG BÀI
        // ==========================================
        // Lấy thời gian từ Model Post (đảm bảo hàm Getter trong Post.java của bạn tên là getCreateAt hoặc getCreatedAt)
        com.google.firebase.Timestamp timestamp = currentPost.getCreateAt();

        if (timestamp != null) {
            java.util.Date date = timestamp.toDate();
            // Định dạng ngày giờ: Ngày/Tháng/Năm Giờ:Phút
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
            holder.tvDate.setText(sdf.format(date));
            holder.tvDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvDate.setVisibility(View.GONE);
        }
           //caption rỗng thì ẩn ô caption
        String caption = currentPost.getCaption();

        if (caption == null || caption.trim().isEmpty()) {
            holder.tvCaption.setVisibility(View.GONE);
        } else {
            holder.tvCaption.setVisibility(View.VISIBLE);
            holder.tvCaption.setText(caption);
        }

        // --- B. XỬ LÝ SỰ KIỆN THẢ CẢM XÚC ---
        // Khi bấm vào cảm xúc nào, Adapter sẽ dùng bộ đàm gọi về Fragment kèm theo ID bài viết
        holder.reactAngry.setOnClickListener(v -> listener.onReactionClick(currentPost, "angry"));
        holder.reactHappy.setOnClickListener(v -> listener.onReactionClick(currentPost, "happy"));
        holder.reactNeutral.setOnClickListener(v -> listener.onReactionClick(currentPost, "neutral"));
        holder.reactSad.setOnClickListener(v -> listener.onReactionClick(currentPost, "sad"));
        holder.reactCalm.setOnClickListener(v -> listener.onReactionClick(currentPost, "calm"));
        //Load ảnh
        if (currentPost.getImageUrl() != null && !currentPost.getImageUrl().isEmpty()) {

            // Dùng thư viện Glide để tải ảnh
            Glide.with(holder.itemView.getContext())
                    .load(currentPost.getImageUrl())
                    .into(holder.imgPostContent);
            // Đảm bảo khung ảnh hiện ra
            holder.imgPostContent.setVisibility(View.VISIBLE);

        } else {
            // Nếu bài viết không có ảnh, ra lệnh giấu luôn cái khung đi
            holder.imgPostContent.setVisibility(View.GONE);
        }
        // ==========================================
        // 1. HIỂN THỊ AVATAR NGƯỜI DÙNG
        // ==========================================
        if (currentPost.getUserAvatar() != null && !currentPost.getUserAvatar().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(currentPost.getUserAvatar())
                    .circleCrop()
                    .into(holder.imgAvatar);
        } else {
            // Nếu user chưa cài avatar, cho hiển thị cái nền tròn mặc định
            holder.imgAvatar.setImageResource(R.drawable.avatar_circle);
        }
        // ==========================================
        // 2. HIỂN THỊ MOOD CỦA BÀI VIẾT
        // ==========================================
        String mood = currentPost.getEmotion(); // Lấy chữ từ DB (VD: "HAPPY", "ANGRY")

        if (mood != null && !mood.trim().isEmpty()) {
            holder.imgMood.setVisibility(View.VISIBLE);

            // Đưa chữ vào hàm dịch để lấy icon tương ứng set lên giao diện
            holder.imgMood.setText(getMoodIcon(mood));
        } else {
            holder.imgMood.setVisibility(View.GONE);
        }
        // Lấy dữ liệu Location từ bài viết
        com.example.diary_app.data.model.Location location = currentPost.getLocation();

        // Kiểm tra xem bài viết này có lưu địa điểm hay không
        if (location != null && location.getAddress() != null && !location.getAddress().trim().isEmpty()) {

            // Có địa điểm -> Bật khung hiển thị lên
            holder.layoutLocation.setVisibility(View.VISIBLE);

            // Set tên địa điểm thực tế đè lên chữ "Da Lat"
            holder.tvLocation.setText(location.getAddress());

            // (Tuỳ chọn: Nếu bạn chỉ muốn hiện tên thành phố cho ngắn, có thể dùng location.getCity() thay thế)

        } else {
            // Không có địa điểm -> Tắt hẳn cái khung nền đi cho gọn giao diện
            holder.layoutLocation.setVisibility(View.GONE);
        }

        // long click trên post
        holder.itemView.setOnLongClickListener(v -> {

            if (listener != null) {
                listener.onPostLongClick(currentPost, holder.itemView.findViewById(R.id.tvUsername));
            }

            return true;
        });

        // double tap mở edit
        GestureDetector gestureDetector =
                new GestureDetector(
                        holder.itemView.getContext(),
                        new GestureDetector.SimpleOnGestureListener() {

                            @Override
                            public boolean onDoubleTap(MotionEvent e) {

                                if (listener != null) {
                                    listener.onMyPostDoubleTap(currentPost);
                                }

                                return true;
                            }
                        });


        holder.itemView.setOnTouchListener((v, event) -> {

            gestureDetector.onTouchEvent(event);

            return false;
        });
        // ==========================================
        // 3. XỬ LÝ GỬI BÌNH LUẬN -> CHUYỂN THÀNH TIN NHẮN (DM)
        // ==========================================
        if (holder.btnSendComment != null && holder.edtComment != null) {
            holder.btnSendComment.setOnClickListener(v -> {
                String commentText = holder.edtComment.getText().toString().trim();

                if (commentText.isEmpty()) return;

                // Khởi tạo AuthRepository ngay tại đây để lấy ID người đang lướt Feed
                String postOwnerId = currentPost.getUserId();
                String postImageUrl = currentPost.getImageUrl();

                // Chặn user tự nhắn tin cho chính mình
                if (myUid == null || myUid.equals(postOwnerId)) {
                    holder.edtComment.setText("");
                    android.widget.Toast.makeText(context, "Đây là bài viết của bạn mà!", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }

                // Vô hiệu hóa nút gửi tạm thời để tránh spam click
                holder.btnSendComment.setEnabled(false);

                // Tạo ID phòng chat chung
                String chatId = (myUid.compareTo(postOwnerId) < 0) ? myUid + "_" + postOwnerId : postOwnerId + "_" + myUid;

                // Đóng gói tin nhắn (Kèm theo link ảnh của bài viết hiện tại)
                com.example.diary_app.data.model.ChatMessage newMsg = new com.example.diary_app.data.model.ChatMessage(
                        "",
                        myUid,
                        commentText,
                        com.google.firebase.Timestamp.now(),
                        postImageUrl
                );

                // Gọi Repo để đẩy lên Firebase
                com.example.diary_app.repository.ChatRepository chatRepo = new com.example.diary_app.repository.ChatRepository();
                java.util.List<String> participants = java.util.Arrays.asList(myUid, postOwnerId);

                chatRepo.sendMessage(chatId, newMsg, participants)
                        .addOnSuccessListener(aVoid -> {
                            // Thành công: Xóa trắng ô nhập liệu và hiện thông báo
                            holder.edtComment.setText("");
                            holder.btnSendComment.setEnabled(true); // Mở khóa lại nút gửi
                            android.widget.Toast.makeText(context, "Đã gửi bình luận qua tin nhắn!", android.widget.Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            holder.btnSendComment.setEnabled(true);
                            android.widget.Toast.makeText(context, "Lỗi: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                        });
            });
        }

    }
    // Hàm hỗ trợ dịch từ chữ (lưu trên Firebase) sang Icon để hiển thị cho User
    private String getMoodIcon(String moodName) {
        if (moodName == null) return "😐"; // Mặc định nếu bị rỗng

        switch (moodName) {
            case "HAPPY": return "😊";
            case "SAD": return "😭";
            case "CALM": return "😌";
            case "ANGRY": return "😡";
            case "NEUTRAL": return "😳";
            default: return "😐";
        }
    }
    @Override
    public int getItemCount() {
        return postList != null ? postList.size() : 0;
    }

    // =========================================================
    // LỚP VIEWHOLDER: Tìm và lưu trữ ID của các View
    // =========================================================
    public static class FeedViewHolder extends RecyclerView.ViewHolder {

        TextView reactAngry, reactHappy, reactNeutral, reactSad, reactCalm;

        TextView tvUsername, tvCaption, tvDate;
        android.widget.ImageView imgPostContent;
        android.widget.ImageView imgAvatar;
        android.widget.TextView imgMood;
        LinearLayout layoutLocation;
        TextView tvLocation;
        android.widget.EditText edtComment;
        android.widget.ImageView btnSendComment;
        android.widget.HorizontalScrollView layoutReactionBar;
        LinearLayout layoutCommentBar;
        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);

            reactAngry = itemView.findViewById(R.id.reactAngry);
            reactHappy = itemView.findViewById(R.id.reactHappy);
            reactNeutral = itemView.findViewById(R.id.reactNeutral);
            reactSad = itemView.findViewById(R.id.reactSad);
            reactCalm = itemView.findViewById(R.id.reactCalm);

            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            imgPostContent = itemView.findViewById(R.id.imgDiary);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            imgMood = itemView.findViewById(R.id.imgMood);

            layoutLocation = itemView.findViewById(R.id.layoutLocation);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            edtComment = itemView.findViewById(R.id.etMessage);
            btnSendComment = itemView.findViewById(R.id.btnSend);
            layoutReactionBar = itemView.findViewById(R.id.layoutReactionBar);
            layoutCommentBar = itemView.findViewById(R.id.layoutCommentBar);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
