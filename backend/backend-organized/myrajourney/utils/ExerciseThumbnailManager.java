package com.example.myrajourney.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Manages exercise thumbnails for local videos and YouTube videos
 * Provides caching and lazy loading functionality
 */
public class ExerciseThumbnailManager {
    
    private static final String TAG = "ExerciseThumbnailManager";
    private static final String THUMBNAIL_DIR = "exercise_thumbnails";
    private static final int THUMBNAIL_WIDTH = 320;
    private static final int THUMBNAIL_HEIGHT = 180;
    private static final int JPEG_QUALITY = 85;
    
    private Context context;
    private File thumbnailDirectory;
    private Map<String, String> exerciseVideoMap;
    
    public ExerciseThumbnailManager(Context context) {
        this.context = context;
        this.thumbnailDirectory = new File(context.getFilesDir(), THUMBNAIL_DIR);
        
        if (!thumbnailDirectory.exists()) {
            thumbnailDirectory.mkdirs();
        }
        
        initializeExerciseVideoMap();
    }
    
    /**
     * Initialize mapping of exercise IDs to video files
     */
    private void initializeExerciseVideoMap() {
        exerciseVideoMap = new HashMap<>();
        
        // Map exercise IDs to local video files
        exerciseVideoMap.put("ex_001", "ex_001_wrist_flexion.mp4");
        exerciseVideoMap.put("ex_002", "ex_002_wrist_rotation.mp4");
        exerciseVideoMap.put("ex_003", "ex_003_thumb_opposition.mp4");
        exerciseVideoMap.put("ex_004", "ex_004_thumb_flexion.mp4");
        exerciseVideoMap.put("ex_005", "ex_005_finger_flexion.mp4");
        exerciseVideoMap.put("ex_006", "ex_006_finger_extension.mp4");
        exerciseVideoMap.put("ex_007", "ex_007_finger_pinch.mp4");
        exerciseVideoMap.put("ex_008", "ex_008_knee_flexion.mp4");
        exerciseVideoMap.put("ex_009", "ex_009_hip_flexion.mp4");
        exerciseVideoMap.put("ex_010", "ex_010_hip_abduction.mp4");
    }
    
