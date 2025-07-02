package com.example.videoplayer;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.ui.PlayerView;

public class MainActivity extends AppCompatActivity {
    private ExoPlayer player;
    private ActivityResultLauncher<String> pickVideoLauncher;
    private Handler progressHandler;
    private Runnable progressRunnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PlayerView playerView = findViewById(R.id.player_view);
        SeekBar progressBar = findViewById(R.id.progress_bar);
        Spinner speedSpinner = findViewById(R.id.speed_spinner);
        Button pickButton = findViewById(R.id.pick_button);

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.playback_speed_labels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speedSpinner.setAdapter(adapter);
        speedSpinner.setSelection(1);
        speedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                float[] speeds = {0.5f, 1f, 1.5f, 2f};
                player.setPlaybackParameters(new PlaybackParameters(speeds[position]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        progressHandler = new Handler(Looper.getMainLooper());
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                long duration = player.getDuration();
                if (duration > 0) {
                    int progress = (int) (player.getCurrentPosition() * 1000 / duration);
                    progressBar.setProgress(progress);
                } else {
                    progressBar.setProgress(0);
                }
                progressHandler.postDelayed(this, 500);
            }
        };
        progressHandler.post(progressRunnable);

        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    long duration = player.getDuration();
                    if (duration > 0) {
                        long newPosition = duration * progress / 1000;
                        player.seekTo(newPosition);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        pickVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        playVideo(uri);
                    }
                });

        pickButton.setOnClickListener(v -> pickVideoLauncher.launch("video/*"));

        // Load a remote sample video on startup. Replace the URL with your own if desired.
        Uri videoUri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
        playVideo(videoUri);
    }

    private void playVideo(Uri uri) {
        MediaItem mediaItem = MediaItem.fromUri(uri);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
        if (progressHandler != null) {
            progressHandler.removeCallbacksAndMessages(null);
        }
    }
}
