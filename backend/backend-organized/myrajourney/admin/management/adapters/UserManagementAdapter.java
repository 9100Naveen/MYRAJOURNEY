package com.example.myrajourney.admin.management.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.data.model.User;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying users in admin management interface
 * Supports both doctors and patients with different layouts
 */
public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.UserViewHolder> {
    
    private Context context;
    private List<User> userList;
    private String userType;
    private OnUserActionListener listener;
    
    public interface OnUserActionListener {
        void onEditUser(User user);
        void onDeleteUser(User user);
        void onViewUserDetails(User user);
        void onToggleUserStatus(User user);
    }
    
    public UserManagementAdapter(Context context, List<User> userList, String userType, OnUserActionListener listener) {
        this.context = context;
        this.userList = userList;
        this.userType = userType;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_management, parent, false);
        return new UserViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        
        // Basic user info
        holder.tvName.setText(user.getName() != null ? user.getName() : "Unknown");
        holder.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "No email");
        holder.tvRole.setText(user.getRole() != null ? user.getRole() : "Unknown Role");
        
        // User status
        if (user.isActive()) {
            holder.tvStatus.setText("Active");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.success_color));
            holder.ivStatusIndicator.setImageResource(R.drawable.ic_check_circle);
            holder.ivStatusIndicator.setColorFilter(context.getResources().getColor(R.color.success_color));
        } else {
            holder.tvStatus.setText("Inactive");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.error_color));
            holder.ivStatusIndicator.setImageResource(R.drawable.ic_cancel);
            holder.ivStatusIndicator.setColorFilter(context.getResources().getColor(R.color.error_color));
        }
        
        // Additional info based on user type
        if ("DOCTOR".equals(userType)) {
            holder.tvAdditionalInfo.setText("Specialization: " + 
                (user.getSpecialization() != null ? user.getSpecialization() : "General"));
        } else if ("PATIENT".equals(userType)) {
            holder.tvAdditionalInfo.setText("Age: " + 
                (user.getAge() != null ? user.getAge() : "Not specified"));
        }
        
        // Registration date
        if (user.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            holder.tvRegistrationDate.setText("Registered: " + sdf.format(user.getCreatedAt()));
        } else {
            holder.tvRegistrationDate.setText("Registration date unknown");
        }
        
        // Set user type icon
        if ("DOCTOR".equals(user.getRole())) {
            holder.ivUserIcon.setImageResource(R.drawable.ic_doctor);
        } else if ("PATIENT".equals(user.getRole())) {
            holder.ivUserIcon.setImageResource(R.drawable.ic_patient);
        } else {
            holder.ivUserIcon.setImageResource(R.drawable.ic_person_default);
        }
        
        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewUserDetails(user);
            }
        });
        
        holder.ivEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditUser(user);
            }
        });
        
        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteUser(user);
            }
        });
        
        holder.ivToggleStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onToggleUserStatus(user);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return userList.size();
    }
    
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvEmail;
        TextView tvRole;
        TextView tvStatus;
        TextView tvAdditionalInfo;
        TextView tvRegistrationDate;
        ImageView ivUserIcon;
        ImageView ivStatusIndicator;
        ImageView ivEdit;
        ImageView ivDelete;
        ImageView ivToggleStatus;
        
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvName = itemView.findViewById(R.id.tv_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvRole = itemView.findViewById(R.id.tv_role);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvAdditionalInfo = itemView.findViewById(R.id.tv_additional_info);
            tvRegistrationDate = itemView.findViewById(R.id.tv_registration_date);
            ivUserIcon = itemView.findViewById(R.id.iv_user_icon);
            ivStatusIndicator = itemView.findViewById(R.id.iv_status_indicator);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
            ivToggleStatus = itemView.findViewById(R.id.iv_toggle_status);
        }
    }
}