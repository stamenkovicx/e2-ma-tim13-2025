package com.example.myapplication.presentation.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.database.CategoryRepositorySQLiteImpl;
import com.example.myapplication.data.database.TaskRepositorySQLiteImpl;
import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.domain.models.Category;
import com.example.myapplication.domain.models.DifficultyType;
import com.example.myapplication.domain.models.ImportanceType;
import com.example.myapplication.domain.models.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditTaskActivity extends AppCompatActivity {

    private EditText etTaskName, etTaskDescription, etInterval, etStartDate, etEndDate, etExecutionTime;
    private Spinner spCategory, spDifficulty, spImportance, spIntervalUnit;
    private RadioGroup rgFrequency;
    private LinearLayout recurringGroup;
    private Button btnSaveTask;

    private TaskRepository taskRepository;
    private CategoryRepository categoryRepository;
    private Task taskToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        categoryRepository = new CategoryRepositorySQLiteImpl(this);
        taskRepository = new TaskRepositorySQLiteImpl(this, categoryRepository);

        if (getIntent().getSerializableExtra("task") != null) {
            taskToEdit = (Task) getIntent().getSerializableExtra("task");
        }

        etTaskName = findViewById(R.id.etTaskName);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        etInterval = findViewById(R.id.etInterval);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etExecutionTime = findViewById(R.id.etExecutionTime);
        spCategory = findViewById(R.id.spCategory);
        spDifficulty = findViewById(R.id.spDifficulty);
        spImportance = findViewById(R.id.spImportance);
        spIntervalUnit = findViewById(R.id.spIntervalUnit);
        rgFrequency = findViewById(R.id.rgFrequency);
        recurringGroup = findViewById(R.id.recurringGroup);
        btnSaveTask = findViewById(R.id.btnCreateTask);

        btnSaveTask.setText("Sačuvaj izmene");

        if (taskToEdit != null) {
            populateFields(taskToEdit);
        }

        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));
        etExecutionTime.setOnClickListener(v -> showTimePicker());
        rgFrequency.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbRecurring) {
                recurringGroup.setVisibility(View.VISIBLE);
            } else {
                recurringGroup.setVisibility(View.GONE);
            }
        });

        btnSaveTask.setOnClickListener(v -> saveTaskChanges());
    }

    private void populateFields(Task task) {
        etTaskName.setText(task.getName());
        etTaskDescription.setText(task.getDescription());

        // Postavljanje kategorije
        List<Category> categories = categoryRepository.getAllCategories();
        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);
        int categoryPosition = -1;
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == task.getCategory().getId()) {
                categoryPosition = i;
                break;
            }
        }
        if (categoryPosition != -1) {
            spCategory.setSelection(categoryPosition);
        }


        // Postavljanje težine i bitnosti
        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(this, R.array.difficulty_options, android.R.layout.simple_spinner_item);
        spDifficulty.setSelection(difficultyAdapter.getPosition(task.getDifficulty().getSerbianName()));

        ArrayAdapter<CharSequence> importanceAdapter = ArrayAdapter.createFromResource(this, R.array.importance_options, android.R.layout.simple_spinner_item);
        spImportance.setSelection(importanceAdapter.getPosition(task.getImportance().getSerbianName()));

        // Postavljanje ostalih polja
        if ("recurring".equals(task.getFrequency())) {
            rgFrequency.check(R.id.rbRecurring);
            recurringGroup.setVisibility(View.VISIBLE);
            etInterval.setText(String.valueOf(task.getInterval()));

            ArrayAdapter<CharSequence> intervalUnitAdapter = ArrayAdapter.createFromResource(this, R.array.interval_units, android.R.layout.simple_spinner_item);
            spIntervalUnit.setSelection(intervalUnitAdapter.getPosition(task.getIntervalUnit()));

            if (task.getStartDate() != null) {
                etStartDate.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(task.getStartDate()));
            }
            if (task.getEndDate() != null) {
                etEndDate.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(task.getEndDate()));
            }
        } else {
            rgFrequency.check(R.id.rbOneTime);
            recurringGroup.setVisibility(View.GONE);
        }

        if (task.getExecutionTime() != null) {
            etExecutionTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(task.getExecutionTime()));
        }
    }

    private void saveTaskChanges() {
        if (taskToEdit == null) {
            Toast.makeText(this, "Greška: Zadatak nije pronađen.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Preuzmi sve podatke iz UI elemenata
        String name = etTaskName.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();
        Category selectedCategory = (Category) spCategory.getSelectedItem();
        String frequency = (rgFrequency.getCheckedRadioButtonId() == R.id.rbRecurring) ? "recurring" : "one-time";

        // Pronađi ispravne enum vrednosti
        DifficultyType difficulty = null;
        String selectedDifficultyStr = spDifficulty.getSelectedItem().toString();
        for (DifficultyType d : DifficultyType.values()) {
            if (d.getSerbianName().equals(selectedDifficultyStr)) {
                difficulty = d;
                break;
            }
        }

        ImportanceType importance = null;
        String selectedImportanceStr = spImportance.getSelectedItem().toString();
        for (ImportanceType i : ImportanceType.values()) {
            if (i.getSerbianName().equals(selectedImportanceStr)) {
                importance = i;
                break;
            }
        }

        if (name.isEmpty() || difficulty == null || importance == null || selectedCategory == null) {
            Toast.makeText(this, "Naziv, težina, bitnost i kategorija su obavezni.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Ažuriraj taskToEdit objekat
        taskToEdit.setName(name);
        taskToEdit.setDescription(description);
        taskToEdit.setCategory(selectedCategory);
        taskToEdit.setFrequency(frequency);
        taskToEdit.setDifficulty(difficulty);
        taskToEdit.setImportance(importance);

        if ("recurring".equals(frequency)) {
            try {
                int interval = Integer.parseInt(etInterval.getText().toString());
                String intervalUnit = spIntervalUnit.getSelectedItem().toString();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                Date startDate = dateFormat.parse(etStartDate.getText().toString());
                Date endDate = dateFormat.parse(etEndDate.getText().toString());

                taskToEdit.setInterval(interval);
                taskToEdit.setIntervalUnit(intervalUnit);
                taskToEdit.setStartDate(startDate);
                taskToEdit.setEndDate(endDate);

            } catch (NumberFormatException | ParseException e) {
                Toast.makeText(this, "Greška u formatu datuma/intervala.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date executionTime = timeFormat.parse(etExecutionTime.getText().toString());
            taskToEdit.setExecutionTime(executionTime);
        } catch (ParseException e) {
            Toast.makeText(this, "Greška u formatu vremena.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Pozovi metodu za ažuriranje u repozitorijumu
        int rowsAffected = taskRepository.updateTask(taskToEdit);

        if (rowsAffected > 0) {
            Toast.makeText(this, "Promene uspešno sačuvane!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Greška prilikom čuvanja izmena.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePicker(final EditText dateField) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%02d.%02d.%d", dayOfMonth, monthOfYear + 1, year1);
                    dateField.setText(formattedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                    etExecutionTime.setText(formattedTime);
                }, hour, minute, true);
        timePickerDialog.show();
    }
}