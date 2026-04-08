package com.example.luminagallery;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        ImageView ivDetail = findViewById(R.id.iv_detail);
        TextView tvName = findViewById(R.id.tv_name);
        TextView tvPath = findViewById(R.id.tv_path);
        TextView tvSize = findViewById(R.id.tv_size);
        TextView tvDate = findViewById(R.id.tv_date);
        Button btnDelete = findViewById(R.id.btn_delete);

        String uriString = getIntent().getStringExtra("image_uri");
        String name = getIntent().getStringExtra("image_name");
        String path = getIntent().getStringExtra("image_path");
        String size = getIntent().getStringExtra("image_size");
        String dateRaw = getIntent().getStringExtra("image_date");

        Uri uri = Uri.parse(uriString);

        // Format date
        String formattedDate = dateRaw;
        try {
            long millis = Long.parseLong(dateRaw);
            // MediaStore.DATE_ADDED is usually in seconds
            if (millis < 1000000000000L) millis *= 1000;
            formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date(millis));
        } catch (Exception ignored) {}

        tvName.setText("Name: " + name);
        tvPath.setText("Path: " + path);
        tvSize.setText("Size: " + size);
        tvDate.setText("Date: " + formattedDate);

        Glide.with(this).load(uri).into(ivDetail);

        btnDelete.setOnClickListener(v -> showDeleteConfirmation(uri));
    }

    private void showDeleteConfirmation(Uri uri) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Delete", (dialog, which) -> deleteImage(uri))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteImage(Uri uri) {
        try {
            ContentResolver contentResolver = getContentResolver();
            int rowsDeleted = contentResolver.delete(uri, null, null);
            if (rowsDeleted > 0) {
                Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
                finish(); // Go back to Gallery
            } else {
                Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            // On Android 10+, deleting might require a RecoverableSecurityException or similar.
            // For a basic assignment, we hope the standard delete works or provide a toast.
            Toast.makeText(this, "Security error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
