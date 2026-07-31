package com.example.myrajourney.exercise.tracking;

import android.util.Log;

/**
 * Tracks exercise repetitions based on joint movement phases.
 */
public class RepetitionCounter {
    private static final String TAG = "RepetitionCounter";

    private int totalReps = 0;
    private int correctReps = 0;
    private int incorrectReps = 0;

    // Thresholds for RA-friendly tracking
    private static final double FLEXION_THRESHOLD = 100.0;
    private static final double EXTENSION_THRESHOLD = 150.0;

    private boolean isFullyExtended = true;
    private boolean isFullyFlexed = false;

    public enum Phase {
        START,
        FLEXING,
        HOLDING,
        EXTENDING,
        COMPLETE
    }

    private Phase currentPhase = Phase.START;

    public void onNewAngle(double currentAngle) {
        switch (currentPhase) {
            case START:
                if (currentAngle > EXTENSION_THRESHOLD) {
                    currentPhase = Phase.FLEXING;
                }
                break;

            case FLEXING:
                if (currentAngle < FLEXION_THRESHOLD) {
                    currentPhase = Phase.HOLDING;
                    isFullyFlexed = true;
                }
                break;

            case HOLDING:
                if (currentAngle > FLEXION_THRESHOLD + 10) {
                    currentPhase = Phase.EXTENDING;
                }
                break;

            case EXTENDING:
                if (currentAngle > EXTENSION_THRESHOLD) {
                    totalReps++;
                    if (isFullyFlexed) {
                        correctReps++;
                        Log.d(TAG, "Correct rep completed. Total: " + totalReps);
                    } else {
                        incorrectReps++;
                        Log.d(TAG, "Incorrect rep completed (insufficient flexion). Total: " + totalReps);
                    }

                    // Reset for next rep
                    currentPhase = Phase.FLEXING;
                    isFullyFlexed = false;
                }
                break;
            case COMPLETE:
                break;
        }
    }

    public int getTotalReps() {
        return totalReps;
    }

    public int getCorrectReps() {
        return correctReps;
    }

    public int getIncorrectReps() {
        return incorrectReps;
    }

    public void reset() {
        totalReps = 0;
        correctReps = 0;
        incorrectReps = 0;
        currentPhase = Phase.START;
        isFullyFlexed = false;
    }
}
