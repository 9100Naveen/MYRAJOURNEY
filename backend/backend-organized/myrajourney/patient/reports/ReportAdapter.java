package com.example.myrajourney.patient.reports;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.data.model.Report;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private Context context;
    private List<Report> reportList;

    public ReportAdapter(Context context, List<Report> reportList) {
        this.context = context;
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null)
            context = parent.getContext();

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_report_patient, parent, false);

        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);

        String title = report.getTitle() != null ? report.getTitle() : "Report";
        String date = report.getCreatedAt() != null ? report.getCreatedAt() : "";
        String file = report.getFileUrl() != null ? report.getFileUrl() : "No file";

        holder.reportName.setText(title);
        holder.reportDate.setText(date);
        holder.reportFile.setText(file);

        String status = report.getStatus() != null ? report.getStatus() : "PENDING";
        String displayStatus = getDisplayStatus(status);
        holder.reportStatus.setText(displayStatus);

        // Color based on status (using backend status values)
        if (status.equalsIgnoreCase("NORMAL") || status.equalsIgnoreCase("REVIEWED")) {
            holder.reportStatus.setBackgroundResource(R.drawable.status_chip_green);
        } else if (status.equalsIgnoreCase("ABNORMAL")) {
            holder.reportStatus.setBackgroundResource(R.drawable.status_chip_red);
        } else {
            holder.reportStatus.setBackgroundResource(R.drawable.status_chip_orange);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportDetailsActivity.class);

            intent.putExtra("patient_name", "You");
            intent.putExtra("report_type", title);
            intent.putExtra("report_date", date);
            intent.putExtra("report_status", report.getStatus());
            intent.putExtra("report_id", report.getId());
            intent.putExtra("report_file", report.getFileUrl());

            // ---------------------------
            // ⭐ FORCE PATIENT MODE
            // ---------------------------
            intent.putExtra("mode", "patient");

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public void updateList(List<Report> newList) {
        this.reportList = newList;
        notifyDataSetChanged();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView reportName, reportDate, reportFile, reportStatus;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);

            reportName = itemView.findViewById(R.id.report_name);
            reportDate = itemView.findViewById(R.id.report_date);
            reportFile = itemView.findViewById(R.id.report_file);
            reportStatus = itemView.findViewById(R.id.report_status_patient);
        }
    }

    /**
     * Convert backend status values to user-friendly display text
     */
    private String getDisplayStatus(String backendStatus) {
        if (backendStatus == null) return "Pending";
        
        switch (backendStatus) {
            case "NORMAL":
                return "Normal";
            case "ABNORMAL":
                return "Abnormal";
            case "REVIEWED":
                return "Reviewed";
            case "PENDING":
                return "Pending";
            case "ARCHIVED":
                return "Archived";
            default:
                return backendStatus; // fallback to original value
        }
    }
}
