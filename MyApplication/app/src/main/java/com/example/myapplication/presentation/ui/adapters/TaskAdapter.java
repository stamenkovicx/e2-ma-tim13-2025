package com.example.myapplication.presentation.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.domain.models.Task;
import com.example.myapplication.presentation.ui.TaskDetailsActivity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private Context context;

    public TaskAdapter(List<Task> tasks, Context context) {
        this.tasks = tasks;
        this.context = context;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        String taskName = task.getName();
        if ("recurring".equals(task.getFrequency())) {
            taskName += " (ponavljajući)";
        }
        holder.tvTaskName.setText(taskName);
        holder.tvTaskDescription.setText(task.getDescription());
        holder.vTaskColor.setBackgroundColor(task.getCategory().getColor());

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.tvTaskTime.setText(timeFormat.format(task.getExecutionTime()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TaskDetailsActivity.class);

            if (task.getId() != null) {
                intent.putExtra("taskId", task.getId());
                context.startActivity(intent);
            } else {
                Log.e("TaskAdapter", "ID of the task is null!");
                Toast.makeText(context, "Greška: Ne postoji ID za ovaj zadatak.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        public View vTaskColor;
        public TextView tvTaskName;
        public TextView tvTaskDescription;
        public TextView tvTaskTime;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            vTaskColor = itemView.findViewById(R.id.vTaskColor);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvTaskDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvTaskTime = itemView.findViewById(R.id.tvTaskTime);
        }
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }
}