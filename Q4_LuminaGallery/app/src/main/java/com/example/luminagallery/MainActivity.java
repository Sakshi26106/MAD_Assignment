package com.example.luminagallery;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private File photoFile;
    private Uri photoUri;

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success) {
                    saveImageToGallery();
                    Toast.makeText(this, "Photo saved to Gallery", Toast.LENGTH_SHORT).show();
                } else {
                    if (photoFile != null && photoFile.exists()) {
                        photoFile.delete();
                    }
                    Toast.makeText(this, "Photo capture failed or cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean granted : result.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Permissions required to take photo", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnTakePhoto = findViewById(R.id.btn_take_photo);
        Button btnOpenGallery = findViewById(R.id.btn_open_gallery);

        btnTakePhoto.setOnClickListener(v -> checkPermissionsAndOpenFile());
        btnOpenGallery.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
            startActivity(intent);
        });
    }

    private void checkPermissionsAndOpenFile() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);
        
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            requestPermissionLauncher.launch(listPermissionsNeeded.toArray(new String[0]));
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            // Use app-specific directory for the temporary file - always works
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            photoFile = File.createTempFile(imageFileName, ".jpg", storageDir);

            photoUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    photoFile);

            takePictureLauncher.launch(photoUri);
        } catch (Exception e) {
            Toast.makeText(this, "Error starting camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageToGallery() {
        if (photoFile == null || !photoFile.exists()) return;

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, photoFile.getName());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LuminaGallery");
        }

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri);
                 FileInputStream inputStream = new FileInputStream(photoFile)) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                // Once saved to MediaStore, we can delete the temp file
                photoFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
