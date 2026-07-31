package com.example.myrajourney.rehab.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import com.example.myrajourney.R;

import java.io.File;

/**
 * Fragment for handling exercise video playback with ExoPlayer
 * Supports local file playback for high-performance exercise guidance
 */
public class VideoPlayerFragment extends Fragment {

    private ExoPlayer player;
    private PlayerView playerView;
    private ProgressBar progressBar;
    private TextView txtError;

    private String videoUrl;
    private String exerciseName;
    private VideoPlayerListener listener;

    public interface VideoPlayerListener {
        void onVideoLoaded();

        void onVideoError();

        void onVideoStarted();

        void onVideoPaused();

        void onVideoCompleted();
    }

    public static VideoPlayerFragment newInstance(String videoUrl, String exerciseName) {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        Bundle args = new Bundle();
        args.putString("video_url", videoUrl);
        args.putString("exercise_name", exerciseName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            videoUrl = getArguments().getString("video_url");
            exerciseName = getArguments().getString("exercise_name");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        // Inflate the layout which contains the PlayerView with correctly configured
        // surface_type
        View view = inflater.inflate(R.layout.fragment_video_player, container, false);

        playerView = view.findViewById(R.id.player_view);
        progressBar = view.findViewById(R.id.progress_bar);
        txtError = view.findViewById(R.id.text_error);

        // Configure player view behavior
        if (playerView != null) {
            playerView.setControllerAutoShow(false);
            playerView.setControllerHideOnTouch(false);
            android.util.Log.d("VideoPlayerFragment", "PlayerView configured from XML");
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializePlayer();
    }

    private void initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(requireContext()).build();
            playerView.setPlayer(player);

            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == Player.STATE_READY) {
                        progressBar.setVisibility(View.GONE);
                        if (listener != null)
                            listener.onVideoLoaded();
                    } else if (playbackState == Player.STATE_ENDED) {
                        if (listener != null)
                            listener.onVideoCompleted();
                    } else if (playbackState == Player.STATE_BUFFERING) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    progressBar.setVisibility(View.GONE);
                    txtError.setVisibility(View.VISIBLE);
                    txtError.setText("Error: " + error.getMessage());
                    if (listener != null)
                        listener.onVideoError();
                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    if (listener != null) {
                        if (isPlaying)
                            listener.onVideoStarted();
                        else
                            listener.onVideoPaused();
                    }
                }
            });
            player.setRepeatMode(Player.REPEAT_MODE_ALL); // Enable continuous looping
        }

        loadVideoSource();
    }

    private void loadVideoSource() {
        if (videoUrl == null || videoUrl.isEmpty()) {
            android.util.Log.e("VideoPlayerFragment", "No video URL provided");
            showError("No video path provided");
            return;
        }

        android.util.Log.d("VideoPlayerFragment", "Loading video from: " + videoUrl);

        try {
            MediaItem mediaItem;
            // Handle local files vs remote URLs
            if (videoUrl.startsWith("/")) {
                android.util.Log.d("VideoPlayerFragment", "Loading local file");
                mediaItem = MediaItem.fromUri(Uri.fromFile(new File(videoUrl)));
            } else if (videoUrl.startsWith("content://") || videoUrl.startsWith("file://")) {
                android.util.Log.d("VideoPlayerFragment", "Loading content URI");
                mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
            } else {
                android.util.Log.d("VideoPlayerFragment", "Loading remote/asset URL");
                // Assume remote or asset
                mediaItem = MediaItem.fromUri(videoUrl);
            }

            player.setMediaItem(mediaItem);
            player.prepare();
            android.util.Log.d("VideoPlayerFragment", "Video prepared");
            // Do NOT auto-play here, wait for synchronization signal
        } catch (Exception e) {
            android.util.Log.e("VideoPlayerFragment", "Failed to load video", e);
            showError("Failed to load: " + e.getMessage());
        }
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        txtError.setVisibility(View.VISIBLE);
        txtError.setText(message);
        if (listener != null)
            listener.onVideoError();
    }

    public void setVideoPlayerListener(VideoPlayerListener listener) {
        this.listener = listener;
    }

    public void pauseVideo() {
        if (player != null) {
            player.pause();
        }
    }

    public void resumeVideo() {
        if (player != null) {
            player.play();
        }
    }

    public void startVideo() {
        if (player != null) {
            player.play();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume video playback when fragment becomes visible
        if (player != null && !player.isPlaying()) {
            android.util.Log.d("VideoPlayerFragment", "Resuming video playback");
            resumeVideo();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Only pause if activity is actually pausing (not just camera starting)
        if (getActivity() != null && getActivity().isFinishing()) {
            android.util.Log.d("VideoPlayerFragment", "Pausing video - activity finishing");
            pauseVideo();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releasePlayer();
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }
}