package com.example.myapplication.presentation.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.database.CategoryRepositorySQLiteImpl;
import com.example.myapplication.data.database.TaskRepositorySQLiteImpl;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.domain.models.Task;
import com.example.myapplication.presentation.ui.adapters.TaskAdapter;

import java.util.List;
import java.util.stream.Collectors;

public class TaskListFragment extends Fragment {

    private RecyclerView rvAllTasks;
    private Spinner spTaskFilter;
    private TaskRepository taskRepository;
    private TaskAdapter taskAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        if (getContext() != null) {
            CategoryRepositorySQLiteImpl categoryRepository = new CategoryRepositorySQLiteImpl(getContext());
            taskRepository = new TaskRepositorySQLiteImpl(getContext(), categoryRepository);        }

        rvAllTasks = view.findViewById(R.id.rvAllTasks);
        spTaskFilter = view.findViewById(R.id.spTaskFilter);

        rvAllTasks.setLayoutManager(new LinearLayoutManager(getContext()));

        // Postavljanje adaptera za Spinner
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.task_filter_options, android.R.layout.simple_spinner_item);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTaskFilter.setAdapter(filterAdapter);

        // Listener za promene u Spinneru
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

        // Inicijalno učitavanje svih zadataka
        loadAllTasks();

        return view;
    }

    private void loadAllTasks() {
        List<Task> allTasks = taskRepository.getAllTasks();
        if (taskAdapter == null) {
            if (getContext() != null) {

                taskAdapter = new TaskAdapter(allTasks, getContext());
                rvAllTasks.setAdapter(taskAdapter);
            }
        } else {
            taskAdapter.updateTasks(allTasks);
        }
    }

    private void loadTasks(String filter) {
        List<Task> filteredTasks;
        List<Task> allTasks = taskRepository.getAllTasks();

        if (filter.equals("Jednokratni")) {
            filteredTasks = allTasks.stream()
                    .filter(task -> "one-time".equals(task.getFrequency()))
                    .collect(Collectors.toList());
        } else if (filter.equals("Ponavljajući")) {
            filteredTasks = allTasks.stream()
                    .filter(task -> "recurring".equals(task.getFrequency()))
                    .collect(Collectors.toList());
        } else {
            // "Svi zadaci"
            filteredTasks = allTasks;
        }

        updateTaskList(filteredTasks);
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