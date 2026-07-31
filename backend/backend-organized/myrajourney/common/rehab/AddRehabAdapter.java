package com.example.myrajourney.common.rehab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myrajourney.R;
import com.example.myrajourney.data.model.Rehab;

import java.util.List;

public class AddRehabAdapter extends RecyclerView.Adapter<AddRehabAdapter.AddRehabViewHolder> {

    private Context context;

    private List<Rehab> masterList; // ALL items
    private List<Rehab> filteredList; // FILTERED items (shown on screen)

    public AddRehabAdapter(Context context, List<Rehab> masterList, List<Rehab> filteredList) {
        this.context = context;
        this.masterList = masterList;
        this.filteredList = filteredList;
    }

    @NonNull
    @Override
    public AddRehabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_add_rehab, parent, false);
        return new AddRehabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddRehabViewHolder holder, int position) {

        Rehab rehab = filteredList.get(position);

        holder.name.setText(rehab.getName());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(rehab.isSelected());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            rehab.setSelected(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class AddRehabViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        CheckBox checkBox;

        public AddRehabViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.rehab_name);
            checkBox = itemView.findViewById(R.id.rehab_checkbox);
        }
    }
}
