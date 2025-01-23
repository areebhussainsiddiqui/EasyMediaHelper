# EasyMediaHelper

EasyMediaHelper is a lightweight Android library designed to simplify access to media such as camera, video, and file browsing functionalities. This library handles permissions, media capture, and file browsing seamlessly.

## Installation

To use EasyMediaHelper in your project, add the following dependency to your `build.gradle` file:

```groovy
// Add JitPack repository to your root build.gradle (project-level)
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}



## Permissions

Add the following permissions to your `AndroidManifest.xml`:

```xml
<uses-feature
    android:name="android.hardware.camera"
    android:required="false" />

<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission
    android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
<uses-permission
    android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
```

## Configuration

### 1. `file_paths.xml`

Create a `file_paths.xml` file in the `res/xml` folder with the following content:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-files-path name="images" path="Pictures/images/" />
    <external-files-path name="videos" path="Pictures/videos/" />
</paths>
```

### 2. Provider Declaration

Add the following provider to your `AndroidManifest.xml`:

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## Usage

### Initialize EasyMediaHelper

Here is an example snippet demonstrating how to use EasyMediaHelper:

```java
public class MainActivity extends AppCompatActivity {

    private EasyMediaHelper mediaHelper;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mediaHelper = new EasyMediaHelper(this);
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        binding.btnCamera.setOnClickListener(v -> {
            mediaHelper.setImageWidth(400);
            mediaHelper.setImageHeight(400);
            mediaHelper.captureImage("MediaHelper_" + System.currentTimeMillis());
        });

        binding.btnVideo.setOnClickListener(v -> {
            mediaHelper.setVideoDuration(30);
            mediaHelper.captureVideo("MediaHelper_" + System.currentTimeMillis());
        });

        binding.btnBrowse.setOnClickListener(v -> {
            mediaHelper.setMaxFileSizeMB(10);
            mediaHelper.selectFile();
        });
    }
}
```

### Handling Results

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == Activity.RESULT_OK) {
        switch (requestCode) {
            case EasyMediaHelper.REQUEST_IMAGE_CAPTURE:
                handleImageCaptureResult(data);
                break;
            case EasyMediaHelper.REQUEST_VIDEO_CAPTURE:
                handleVideoCaptureResult(data);
                break;
            case EasyMediaHelper.REQUEST_BROWSE:
                handleFileBrowseResult(data);
                break;
        }
    }
}

private void handleImageCaptureResult(@Nullable Intent data) {
    String imagePath = mediaHelper.handleActivityResult(EasyMediaHelper.REQUEST_IMAGE_CAPTURE, Activity.RESULT_OK, data);
    Bitmap bitmap = mediaHelper.getBitmapFromPath(this, imagePath);
    binding.imageView.setImageBitmap(bitmap);
}

private void handleVideoCaptureResult(@Nullable Intent data) {
    String videoPath = mediaHelper.handleActivityResult(EasyMediaHelper.REQUEST_VIDEO_CAPTURE, Activity.RESULT_OK, data);
    Bitmap thumbnail = mediaHelper.getVideoThumbnail(videoPath);
    binding.videoThumbnail.setImageBitmap(thumbnail);
}

private void handleFileBrowseResult(@Nullable Intent data) {
    String filePath = mediaHelper.handleActivityResult(EasyMediaHelper.REQUEST_BROWSE, Activity.RESULT_OK, data);
    Log.d("MainActivity", "Selected file: " + filePath);
}
```

### Full Example
Find the full implementation of `MainActivity` [here](https://github.com/areebhussainsiddiqui/EasyMediaHelper/blob/main/app/src/main/java/com/ahs/easymediahelper/MainActivity.java).

## Contributions
Contributions are welcome! Feel free to open issues or submit pull requests to improve the library.

## License
This project is licensed under the [MIT License](LICENSE).

## Contributions
Contributions are welcome! Feel free to open issues or submit pull requests to improve the library.
Maintained by Areeb Hussain Siddiqui.
