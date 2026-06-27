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

        // --- A. HIỂN THỊ DỮ LIỆU BÀI VIẾT ---
        holder.tvUsername.setText(currentPost.getUserName());
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

        TextView tvUsername, tvCaption;
        android.widget.ImageView imgPostContent;
        android.widget.ImageView imgAvatar;
        android.widget.TextView imgMood;
        LinearLayout layoutLocation;
        TextView tvLocation;
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
        }
    }
}
