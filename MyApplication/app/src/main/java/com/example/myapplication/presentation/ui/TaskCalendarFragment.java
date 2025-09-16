package com.example.myapplication.presentation.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.database.TaskRepositorySQLiteImpl;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.domain.models.Task;
import com.example.myapplication.presentation.ui.adapters.TaskAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TaskCalendarFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView rvTasks;
    private TaskRepository taskRepository;
    private TaskAdapter taskAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_calendar, container, false);

        if (getContext() != null) {
            taskRepository = new TaskRepositorySQLiteImpl(getContext());
        }

        calendarView = view.findViewById(R.id.calendarView);
        rvTasks = view.findViewById(R.id.rvTasks);

        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));

        calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            loadTasksForDate(selectedDate);
        });

        loadTasksForDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        return view;
    }

    private void loadTasksForDate(String date) {
        List<Task> allTasks = taskRepository.getAllTasks();

        List<Task> tasksForDate = allTasks.stream()
                .filter(task -> {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    if (task.getStartDate() != null) {
                        String taskDate = dateFormat.format(task.getStartDate());
                        return taskDate.equals(date);
                    }
                    return false;
                })
                .collect(Collectors.toList());

        if (taskAdapter == null) {
            taskAdapter = new TaskAdapter(tasksForDate, getContext()); // Dodaj 'getContext()'
            rvTasks.setAdapter(taskAdapter);
        } else {
            taskAdapter.updateTasks(tasksForDate);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasksForDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
    }
}