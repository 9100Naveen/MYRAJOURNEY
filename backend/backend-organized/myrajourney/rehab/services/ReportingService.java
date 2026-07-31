package com.example.myrajourney.rehab.services;

import com.example.myrajourney.rehab.models.ExerciseSession;
import com.example.myrajourney.rehab.models.PerformanceReport;
import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for generating and managing performance reports
 */
public interface ReportingService {

    /**
     * Get a performance report for a specific session
     * 
     * @param sessionId The session ID
     * @return CompletableFuture with the performance report
     */
    java.util.concurrent.CompletableFuture<PerformanceReport> getSessionPerformanceReport(String sessionId);

    /**
     * Generate a performance report from session data
     * 
     * @param sessionData The exercise session data
     * @return Generated performance report
     */
    PerformanceReport generateSessionReport(ExerciseSession sessionData);

    /**
     * Get all reports for a patient within a date range
     * 
     * @param patientId The patient's ID
     * @param startDate Start date (inclusive)
     * @param endDate   End date (inclusive)
     * @return List of performance reports
     */
    List<PerformanceReport> getPatientReports(String patientId, LocalDate startDate, LocalDate endDate);

    /**
     * Get all reports for a specific patient (doctor view)
     * 
     * @param doctorId  The doctor's ID
     * @param patientId The patient's ID
     * @return List of performance reports
     */
    List<PerformanceReport> getDoctorPatientReports(String doctorId, String patientId);

    /**
     * Generate progress analysis for a patient
     * 
     * @param patientId The patient's ID
     * @param timeframe The timeframe for analysis
     * @return Progress analysis data
     */
    ProgressAnalysis generateProgressAnalysis(String patientId, Timeframe timeframe);

    /**
     * Save a performance report
     * 
     * @param report The report to save
     * @return true if save was successful
     */
    boolean saveReport(PerformanceReport report);

    /**
     * Get reports filtered by exercise type
     * 
     * @param patientId  The patient's ID
     * @param exerciseId The exercise ID to filter by
     * @return List of reports for the specific exercise
     */
    List<PerformanceReport> getReportsByExercise(String patientId, String exerciseId);

    /**
     * Enum for timeframe options
     */
    enum Timeframe {
        WEEK, MONTH, QUARTER, YEAR
    }

    /**
     * Class representing progress analysis data
     */
    class ProgressAnalysis {
        private float averageAccuracy;
        private float improvementRate;
        private int totalSessions;
        private List<String> insights;

        public ProgressAnalysis(float averageAccuracy, float improvementRate,
                int totalSessions, List<String> insights) {
            this.averageAccuracy = averageAccuracy;
            this.improvementRate = improvementRate;
            this.totalSessions = totalSessions;
            this.insights = insights;
        }

        // Getters
        public float getAverageAccuracy() {
            return averageAccuracy;
        }

        public float getImprovementRate() {
            return improvementRate;
        }

        public int getTotalSessions() {
            return totalSessions;
        }

        public List<String> getInsights() {
            return insights;
        }
    }
}