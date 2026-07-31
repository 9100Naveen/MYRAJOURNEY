package com.example.myrajourney.admin.logs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.data.model.Notification;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {

    private final Context context;
    private final List<Notification> data;

    public NotificationsAdapter(Context context, List<Notification> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Notification n = data.get(position);

        // Title
        h.title.setText(n.getTitle() == null ? "No Title" : n.getTitle());

        // Body
        String bodyText = n.getBody();
        h.body.setText(bodyText == null ? "" : bodyText);

        // Timestamp
        h.time.setText(n.getCreatedAt() == null ? "" : n.getCreatedAt());

        // Unread indicator
        h.unreadDot.setVisibility(n.isUnread() ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, body, time;
        View unreadDot;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            body = itemView.findViewById(R.id.body);
            time = itemView.findViewById(R.id.time);
            unreadDot = itemView.findViewById(R.id.unreadDot);
        }
    }
}
