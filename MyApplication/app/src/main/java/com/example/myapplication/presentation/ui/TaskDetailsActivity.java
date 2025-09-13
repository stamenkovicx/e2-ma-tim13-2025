package com.example.myapplication.presentation.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.database.TaskRepositorySQLiteImpl;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.domain.models.Task;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TaskDetailsActivity extends AppCompatActivity {

    private Task task;

    private TextView tvTaskName, tvTaskDescription, tvCategory, tvFrequency, tvDates, tvExecutionTime, tvDifficulty, tvImportance;
    private View vCategoryColor;
    private Button btnEdit, btnDelete;
    private TaskRepository taskRepository;


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

        if (task != null) {
            displayTaskDetails(task);
        }

        // Dugmad su ostavljena bez logike za sada
        btnEdit.setOnClickListener(v -> {
            // Logika za izmenu zadatka (biće implementirana kasnije)
        });

        btnDelete.setOnClickListener(v -> {
            deleteTask();
        });
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
            // Prema specifikaciji, nije moguće obrisati završene zadatke
            // Moramo proveriti status zadatka
            // (Za sada pretpostavljamo da ne postoji status, pa se brišu svi)
            int rowsDeleted = taskRepository.deleteTask(task.getId());
            if (rowsDeleted > 0) {
                Toast.makeText(this, "Zadatak uspešno obrisan.", Toast.LENGTH_SHORT).show();
                finish(); // Vrati se na prethodni ekran
            } else {
                Toast.makeText(this, "Greška prilikom brisanja zadatka.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}