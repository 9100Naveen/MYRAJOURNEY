package com.example.myrajourney.exercise.tracking;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import com.example.myrajourney.exercise.models.ExerciseTemplate;
import com.example.myrajourney.exercise.models.PoseFrame;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Processes reference exercise videos to extract pose sequences for live
 * tracking comparison
 */
public class ReferenceVideoProcessor {
    private static final String TAG = "ReferenceVideoProcessor";

    private Context context;
    private PoseDetector poseDetector;
    private ExecutorService processingExecutor;

    // Processing parameters
    private static final int TARGET_FPS = 10; // Extract 10 frames per second
    private static final int MAX_FRAMES = 300; // Maximum frames to process (30 seconds at 10fps)

    public interface ProcessingCallback {
        void onProcessingStarted(String videoUrl);

        void onFrameProcessed(int frameIndex, int totalFrames, PoseFrame poseFrame);

        void onProcessingComplete(ExerciseTemplate template);

        void onProcessingError(String error);
    }

    public ReferenceVideoProcessor(Context context) {
        this.context = context;
        this.processingExecutor = Executors.newSingleThreadExecutor();

        // Initialize pose detector with accurate mode for reference processing
        AccuratePoseDetectorOptions options = new AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
                .build();

        this.poseDetector = PoseDetection.getClient(options);
    }

    /**
     * Process reference video to create exercise template
     */
    public void processReferenceVideo(String videoUrl, String exerciseId, String exerciseName,
            ProcessingCallback callback) {

        processingExecutor.execute(() -> {
            try {
                callback.onProcessingStarted(videoUrl);

                // Extract frames from video
                List<VideoFrame> videoFrames = extractVideoFrames(videoUrl);

                if (videoFrames.isEmpty()) {
                    callback.onProcessingError("No frames could be extracted from video");
                    return;
                }

                // Process each frame for pose detection
                List<PoseFrame> referencePoses = new ArrayList<>();

                for (int i = 0; i < videoFrames.size(); i++) {
                    VideoFrame videoFrame = videoFrames.get(i);

                    try {
                        // Detect pose in frame
                        InputImage image = InputImage.fromBitmap(videoFrame.bitmap, 0);
                        Task<Pose> poseTask = poseDetector.process(image);

                        // Wait for pose detection to complete
                        Pose pose = Tasks.await(poseTask);

                        if (pose != null && !pose.getAllPoseLandmarks().isEmpty()) {
                            PoseFrame poseFrame = new PoseFrame(pose, videoFrame.timestampMs);

                            // Only add frames with good pose detection confidence
                            if (poseFrame.isValidForAnalysis()) {
                                referencePoses.add(poseFrame);

                                // Notify progress
                                final int frameIndex = i;
                                final PoseFrame finalPoseFrame = poseFrame;
                                callback.onFrameProcessed(frameIndex, videoFrames.size(), finalPoseFrame);
                            }
                        }

                    } catch (Exception e) {
                        Log.w(TAG, "Failed to process frame " + i + ": " + e.getMessage());
                    }
                }

                if (referencePoses.isEmpty()) {
                    callback.onProcessingError("No valid poses detected in video");
                    return;
                }

                // Create exercise template
                ExerciseTemplate template = createExerciseTemplate(exerciseId, exerciseName,
                        videoUrl, referencePoses);

                callback.onProcessingComplete(template);

            } catch (Exception e) {
                Log.e(TAG, "Error processing reference video", e);
                callback.onProcessingError("Failed to process video: " + e.getMessage());
            }
        });
    }

    /**
     * Extract frames from video at specified intervals
     */
    private List<VideoFrame> extractVideoFrames(String videoUrl) {
        List<VideoFrame> frames = new ArrayList<>();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try {
            // Set video source
            if (videoUrl.startsWith("http")) {
                retriever.setDataSource(videoUrl);
            } else if (videoUrl.startsWith("file:///android_asset/")) {
                String assetPath = videoUrl.replace("file:///android_asset/", "");
                android.content.res.AssetFileDescriptor afd = context.getAssets().openFd(assetPath);
                retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
            } else if (videoUrl.startsWith("file://")) {
                retriever.setDataSource(videoUrl.replace("file://", ""));
            } else {
                retriever.setDataSource(context, Uri.parse(videoUrl));
            }

            // Get video duration
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long durationMs = Long.parseLong(durationStr);

            // Calculate frame extraction interval
            long intervalMs = 1000 / TARGET_FPS; // Extract every 100ms for 10fps

            Log.d(TAG, "Video duration: " + durationMs + "ms, extracting frames every " + intervalMs + "ms");

            // Extract frames
            for (long timeMs = 0; timeMs < durationMs && frames.size() < MAX_FRAMES; timeMs += intervalMs) {
                try {
                    Bitmap bitmap = retriever.getFrameAtTime(timeMs * 1000, // Convert to microseconds
                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

                    if (bitmap != null) {
                        // Resize bitmap for processing efficiency
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 640, 480, true);
                        frames.add(new VideoFrame(resizedBitmap, timeMs));

                        // Clean up original bitmap if different
                        if (bitmap != resizedBitmap) {
                            bitmap.recycle();
                        }
                    }

                } catch (Exception e) {
                    Log.w(TAG, "Failed to extract frame at " + timeMs + "ms: " + e.getMessage());
                }
            }

            Log.d(TAG, "Extracted " + frames.size() + " frames from video");

        } catch (Exception e) {
            Log.e(TAG, "Error extracting video frames", e);
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                Log.w(TAG, "Error releasing MediaMetadataRetriever", e);
            }
        }

