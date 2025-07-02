package com.example.videoplayer;

import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Rational;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private ExoPlayer player;
    private ActivityResultLauncher<String> pickVideoLauncher;
    private Handler progressHandler;
    private Runnable progressRunnable;
    private Uri[] playlist = new Uri[] {
            Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
            Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
            Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4")
    };
    private int playlistIndex = 0;
    private boolean isFullScreen = false;
    private boolean landscapeLocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView drawerView = findViewById(R.id.drawer_view);
        populateDrawer(drawerView);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_sort_by_size);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(android.view.Gravity.START));

        PlayerView playerView = findViewById(R.id.player_view);
        SeekBar progressBar = findViewById(R.id.progress_bar);
        SeekBar volumeBar = findViewById(R.id.volume_bar);
        SeekBar brightnessBar = findViewById(R.id.brightness_bar);
        Spinner speedSpinner = findViewById(R.id.speed_spinner);
        Button pickButton = findViewById(R.id.pick_button);
        Button nextButton = findViewById(R.id.next_button);
        Button prevButton = findViewById(R.id.prev_button);
        Button fullscreenButton = findViewById(R.id.fullscreen_button);

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

        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.screenBrightness = progress / 100f;
                    getWindow().setAttributes(params);
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
                        playlistIndex = playlist.length; // append to end
                        playlist = java.util.Arrays.copyOf(playlist, playlist.length + 1);
                        playlist[playlistIndex] = uri;
                        playVideo(uri);
                        populateDrawer(drawerView);
                    }
                });

        pickButton.setOnClickListener(v -> pickVideoLauncher.launch("video/*"));

        nextButton.setOnClickListener(v -> playNext());
        prevButton.setOnClickListener(v -> playPrevious());
        fullscreenButton.setOnClickListener(v -> toggleFullscreen());

        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    player.setVolume(progress / 100f);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        player.addListener(new com.google.android.exoplayer2.Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == com.google.android.exoplayer2.Player.STATE_ENDED) {
                    playNext();
                }
            }
        });

        toolbar.setOnMenuItemClickListener(item -> onOptionsItemSelected(item));

        playVideo(playlist[playlistIndex]);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            shareCurrent();
            return true;
        } else if (item.getItemId() == R.id.action_orientation) {
            toggleOrientation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareCurrent() {
        Uri uri = playlist[playlistIndex];
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, uri.toString());
        startActivity(Intent.createChooser(share, getString(R.string.share)));
    }

    private void toggleOrientation() {
        landscapeLocked = !landscapeLocked;
        if (landscapeLocked) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    private void populateDrawer(NavigationView drawerView) {
        drawerView.getMenu().clear();
        for (int i = 0; i < playlist.length; i++) {
            Uri uri = playlist[i];
            drawerView.getMenu().add(0, i, 0, uri.getLastPathSegment()).setOnMenuItemClickListener(item -> {
                playlistIndex = item.getItemId();
                playVideo(playlist[playlistIndex]);
                DrawerLayout layout = findViewById(R.id.drawer_layout);
                layout.closeDrawer(android.view.Gravity.START);
                return true;
            });
        }
    }

    private void playVideo(Uri uri) {
        MediaItem.SubtitleConfiguration subtitle = new MediaItem.SubtitleConfiguration.Builder(
                Uri.parse("asset:///sample.srt"))
                .setMimeType(com.google.android.exoplayer2.util.MimeTypes.APPLICATION_SUBRIP)
                .setLanguage("en")
                .build();
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(uri)
                .setSubtitleConfigurations(java.util.Collections.singletonList(subtitle))
                .build();
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    private void playNext() {
        if (playlistIndex < playlist.length - 1) {
            playlistIndex++;
            playVideo(playlist[playlistIndex]);
        }
    }

    private void playPrevious() {
        if (playlistIndex > 0) {
            playlistIndex--;
            playVideo(playlist[playlistIndex]);
        }
    }

    private void toggleFullscreen() {
        View decor = getWindow().getDecorView();
        if (isFullScreen) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        isFullScreen = !isFullScreen;
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

    @Override
    public void onUserLeaveHint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PictureInPictureParams params = new PictureInPictureParams.Builder()
                    .setAspectRatio(new Rational(16, 9))
                    .build();
            enterPictureInPictureMode(params);
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPipMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPipMode, newConfig);
        if (isInPipMode) {
            findViewById(R.id.pick_button).setVisibility(View.GONE);
            findViewById(R.id.speed_spinner).setVisibility(View.GONE);
            findViewById(R.id.volume_bar).setVisibility(View.GONE);
            findViewById(R.id.fullscreen_button).setVisibility(View.GONE);
            findViewById(R.id.next_button).setVisibility(View.GONE);
            findViewById(R.id.prev_button).setVisibility(View.GONE);
            findViewById(R.id.brightness_bar).setVisibility(View.GONE);
        } else {
            findViewById(R.id.pick_button).setVisibility(View.VISIBLE);
            findViewById(R.id.speed_spinner).setVisibility(View.VISIBLE);
            findViewById(R.id.volume_bar).setVisibility(View.VISIBLE);
            findViewById(R.id.fullscreen_button).setVisibility(View.VISIBLE);
            findViewById(R.id.next_button).setVisibility(View.VISIBLE);
            findViewById(R.id.prev_button).setVisibility(View.VISIBLE);
            findViewById(R.id.brightness_bar).setVisibility(View.VISIBLE);
        }
    }
}
