package com.example.myapplication.presentation.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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

import java.util.List;
import java.util.stream.Collectors;

public class TaskListFragment extends Fragment {

    private RecyclerView rvAllTasks;
    private Spinner spTaskFilter;
    private TaskRepository taskRepository;
    private TaskAdapter taskAdapter;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        if (getContext() != null) {
            // Instanciramo Firebase repozitorijum
            taskRepository = new TaskRepositoryFirebaseImpl();

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                userId = currentUser.getUid();
            } else {
                Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
                return view;
            }
        }

        rvAllTasks = view.findViewById(R.id.rvAllTasks);
        spTaskFilter = view.findViewById(R.id.spTaskFilter);

        rvAllTasks.setLayoutManager(new LinearLayoutManager(getContext()));

        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.task_filter_options, android.R.layout.simple_spinner_item);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTaskFilter.setAdapter(filterAdapter);

        spTaskFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = parent.getItemAtPosition(position).toString();
                loadTasks(selectedFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                loadAllTasks();
            }
        });

        loadAllTasks();

        return view;
    }

    private void loadAllTasks() {
        if (userId == null || userId.isEmpty()) {
            Log.e("TaskListFragment", "User email is null or empty.");
            return;
        }

        // Koristimo asinhroni poziv za učitavanje svih zadataka
        taskRepository.getAllTasks(userId, new TaskRepository.OnTasksLoadedListener() {
            @Override
            public void onSuccess(List<Task> allTasks) {
                // Ažuriramo listu unutar callback-a
                if (taskAdapter == null) {
                    if (getContext() != null) {
                        taskAdapter = new TaskAdapter(allTasks, getContext());
                        rvAllTasks.setAdapter(taskAdapter);
                    }
                } else {
                    taskAdapter.updateTasks(allTasks);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TaskListFragment", "Failed to load tasks.", e);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load tasks.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadTasks(String filter) {
        if (userId == null || userId.isEmpty()) {
            Log.e("TaskListFragment", "User email is null or empty.");
            return;
        }

        // Koristimo asinhroni poziv za učitavanje svih zadataka, a zatim filtriramo lokalno
        taskRepository.getAllTasks(userId, new TaskRepository.OnTasksLoadedListener() {
            @Override
            public void onSuccess(List<Task> allTasks) {
                List<Task> filteredTasks;

                if ("Jednokratni".equals(filter)) {
                    filteredTasks = allTasks.stream()
                            .filter(task -> "one-time".equals(task.getFrequency()))
                            .collect(Collectors.toList());
                } else if ("Ponavljajući".equals(filter)) {
                    filteredTasks = allTasks.stream()
                            .filter(task -> "recurring".equals(task.getFrequency()))
                            .collect(Collectors.toList());
                } else {
                    filteredTasks = allTasks;
                }
                updateTaskList(filteredTasks);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TaskListFragment", "Failed to filter tasks.", e);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to filter tasks.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateTaskList(List<Task> tasks) {
        if (taskAdapter == null) {
            if (getContext() != null) {
                taskAdapter = new TaskAdapter(tasks, getContext());
                rvAllTasks.setAdapter(taskAdapter);
            }
        } else {
            taskAdapter.updateTasks(tasks);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllTasks();
    }
}