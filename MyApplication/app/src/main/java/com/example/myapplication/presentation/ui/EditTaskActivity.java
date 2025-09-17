package com.example.myapplication.presentation.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
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
import com.example.myapplication.data.database.CategoryRepositoryFirebaseImpl;
import com.example.myapplication.data.database.TaskRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.domain.models.Category;
import com.example.myapplication.domain.models.DifficultyType;
import com.example.myapplication.domain.models.ImportanceType;
import com.example.myapplication.domain.models.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditTaskActivity extends AppCompatActivity {

    private static final String TAG = "EditTaskActivity";

    private EditText etTaskName, etTaskDescription, etInterval, etStartDate, etEndDate, etExecutionTime;
    private Spinner spCategory, spDifficulty, spImportance, spIntervalUnit;
    private RadioGroup rgFrequency;
    private LinearLayout recurringGroup;
    private Button btnSaveTask;

    private TaskRepository taskRepository;
    private CategoryRepository categoryRepository;
    private Task taskToEdit;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        // Instanciranje Firebase repozitorijuma
        taskRepository = new TaskRepositoryFirebaseImpl();
        categoryRepository = new CategoryRepositoryFirebaseImpl();

        // Dohvatanje trenutnog korisnika
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String taskId = getIntent().getStringExtra("taskId");
        if (taskId != null) {
            // Asinhrono dohvati zadatak iz baze
            taskRepository.getTaskById(taskId, userId, new TaskRepository.OnTaskLoadedListener() {
                @Override
                public void onSuccess(Task task) {
                    if (task != null) {
                        taskToEdit = task;
                        populateFields(taskToEdit);
                    } else {
                        runOnUiThread(() -> Toast.makeText(EditTaskActivity.this, "Task not found.", Toast.LENGTH_SHORT).show());
                        finish();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Error fetching task by ID", e);
                    runOnUiThread(() -> Toast.makeText(EditTaskActivity.this, "Error loading task.", Toast.LENGTH_SHORT).show());
                    finish();
                }
            });
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

        btnSaveTask.setText("Save Changes");

        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));
        etExecutionTime.setOnClickListener(v -> showTimePicker());
        rgFrequency.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbRecurring) {
                recurringGroup.setVisibility(View.VISIBLE);
            } else {
                recurringGroup.setVisibility(View.GONE);
                // Clear fields when switching to one-time
                etInterval.setText("");
                etStartDate.setText("");
                etEndDate.setText("");
                spIntervalUnit.setSelection(0);
            }
        });

        btnSaveTask.setOnClickListener(v -> saveTaskChanges());
    }

    private void populateFields(Task task) {
        etTaskName.setText(task.getName());
        etTaskDescription.setText(task.getDescription());

        // Asinhrono učitavanje kategorija i postavljanje spinnera
        categoryRepository.getAllCategories(userId, new CategoryRepository.OnCategoriesLoadedListener() {
            @Override
            public void onSuccess(List<Category> categories) {
                ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(EditTaskActivity.this,
                        android.R.layout.simple_spinner_item, categories);
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spCategory.setAdapter(categoryAdapter);
                int categoryPosition = -1;
                if (task.getCategory() != null) {
                    for (int i = 0; i < categories.size(); i++) {
                        if (categories.get(i).getId().equals(task.getCategory().getId())) {
                            categoryPosition = i;
                            break;
                        }
                    }
                }
                if (categoryPosition != -1) {
                    spCategory.setSelection(categoryPosition);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load categories.", e);
                runOnUiThread(() -> Toast.makeText(EditTaskActivity.this, "Failed to load categories.", Toast.LENGTH_SHORT).show());
            }
        });

        // Postavljanje težine i bitnosti
        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(this, R.array.difficulty_options, android.R.layout.simple_spinner_item);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDifficulty.setAdapter(difficultyAdapter);
        if (task.getDifficulty() != null) {
            spDifficulty.setSelection(difficultyAdapter.getPosition(task.getDifficulty()));
        }

        ArrayAdapter<CharSequence> importanceAdapter = ArrayAdapter.createFromResource(this, R.array.importance_options, android.R.layout.simple_spinner_item);
        importanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spImportance.setAdapter(importanceAdapter);
        if (task.getImportance() != null) {
            spImportance.setSelection(importanceAdapter.getPosition(task.getImportance()));
        }

        ArrayAdapter<CharSequence> intervalUnitAdapter = ArrayAdapter.createFromResource(this, R.array.interval_units, android.R.layout.simple_spinner_item);
        intervalUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spIntervalUnit.setAdapter(intervalUnitAdapter);

        // Postavljanje ostalih polja
        if ("recurring".equals(task.getFrequency())) {
            rgFrequency.check(R.id.rbRecurring);
            recurringGroup.setVisibility(View.VISIBLE);
            if (task.getInterval() != null) {
                etInterval.setText(String.valueOf(task.getInterval()));
            }

            if (task.getIntervalUnit() != null) {
                spIntervalUnit.setSelection(intervalUnitAdapter.getPosition(task.getIntervalUnit()));
            }

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
            Toast.makeText(this, "Error: Task not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etTaskName.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();
        Category selectedCategory = (Category) spCategory.getSelectedItem();
        String frequency = (rgFrequency.getCheckedRadioButtonId() == R.id.rbRecurring) ? "recurring" : "one-time";

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
            Toast.makeText(this, "Name, difficulty, importance and category are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        taskToEdit.setName(name);
        taskToEdit.setDescription(description);
        taskToEdit.setCategory(selectedCategory);
        taskToEdit.setFrequency(frequency);
        taskToEdit.setDifficulty(difficulty.name());
        taskToEdit.setImportance(importance.name());
        taskToEdit.setXpValue(difficulty.getXpValue() + importance.getXpValue());

        try {
            if ("recurring".equals(frequency)) {
                Integer interval = Integer.parseInt(etInterval.getText().toString());
                String intervalUnit = spIntervalUnit.getSelectedItem().toString();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                Date startDate = dateFormat.parse(etStartDate.getText().toString());
                Date endDate = dateFormat.parse(etEndDate.getText().toString());

                taskToEdit.setInterval(interval);
                taskToEdit.setIntervalUnit(intervalUnit);
                taskToEdit.setStartDate(startDate);
                taskToEdit.setEndDate(endDate);
            } else {
                taskToEdit.setInterval(null);
                taskToEdit.setIntervalUnit(null);
                taskToEdit.setStartDate(null);
                taskToEdit.setEndDate(null);
            }

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date executionTime = timeFormat.parse(etExecutionTime.getText().toString());
            taskToEdit.setExecutionTime(executionTime);

        } catch (NumberFormatException | ParseException e) {
            Toast.makeText(this, "Error in date/time format or interval.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Asinhroni poziv za ažuriranje zadatka u Firebase-u
        taskRepository.updateTask(taskToEdit, userId, new TaskRepository.OnTaskUpdatedListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(EditTaskActivity.this, "Changes saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error saving task changes", e);
                runOnUiThread(() -> Toast.makeText(EditTaskActivity.this, "Error saving changes.", Toast.LENGTH_SHORT).show());
            }
        });
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