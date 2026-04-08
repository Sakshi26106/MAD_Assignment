package com.example.mediaplayerapp;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button openFileBtn, openUrlBtn, playBtn, pauseBtn, stopBtn, restartBtn;
    EditText urlInput;
    VideoView videoView;
    MediaPlayer mediaPlayer;
    Uri audioUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openFileBtn = findViewById(R.id.openFileBtn);
        openUrlBtn = findViewById(R.id.openUrlBtn);
        playBtn = findViewById(R.id.playBtn);
        pauseBtn = findViewById(R.id.pauseBtn);
        stopBtn = findViewById(R.id.stopBtn);
        restartBtn = findViewById(R.id.restartBtn);
        urlInput = findViewById(R.id.urlInput);
        videoView = findViewById(R.id.videoView);

        // 🎵 OPEN AUDIO FILE
        openFileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1);
        });

        // 🎬 OPEN VIDEO URL
        openUrlBtn.setOnClickListener(v -> {
            String url = urlInput.getText().toString().trim();

            if (!url.isEmpty()) {
                Toast.makeText(this, "Loading video...", Toast.LENGTH_SHORT).show();

                Uri uri = Uri.parse(url);
                videoView.setVideoURI(uri);

                MediaController mediaController = new MediaController(this);
                mediaController.setAnchorView(videoView);
                videoView.setMediaController(mediaController);

                videoView.setOnPreparedListener(mp -> {
                    Toast.makeText(this, "Video Loaded", Toast.LENGTH_SHORT).show();
                    videoView.start();
                });

            } else {
                Toast.makeText(this, "Enter URL first", Toast.LENGTH_SHORT).show();
            }
        });

        // ▶ PLAY
        playBtn.setOnClickListener(v -> {
            if (mediaPlayer != null) mediaPlayer.start();
            if (videoView != null) videoView.start();
        });

        // ⏸ PAUSE
        pauseBtn.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
            if (videoView != null && videoView.isPlaying()) videoView.pause();
        });

        // ⏹ STOP
        stopBtn.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            if (videoView != null) videoView.stopPlayback();
        });

        // 🔄 RESTART
        restartBtn.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
            }

            if (videoView != null) {
                videoView.seekTo(0);
                videoView.start();
            }
        });
    }

    // 🎵 HANDLE AUDIO SELECTION
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            audioUri = data.getData();

            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = MediaPlayer.create(this, audioUri);
            Toast.makeText(this, "Audio Loaded", Toast.LENGTH_SHORT).show();
        }
    }
}