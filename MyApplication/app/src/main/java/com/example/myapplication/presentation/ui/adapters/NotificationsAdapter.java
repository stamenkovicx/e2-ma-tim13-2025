package com.example.myapplication.presentation.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.domain.models.Notification;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private List<Notification> notifications;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Notification notification);
    }

    public NotificationsAdapter(List<Notification> notifications, OnItemClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.tvNotificationMessage.setText(notification.getMessage());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
        holder.tvNotificationTimestamp.setText(sdf.format(notification.getTimestamp()));

        // Vizuelno istakni nepročitane notifikacije
        if (!notification.getIsRead()) {
            holder.itemView.setBackgroundResource(R.drawable.notification_background_unread);
            //holder.tvNotificationMessage.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black)); // Pročitana
        } else {
            holder.itemView.setBackgroundResource(R.drawable.notification_background);
            //holder.tvNotificationMessage.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black)); // Crna
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvNotificationMessage, tvNotificationTimestamp;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNotificationMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvNotificationTimestamp = itemView.findViewById(R.id.tvNotificationTimestamp);
        }
    }
}