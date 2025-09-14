package com.example.myapplication.presentation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.database.TaskRepositorySQLiteImpl;
import com.example.myapplication.data.database.UserRepositorySQLiteImpl;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.domain.models.Task;
import com.example.myapplication.domain.models.TaskStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskDetailsActivity extends AppCompatActivity {

    private Task task;
    private TaskRepository taskRepository;

    private TextView tvTaskName, tvTaskDescription, tvCategory, tvFrequency, tvDates, tvExecutionTime, tvDifficulty, tvImportance;
    private View vCategoryColor;
    private Button btnEdit, btnDelete;
    private Button btnComplete, btnCancel, btnPause, btnActivate;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        taskRepository = new TaskRepositorySQLiteImpl(this);

        // Preuzmi Task objekat iz Intent-a
        if (getIntent().getSerializableExtra("task") != null) {
            task = (Task) getIntent().getSerializableExtra("task");
        }

        tvTaskName = findViewById(R.id.tvTaskName);
        tvTaskDescription = findViewById(R.id.tvTaskDescription);
        vCategoryColor = findViewById(R.id.vCategoryColor);
        tvCategory = findViewById(R.id.tvCategory);
        tvFrequency = findViewById(R.id.tvFrequency);
        tvDates = findViewById(R.id.tvDates);
        tvExecutionTime = findViewById(R.id.tvExecutionTime);
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvImportance = findViewById(R.id.tvImportance);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        btnComplete = findViewById(R.id.btnComplete);
        btnCancel = findViewById(R.id.btnCancel);
        btnPause = findViewById(R.id.btnPause);
        btnActivate = findViewById(R.id.btnActivate);

        if (task != null) {
            displayTaskDetails(task);
            updateStatusButtons();
        }


        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditTaskActivity.class);
            intent.putExtra("task", task);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> {
            deleteTask();
        });

        btnComplete.setOnClickListener(v -> {
            // Promeni status lokalno
            updateTaskStatus(TaskStatus.URAĐEN);

            // Dodaj XP korisniku
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String email = currentUser.getEmail();
                UserRepositorySQLiteImpl userRepo = new UserRepositorySQLiteImpl(this);
                userRepo.completeTaskAndAddXp(email, task);
            }
        });
        btnCancel.setOnClickListener(v -> updateTaskStatus(TaskStatus.OTKAZAN));
        btnPause.setOnClickListener(v -> updateTaskStatus(TaskStatus.PAUZIRAN));
        btnActivate.setOnClickListener(v -> updateTaskStatus(TaskStatus.AKTIVAN));
    }

    private void displayTaskDetails(Task task) {
        tvTaskName.setText(task.getName());
        tvTaskDescription.setText(task.getDescription());
        vCategoryColor.setBackgroundColor(task.getCategory().getColor());
        tvCategory.setText(String.format("Kategorija: %s", task.getCategory().getName()));

        String frequencyText = task.getFrequency();
        if ("recurring".equals(task.getFrequency())) {
            frequencyText = String.format("Ponavljajući (svaki %d. %s)", task.getInterval(), task.getIntervalUnit());
        } else {
            frequencyText = "Jednokratan";
        }
        tvFrequency.setText(String.format("Učestalost: %s", frequencyText));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String startDate = task.getStartDate() != null ? dateFormat.format(task.getStartDate()) : "Nije postavljeno";
        String endDate = task.getEndDate() != null ? dateFormat.format(task.getEndDate()) : "Nije postavljeno";
        tvDates.setText(String.format("Datum: %s - %s", startDate, endDate));

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String executionTime = task.getExecutionTime() != null ? timeFormat.format(task.getExecutionTime()) : "Nije postavljeno";
        tvExecutionTime.setText(String.format("Vreme izvršenja: %s", executionTime));

        tvDifficulty.setText(String.format("Težina: %s (%d XP)", task.getDifficulty().getSerbianName(), task.getDifficulty().getXpValue()));
        tvImportance.setText(String.format("Bitnost: %s (%d XP)", task.getImportance().getSerbianName(), task.getImportance().getXpValue()));
    }
    private void deleteTask() {
        if (task != null) {
            // nije moguce obrisati zavrsene zadatke
            if (task.getStatus() == TaskStatus.URAĐEN) {
                Toast.makeText(this, "Ne možete obrisati urađen zadatak.", Toast.LENGTH_SHORT).show();
            } else {
                int rowsDeleted = taskRepository.deleteTask(task.getId());
                if (rowsDeleted > 0) {
                    Toast.makeText(this, "Zadatak uspešno obrisan.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Greška prilikom brisanja zadatka.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (task != null) {
            // Ponovo ucitaj zadatak iz baze da bi dobio najnovije podatke
            Task updatedTask = taskRepository.getTaskById(task.getId());
            if (updatedTask != null) {
                this.task = updatedTask;

                checkTaskExpiration(task);

                displayTaskDetails(this.task);
                updateStatusButtons();
            }
        }
    }

    private void checkTaskExpiration(Task task) {
        if (task.getStatus() == TaskStatus.AKTIVAN || task.getStatus() == TaskStatus.PAUZIRAN) {
            if (task.getStartDate() != null) {
                long diffMillis = new java.util.Date().getTime() - task.getStartDate().getTime();
                long diffDays = diffMillis / (24 * 60 * 60 * 1000);

                if (diffDays > 3) {
                    task.setStatus(TaskStatus.NEURAĐEN);
                    Executor executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> taskRepository.updateTask(task));
                }
            }
        }
    }

    private void updateTaskStatus(TaskStatus newStatus) {
        task.setStatus(newStatus);
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            taskRepository.updateTask(task);
            runOnUiThread(() -> {
                Toast.makeText(this, "Status zadatka promenjen na " + newStatus, Toast.LENGTH_SHORT).show();
                displayTaskDetails(task);
                updateStatusButtons();
            });
        });
    }

    private void updateStatusButtons() {
        boolean isRecurring = "recurring".equals(task.getFrequency());

        switch (task.getStatus()) {
            case AKTIVAN:
                btnComplete.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                btnActivate.setVisibility(View.GONE);
                btnDelete.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.VISIBLE);

                // Dugme za pauziranje prikazujemo samo za ponavljajuće zadatke
                if (isRecurring) {
                    btnPause.setVisibility(View.VISIBLE);
                } else {
                    btnPause.setVisibility(View.GONE);
                }
                break;

            case PAUZIRAN:
                btnComplete.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                btnPause.setVisibility(View.GONE);
                btnActivate.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.VISIBLE);
                break;

            default: // URAĐEN, NEURAĐEN, OTKAZAN
                btnComplete.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                btnPause.setVisibility(View.GONE);
                btnActivate.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
                break;
        }
    }

}