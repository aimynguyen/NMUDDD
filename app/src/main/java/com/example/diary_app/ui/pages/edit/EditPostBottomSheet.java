package com.example.diary_app.ui.pages.edit;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.diary_app.R;
import com.example.diary_app.adapter.ReactionAdapter;
import com.example.diary_app.data.model.Post;
import com.example.diary_app.model.ReactionItem;
import com.example.diary_app.repository.PostRepository;
import com.example.diary_app.repository.UserRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


// Fragment dùng để chỉnh sửa bài viết trong BottomSheet
public class EditPostBottomSheet extends BottomSheetDialogFragment {

    // Key dùng để truyền postId qua Bundle
    private static final String ARG_POST_ID = "post_id";

    // ID bài viết cần chỉnh sửa
    private String postId;

    // Danh sách reaction hiển thị trong RecyclerView
    private ArrayList<ReactionItem> reactionList;

    // Các view trong layout
    private ImageView imgPreview;
    private ImageView btnBack;
    private ImageView imgArrow;

    private EditText edtCaption;

    private Button btnSave;

    private LinearLayout btnPrivate;
    private LinearLayout btnPublic;

    private LinearLayout layoutReactionHeader;

    // Danh sách reaction
    private RecyclerView rvReaction;

    private TextView txtReactionTitle;

    // Repository thao tác với dữ liệu bài viết và user
    private PostRepository postRepository;
    private UserRepository userRepository;

    private Post currentPost;

    private boolean isPublic = true;

    public EditPostBottomSheet() {
    }

    // Tạo instance BottomSheet và truyền postId và
    public static EditPostBottomSheet newInstance(String postId) {

        EditPostBottomSheet fragment = new EditPostBottomSheet();

        Bundle bundle = new Bundle();
        bundle.putString(ARG_POST_ID, postId);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        // Inflate giao diện BottomSheet
        View view = inflater.inflate(
                R.layout.fragment_detail,
                container,
                false);

        if (getArguments() != null)
            postId = getArguments().getString(ARG_POST_ID);

        postRepository = new PostRepository();
        userRepository = new UserRepository();

        // Ánh xạ view
        initView(view);

        // Load dữ liệu bài viết
        loadPost();

        return view;
    }

    // Ánh xạ các thành phần giao diện
    private void initView(View view) {

        imgPreview = view.findViewById(R.id.imgPreview);
        btnBack = view.findViewById(R.id.btnBack);

        imgArrow = view.findViewById(R.id.imgArrow);

        edtCaption = view.findViewById(R.id.edtCaption);

        btnSave = view.findViewById(R.id.btnSave);

        btnPrivate = view.findViewById(R.id.btnPrivate);
        btnPublic = view.findViewById(R.id.btnPublic);

        layoutReactionHeader =
                view.findViewById(R.id.layoutReactionHeader);

        rvReaction = view.findViewById(R.id.rvReactions);

        txtReactionTitle =
                view.findViewById(R.id.txtReactionTitle);

        rvReaction.setLayoutManager(
                new LinearLayoutManager(getContext()));

        btnBack.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {

            savePost();

        });

        // Mở / đóng danh sách reaction
        layoutReactionHeader.setOnClickListener(v -> {

            if(rvReaction.getVisibility() == View.GONE){

                rvReaction.setVisibility(View.VISIBLE);
                imgArrow.setRotation(180);

            }else{

                rvReaction.setVisibility(View.GONE);
                imgArrow.setRotation(0);

            }

        });

        // thay đổi quyền riêng tư / công cộng của post
        btnPrivate.setOnClickListener(v -> {

            isPublic = false;

            updatePrivacyUI();

        });


        btnPublic.setOnClickListener(v -> {

            isPublic = true;

            updatePrivacyUI();

        });
    }


    // Lấy dữ liệu post
    private void loadPost() {

        if (TextUtils.isEmpty(postId))
            return;

        postRepository.getPost(postId)

                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists())
                        return;

                    currentPost = snapshot.toObject(Post.class);

                    if (currentPost == null)
                        return;

                    // Hiển thị dữ liệu lên UI
                    bindPost();
                });

    }


    // Đổ dữ liệu post lên giao diện
    private void bindPost() {

        // Load của post
        Glide.with(requireContext())
                .load(currentPost.getImageUrl())
                .into(imgPreview);

        // Hiện caption cũ
        edtCaption.setText(currentPost.getCaption());

        // Hiện số reaction
        txtReactionTitle.setText(
                "Reactions (" +
                        (currentPost.getReactions() == null ?
                                0 :
                                currentPost.getReactions().size())
                        + ")"
        );

        // Lấy trạng thái privacy
        isPublic =
                "public".equalsIgnoreCase(currentPost.getPrivacy());

        loadReactions();

        updatePrivacyUI();

    }

    // Load danh sách người reaction
    private void loadReactions(){

        reactionList = new ArrayList<>();

        if(currentPost.getReactions() == null)
            return;


        // Duyệt qua từng reaction
        // key = uid người reaction
        // value = loại reaction
        for(Map.Entry<String,String> entry
                : currentPost.getReactions().entrySet()){


            String uid = entry.getKey();
            String reaction = entry.getValue();


            userRepository.getUserProfile(uid)
                    .addOnSuccessListener(snapshot -> {

                        String username = "Người dùng";
                        String avatar = null;


                        if(snapshot.exists()){
                            com.example.diary_app.data.model.User user =
                                    snapshot.toObject(
                                            com.example.diary_app.data.model.User.class
                                    );


                            if(user != null){
                                // Lấy username
                                if(user.getUserName()!=null)
                                    username = user.getUserName();

                                // Lấy avatar
                                avatar = user.getAvatarUrl();
                            }
                        }


                        reactionList.add(
                                new ReactionItem(
                                        uid,
                                        username,
                                        avatar,
                                        reaction == null
                                                ? "NEUTRAL"
                                                : reaction.toUpperCase()
                                )
                        );

                        rvReaction.getAdapter().notifyDataSetChanged();

                    });

        }


        ReactionAdapter adapter =
                new ReactionAdapter(
                        reactionList
                );

        rvReaction.setAdapter(adapter);
    }


    // Cập nhật UI privacy
    private void updatePrivacyUI() {

        if (isPublic) {

            btnPublic.setAlpha(1f);
            btnPrivate.setAlpha(0.4f);

        } else {

            btnPublic.setAlpha(0.4f);
            btnPrivate.setAlpha(1f);

        }

    }

    // Lưu thay đổi bài viết
    private void savePost(){

        String caption =
                edtCaption.getText()
                        .toString()
                        .trim();


        String privacy =
                isPublic ? "public" : "private";


        Map<String,Object> updates =
                new HashMap<>();

        updates.put("caption", caption);
        updates.put("privacy", privacy);


        postRepository
                .updatePost(postId, updates)
                .addOnSuccessListener(unused -> {


                    Bundle result = new Bundle();
                    result.putBoolean("refresh", true);


                    getParentFragmentManager()
                            .setFragmentResult(
                                    "post_updated",
                                    result
                            );


                    dismiss();

                });

    }

    // Cấu hình kích thước BottomSheet khi mở
    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null) {

            View bottomSheet =
                    getDialog()
                            .findViewById(com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet != null) {

                bottomSheet.getLayoutParams().height =
                        ViewGroup.LayoutParams.MATCH_PARENT;

                bottomSheet.requestLayout();
            }
        }
    }

}