        return frames;
    }

    /**
     * Create exercise template from processed poses
     */
    private ExerciseTemplate createExerciseTemplate(String exerciseId, String exerciseName,
            String videoUrl, List<PoseFrame> referencePoses) {

        ExerciseTemplate template = new ExerciseTemplate();
        template.setExerciseId(exerciseId);
        template.setExerciseName(exerciseName);
        template.setVideoUrl(videoUrl);
        template.setReferencePoses(referencePoses);

        // Calculate exercise duration
        if (!referencePoses.isEmpty()) {
            long startTime = referencePoses.get(0).getTimestamp();
            long endTime = referencePoses.get(referencePoses.size() - 1).getTimestamp();
            template.setExpectedDurationSeconds((int) ((endTime - startTime) / 1000));
        }

        // Determine exercise type based on movement analysis
        ExerciseTemplate.ExerciseType exerciseType = analyzeExerciseType(referencePoses);
        template.setExerciseType(exerciseType);

        // Set difficulty based on movement complexity
        template.setDifficultyLevel(ExerciseTemplate.DifficultyLevel.BEGINNER);

        // Add target joints based on exercise type
        addTargetJoints(template, exerciseType);

        // Analyze and set joint angle ranges
        analyzeJointAngleRanges(template, referencePoses);

        // Identify exercise phases
        identifyExercisePhases(template, referencePoses);

        Log.d(TAG, "Created exercise template: " + exerciseName +
                " with " + referencePoses.size() + " reference poses");

        return template;
    }

    /**
     * Analyze movement patterns to determine exercise type
     */
    private ExerciseTemplate.ExerciseType analyzeExerciseType(List<PoseFrame> poses) {
        // Simple heuristic based on primary movement joints
        // In a real implementation, this would use more sophisticated analysis

        // For now, return shoulder roll as default
        // TODO: Implement movement pattern analysis
        return ExerciseTemplate.ExerciseType.SHOULDER_ROLL;
    }

    /**
     * Add target joints based on exercise type
     */
    private void addTargetJoints(ExerciseTemplate template, ExerciseTemplate.ExerciseType exerciseType) {
        switch (exerciseType) {
            case SHOULDER_ROLL:
                template.getTargetJoints().add("LEFT_SHOULDER");
                template.getTargetJoints().add("RIGHT_SHOULDER");
                template.getTargetJoints().add("LEFT_ELBOW");
                template.getTargetJoints().add("RIGHT_ELBOW");
                break;
            case FINGER_FLEXION:
                template.getTargetJoints().add("LEFT_WRIST");
                template.getTargetJoints().add("RIGHT_WRIST");
                break;
            case ANKLE_CIRCLE:
                template.getTargetJoints().add("LEFT_ANKLE");
                template.getTargetJoints().add("RIGHT_ANKLE");
                break;
            default:
                template.getTargetJoints().add("LEFT_ELBOW");
                template.getTargetJoints().add("RIGHT_ELBOW");
                break;
        }
    }

    /**
     * Analyze joint angle ranges from reference poses
     */
    private void analyzeJointAngleRanges(ExerciseTemplate template, List<PoseFrame> poses) {
        // TODO: Implement joint angle range analysis
        // For now, set default ranges

        for (String joint : template.getTargetJoints()) {
            if (joint.contains("SHOULDER")) {
                template.getJointAngleRanges().put(joint,
                        new ExerciseTemplate.JointAngleRange(30, 150, 90));
            } else if (joint.contains("ELBOW")) {
                template.getJointAngleRanges().put(joint,
                        new ExerciseTemplate.JointAngleRange(45, 180, 120));
            } else if (joint.contains("WRIST")) {
                template.getJointAngleRanges().put(joint,
                        new ExerciseTemplate.JointAngleRange(0, 90, 45));
            }
        }
    }

    /**
     * Identify key phases in the exercise
     */
    private void identifyExercisePhases(ExerciseTemplate template, List<PoseFrame> poses) {
        if (poses.size() < 3)
            return;

        // Simple phase identification: start, middle, end
        int totalFrames = poses.size();

        ExerciseTemplate.ExercisePhase startPhase = new ExerciseTemplate.ExercisePhase(
                "Start Position", 0, totalFrames / 4, "Initial exercise position");

        ExerciseTemplate.ExercisePhase middlePhase = new ExerciseTemplate.ExercisePhase(
                "Movement Phase", totalFrames / 4, (3 * totalFrames) / 4, "Main exercise movement");

        ExerciseTemplate.ExercisePhase endPhase = new ExerciseTemplate.ExercisePhase(
                "End Position", (3 * totalFrames) / 4, totalFrames - 1, "Final exercise position");

        template.getExercisePhases().add(startPhase);
        template.getExercisePhases().add(middlePhase);
        template.getExercisePhases().add(endPhase);
    }

    /**
     * Release resources
     */
    public void release() {
        if (poseDetector != null) {
            poseDetector.close();
        }

        if (processingExecutor != null && !processingExecutor.isShutdown()) {
            processingExecutor.shutdown();
        }
    }

    /**
     * Video frame data class
     */
    private static class VideoFrame {
        final Bitmap bitmap;
        final long timestampMs;

        VideoFrame(Bitmap bitmap, long timestampMs) {
            this.bitmap = bitmap;
            this.timestampMs = timestampMs;
        }
    }
}