    /**
     * Get thumbnail for exercise (async)
     */
    public CompletableFuture<Bitmap> getThumbnailAsync(String exerciseId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check if thumbnail already exists in cache
                Bitmap cachedThumbnail = loadCachedThumbnail(exerciseId);
                if (cachedThumbnail != null) {
                    Log.d(TAG, "Loaded cached thumbnail for " + exerciseId);
                    return cachedThumbnail;
                }
                
                // Generate thumbnail from video
                Bitmap thumbnail = generateThumbnailFromVideo(exerciseId);
                if (thumbnail != null) {
                    // Cache the thumbnail
                    cacheThumbnail(exerciseId, thumbnail);
                    Log.d(TAG, "Generated and cached thumbnail for " + exerciseId);
                    return thumbnail;
                }
                
                // Fallback to default thumbnail
                return getDefaultThumbnail();
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting thumbnail for " + exerciseId, e);
                return getDefaultThumbnail();
            }
        });
    }
    
    /**
     * Get thumbnail synchronously (for immediate display)
     */
    public Bitmap getThumbnail(String exerciseId) {
        try {
            // Try to load from cache first
            Bitmap cachedThumbnail = loadCachedThumbnail(exerciseId);
            if (cachedThumbnail != null) {
                return cachedThumbnail;
            }
            
            // If not cached, return default and generate in background
            generateThumbnailInBackground(exerciseId);
            return getDefaultThumbnail();
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting thumbnail for " + exerciseId, e);
            return getDefaultThumbnail();
        }
    }
    
    /**
     * Load cached thumbnail from storage
     */
    private Bitmap loadCachedThumbnail(String exerciseId) {
        try {
            File thumbnailFile = new File(thumbnailDirectory, exerciseId + ".jpg");
            if (thumbnailFile.exists()) {
                return BitmapFactory.decodeFile(thumbnailFile.getAbsolutePath());
            }
        } catch (Exception e) {
            Log.w(TAG, "Error loading cached thumbnail for " + exerciseId, e);
        }
        return null;
    }
    
    /**
     * Generate thumbnail from local video file
     */
    private Bitmap generateThumbnailFromVideo(String exerciseId) {
        String videoFileName = exerciseVideoMap.get(exerciseId);
        if (videoFileName == null) {
            Log.w(TAG, "No video file mapped for exercise " + exerciseId);
            return null;
        }
        
        try {
            // Get video file from assets
            String assetPath = "android_asset/exercise_videos/" + videoFileName;
            
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, Uri.parse("file:///android_asset/exercise_videos/" + videoFileName));
            
            // Get frame at 2 seconds (or 10% of video duration)
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long duration = durationStr != null ? Long.parseLong(durationStr) : 10000;
            long timeUs = Math.min(2000000, duration * 1000 / 10); // 2 seconds or 10% of duration
            
            Bitmap frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            retriever.release();
            
            if (frame != null) {
                // Resize to standard thumbnail size
                return Bitmap.createScaledBitmap(frame, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, true);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating thumbnail from video for " + exerciseId, e);
        }
        
        return null;
    }
    
    /**
     * Generate thumbnail from YouTube video (if URL is provided)
     */
    private Bitmap generateThumbnailFromYouTube(String youtubeUrl) {
        try {
            // Extract video ID from YouTube URL
            String videoId = extractYouTubeVideoId(youtubeUrl);
            if (videoId == null) {
                return null;
            }
            
            // YouTube thumbnail URL patterns
            String[] thumbnailUrls = {
                "https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg",
                "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg",
                "https://img.youtube.com/vi/" + videoId + "/mqdefault.jpg"
            };
            
            // Try each thumbnail URL
            for (String thumbnailUrl : thumbnailUrls) {
                Bitmap thumbnail = downloadImageFromUrl(thumbnailUrl);
                if (thumbnail != null) {
                    return Bitmap.createScaledBitmap(thumbnail, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, true);
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating thumbnail from YouTube", e);
        }
        
        return null;
    }
    
    /**
     * Extract YouTube video ID from URL
     */
    private String extractYouTubeVideoId(String youtubeUrl) {
        try {
            if (youtubeUrl.contains("youtube.com/watch?v=")) {
                return youtubeUrl.split("v=")[1].split("&")[0];
            } else if (youtubeUrl.contains("youtu.be/")) {
                return youtubeUrl.split("youtu.be/")[1].split("\\?")[0];
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting YouTube video ID", e);
        }
        return null;
    }
    
    /**
     * Download image from URL
     */
    private Bitmap downloadImageFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                input.close();
                return bitmap;
            }
            
        } catch (Exception e) {
            Log.w(TAG, "Error downloading image from " + imageUrl, e);
        }
        
        return null;
    }
    
    /**
     * Cache thumbnail to storage
     */
    private void cacheThumbnail(String exerciseId, Bitmap thumbnail) {
        try {
            File thumbnailFile = new File(thumbnailDirectory, exerciseId + ".jpg");
            FileOutputStream fos = new FileOutputStream(thumbnailFile);
            thumbnail.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
            fos.close();
            
            Log.d(TAG, "Cached thumbnail for " + exerciseId);
            
        } catch (Exception e) {
            Log.e(TAG, "Error caching thumbnail for " + exerciseId, e);
        }
    }
    
    /**
     * Generate thumbnail in background thread
     */
    private void generateThumbnailInBackground(String exerciseId) {
        new Thread(() -> {
            try {
                Bitmap thumbnail = generateThumbnailFromVideo(exerciseId);
                if (thumbnail != null) {
                    cacheThumbnail(exerciseId, thumbnail);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error generating thumbnail in background for " + exerciseId, e);
            }
        }).start();
    }
    
    /**
     * Get default thumbnail for exercises
     */
    private Bitmap getDefaultThumbnail() {
        try {
            // Create a simple default thumbnail
            Bitmap defaultBitmap = Bitmap.createBitmap(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Bitmap.Config.ARGB_8888);
            defaultBitmap.eraseColor(0xFF4CAF50); // Green background
            
            // TODO: Add exercise icon or text overlay
            return defaultBitmap;
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating default thumbnail", e);
            return null;
        }
    }
    
    /**
     * Preload thumbnails for all exercises
     */
    public void preloadThumbnails() {
        new Thread(() -> {
            for (String exerciseId : exerciseVideoMap.keySet()) {
                try {
                    if (loadCachedThumbnail(exerciseId) == null) {
                        Bitmap thumbnail = generateThumbnailFromVideo(exerciseId);
                        if (thumbnail != null) {
                            cacheThumbnail(exerciseId, thumbnail);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error preloading thumbnail for " + exerciseId, e);
                }
            }
            Log.d(TAG, "Thumbnail preloading completed");
        }).start();
    }
    
    /**
     * Clear thumbnail cache
     */
    public void clearCache() {
        try {
            File[] files = thumbnailDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            Log.d(TAG, "Thumbnail cache cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing thumbnail cache", e);
        }
    }
    
    /**
     * Get cache size in bytes
     */
    public long getCacheSize() {
        long size = 0;
        try {
            File[] files = thumbnailDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    size += file.length();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating cache size", e);
        }
        return size;
    }
    
    /**
     * Check if thumbnail exists for exercise
     */
    public boolean hasThumbnail(String exerciseId) {
        File thumbnailFile = new File(thumbnailDirectory, exerciseId + ".jpg");
        return thumbnailFile.exists();
    }
    
    /**
     * Get formatted cache size string
     */
    public String getFormattedCacheSize() {
        long bytes = getCacheSize();
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}