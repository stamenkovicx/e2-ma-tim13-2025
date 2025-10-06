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

public class CreateTaskActivity extends AppCompatActivity {

    private static final String TAG = "CreateTaskActivity";

    private EditText etTaskName, etTaskDescription, etInterval, etStartDate, etEndDate, etExecutionTime;
    private Spinner spCategory, spDifficulty, spImportance, spIntervalUnit;
    private RadioGroup rgFrequency;
    private LinearLayout recurringGroup;
    private Button btnCreateTask;

    private TaskRepository taskRepository;
    private CategoryRepository categoryRepository;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        categoryRepository = new CategoryRepositoryFirebaseImpl();
        taskRepository = new TaskRepositoryFirebaseImpl();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupSpinners();
        loadCategories();
        setupListeners();
    }

    private void initializeViews() {
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
        btnCreateTask = findViewById(R.id.btnCreateTask);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_options, android.R.layout.simple_spinner_item);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDifficulty.setAdapter(difficultyAdapter);

        ArrayAdapter<CharSequence> importanceAdapter = ArrayAdapter.createFromResource(this,
                R.array.importance_options, android.R.layout.simple_spinner_item);
        importanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spImportance.setAdapter(importanceAdapter);

        ArrayAdapter<CharSequence> intervalUnitAdapter = ArrayAdapter.createFromResource(this,
                R.array.interval_units, android.R.layout.simple_spinner_item);
        intervalUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spIntervalUnit.setAdapter(intervalUnitAdapter);
    }

    private void setupListeners() {
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));
        etExecutionTime.setOnClickListener(v -> showTimePicker());

        rgFrequency.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbRecurring) {
                // Ako je ponavljajući, prikaži grupu za interval i krajnji datum
                recurringGroup.setVisibility(View.VISIBLE);
            } else {
                // Ako je jednokratni, sakrij samo tu grupu
                recurringGroup.setVisibility(View.GONE);
                // Ne čistimo startDate, jer nam treba!
                etInterval.setText("");
                etEndDate.setText("");
                spIntervalUnit.setSelection(0);
            }
        });

        btnCreateTask.setOnClickListener(v -> {
            createTask();
        });
    }

    private void loadCategories() {
        categoryRepository.getAllCategories(userId, new CategoryRepository.OnCategoriesLoadedListener() {
            @Override
            public void onSuccess(List<Category> categories) {
                if (categories != null && !categories.isEmpty()) {
                    ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(CreateTaskActivity.this,
                            android.R.layout.simple_spinner_item, categories);
                    categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spCategory.setAdapter(categoryAdapter);
                } else {
                    Toast.makeText(CreateTaskActivity.this, "No categories found. Please add a category first.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load categories.", e);
                Toast.makeText(CreateTaskActivity.this, "Failed to load categories.", Toast.LENGTH_SHORT).show();
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

    private void createTask() {
        // --- Provera osnovnih polja koja su uvek obavezna ---
        String name = etTaskName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Naziv zadatka je obavezan.", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = etTaskDescription.getText().toString().trim();
        Category selectedCategory = (Category) spCategory.getSelectedItem();
        if (selectedCategory == null) {
            Toast.makeText(this, "Molimo izaberite kategoriju.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Provera vremena izvršenja, jer je potrebno za oba tipa zadatka
        if (etExecutionTime.getText().toString().isEmpty()) {
            Toast.makeText(this, "Vreme izvršenja je obavezno.", Toast.LENGTH_SHORT).show();
            return;
        }

        DifficultyType difficulty = DifficultyType.fromSerbianName(spDifficulty.getSelectedItem().toString());
        ImportanceType importance = ImportanceType.fromSerbianName(spImportance.getSelectedItem().toString());

        // --- POČETAK NOVE LOGIKE ---

        // Definišemo promenljive koje će se popuniti u zavisnosti od tipa zadatka
        Date startDate;
        Date endDate;
        Date executionTime;
        Integer interval = null;
        String intervalUnit = null;

        // Formati za parsiranje
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        try {
            // Vreme izvršenja parsiramo odmah, jer je zajedničko za oba slučaja
            executionTime = timeFormat.parse(etExecutionTime.getText().toString());
        } catch (ParseException e) {
            Log.e(TAG, "Greška pri parsiranju vremena", e);
            Toast.makeText(this, "Neispravan format vremena.", Toast.LENGTH_SHORT).show();
            return;
        }

        String frequency = (rgFrequency.getCheckedRadioButtonId() == R.id.rbRecurring) ? "recurring" : "one-time";

        if (frequency.equals("recurring")) {
            // --- LOGIKA ZA PONAVLJAJUĆI ZADATAK ---
            // Za ponavljajući, sva polja u `recurringGroup` moraju biti popunjena
            if (etStartDate.getText().toString().isEmpty() ||
                    etEndDate.getText().toString().isEmpty() ||
                    etInterval.getText().toString().isEmpty()) {
                Toast.makeText(this, "Početni datum, krajnji datum i interval su obavezni za ponavljajuće zadatke.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Parsiramo vrednosti iz EditText polja
                startDate = dateFormat.parse(etStartDate.getText().toString());
                endDate = dateFormat.parse(etEndDate.getText().toString());
                interval = Integer.parseInt(etInterval.getText().toString());
                intervalUnit = spIntervalUnit.getSelectedItem().toString();

                // Proveravamo logiku datuma
                if(startDate.after(endDate)) {
                    Toast.makeText(this, "Početni datum ne može biti posle krajnjeg datuma.", Toast.LENGTH_SHORT).show();
                    return;
                }

            } catch (Exception e) {
                Log.e(TAG, "Greška pri parsiranju polja za ponavljajući zadatak", e);
                Toast.makeText(this, "Neispravan format za ponavljajući zadatak.", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            // --- LOGIKA ZA JEDNOKRATNI ZADATAK ---
            // Datum se postavlja automatski na današnji dan
            startDate = new Date();
            // Krajnji datum je isti kao početni za jednokratne zadatke
            endDate = startDate;
            // Interval i jedinica ostaju null
        }

        int xpValue = (difficulty != null ? difficulty.getXpValue() : 0) + (importance != null ? importance.getXpValue() : 0);

        // Kreiramo objekat sa ispravno postavljenim vrednostima
        Task newTask = new Task(
                name, description, selectedCategory, frequency,
                interval, intervalUnit, startDate, endDate, executionTime,
                difficulty != null ? difficulty.name() : null,
                importance != null ? importance.name() : null,
                xpValue, userId
        );

        // Upis u bazu ostaje isti
        taskRepository.insertTask(newTask, userId, new TaskRepository.OnTaskAddedListener() {
            @Override
            public void onSuccess(String taskId) {
                Toast.makeText(CreateTaskActivity.this, "Zadatak uspešno kreiran!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Greška pri kreiranju zadatka", e);
                Toast.makeText(CreateTaskActivity.this, "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}