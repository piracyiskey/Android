package org.convenient.rest_controller;

import org.convenient.dto.ProfilePicUpdateRequest;
import org.convenient.dto.ProfileUpdateRequest;
import org.convenient.models.User;
import org.convenient.repository.UserRepository;
import org.convenient.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URLConnection;
import java.time.Instant;
import java.util.Base64;

@RestController
@RequestMapping("/api/v1/user")
public class UpdateProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final S3Client s3Client = S3Client.builder()
            .region(Region.AP_SOUTHEAST_1)  // Set your region
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .build();

    private final String bucketName = "my-lambda-artifacts-s3-bucket";
    private final String profilePath = "images/profiles/";

    @PostMapping(value = "/update-profile-pic", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateProfilePic(@org.springframework.web.bind.annotation.RequestBody ProfilePicUpdateRequest request) {
        try {
            String email = request.getEmail();
            String base64Image = request.getBase64Image();

            if (email == null || base64Image == null || base64Image.isEmpty()) {
                return ResponseEntity.badRequest().body("Missing email or image data");
            }

            // Decode Base64 image
            byte[] imageBytes = Base64.getDecoder().decode(base64Image.split(",")[1]); // skip data:image/...;base64, prefix
            String fileName = "profile_" + Instant.now().toEpochMilli() + ".jpg";
            String key = profilePath + fileName;
            String imageUrl = "https://" + bucketName + ".s3.amazonaws.com/" + key;

            // Find user
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            // Delete old image if it exists
            String oldImageUrl = user.getProfile_pic();
            if (oldImageUrl != null && oldImageUrl.contains(bucketName)) {
                try {
                    String oldKey = oldImageUrl.substring(oldImageUrl.indexOf("images/profiles/"));
                    s3Client.deleteObject(builder -> builder
                            .bucket(bucketName)
                            .key(oldKey)
                    );
                } catch (Exception ex) {
                    System.err.println("Warning: Failed to delete old image from S3");
                    ex.printStackTrace(); // non-blocking
                }
            }

            // Upload new image to S3
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType("image/jpeg")
                            .build(),
                    RequestBody.fromBytes(imageBytes));

            user.setProfile_pic(imageUrl);
            userRepository.save(user);

            return ResponseEntity.ok(imageUrl);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed");
        }
    }

    @PostMapping(value = "/update-profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader,
                                           @org.springframework.web.bind.annotation.RequestBody ProfileUpdateRequest request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.isValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

            String tokenEmail = jwtUtil.extractEmail(token);
            if (!tokenEmail.equals(request.getEmail())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email mismatch");
            }

            User user = userRepository.findByEmail(request.getEmail());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            user.setFull_name(request.getFullName());
            user.setPhone(request.getPhone());
            userRepository.save(user);

            return ResponseEntity.ok("Profile updated");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Update failed");
        }
    }



}
