package com.example.luminagallery;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private GridView gridView;
    private GalleryAdapter adapter;
    private final List<ImageModel> imageList = new ArrayList<>();

    private final ActivityResultLauncher<Uri> openDocumentTreeLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), uri -> {
                if (uri != null) {
                    loadImagesFromFolder(uri);
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadAllImages();
                } else {
                    Toast.makeText(this, "Permission denied to read images", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        gridView = findViewById(R.id.grid_view);

        adapter = new GalleryAdapter(this, imageList);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            ImageModel selectedImage = imageList.get(position);
            Intent intent = new Intent(GalleryActivity.this, ImageDetailActivity.class);
            intent.putExtra("image_uri", selectedImage.getUri().toString());
            intent.putExtra("image_name", selectedImage.getName());
            intent.putExtra("image_path", selectedImage.getPath());
            intent.putExtra("image_size", selectedImage.getSize());
            intent.putExtra("image_date", selectedImage.getDate());
            startActivity(intent);
        });

        checkStoragePermission();
    }

    private void checkStoragePermission() {
        String permission;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permission = android.Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = android.Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (androidx.core.content.ContextCompat.checkSelfPermission(this, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(permission);
        } else {
            loadAllImages();
        }
    }

    private void loadAllImages() {
        imageList.clear();
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_ADDED
        };

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        try (Cursor cursor = getContentResolver().query(
                collection,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED + " DESC"
        )) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                int dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    long size = cursor.getLong(sizeColumn);
                    long date = cursor.getLong(dateColumn);

                    Uri contentUri = android.content.ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    
                    imageList.add(new ImageModel(contentUri, name, "Gallery Path", size + " bytes", String.valueOf(date)));
                }
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading images: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        
        if (imageList.isEmpty()) {
            Toast.makeText(this, "Gallery is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadImagesFromFolder(Uri folderUri) {
        imageList.clear();
        DocumentFile root = DocumentFile.fromTreeUri(this, folderUri);
        if (root != null) {
            for (DocumentFile file : root.listFiles()) {
                if (file.isFile() && file.getType() != null && file.getType().startsWith("image/")) {
                    imageList.add(new ImageModel(
                            file.getUri(),
                            file.getName(),
                            file.getUri().getPath(),
                            file.length() + " bytes",
                            String.valueOf(file.lastModified())
                    ));
                }
            }
        }
        adapter.notifyDataSetChanged();
        if (imageList.isEmpty()) {
            Toast.makeText(this, "No images found in this folder", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning to this activity
        loadAllImages();
    }
}
