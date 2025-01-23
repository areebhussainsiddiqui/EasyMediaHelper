package com.ahs.easymediahelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EasyMediaHelper {

    // Constants
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_VIDEO_CAPTURE = 2;
    public static final int REQUEST_BROWSE = 3;

    private int imageWidith = 800;
    private int imageHeight = 600;
    private int imageQuality = 80;
    private  int videoDuration = 60; // Seconds
    private static String  folderName = "MediaHelper";

    // Variables
    private final Context context;
    private final Activity activity;
    private int maxFileSizeMB = 10;

    private Uri imageUri;
    private Uri videoUri;
    private String fileName;

    public EasyMediaHelper(Activity activity) {
        this.context = activity.getApplicationContext();
        this.activity = activity;
    }

    /**
     * Launches an intent to capture a video.
     */
    public void captureVideo(String fileName) {
        this.fileName = sanitizeFileName(fileName);
        videoUri = createVideoUri();
        //createMediaUri("videos", generateFileName("VID_", ".mp4"));

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, getVideoDuration());
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); // Low quality
        activity.startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
    }

    /**
     * Launches an intent to capture an image.
     */
    public void captureImage(String fileName) {
        this.fileName = sanitizeFileName(fileName);
        imageUri = createImageUri(); //createMediaUri("images", generateFileName("IMG_", ".jpg"));

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private Uri createImageUri() {
        return getFileUri("images", generateImageFileName());
    }

    // Create a Uri for saving the video in a custom directory
    private Uri createVideoUri() {
        return getFileUri("videos", generateVideoFileName());
    }

    // Helper method to create Uri for media (image or video) in app-specific directory
    private Uri getFileUri(String subDir, String fileName) {
        File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), subDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File mediaFile = new File(directory, fileName);
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", mediaFile);
    }
    private String generateImageFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return "IMG_" + timeStamp + ".jpg";
    }

    private String generateVideoFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return "VID_" + timeStamp + ".mp4";
    }

    /**
     * Launches a file selection intent.
     */
    public void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "image/*", "video/*", "application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "text/plain"
        });
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_BROWSE);
    }

    /**
     * Handles the result from an activity.
     */
    public String handleActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) return null;

        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                return processCapturedImage();
            case REQUEST_VIDEO_CAPTURE:
                return processCapturedVideo();
            case REQUEST_BROWSE:
                return processSelectedFile(data);
            default:
                return null;
        }
    }

    private String processCapturedImage() {
        if (imageUri != null) {
            return saveCompressedImage(imageUri, getImageWidth(), getImageHeight(), getImageQuality());
        }
        return null;
    }

    private String processCapturedVideo() {
        if (videoUri != null) {
            return saveMedia(videoUri, "videos");
        }
        return null;
    }

  /*  private String processSelectedFile(Intent data) {
        if (data == null || data.getData() == null) return null;

        Uri fileUri = data.getData();
        try {
            long fileSize = getFileSize(fileUri);
            if (fileSize > maxFileSizeMB * 1024 * 1024) {
                Toast.makeText(context, "File size exceeds " + maxFileSizeMB + " MB.", Toast.LENGTH_LONG).show();
                return null;
            }
            return saveMedia(fileUri, "selected_files");
        } catch (IOException e) {
            Log.e("MediaHelper", "Error processing selected file", e);
        }
        return null;
    }*/

    private String saveCompressedImage(Uri imageUri, int width, int height, int quality) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

            File imageFile = new File(createMediaDirectory("images"), generateFileName(fileName, ".jpg"));
            try (FileOutputStream out = new FileOutputStream(imageFile)) {
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
                return imageFile.getAbsolutePath();
            }
        } catch (IOException e) {
            Log.e("MediaHelper", "Error saving compressed image", e);
        }
        return null;
    }

    public String processSelectedFile(Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                Cursor cursor = null;
                try {
                    // Query the content resolver for the file metadata
                    cursor = activity.getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        // Get the display name (file name) from the metadata
                        String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        Log.i("TAG", "Display Name: " + displayName);

                        // Get the file size from the metadata
                        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                        long fileSize = cursor.isNull(sizeIndex) ? -1 : cursor.getLong(sizeIndex);
                        Log.d("TAG", "File Size: " + (fileSize != -1 ? fileSize + " bytes" : "Unknown"));

                        // Check if the file size exceeds the maximum allowed size
                        if (fileSize > maxFileSizeMB * 1024 * 1024) { // Convert MB to bytes
                            Toast.makeText(context, "File size exceeds " + maxFileSizeMB + " MB limit. Please select a smaller file.", Toast.LENGTH_LONG).show();
                            return "";
                        }

                        // Save the file to the app's local storage and return the path
                        return saveFileFromUri(context, uri, displayName, (int) fileSize);
                    }
                } catch (Exception e) {
                    Log.e("TAG", "Error reading file metadata: " + e.getMessage(), e);
                } finally {
                    // Close the cursor to release resources
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
        return "";
    }

    public static String saveFileFromUri(Context context, Uri uri, String outputFileName, int size) {
        String path = "";
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            // Open an InputStream from the Uri
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), folderName);
                File imageFolder = null;
                if (!directory.exists()) {
                    directory.mkdir();
                    imageFolder = new File(directory, "images");
                    if (!imageFolder.exists()) {
                        imageFolder.mkdir();
                    }
                } else {
                    imageFolder = new File(directory, "images");
                    if (!imageFolder.exists()) {
                        imageFolder.mkdir();
                    }
                }
                File outputFile = null;
                if (hasExtension(outputFileName)) {
                    outputFile = new File(imageFolder, outputFileName);
                } else {
                    outputFile = new File(imageFolder, outputFileName + "" + System.currentTimeMillis());
                    // Create directories if they do not exist
                    outputFile.getParentFile().mkdirs();
                }
                // Open an OutputStream to the output file
                outputStream = new FileOutputStream(outputFile);
                // Read from the InputStream and write to the OutputStream
                byte[] buffer = new byte[size];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                path = outputFile.getAbsolutePath();
                Log.d("FileUtils", "File saved successfully at: " + outputFile.getAbsolutePath());
            } else {
                Log.e("FileUtils", "Failed to open InputStream from Uri");
            }
        } catch (Exception e) {
            Log.e("FileUtils", "Error saving file: " + e.getMessage());
        } finally {
            // Close InputStream and OutputStream
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                Log.e("FileUtils", "Error closing streams: " + e.getMessage());
            }
        }
        return path;
    }
    public static boolean hasExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 && dotIndex < fileName.length() - 1;
    }

    private String saveMedia(Uri mediaUri, String subDir) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(mediaUri)) {
            File mediaFile = new File(createMediaDirectory(subDir), generateFileName(fileName, ""));
            try (OutputStream outputStream = new FileOutputStream(mediaFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                return mediaFile.getAbsolutePath();
            }
        } catch (IOException e) {
            Log.e("MediaHelper", "Error saving media", e);
        }
        return null;
    }

    public String getFolderName() {
        return folderName;
    }
    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public int getImageWidth() {
        return imageWidith;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidith = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public int getImageQuality() {
        return imageQuality;
    }

    public void setImageQuality(int imageQuality) {
        this.imageQuality = imageQuality;
    }

    public int getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(int videoDuration) {
        this.videoDuration = videoDuration;
    }

    private File createMediaDirectory(String subDir) {
        File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), folderName + "/" + subDir);
        if (!directory.exists()) directory.mkdirs();
        return directory;
    }

    private Uri createMediaUri(String subDir, String fileName) {
        File mediaFile = new File(createMediaDirectory(subDir), fileName);
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", mediaFile);
    }

    private long getFileSize(Uri uri) throws IOException {
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                return cursor.getLong(sizeIndex);
            }
        }
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            return inputStream.available();
        }
    }

    private String generateFileName(String prefix, String extension) {
        return prefix + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + extension;
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }

    public void setMaxFileSizeMB(int maxFileSizeMB) {
        this.maxFileSizeMB = maxFileSizeMB;
    }
    /**
     * Retrieves a Bitmap from the specified file path.
     * Corrects the orientation if required.
     *
     * @param context  The application context.
     * @param filePath The absolute path of the image file.
     * @return A Bitmap object or null if the file doesn't exist.
     */
    public static Bitmap getBitmapFromPath(Context context, String filePath) {
        File imgFile = new File(filePath);

        // Check if the file exists
        if (imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            try {
                // Correct the image orientation
                bitmap = correctImageOrientation(context, bitmap, Uri.fromFile(imgFile));
            } catch (IOException e) {
                Log.e("MediaHelper", "Error correcting image orientation", e);
            }
            return bitmap;
        } else {
            Log.e("MediaHelper", "File not found: " + filePath);
            return null;
        }
    }

    /**
     * Generates a thumbnail for the specified video file.
     *
     * @param videoPath The absolute path of the video file.
     * @return A Bitmap containing the video thumbnail, or null if generation fails.
     */
    public static Bitmap getVideoThumbnail(String videoPath) {
        return ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MINI_KIND);
    }

    /**
     * Saves a Bitmap to a file in the app's external pictures directory.
     *
     * @param context  The application context.
     * @param bitmap   The Bitmap to save.
     * @param fileName The desired file name (without extension).
     * @return The absolute path of the saved file, or null if saving fails.
     */
    public static String saveBitmapToFile(Context context, Bitmap bitmap, String fileName, int imageQuality) {
        FileOutputStream fos = null;
        try {
            // Create the directory if it doesn't exist
            File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), folderName + "/images");
            if (!directory.exists() && !directory.mkdirs()) {
                Log.e("MediaHelper", "Failed to create directories: " + directory.getAbsolutePath());
                return null;
            }

            // Sanitize the file name and add a timestamp
            fileName = fileName.replaceAll("[/\\s]", "_"); // Replace invalid characters
            String generatedFileName = fileName + "_" + System.currentTimeMillis() + ".png";
            File imageFile = new File(directory, generatedFileName);

            // Save the Bitmap to the file
            fos = new FileOutputStream(imageFile);
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, imageQuality, fos)) {
                Log.e("MediaHelper", "Failed to compress bitmap");
                return null;
            }
            fos.flush();

            // Return the file path of the saved image
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e("MediaHelper", "Error saving bitmap to file", e);
        } finally {
            // Close the FileOutputStream
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e("MediaHelper", "Error closing FileOutputStream", e);
                }
            }
        }
        return null;
    }


    /**
     * Corrects the orientation of an image based on EXIF data.
     * (Implement this function as per your application's logic.)
     *
     * @param context   The application context.
     * @param bitmap    The original Bitmap.
     * @param imageUri  The Uri of the image file.
     * @return The corrected Bitmap.
     * @throws IOException If reading EXIF data fails.
     */
    public static Bitmap correctImageOrientation(Context context, Bitmap bitmap, Uri imageUri) throws IOException {
        // Dummy implementation: Replace with actual orientation correction logic.
        return bitmap;
    }
}
