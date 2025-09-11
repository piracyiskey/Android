package com.example.convenient.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.convenient.R;
import com.example.convenient.Utils.FileUtils;
import com.example.convenient.Utils.HttpClientInstance;
import com.example.convenient.Utils.SharedPrefManager;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.*;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;
    private static final String PROFILE_URL = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/user/me";
    private static final String UPLOAD_URL = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/user/update-profile-pic";

    private TextView txtEmail, txtFullName;
    private LinearLayout btnLogout, btnProfile, btnFavorites, btnAddress, btnHistory, btnNotifications;
    private ImageView imgProfilePic, btnEditProfilePic;
    private SharedPrefManager prefManager;
    private String currentEmail;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        txtEmail = view.findViewById(R.id.txtEmail);
        txtFullName = view.findViewById(R.id.txtFullName);
        btnLogout = view.findViewById(R.id.btnLogout);
        imgProfilePic = view.findViewById(R.id.imgProfilePic);
        btnEditProfilePic = view.findViewById(R.id.btnEditProfilePic); // Make sure this ID exists in your XML

        btnProfile = view.findViewById(R.id.btnProfile);
        btnFavorites = view.findViewById(R.id.btnFavorites);
        btnAddress = view.findViewById(R.id.btnAddress);
        btnHistory = view.findViewById(R.id.btnHistory);
        btnNotifications = view.findViewById(R.id.btnNotifications);

        // Set click listeners for buttons
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProfileActivity.class);
            startActivity(intent);
            requireActivity().overridePendingTransition(R.transition.slide_in_right, R.transition.slide_out_left);
        });

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), OrderActivity.class);
            startActivity(intent);
            requireActivity().overridePendingTransition(R.transition.slide_in_right, R.transition.slide_out_left);
        });


        btnFavorites.setOnClickListener(v -> {
        });

        prefManager = new SharedPrefManager(requireContext());

        fetchUserProfile();

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setMessage("Bạn có muốn đăng xuất?")
                    .setPositiveButton("Có", (dialog, id) -> {
                        prefManager.clearToken();
                        startActivity(new Intent(requireActivity(), LoginActivity.class));
                        requireActivity().finish();
                    })
                    .setNegativeButton("Hủy", (dialog, id) -> dialog.dismiss())
                    .show();
        });

        btnEditProfilePic.setOnClickListener(v -> openImagePicker());

        return view;
    }

    private void fetchUserProfile() {
        String token = prefManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
            return;
        }

        Request request = new Request.Builder()
                .url(PROFILE_URL)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        HttpClientInstance.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Phiên đăng nhập không hợp lệ", Toast.LENGTH_SHORT).show();
                        prefManager.clearToken();
                        startActivity(new Intent(requireActivity(), LoginActivity.class));
                        requireActivity().finish();
                    });
                    return;
                }

                String json = response.body().string();
                requireActivity().runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(json);
                        String email = obj.getString("email");
                        String fullName = obj.getString("full_name");
                        String profilePicUrl = obj.getString("profile_pic");

                        currentEmail = email;
                        txtEmail.setText(email);
                        txtFullName.setText(fullName);

                        Glide.with(requireContext())
                                .load(profilePicUrl)
                                .transform(new CircleCrop())
                                .placeholder(R.drawable.default_profile_pic)
                                .into(imgProfilePic);

                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Lỗi khi phân tích phản hồi", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void openImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES},
                        STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                launchImagePicker();
            }
        } else {
            // For older versions (not likely needed on API 35, but for completeness)
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                launchImagePicker();
            }
        }
    }

    private void launchImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                uploadImageToServer(selectedImageUri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchImagePicker();
            } else {
                Toast.makeText(requireContext(), "Không có quyền truy cập ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void uploadImageToServer(Uri imageUri) {
        try {
            String filePath = FileUtils.getPath(requireContext(), imageUri);
            if (filePath == null) {
                Toast.makeText(requireContext(), "Không thể truy cập ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            File file = new File(filePath);
            byte[] fileBytes = FileUtils.readFileToByteArray(file);
            String base64Image = "data:image/jpeg;base64," + android.util.Base64.encodeToString(fileBytes, android.util.Base64.NO_WRAP);

            JSONObject json = new JSONObject();
            json.put("email", currentEmail);
            json.put("base64Image", base64Image);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(UPLOAD_URL)
                    .post(body)
                    .build();

            HttpClientInstance.getInstance().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Tải ảnh thất bại", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String imageUrl = response.body().string();
                    requireActivity().runOnUiThread(() -> {
                        Glide.with(requireContext())
                                .load(imageUrl)
                                .transform(new CircleCrop())
                                .into(imgProfilePic);
                        Toast.makeText(requireContext(), "Ảnh đại diện đã cập nhật", Toast.LENGTH_SHORT).show();
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Lỗi khi chọn ảnh", Toast.LENGTH_SHORT).show();
        }
    }
}
