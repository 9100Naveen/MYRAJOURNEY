package com.example.myrajourney.patient.rehab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.data.model.Rehab; // Forces use of correct model
// ---------------------

import java.util.List;

public class RehabAdapter extends RecyclerView.Adapter<RehabAdapter.RehabViewHolder> {

    private Context context;
    private List<Rehab> rehabList;
    private boolean isDoctorView = false;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Rehab rehab);
    }

    public RehabAdapter(Context context, List<Rehab> rehabList) {
        this.context = context;
        this.rehabList = rehabList;
    }

    public void setDoctorView(boolean isDoctorView) {
        this.isDoctorView = isDoctorView;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    @NonNull
    @Override
    public RehabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rehab, parent, false);
        return new RehabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RehabViewHolder holder, int position) {
        Rehab rehab = rehabList.get(position);

        holder.name.setText(rehab.getName());

        // Show/Hide delete button for doctors
        if (isDoctorView) {
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setOnClickListener(v -> {
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(rehab);
                }
            });
        } else {
            holder.deleteBtn.setVisibility(View.GONE);
        }

        // Launch LocalVideoPlayerActivity on click
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context,
                    com.example.myrajourney.patient.rehab.LocalVideoPlayerActivity.class);
            intent.putExtra("exercise_id", rehab.getId());
            intent.putExtra("exercise_name", rehab.getName());
            intent.putExtra("exercise_description", rehab.getDescription());
            intent.putExtra("video_path", rehab.getVideoUrl());
            intent.putExtra("is_doctor_view", isDoctorView); // Pass this to hide live track button
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return rehabList.size();
    }

    public void filterList(List<Rehab> filteredList) {
        this.rehabList = filteredList;
        notifyDataSetChanged();
    }

    static class RehabViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView deleteBtn;

        public RehabViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.rehab_name);
            deleteBtn = itemView.findViewById(R.id.rehab_delete);
        }
    }
}