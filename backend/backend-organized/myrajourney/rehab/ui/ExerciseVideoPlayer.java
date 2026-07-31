package com.example.myrajourney.rehab.ui;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.myrajourney.R;
import com.example.myrajourney.rehab.models.RAExercise;

/**
 * Custom video player component that handles YouTube videos with fallback to animations
 */
public class ExerciseVideoPlayer extends FrameLayout {
    
    private WebView webViewVideo;
    private ImageView animationView;
    private TextView txtVideoTitle;
    private TextView txtVideoInstructions;
    private LinearLayout layoutVideoError;
    private TextView txtErrorMessage;
    private Button btnTryVideo;
    private Button btnFloatingVideo;
    
    private RAExercise currentExercise;
    private AnimationDrawable currentAnimation;
    
    public ExerciseVideoPlayer(Context context) {
        super(context);
        init();
    }
    
    public ExerciseVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ExerciseVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.component_exercise_video_player, this, true);
        
        webViewVideo = findViewById(R.id.webViewVideo);
        animationView = findViewById(R.id.animationView);
        txtVideoTitle = findViewById(R.id.txtVideoTitle);
        txtVideoInstructions = findViewById(R.id.txtVideoInstructions);
        layoutVideoError = findViewById(R.id.layoutVideoError);
        txtErrorMessage = findViewById(R.id.txtErrorMessage);
        btnTryVideo = findViewById(R.id.btnTryVideo);
        btnFloatingVideo = findViewById(R.id.btnFloatingVideo);
        
        setupWebView();
        setupListeners();
    }
    
    private void setupWebView() {
        if (webViewVideo != null) {
            // Clear any existing content
            webViewVideo.clearCache(true);
            webViewVideo.clearHistory();
            
            // Enable all necessary settings for YouTube playback
            webViewVideo.getSettings().setJavaScriptEnabled(true);
            webViewVideo.getSettings().setDomStorageEnabled(true);
            webViewVideo.getSettings().setDatabaseEnabled(true);
            webViewVideo.getSettings().setCacheMode(android.webkit.WebSettings.LOAD_DEFAULT);
            webViewVideo.getSettings().setMediaPlaybackRequiresUserGesture(false);
            webViewVideo.getSettings().setLoadWithOverviewMode(true);
            webViewVideo.getSettings().setUseWideViewPort(true);
            webViewVideo.getSettings().setBuiltInZoomControls(false);
            webViewVideo.getSettings().setDisplayZoomControls(false);
            webViewVideo.getSettings().setSupportZoom(false);
            webViewVideo.getSettings().setAllowFileAccess(true);
            webViewVideo.getSettings().setAllowContentAccess(true);
            webViewVideo.getSettings().setAllowFileAccessFromFileURLs(true);
            webViewVideo.getSettings().setAllowUniversalAccessFromFileURLs(true);
            webViewVideo.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            
            // Enable hardware acceleration for better video performance
            webViewVideo.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            
            // Set user agent for better YouTube compatibility
            webViewVideo.getSettings().setUserAgentString(
                "Mozilla/5.0 (Linux; Android 11; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36"
            );
            
            // Set WebView client with enhanced error handling
            webViewVideo.setWebViewClient(new WebViewClient() {
                private boolean hasError = false;
                
                @Override
                public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    hasError = false;
                    android.util.Log.d("ExerciseVideoPlayer", "YouTube video loading started: " + url);
                }
                
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    
                    if (!hasError) {
                        android.util.Log.d("ExerciseVideoPlayer", "YouTube video loaded successfully: " + url);
                        showVideo();
                    }
                }
                
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    super.onReceivedError(view, errorCode, description, failingUrl);
                    hasError = true;
                    
                    android.util.Log.e("ExerciseVideoPlayer", "WebView error: " + description + " (Code: " + errorCode + ")");
                    
                    // Handle specific YouTube embedding errors
                    if (description.contains("152") || description.contains("embedding")) {
                        showVideoError("Video embedding restricted by YouTube\n\n" +
                                     "Error Code: " + errorCode + "\n\n" +
                                     "This video cannot be played in the app due to YouTube's embedding restrictions.\n\n" +
                                     "Tap 'Try Video' to attempt with a different approach.");
                    } else {
                        showVideoError("Video failed to load: " + description + "\n\nTap 'Try Video' to retry.");
                    }
                }
                
                @Override
                public void onReceivedHttpError(WebView view, android.webkit.WebResourceRequest request, 
                                              android.webkit.WebResourceResponse errorResponse) {
                    super.onReceivedHttpError(view, request, errorResponse);
                    android.util.Log.e("ExerciseVideoPlayer", "HTTP error: " + errorResponse.getStatusCode() + 
                                     " for URL: " + request.getUrl());
                    
                    // Only show error if it's the main page that failed
                    if (request.getUrl().toString().contains("youtube.com")) {
                        hasError = true;
                        
                        // Handle specific HTTP error codes
                        int statusCode = errorResponse.getStatusCode();
                        if (statusCode == 403 || statusCode == 404) {
                            showVideoError("Video unavailable (HTTP " + statusCode + ")\n\n" +
                                         "This video may be private, deleted, or restricted.\n\n" +
                                         "Tap 'Try Video' to attempt with alternative content.");
                        } else {
                            showVideoError("Video unavailable (HTTP " + statusCode + ")\n\n" +
                                         "This may be due to network issues or video restrictions.\n\n" +
                                         "Tap 'Try Video' to retry.");
                        }
                    }
                }
                
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, android.webkit.WebResourceRequest request) {
                    String url = request.getUrl().toString();
                    android.util.Log.d("ExerciseVideoPlayer", "URL loading: " + url);
                    
                    // Allow YouTube URLs to load in WebView
                    if (url.contains("youtube.com") || url.contains("youtu.be")) {
                        return false; // Let WebView handle it
                    }
                    
                    return super.shouldOverrideUrlLoading(view, request);
                }
            });
            
            // Set WebChromeClient for better media support and permissions
            webViewVideo.setWebChromeClient(new android.webkit.WebChromeClient() {
                @Override
                public void onPermissionRequest(android.webkit.PermissionRequest request) {
                    android.util.Log.d("ExerciseVideoPlayer", "Permission requested: " + java.util.Arrays.toString(request.getResources()));
                    request.grant(request.getResources());
                }
                
                @Override
                public boolean onConsoleMessage(android.webkit.ConsoleMessage consoleMessage) {
                    android.util.Log.d("ExerciseVideoPlayer", "Console [" + consoleMessage.messageLevel() + "]: " + consoleMessage.message());
                    return true;
                }
                
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    android.util.Log.v("ExerciseVideoPlayer", "Loading progress: " + newProgress + "%");
                }
            });
            
            android.util.Log.d("ExerciseVideoPlayer", "WebView configured for enhanced YouTube playback");
        }
    }
    
    private void setupListeners() {
        if (btnTryVideo != null) {
            btnTryVideo.setOnClickListener(v -> tryLoadVideo());
        }
        
        if (btnFloatingVideo != null) {
            btnFloatingVideo.setOnClickListener(v -> tryLoadVideo());
        }
    }
    
    /**
     * Load exercise with local video - Enhanced for reliable local playback
     */
    public void loadExercise(RAExercise exercise) {
        this.currentExercise = exercise;
        
        if (exercise == null) {
            showVideoError("No exercise selected");
            return;
        }
        
        android.util.Log.d("ExerciseVideoPlayer", "Loading exercise: " + exercise.getName());
        
        // Update title and instructions
        if (txtVideoTitle != null) {
            txtVideoTitle.setText(exercise.getName() + " - Video Demonstration");
        }
        if (txtVideoInstructions != null) {
            txtVideoInstructions.setText("Follow the video demonstration for proper form and technique");
        }
        
        // PRIORITIZE LOCAL VIDEOS - Try local video first
        String localVideoPath = getLocalVideoPath(exercise.getId());
        if (localVideoPath != null && loadLocalVideo(localVideoPath)) {
            android.util.Log.d("ExerciseVideoPlayer", "Local video loaded successfully: " + localVideoPath);
        } else {
            android.util.Log.w("ExerciseVideoPlayer", "Local video not found for: " + exercise.getId());
            
            // Try to show static demonstration image as immediate fallback
            if (showStaticDemonstrationImage()) {
                android.util.Log.d("ExerciseVideoPlayer", "Showing static demonstration image as fallback");
            } else {
                // Final fallback to animation
                android.util.Log.d("ExerciseVideoPlayer", "Showing animation as final fallback");
                forceShowAnimation();
            }
        }
    }
    
    /**
     * Get local video path based on exercise ID
     */
    private String getLocalVideoPath(String exerciseId) {
        switch (exerciseId) {
            case "ex_001": return "exercise_videos/ex_001_wrist_flexion.mp4";
            case "ex_002": return "exercise_videos/ex_002_wrist_rotation.mp4";
            case "ex_003": return "exercise_videos/ex_003_thumb_opposition.mp4";
            case "ex_004": return "exercise_videos/ex_004_thumb_flexion.mp4";
            case "ex_005": return "exercise_videos/ex_005_finger_flexion.mp4";
            case "ex_006": return "exercise_videos/ex_006_finger_extension.mp4";
            case "ex_007": return "exercise_videos/ex_007_finger_pinch.mp4";
            case "ex_008": return "exercise_videos/ex_008_knee_flexion.mp4";
            case "ex_009": return "exercise_videos/ex_009_hip_flexion.mp4";
            case "ex_010": return "exercise_videos/ex_010_hip_abduction.mp4";
            default: return null;
        }
    }
    
    /**
     * Load local video from assets folder
     */
    private boolean loadLocalVideo(String videoPath) {
        if (videoPath == null || webViewVideo == null) {
            return false;
        }
        
        try {
            android.util.Log.d("ExerciseVideoPlayer", "Attempting to load local video: " + videoPath);
            
            // Create HTML5 video player for local assets
            String html = createLocalVideoPlayerHtml(videoPath);
            webViewVideo.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
            
            showLoadingState();
            
            // Monitor local video loading
            monitorLocalVideoLoading();
            
            return true;
            
        } catch (Exception e) {
            android.util.Log.e("ExerciseVideoPlayer", "Error loading local video: " + videoPath, e);
            return false;
        }
    }
    
    /**
     * Create HTML5 video player for local assets
     */
    private String createLocalVideoPlayerHtml(String videoPath) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<meta charset='UTF-8'>" +
               "<meta name='viewport' content='width=device-width, initial-scale=1.0, user-scalable=no'>" +
               "<style>" +
               "* { margin: 0; padding: 0; box-sizing: border-box; }" +
               "html, body { width: 100%; height: 100%; background: #000; overflow: hidden; font-family: Arial, sans-serif; }" +
               ".container { position: relative; width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; }" +
               "video { width: 100%; height: 100%; object-fit: contain; }" +
               ".loading { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: white; text-align: center; }" +
               ".controls { position: absolute; bottom: 10px; left: 50%; transform: translateX(-50%); }" +
               ".btn { background: rgba(0,0,0,0.7); color: white; border: none; padding: 10px 15px; margin: 0 5px; border-radius: 5px; cursor: pointer; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "<div class='container'>" +
               "<div class='loading' id='loading'>🎥 Loading exercise video...</div>" +
               "<video id='exerciseVideo' controls playsinline preload='auto' style='display: none;'>" +
               "<source src='" + videoPath + "' type='video/mp4'>" +
               "Your browser does not support the video tag." +
               "</video>" +
               "<div class='controls' id='controls' style='display: none;'>" +
               "<button class='btn' onclick='restartVideo()'>🔄 Restart</button>" +
               "<button class='btn' onclick='togglePlayPause()' id='playPauseBtn'>⏸️ Pause</button>" +
               "</div>" +
               "</div>" +
               "<script>" +
               "var video = document.getElementById('exerciseVideo');" +
               "var loading = document.getElementById('loading');" +
               "var controls = document.getElementById('controls');" +
               "var playPauseBtn = document.getElementById('playPauseBtn');" +
               "" +
               "video.addEventListener('loadeddata', function() {" +
               "  console.log('Local video loaded successfully');" +
               "  loading.style.display = 'none';" +
               "  video.style.display = 'block';" +
               "  controls.style.display = 'block';" +
               "  video.play();" +
               "});" +
               "" +
               "video.addEventListener('error', function(e) {" +
               "  console.log('Local video error:', e);" +
               "  loading.innerHTML = '❌ Video failed to load';" +
               "});" +
               "" +
               "video.addEventListener('play', function() {" +
               "  playPauseBtn.innerHTML = '⏸️ Pause';" +
               "});" +
               "" +
               "video.addEventListener('pause', function() {" +
               "  playPauseBtn.innerHTML = '▶️ Play';" +
               "});" +
               "" +
               "function togglePlayPause() {" +
               "  if (video.paused) {" +
               "    video.play();" +
               "  } else {" +
               "    video.pause();" +
               "  }" +
               "}" +
               "" +
               "function restartVideo() {" +
               "  video.currentTime = 0;" +
               "  video.play();" +
               "}" +
               "" +
               "// Auto-loop the video" +
               "video.addEventListener('ended', function() {" +
               "  video.currentTime = 0;" +
               "  video.play();" +
               "});" +
               "" +
               "console.log('Local video player initialized for: " + videoPath + "');" +
               "</script>" +
               "</body>" +
               "</html>";
    }
    
    /**
     * Monitor local video loading
     */
    private void monitorLocalVideoLoading() {
        // Check after 3 seconds if local video loaded
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                android.util.Log.d("ExerciseVideoPlayer", "Checking local video loading status");
                
                if (webViewVideo != null && webViewVideo.getVisibility() == View.VISIBLE) {
                    // Inject JavaScript to check video status
                    webViewVideo.evaluateJavascript(
                        "javascript:(function() { " +
                        "  var video = document.getElementById('exerciseVideo'); " +
                        "  return video ? JSON.stringify({loaded: !video.error, duration: video.duration}) : 'null'; " +
                        "})()", 
                        new android.webkit.ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                android.util.Log.d("ExerciseVideoPlayer", "Local video status: " + value);
                                
                                if (value != null && value.contains("\"loaded\":false")) {
                                    android.util.Log.w("ExerciseVideoPlayer", "Local video failed to load, trying fallback");
                                    
                                    // Try static image fallback
                                    if (!showStaticDemonstrationImage()) {
                                        forceShowAnimation();
                                    }
                                }
                            }
                        }
                    );
                }
            }
        }, 3000);
    }
    
    /**
     * Load YouTube video with simplified and reliable approach
     */
    private void loadYouTubeVideo(String videoUrl) {
        if (videoUrl == null || videoUrl.isEmpty() || webViewVideo == null) {
            showVideoError("Invalid video URL");
            return;
        }
        
        android.util.Log.d("ExerciseVideoPlayer", "Loading YouTube video: " + videoUrl);
        
        try {
            String videoId = extractVideoId(videoUrl);
            if (videoId != null) {
                android.util.Log.d("ExerciseVideoPlayer", "Extracted video ID: " + videoId);
                
                // Show loading state immediately
                showLoadingState();
                
                // Create simplified HTML for YouTube playback
                String html = createSimplifiedYouTubeEmbedHtml(videoId);
                webViewVideo.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null);
                
                // Set up monitoring to check if video loads
                monitorVideoLoading();
                
                android.util.Log.d("ExerciseVideoPlayer", "YouTube video loading initiated for ID: " + videoId);
            } else {
                android.util.Log.e("ExerciseVideoPlayer", "Could not extract video ID from URL: " + videoUrl);
                showVideoError("Could not extract video ID from URL");
            }
        } catch (Exception e) {
            android.util.Log.e("ExerciseVideoPlayer", "Error loading YouTube video", e);
            showVideoError("Error loading video: " + e.getMessage());
        }
    }
    
    /**
     * Extract video ID from YouTube URL
     */
    private String extractVideoId(String videoUrl) {
        if (videoUrl.contains("youtu.be/")) {
            String videoId = videoUrl.substring(videoUrl.lastIndexOf("/") + 1);
            if (videoId.contains("?")) {
                videoId = videoId.substring(0, videoId.indexOf("?"));
            }
            return videoId;
        } else if (videoUrl.contains("youtube.com/watch?v=")) {
            String videoId = videoUrl.substring(videoUrl.indexOf("v=") + 2);
            if (videoId.contains("&")) {
                videoId = videoId.substring(0, videoId.indexOf("&"));
            }
            return videoId;
        }
        return null;
    }
    
    /**
     * Create enhanced HTML for YouTube embed with multiple fallback approaches
     */
    private String createSimplifiedYouTubeEmbedHtml(String videoId) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<meta charset='UTF-8'>" +
               "<meta name='viewport' content='width=device-width, initial-scale=1.0, user-scalable=no'>" +
               "<style>" +
               "* { margin: 0; padding: 0; box-sizing: border-box; }" +
               "html, body { width: 100%; height: 100%; background: #000; overflow: hidden; font-family: Arial, sans-serif; }" +
               ".container { position: relative; width: 100%; height: 100%; }" +
               ".video-wrapper { position: relative; width: 100%; height: 0; padding-bottom: 56.25%; }" +
               "iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: none; }" +
               ".loading { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: white; text-align: center; }" +
               ".error { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: #ff6b6b; text-align: center; padding: 20px; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "<div class='container'>" +
               "<div class='loading' id='loading'>🎥 Loading exercise video...<br><small>Please wait</small></div>" +
               "<div class='video-wrapper' id='videoWrapper' style='display: none;'>" +
               "<iframe id='youtubePlayer' " +
               "src='https://www.youtube-nocookie.com/embed/" + videoId + "?" +
               "autoplay=0&" +
               "controls=1&" +
               "modestbranding=1&" +
               "rel=0&" +
               "showinfo=0&" +
               "playsinline=1&" +
               "enablejsapi=1&" +
               "fs=1&" +
               "cc_load_policy=0&" +
               "iv_load_policy=3&" +
               "start=0' " +
               "frameborder='0' " +
               "allowfullscreen " +
               "allow='accelerometer; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share'>" +
               "</iframe>" +
               "</div>" +
               "<div class='error' id='errorDiv' style='display: none;'>" +
               "❌ Video temporarily unavailable<br>" +
               "<small>Trying alternative approach...</small>" +
               "</div>" +
               "</div>" +
               "<script>" +
               "var loaded = false;" +
               "var errorShown = false;" +
               "var retryCount = 0;" +
               "var maxRetries = 2;" +
               "" +
               "function showVideo() {" +
               "  if (!loaded && !errorShown) {" +
               "    loaded = true;" +
               "    document.getElementById('loading').style.display = 'none';" +
               "    document.getElementById('videoWrapper').style.display = 'block';" +
               "    console.log('YouTube video loaded successfully');" +
               "  }" +
               "}" +
               "" +
               "function showError() {" +
               "  if (!loaded && !errorShown) {" +
               "    errorShown = true;" +
               "    document.getElementById('loading').style.display = 'none';" +
               "    document.getElementById('errorDiv').style.display = 'block';" +
               "    console.log('YouTube video failed to load');" +
               "    " +
               "    // Try alternative approach after 2 seconds" +
               "    setTimeout(function() {" +
               "      if (retryCount < maxRetries) {" +
               "        retryCount++;" +
               "        tryAlternativeEmbed();" +
               "      }" +
               "    }, 2000);" +
               "  }" +
               "}" +
               "" +
               "function tryAlternativeEmbed() {" +
               "  console.log('Trying alternative embed approach, attempt: ' + retryCount);" +
               "  var iframe = document.getElementById('youtubePlayer');" +
               "  var newSrc = 'https://www.youtube.com/embed/" + videoId + "?" +
               "    'autoplay=0&controls=1&rel=0&modestbranding=1&playsinline=1';" +
               "  iframe.src = newSrc;" +
               "  " +
               "  // Reset states for retry" +
               "  loaded = false;" +
               "  errorShown = false;" +
               "  document.getElementById('errorDiv').style.display = 'none';" +
               "  document.getElementById('loading').style.display = 'block';" +
               "  document.getElementById('loading').innerHTML = '🔄 Retrying video load...';" +
               "}" +
               "" +
               "// Set up iframe load handlers" +
               "document.getElementById('youtubePlayer').onload = function() {" +
               "  console.log('YouTube iframe loaded');" +
               "  setTimeout(showVideo, 1500);" +
               "};" +
               "" +
               "document.getElementById('youtubePlayer').onerror = function() {" +
               "  console.log('YouTube iframe error');" +
               "  showError();" +
               "};" +
               "" +
               "// Fallback timeout" +
               "setTimeout(function() {" +
               "  if (!loaded && !errorShown) {" +
               "    console.log('YouTube load timeout');" +
               "    showError();" +
               "  }" +
               "}, 8000);" +
               "" +
               "// Force show video after reasonable time" +
               "setTimeout(function() {" +
               "  if (!loaded && !errorShown) {" +
               "    console.log('Force showing video after timeout');" +
               "    showVideo();" +
               "  }" +
               "}, 12000);" +
               "" +
               "console.log('YouTube player initialized for video: " + videoId + "');" +
               "</script>" +
               "</body>" +
               "</html>";
    }
    
    /**
     * Show loading state for video
     */
    private void showLoadingState() {
        if (webViewVideo != null) {
            webViewVideo.setVisibility(View.VISIBLE);
        }
        animationView.setVisibility(View.GONE);
        layoutVideoError.setVisibility(View.GONE);
        if (btnFloatingVideo != null) {
            btnFloatingVideo.setVisibility(View.GONE);
        }
    }
    
    /**
     * Monitor video loading with enhanced timeout and automatic static image fallback
     */
    private void monitorVideoLoading() {
        // First check after 3 seconds - quick fallback to static image
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                android.util.Log.d("ExerciseVideoPlayer", "Quick video loading check after 3 seconds");
                
                // Check if WebView is visible but might be showing error
                if (webViewVideo != null && webViewVideo.getVisibility() == View.VISIBLE) {
                    // Inject JavaScript to check video status
                    webViewVideo.evaluateJavascript(
                        "javascript:(function() { " +
                        "  var iframe = document.getElementById('youtubePlayer'); " +
                        "  var loaded = window.loaded || false; " +
                        "  var errorShown = window.errorShown || false; " +
                        "  return JSON.stringify({loaded: loaded, error: errorShown, iframeExists: !!iframe}); " +
                        "})()", 
                        new android.webkit.ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                android.util.Log.d("ExerciseVideoPlayer", "Video status check result: " + value);
                                
                                // If video hasn't loaded after 3 seconds, try static image fallback
                                if (value != null && (value.contains("\"loaded\":false") || value.contains("\"error\":true"))) {
                                    android.util.Log.w("ExerciseVideoPlayer", "Video not loaded after 3 seconds, trying static image fallback");
                                    
                                    // Try to show static demonstration image immediately
                                    if (showStaticDemonstrationImage()) {
                                        android.util.Log.d("ExerciseVideoPlayer", "Successfully showed static demonstration image");
                                        return;
                                    }
                                    
                                    // If static image not available, force the video to show
                                    webViewVideo.evaluateJavascript("javascript:showVideo()", null);
                                }
                            }
                        }
                    );
                }
            }
        }, 3000);
        
        // Second check after 8 seconds - try animation fallback
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                android.util.Log.d("ExerciseVideoPlayer", "Secondary video loading check after 8 seconds");
                
                if (webViewVideo != null && webViewVideo.getVisibility() == View.VISIBLE) {
                    webViewVideo.evaluateJavascript(
                        "javascript:(function() { return window.loaded || false; })()", 
                        new android.webkit.ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                if (value != null && value.equals("false")) {
                                    android.util.Log.w("ExerciseVideoPlayer", "Video still not loaded after 8 seconds");
                                    
                                    // If we haven't already shown a static image, try again
                                    if (animationView.getVisibility() != View.VISIBLE) {
                                        if (showStaticDemonstrationImage()) {
                                            android.util.Log.d("ExerciseVideoPlayer", "Showed static image as secondary fallback");
                                            return;
                                        }
                                        
                                        // If no static image, show animation
                                        forceShowAnimation();
                                    }
                                }
                            }
                        }
                    );
                }
            }
        }, 8000);
        
        // Final check after 15 seconds - show error with options
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                android.util.Log.d("ExerciseVideoPlayer", "Final video loading check after 15 seconds");
                
                // Only show error if we haven't already shown an alternative
                if (webViewVideo != null && webViewVideo.getVisibility() == View.VISIBLE && 
                    animationView.getVisibility() != View.VISIBLE) {
                    
                    webViewVideo.evaluateJavascript(
                        "javascript:(function() { return window.loaded || false; })()", 
                        new android.webkit.ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                if (value != null && value.equals("false")) {
                                    android.util.Log.w("ExerciseVideoPlayer", "Video completely failed to load, showing error with alternatives");
                                    
                                    // Final attempt at static image before showing error
                                    if (!showStaticDemonstrationImage()) {
                                        showVideoError("Video failed to load after multiple attempts.");
                                    }
                                }
                            }
                        }
                    );
                }
            }
        }, 15000);
    }
    
    /**
     * Show video error with enhanced fallback options including static demonstration images
     */
    private void showVideoError(String errorMessage) {
        android.util.Log.w("ExerciseVideoPlayer", "Video error occurred: " + errorMessage);
        
        // Immediately try to show static demonstration image as fallback
        if (showStaticDemonstrationImage()) {
            android.util.Log.d("ExerciseVideoPlayer", "Showing static demonstration image as fallback");
            return; // Successfully showed static image, no need for error message
        }
        
        // If static image also fails, show error message with options
        if (webViewVideo != null) {
            webViewVideo.setVisibility(View.GONE);
        }
        animationView.setVisibility(View.GONE);
        layoutVideoError.setVisibility(View.VISIBLE);
        
        StringBuilder fullMessage = new StringBuilder();
        fullMessage.append("🎥 Video temporarily unavailable\n\n");
        
        // Add specific troubleshooting for YouTube videos
        if (currentExercise != null) {
            fullMessage.append("📺 Exercise: ").append(currentExercise.getName()).append("\n\n");
        }
        
        fullMessage.append("📱 Available alternatives:\n");
        fullMessage.append("• Static demonstration image (if available)\n");
        fullMessage.append("• Animated exercise guide\n");
        fullMessage.append("• Written step-by-step instructions\n\n");
        
        // Add exercise instructions as fallback
        if (currentExercise != null && currentExercise.getInstructions() != null) {
            fullMessage.append("📋 How to perform ").append(currentExercise.getName()).append(":\n\n");
            for (int i = 0; i < currentExercise.getInstructions().size(); i++) {
                fullMessage.append((i + 1)).append(". ").append(currentExercise.getInstructions().get(i)).append("\n");
            }
            fullMessage.append("\n");
        }
        
        // Add benefits information
        if (currentExercise != null && currentExercise.getRaSpecificBenefits() != null) {
            fullMessage.append("💪 Benefits:\n");
            for (String benefit : currentExercise.getRaSpecificBenefits()) {
                fullMessage.append("• ").append(benefit).append("\n");
            }
        }
        
        if (txtErrorMessage != null) {
            txtErrorMessage.setText(fullMessage.toString());
        }
        
        if (btnTryVideo != null) {
            btnTryVideo.setVisibility(View.VISIBLE);
            btnTryVideo.setText("🖼️ Show Image Guide");
            btnTryVideo.setOnClickListener(v -> {
                if (btnTryVideo.getText().toString().contains("Image Guide")) {
                    if (showStaticDemonstrationImage()) {
                        // Successfully showed image
                        return;
                    }
                    btnTryVideo.setText("🎬 Show Animation");
                } else if (btnTryVideo.getText().toString().contains("Animation")) {
                    forceShowAnimation();
                    btnTryVideo.setText("🔄 Try Video Again");
                } else {
                    // Try video again
                    tryLoadVideo();
                }
            });
        }
    }
    
    /**
     * Show static demonstration image as fallback when video fails
     */
    private boolean showStaticDemonstrationImage() {
        if (currentExercise == null || animationView == null) {
            return false;
        }
        
        int imageResource = getStaticDemonstrationImage(currentExercise.getId());
        if (imageResource != 0) {
            android.util.Log.d("ExerciseVideoPlayer", "Loading static demonstration image for: " + currentExercise.getId());
            
            // Hide other views
            if (webViewVideo != null) {
                webViewVideo.setVisibility(View.GONE);
            }
            layoutVideoError.setVisibility(View.GONE);
            
            // Show static image
            animationView.setVisibility(View.VISIBLE);
            animationView.setImageResource(imageResource);
            animationView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            
            // Update title to indicate it's a static guide
            if (txtVideoTitle != null) {
                txtVideoTitle.setText(currentExercise.getName() + " - Demonstration Guide");
            }
            if (txtVideoInstructions != null) {
                txtVideoInstructions.setText("Follow the demonstration image and instructions below");
            }
            
            return true;
        }
        
        android.util.Log.w("ExerciseVideoPlayer", "No static demonstration image available for: " + currentExercise.getId());
        return false;
    }
    
    /**
     * Get static demonstration image resource for each exercise
     */
    private int getStaticDemonstrationImage(String exerciseId) {
        switch (exerciseId) {
            case "ex_001": 
                // Try specific demonstration image first, fallback to animation
                return getImageResourceSafely("demo_wrist_flexion", R.drawable.animation_wrist_flex);
            case "ex_002": 
                return getImageResourceSafely("demo_wrist_rotation", R.drawable.animation_wrist_rotation);
            case "ex_003": 
                return getImageResourceSafely("demo_thumb_opposition", R.drawable.animation_thumb_opposition);
            case "ex_004": 
                return getImageResourceSafely("demo_thumb_flexion", R.drawable.thumb);
            case "ex_005": 
                return getImageResourceSafely("demo_finger_flexion", R.drawable.animation_finger_flex);
            case "ex_006": 
                return getImageResourceSafely("demo_finger_extension", R.drawable.animation_finger_extension);
            case "ex_007": 
                return getImageResourceSafely("demo_finger_pinch", R.drawable.finger);
            case "ex_008": 
                return getImageResourceSafely("demo_knee_flexion", R.drawable.animation_knee_flex);
            case "ex_009": 
                return getImageResourceSafely("demo_hip_flexion", R.drawable.glute);
            case "ex_010": 
                return getImageResourceSafely("demo_hip_abduction", R.drawable.glute);
            default: 
                return 0;
        }
    }
    
    /**
     * Safely get image resource, with fallback to existing animation
     */
    private int getImageResourceSafely(String preferredImageName, int fallbackResource) {
        try {
            // Try to get the preferred demonstration image
            int resourceId = getContext().getResources().getIdentifier(
                preferredImageName, "drawable", getContext().getPackageName());
            
            if (resourceId != 0) {
                android.util.Log.d("ExerciseVideoPlayer", "Found demonstration image: " + preferredImageName);
                return resourceId;
            } else {
                android.util.Log.d("ExerciseVideoPlayer", "Demonstration image not found: " + preferredImageName + ", using fallback");
                return fallbackResource;
            }
        } catch (Exception e) {
            android.util.Log.w("ExerciseVideoPlayer", "Error loading demonstration image: " + preferredImageName, e);
            return fallbackResource;
        }
    }
    
    /**
     * Try to reload the video with multiple fallback approaches for the specific YouTube URLs
     */
    public void tryLoadVideo() {
        if (currentExercise != null && currentExercise.getVideoUrl() != null) {
            android.util.Log.d("ExerciseVideoPlayer", "Retrying video load: " + currentExercise.getVideoUrl());
            
            String videoUrl = currentExercise.getVideoUrl();
            String videoId = extractVideoId(videoUrl);
            
            if (videoId != null) {
                // Clear WebView cache and try different approach
                if (webViewVideo != null) {
                    webViewVideo.clearCache(true);
                    webViewVideo.clearHistory();
                }
                
                // Show loading state
                showLoadingState();
                
                // Try multiple approaches in sequence
                tryMultipleVideoApproaches(videoId, 0);
                
            } else {
                android.util.Log.e("ExerciseVideoPlayer", "Could not extract video ID for retry");
                showVideoError("Video ID extraction failed. Using animation instead.");
                forceShowAnimation();
            }
        } else {
            android.util.Log.w("ExerciseVideoPlayer", "No exercise or video URL available for retry");
            forceShowAnimation();
        }
    }
    
    /**
     * Try multiple video loading approaches sequentially
     */
    private void tryMultipleVideoApproaches(String videoId, int attemptNumber) {
        android.util.Log.d("ExerciseVideoPlayer", "Trying video approach #" + (attemptNumber + 1) + " for video ID: " + videoId);
        
        String html;
        switch (attemptNumber) {
            case 0:
                // First attempt: YouTube nocookie with minimal parameters
                html = createMinimalYouTubeEmbedHtml(videoId);
                break;
            case 1:
                // Second attempt: Regular YouTube with different parameters
                html = createAlternativeYouTubeEmbedHtml(videoId);
                break;
            case 2:
                // Third attempt: Direct YouTube URL
                html = createDirectYouTubeEmbedHtml(videoId);
                break;
            default:
                // All attempts failed, show animation
                android.util.Log.w("ExerciseVideoPlayer", "All video loading attempts failed, showing animation");
                forceShowAnimation();
                return;
        }
        
        if (webViewVideo != null) {
            webViewVideo.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null);
            
            // Set up timeout for this attempt
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Check if this attempt worked
                    webViewVideo.evaluateJavascript(
                        "javascript:(function() { return window.loaded || false; })()", 
                        new android.webkit.ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                if (value != null && value.equals("false")) {
                                    // This attempt failed, try next approach
                                    android.util.Log.w("ExerciseVideoPlayer", "Video approach #" + (attemptNumber + 1) + " failed, trying next");
                                    tryMultipleVideoApproaches(videoId, attemptNumber + 1);
                                } else {
                                    android.util.Log.d("ExerciseVideoPlayer", "Video approach #" + (attemptNumber + 1) + " succeeded!");
                                }
                            }
                        }
                    );
                }
            }, 6000); // Wait 6 seconds for each attempt
        }
    }
    
    /**
     * Create minimal YouTube embed HTML (most compatible)
     */
    private String createMinimalYouTubeEmbedHtml(String videoId) {
        return "<!DOCTYPE html>" +
               "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
               "<style>*{margin:0;padding:0}html,body{width:100%;height:100%;background:#000}" +
               ".video-wrapper{position:relative;width:100%;height:0;padding-bottom:56.25%}" +
               "iframe{position:absolute;top:0;left:0;width:100%;height:100%;border:none}" +
               ".loading{position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);color:white;text-align:center}" +
               "</style></head><body>" +
               "<div class='loading' id='loading'>🎥 Loading video (Minimal)...</div>" +
               "<div class='video-wrapper' id='videoWrapper' style='display:none'>" +
               "<iframe id='youtubePlayer' src='https://www.youtube-nocookie.com/embed/" + videoId + "?controls=1&rel=0' " +
               "frameborder='0' allowfullscreen></iframe></div>" +
               "<script>var loaded=false;" +
               "function showVideo(){if(!loaded){loaded=true;document.getElementById('loading').style.display='none';" +
               "document.getElementById('videoWrapper').style.display='block'}}" +
               "document.getElementById('youtubePlayer').onload=function(){setTimeout(showVideo,1000)};" +
               "setTimeout(function(){if(!loaded)showVideo()},4000);</script></body></html>";
    }
    
    /**
     * Create direct YouTube embed HTML (fallback approach)
     */
    private String createDirectYouTubeEmbedHtml(String videoId) {
        return "<!DOCTYPE html>" +
               "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
               "<style>*{margin:0;padding:0}html,body{width:100%;height:100%;background:#000}" +
               ".video-wrapper{position:relative;width:100%;height:0;padding-bottom:56.25%}" +
               "iframe{position:absolute;top:0;left:0;width:100%;height:100%;border:none}" +
               ".loading{position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);color:white;text-align:center}" +
               "</style></head><body>" +
               "<div class='loading' id='loading'>🎥 Loading video (Direct)...</div>" +
               "<div class='video-wrapper' id='videoWrapper' style='display:none'>" +
               "<iframe id='youtubePlayer' src='https://www.youtube.com/embed/" + videoId + "' " +
               "frameborder='0' allowfullscreen></iframe></div>" +
               "<script>var loaded=false;" +
               "function showVideo(){if(!loaded){loaded=true;document.getElementById('loading').style.display='none';" +
               "document.getElementById('videoWrapper').style.display='block'}}" +
               "document.getElementById('youtubePlayer').onload=function(){setTimeout(showVideo,1000)};" +
               "setTimeout(function(){if(!loaded)showVideo()},3000);</script></body></html>";
    }
    
    /**
     * Create alternative YouTube embed HTML with different parameters
     */
    private String createAlternativeYouTubeEmbedHtml(String videoId) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<meta charset='UTF-8'>" +
               "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
               "<style>" +
               "* { margin: 0; padding: 0; }" +
               "html, body { width: 100%; height: 100%; background: #000; }" +
               ".video-container { position: relative; width: 100%; height: 0; padding-bottom: 56.25%; }" +
               "iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: none; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "<div class='video-container'>" +
               "<iframe src='https://www.youtube-nocookie.com/embed/" + videoId + "?autoplay=0&controls=1&rel=0&modestbranding=1&playsinline=1' " +
               "frameborder='0' allowfullscreen allow='encrypted-media'></iframe>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
    
    /**
     * Show video view
     */
    private void showVideo() {
        if (webViewVideo != null) {
            webViewVideo.setVisibility(View.VISIBLE);
        }
        animationView.setVisibility(View.GONE);
        layoutVideoError.setVisibility(View.GONE);
        if (btnFloatingVideo != null) {
            btnFloatingVideo.setVisibility(View.GONE);
        }
    }
    
    /**
     * Show animation fallback
     */
    public void forceShowAnimation() {
        if (currentExercise != null) {
            if (loadAnimation(currentExercise.getId())) {
                showAnimation();
            } else {
                showInstructions();
            }
        }
    }
    
    /**
     * Load animation for exercise
     */
    private boolean loadAnimation(String exerciseId) {
        int animationResource = getAnimationResource(exerciseId);
        if (animationResource != 0) {
            animationView.setImageResource(animationResource);
            currentAnimation = (AnimationDrawable) animationView.getDrawable();
            return true;
        }
        return false;
    }
    
    /**
     * Get animation resource for exercise ID
     */
    private int getAnimationResource(String exerciseId) {
        switch (exerciseId) {
            case "ex_001": return R.drawable.animation_wrist_flex;
            case "ex_002": return R.drawable.animation_wrist_rotation;
            case "ex_003": return R.drawable.animation_thumb_opposition;
            case "ex_004": return R.drawable.thumb;
            case "ex_005": return R.drawable.animation_finger_flex;
            case "ex_006": return R.drawable.animation_finger_extension;
            case "ex_007": return R.drawable.finger;
            case "ex_008": return R.drawable.animation_knee_flex;
            case "ex_009": return R.drawable.glute;
            case "ex_010": return R.drawable.glute;
            default: return 0;
        }
    }
    
    /**
     * Show animation view
     */
    private void showAnimation() {
        if (webViewVideo != null) {
            webViewVideo.setVisibility(View.GONE);
        }
        animationView.setVisibility(View.VISIBLE);
        layoutVideoError.setVisibility(View.GONE);
        
        if (currentAnimation != null) {
            currentAnimation.start();
        }
    }
    
    /**
     * Show instructions fallback
     */
    private void showInstructions() {
        if (webViewVideo != null) {
            webViewVideo.setVisibility(View.GONE);
        }
        animationView.setVisibility(View.GONE);
        layoutVideoError.setVisibility(View.VISIBLE);
        
        if (currentExercise != null && currentExercise.getInstructions() != null) {
            StringBuilder instructions = new StringBuilder();
            for (int i = 0; i < currentExercise.getInstructions().size(); i++) {
                instructions.append((i + 1)).append(". ").append(currentExercise.getInstructions().get(i));
                if (i < currentExercise.getInstructions().size() - 1) {
                    instructions.append("\n");
                }
            }
            if (txtErrorMessage != null) {
                txtErrorMessage.setText(instructions.toString());
            }
        }
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        if (webViewVideo != null) {
            webViewVideo.setVisibility(View.GONE);
        }
        animationView.setVisibility(View.GONE);
        layoutVideoError.setVisibility(View.VISIBLE);
        if (txtErrorMessage != null) {
            txtErrorMessage.setText(message);
        }
    }
    
    /**
     * Get current exercise
     */
    public RAExercise getCurrentExercise() {
        return currentExercise;
    }
    
    /**
     * Start animation
     */
    public void startAnimation() {
        if (currentAnimation != null && !currentAnimation.isRunning()) {
            currentAnimation.start();
        }
    }
    
    /**
     * Stop animation
     */
    public void stopAnimation() {
        if (currentAnimation != null && currentAnimation.isRunning()) {
            currentAnimation.stop();
        }
    }
    
    /**
     * Check if animation is playing
     */
    public boolean isAnimationPlaying() {
        return currentAnimation != null && currentAnimation.isRunning();
    }
    
    /**
     * Get current video player state for debugging
     */
    public String getPlayerState() {
        StringBuilder state = new StringBuilder();
        state.append("WebView visible: ").append(webViewVideo != null && webViewVideo.getVisibility() == View.VISIBLE).append("\n");
        state.append("Animation visible: ").append(animationView.getVisibility() == View.VISIBLE).append("\n");
        state.append("Error layout visible: ").append(layoutVideoError.getVisibility() == View.VISIBLE).append("\n");
        state.append("Current exercise: ").append(currentExercise != null ? currentExercise.getName() : "None").append("\n");
        state.append("Video URL: ").append(currentExercise != null ? currentExercise.getVideoUrl() : "None");
        
        return state.toString();
    }
    
    /**
     * Force load video - public method for external calls
     */
    public void forceLoadVideo() {
        if (currentExercise != null && currentExercise.getVideoUrl() != null && !currentExercise.getVideoUrl().isEmpty()) {
            android.util.Log.d("ExerciseVideoPlayer", "Force loading video: " + currentExercise.getVideoUrl());
            
            // Clear WebView and reload
            if (webViewVideo != null) {
                webViewVideo.clearCache(true);
                webViewVideo.clearHistory();
                webViewVideo.loadUrl("about:blank");
                
                // Wait a moment then reload the video
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadYouTubeVideo(currentExercise.getVideoUrl());
                    }
                }, 500);
            }
        } else {
            android.util.Log.w("ExerciseVideoPlayer", "Cannot force load video - no URL available");
            forceShowAnimation();
        }
    }
    
    /**
     * Check if video is currently loading or loaded
     */
    public boolean isVideoLoaded() {
        return webViewVideo != null && webViewVideo.getVisibility() == View.VISIBLE;
    }
    
    /**
     * Check if video is available for current exercise
     */
    public boolean hasVideoUrl() {
        return currentExercise != null && 
               currentExercise.getVideoUrl() != null && 
               !currentExercise.getVideoUrl().isEmpty();
    }
}