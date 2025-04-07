package com.ahs.easymediahelper;


import static com.ahs.easymediahelper.EasyMediaHelper.REQUEST_BROWSE;
import static com.ahs.easymediahelper.EasyMediaHelper.REQUEST_IMAGE_CAPTURE;
import static com.ahs.easymediahelper.EasyMediaHelper.REQUEST_VIDEO_CAPTURE;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ahs.easymediahelper.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // EasyMediaHelper instance for handling media-related tasks
    private EasyMediaHelper mediaHelper;

    // View binding instance for accessing views
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the EasyMediaHelper
        mediaHelper = new EasyMediaHelper(this);

        // Set up button click listeners
        setupButtonListeners();
    }

    /**
     * Sets up click listeners for the camera, video, and browse buttons.
     */
    private void setupButtonListeners() {
        // Capture Image Button
        binding.btnCamera.setOnClickListener(v -> {
            mediaHelper.setImageWidth(400); // Set desired image width
            mediaHelper.setImageHeight(400); // Set desired image height
            mediaHelper.captureImage("MediaHelper_" + System.currentTimeMillis()); // Capture image with unique name
        });

        // Capture Video Button
        binding.btnVideo.setOnClickListener(v -> {
            mediaHelper.setVideoDuration(30); // Set video duration to 30 seconds
            mediaHelper.captureVideo("MediaHelper_" + System.currentTimeMillis()); // Capture video with unique name
        });

        // Select File (Browse) Button
        binding.btnBrowse.setOnClickListener(v -> {
            mediaHelper.setMaxFileSizeMB(10); // Set maximum file size to 10 MB
            mediaHelper.selectFile(); // Launch file selector
        });
    }

    /**
     * Handles the result of an activity (e.g., capturing an image or video, or browsing files).
     *
     * @param requestCode The request code identifying the action.
     * @param resultCode  The result code indicating success or failure.
     * @param data        The Intent containing additional data.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            // Handle the result based on the request code
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    handleImageCaptureResult(data);
                    break;
                case REQUEST_VIDEO_CAPTURE:
                    handleVideoCaptureResult(data);
                    break;
                case REQUEST_BROWSE:
                    handleFileBrowseResult(data);
                    break;
                default:
                    Log.w("MainActivity", "Unhandled request code: " + requestCode);
                    break;
            }
        } else {
            Log.w("MainActivity", "Result not OK for request code: " + requestCode);
        }
    }

    /**
     * Handles the result of an image capture operation.
     *
     * @param data The Intent containing the captured image data.
     */
    private void handleImageCaptureResult(@Nullable Intent data) {
        String imagePath = mediaHelper.handleActivityResult(REQUEST_IMAGE_CAPTURE, Activity.RESULT_OK, data);
        Log.d("MainActivity", "Captured Image Path: " + imagePath);

        // Load and display the image in the ImageView
        Bitmap bitmap = mediaHelper.getBitmapFromPath(this, imagePath);
        if (bitmap != null) {
            binding.imageView.setImageBitmap(bitmap);
        } else {
            Log.e("MainActivity", "Failed to load captured image.");
        }
    }

    /**
     * Handles the result of a video capture operation.
     *
     * @param data The Intent containing the captured video data.
     */
    private void handleVideoCaptureResult(@Nullable Intent data) {
        String videoPath = mediaHelper.handleActivityResult(REQUEST_VIDEO_CAPTURE, Activity.RESULT_OK, data);
        Log.d("MainActivity", "Captured Video Path: " + videoPath);

        // Generate and display the video thumbnail
        Bitmap thumbnail = mediaHelper.getVideoThumbnail(videoPath);
        if (thumbnail != null) {
            binding.videoThumbnail.setImageBitmap(thumbnail);
        } else {
            Log.e("MainActivity", "Failed to generate video thumbnail.");
        }
    }

    /**
     * Handles the result of a file browsing operation.
     *
     * @param data The Intent containing the selected file data.
     */
    private void handleFileBrowseResult(@Nullable Intent data) {
        String filePath = mediaHelper.handleActivityResult(REQUEST_BROWSE, Activity.RESULT_OK, data);
        Log.d("MainActivity", "Browsed File Path: " + filePath);

        // Additional handling for the selected file (if needed)
        if (filePath != null) {
            Log.d("MainActivity", "File successfully selected: " + filePath);
        } else {
            Log.e("MainActivity", "Failed to retrieve file path.");
        }
    }
}
