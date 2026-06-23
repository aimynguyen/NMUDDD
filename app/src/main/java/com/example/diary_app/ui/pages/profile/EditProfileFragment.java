package com.example.diary_app.ui.pages.profile;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.app.AlertDialog;

import java.util.Calendar;

import com.bumptech.glide.Glide;
import com.example.diary_app.Helpers.imageHelper;
import com.example.diary_app.R;
import com.example.diary_app.data.model.User;
import com.example.diary_app.viewmodel.EditProfileViewModel;

public class EditProfileFragment extends Fragment {

    private ImageView imgAvatar;
    private ImageView btnCamera;
    private EditText edtName;
    private TextView tvBirthday;
    private EditText edtEmail;
    private Button btnFinish;
    private TextView tvChangePassword;

    private EditProfileViewModel viewModel;

    // old data
    private String oldName = "";
    private String oldBirthday = "";
    private String oldAvatar = "";

    private ActivityResultLauncher<Intent> galleryLauncher;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            imgAvatar.setImageURI(imageUri);
                            try {
                                Bitmap bitmap = imageHelper.uriToBitmap(requireContext(), imageUri);
                                if (bitmap != null) {
                                    byte[] data = imageHelper.compressBitmap(bitmap, 50);
                                    viewModel.uploadAvatar(data, oldAvatar);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Inflate layout cho Fragment
        View view = inflater.inflate(R.layout.layout_editprofile, container, false);

        // 2. Tìm view từ object 'view' vừa inflate
        imgAvatar = view.findViewById(R.id.imgAvatar);
        btnCamera = view.findViewById(R.id.btnCamera);
        edtName = view.findViewById(R.id.edtName);
        tvBirthday = view.findViewById(R.id.tvBirthday);
        edtEmail = view.findViewById(R.id.edtEmail);
        btnFinish = view.findViewById(R.id.btnFinish);
        tvChangePassword = view.findViewById(R.id.tvChangePassword);

        // 3. Khởi tạo VIEWMODEL gắn liền với Fragment
        viewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);

        // 4. Load profile
        viewModel.loadProfile();

        // 5. Observe dữ liệu với LifecycleOwner của Fragment
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                loadUserData(user);
            }
        });

        // Lắng nghe Loading để khóa nút
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnFinish.setEnabled(!isLoading);
            btnFinish.setText(isLoading ? "Saving..." : "Finish");
        });

        // Observe message thông báo
        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            // Nếu thành công thì tự động đóng màn hình Edit, quay lại trang Profile
            if (message.equals("Cập nhật thành công!")) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Xử lý sự kiện click đổi ngày sinh
        tvBirthday.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (!oldBirthday.isEmpty()) {
                try {
                    String[] parts = oldBirthday.split("/");
                    int day = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]) - 1;
                    int year = Integer.parseInt(parts[2]);
                    calendar.set(year, month, day);
                } catch (Exception ignored) {}
            }
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                        tvBirthday.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Xử lý sự kiện click đổi mật khẩu
        tvChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Xử lý sự kiện click btnCamera
        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        // 6. Xử lý sự kiện click button finish
        btnFinish.setOnClickListener(v -> {
            String newName = edtName.getText().toString().trim();
            String newBirthday = tvBirthday.getText().toString().trim();

            // Validate dữ liệu đầu vào
            if (newName.isEmpty()) {
                edtName.setError("Name cannot be empty");
                return;
            }

            // Gọi hàm update trong ViewModel
            viewModel.updateProfile(
                    oldName,
                    oldBirthday,
                    newName,
                    newBirthday
            );
        });

        return view;
    }

    private void loadUserData(User user) {
        // Lưu lại dữ liệu cũ
        oldName = user.getUserName() != null ? user.getUserName() : "";
        oldBirthday = user.getBirthday() != null ? user.getBirthday() : "";
        oldAvatar = user.getAvatarUrl() != null ? user.getAvatarUrl() : "";

        // Set text lên các ô nhập liệu
        edtName.setText(oldName);
        tvBirthday.setText(oldBirthday);
        edtEmail.setText(user.getEmail());

        // Load ảnh đại diện bằng Glide (Dùng requireContext() thay vì 'this')
        Glide.with(requireContext())
                .load(user.getAvatarUrl())
                .placeholder(R.drawable.human_human)
                .circleCrop()
                .into(imgAvatar);
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_change_password, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText edtCurrentPassword = dialogView.findViewById(R.id.edtCurrentPassword);
        EditText edtNewPassword = dialogView.findViewById(R.id.edtNewPassword);
        EditText edtConfirmNewPassword = dialogView.findViewById(R.id.edtConfirmNewPassword);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelChangePassword);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmChangePassword);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String currentPw = edtCurrentPassword.getText().toString().trim();
            String newPw = edtNewPassword.getText().toString().trim();
            String confirmPw = edtConfirmNewPassword.getText().toString().trim();

            if (currentPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPw.length() < 6) {
                Toast.makeText(requireContext(), "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPw.equals(confirmPw)) {
                Toast.makeText(requireContext(), "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.changePassword(currentPw, newPw);
        });

        viewModel.getChangePasswordSuccess().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        viewModel.getChangePasswordError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}