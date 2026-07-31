package com.example.myrajourney.exercise.tracking;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

/**
 * Generates JSON and PDF reports for rehabilitation sessions.
 */
public class PerformanceReportGenerator {
    private static final String TAG = "ReportGenerator";

    public static void generateReport(Context context, String exerciseName, long durationSeconds,
            double score, int correctReps, int totalReps,
            double accuracy, String mistakes, String suggestions) {

        generateJsonReport(context, exerciseName, durationSeconds, score, correctReps, totalReps, accuracy, mistakes,
                suggestions);
        generatePdfReport(context, exerciseName, durationSeconds, score, correctReps, totalReps, accuracy, mistakes,
                suggestions);
    }

    private static void generateJsonReport(Context context, String exerciseName, long durationSeconds,
            double score, int correctReps, int totalReps,
            double accuracy, String mistakes, String suggestions) {
        try {
            JSONObject report = new JSONObject();
            report.put("exercise_name", exerciseName);
            report.put("timestamp", new Date().toString());
            report.put("duration_seconds", durationSeconds);
            report.put("performance_score", score);
            report.put("correct_reps", correctReps);
            report.put("total_reps", totalReps);
            report.put("joint_accuracy", accuracy);
            report.put("mistakes_detected", mistakes);
            report.put("suggestions", suggestions);

            String filename = "report_" + System.currentTimeMillis() + ".json";
            File dir = new File(context.getExternalFilesDir(null), "reports");
            if (!dir.exists())
                dir.mkdirs();

            File file = new File(dir, filename);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(report.toString(4).getBytes());
            }
            Log.d(TAG, "JSON Report saved: " + file.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Error generating JSON report", e);
        }
    }

    private static void generatePdfReport(Context context, String exerciseName, long durationSeconds,
            double score, int correctReps, int totalReps,
            double accuracy, String mistakes, String suggestions) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(18);

        int y = 50;
        canvas.drawText("Rehabilitation Performance Report", 150, y, paint);

        y += 40;
        paint.setTextSize(14);
        canvas.drawText("Exercise: " + exerciseName, 50, y, paint);
        y += 20;
        canvas.drawText("Date: " + new Date().toString(), 50, y, paint);
        y += 20;
        canvas.drawText("Duration: " + durationSeconds + " seconds", 50, y, paint);

        y += 40;
        paint.setFakeBoldText(true);
        canvas.drawText("Performance Summary", 50, y, paint);
        paint.setFakeBoldText(false);

        y += 30;
        canvas.drawText("Overall Score: " + String.format("%.1f", score) + "%", 70, y, paint);
        y += 20;
        canvas.drawText("Accuracy: " + String.format("%.1f", accuracy) + "%", 70, y, paint);
        y += 20;
        canvas.drawText("Repetitions: " + correctReps + " correct / " + totalReps + " total", 70, y, paint);

        y += 40;
        paint.setFakeBoldText(true);
        canvas.drawText("Observations", 50, y, paint);
        paint.setFakeBoldText(false);

        y += 30;
        canvas.drawText("Mistakes Detected: " + mistakes, 70, y, paint);
        y += 30;
        canvas.drawText("Suggestions: " + suggestions, 70, y, paint);

        document.finishPage(page);

        String filename = "report_" + System.currentTimeMillis() + ".pdf";
        File dir = new File(context.getExternalFilesDir(null), "reports");
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(dir, filename);
        try {
            document.writeTo(new FileOutputStream(file));
            Log.d(TAG, "PDF Report saved: " + file.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Error generating PDF report", e);
        } finally {
            document.close();
        }
    }
}
