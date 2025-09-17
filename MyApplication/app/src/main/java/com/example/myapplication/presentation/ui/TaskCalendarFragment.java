package com.example.myapplication.presentation.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.database.TaskRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.domain.models.Task;
import com.example.myapplication.presentation.ui.adapters.TaskAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TaskCalendarFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView rvTasks;
    private TaskRepository taskRepository;
    private TaskAdapter taskAdapter;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_calendar, container, false);

        if (getContext() != null) {
            taskRepository = new TaskRepositoryFirebaseImpl();

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                userId = currentUser.getUid();
            } else {
                Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
                return view;
            }
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
        if (userId == null || userId.isEmpty()) {
            Log.e("TaskCalendarFragment", "User ID is null or empty.");
            return;
        }

        taskRepository.getAllTasks(userId, new TaskRepository.OnTasksLoadedListener() {
            @Override
            public void onSuccess(List<Task> allTasks) {
                List<Task> tasksForDate = allTasks.stream()
                        .filter(task -> {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                            // Proverava da li je zadatak jednokratan i da li se poklapa sa datumom
                            if ("one-time".equals(task.getFrequency()) && task.getStartDate() != null) {
                                String taskDate = dateFormat.format(task.getStartDate());
                                return taskDate.equals(date);
                            }

                            // Proverava da li je zadatak ponavljajući i da li pada na izabrani datum
                            if ("recurring".equals(task.getFrequency()) && task.getStartDate() != null && task.getEndDate() != null) {
                                try {
                                    Date selectedDate = dateFormat.parse(date);

                                    // Proverava da li je datum unutar opsega ponavljanja
                                    if (selectedDate.after(task.getStartDate()) || selectedDate.equals(task.getStartDate())) {

                                        // Računa razliku u jedinicama (dan, nedelja, mesec)
                                        long diffInMillis = selectedDate.getTime() - task.getStartDate().getTime();
                                        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

                                        switch (task.getIntervalUnit()) {
                                            case "dan":
                                                return diffInDays % task.getInterval() == 0;
                                            case "nedelja":
                                                // Računamo razliku u nedeljama i proveravamo parnost
                                                long diffInWeeks = diffInDays / 7;
                                                return diffInWeeks % task.getInterval() == 0;
                                            case "mesec":
                                                // Složenija logika za mesece
                                                // Preporučujem da se koristi Calendar klasa za preciznije računanje meseci
                                                // Ovde je pojednostavljena logika:
                                                Calendar startCal = Calendar.getInstance();
                                                startCal.setTime(task.getStartDate());
                                                Calendar selectedCal = Calendar.getInstance();
                                                selectedCal.setTime(selectedDate);

                                                int startMonth = startCal.get(Calendar.YEAR) * 12 + startCal.get(Calendar.MONTH);
                                                int selectedMonth = selectedCal.get(Calendar.YEAR) * 12 + selectedCal.get(Calendar.MONTH);

                                                int diffInMonths = selectedMonth - startMonth;
                                                return diffInMonths % task.getInterval() == 0 && selectedCal.get(Calendar.DAY_OF_MONTH) == startCal.get(Calendar.DAY_OF_MONTH);
                                        }
                                    }
                                } catch (ParseException e) {
                                    Log.e("TaskCalendarFragment", "Error parsing date.", e);
                                }
                            }
                            return false;
                        })
                        .collect(Collectors.toList());

                if (taskAdapter == null) {
                    if (getContext() != null) {
                        taskAdapter = new TaskAdapter(tasksForDate, getContext());
                        rvTasks.setAdapter(taskAdapter);
                    }
                } else {
                    taskAdapter.updateTasks(tasksForDate);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TaskCalendarFragment", "Failed to load tasks for selected date.", e);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load tasks.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ažuriramo listu zadataka za trenutno odabrani datum
        if (userId != null) {
            // Ponovno učitavanje za trenutno odabrani datum na kalendaru
            loadTasksForDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(calendarView.getDate())));
        }
    }
}