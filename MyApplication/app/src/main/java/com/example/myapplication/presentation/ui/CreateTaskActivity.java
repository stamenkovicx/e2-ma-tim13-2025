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
import com.example.myapplication.data.database.CategoryRepositorySQLiteImpl;
import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.data.database.TaskRepositorySQLiteImpl;
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

public class CreateTaskActivity extends AppCompatActivity {

    // UI elementi
    private EditText etTaskName, etTaskDescription, etInterval, etStartDate, etEndDate, etExecutionTime;
    private Spinner spCategory, spDifficulty, spImportance, spIntervalUnit;
    private RadioGroup rgFrequency;
    private LinearLayout recurringGroup;
    private Button btnCreateTask;
    private CategoryRepository categoryRepository;


    // Repozitorijum za zadatke
    private TaskRepository taskRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        //Toast.makeText(this, "CreateTaskActivity startovao!", Toast.LENGTH_SHORT).show();



        categoryRepository = new CategoryRepositorySQLiteImpl(this);
        taskRepository = new TaskRepositorySQLiteImpl(this, categoryRepository);

        // Povezivanje UI elemenata
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

        // Postavljanje adaptera za Spinere sa fiksnim opcijama
        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_options, android.R.layout.simple_spinner_item);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDifficulty.setAdapter(difficultyAdapter);

        ArrayAdapter<CharSequence> importanceAdapter = ArrayAdapter.createFromResource(this,
                R.array.importance_options, android.R.layout.simple_spinner_item);
        importanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spImportance.setAdapter(importanceAdapter);

        categoryRepository = new CategoryRepositorySQLiteImpl(this);

        List<Category> categories = categoryRepository.getAllCategories();
        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);


        // Listeners za odabir datuma i vremena
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));
        etExecutionTime.setOnClickListener(v -> showTimePicker());

        // Listener za promene u grupi radio dugmadi
        rgFrequency.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbRecurring) {
                recurringGroup.setVisibility(View.VISIBLE);
            } else {
                recurringGroup.setVisibility(View.GONE);
            }
        });

        // Listener za dugme Kreiraj zadatak
        btnCreateTask.setOnClickListener(v -> {
            try {
                createTask();
            } catch (Exception e) {
                Log.e("CreateTaskActivity", "Crash u createTask()", e);
                Toast.makeText(this, "Greška: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                }, hour, minute, true); // true za 24h format
        timePickerDialog.show();
    }

    private void createTask() {
        // Validacija unosa
        String name = etTaskName.getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(this, "Naziv zadatka je obavezan.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prikupljanje ostalih podataka
        String description = etTaskDescription.getText().toString();
        Category selectedCategory = null;
        Object selectedItem = spCategory.getSelectedItem();
        if (selectedItem instanceof Category) {
            selectedCategory = (Category) selectedItem;
        } else if (selectedItem != null) {
            // Ovo se dešava ako spiner nije popunjen sa Category objektima
            Toast.makeText(this, "Greška: spiner nije pravilno inicijalizovan.", Toast.LENGTH_SHORT).show();
            return;
        }
        String frequency = (rgFrequency.getCheckedRadioButtonId() == R.id.rbRecurring) ? "recurring" : "one-time";

        // **ISPRAVLJENA LOGIKA ZA DOBIJANJE ENUM VREDNOSTI**
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

        // Provera da li su pronađene vrednosti (za slučaj da spiner ne radi kako treba)
        if (difficulty == null || importance == null) {
            Toast.makeText(this, "Greška pri odabiru težine ili bitnosti.", Toast.LENGTH_SHORT).show();
            return;
        }

        int interval = 0;
        String intervalUnit = "";
        Date startDate = null;
        Date endDate = null;

        if (frequency.equals("recurring")) {
            try {
                interval = Integer.parseInt(etInterval.getText().toString());
                intervalUnit = spIntervalUnit.getSelectedItem().toString();
                // Ispravljen format datuma
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                startDate = dateFormat.parse(etStartDate.getText().toString());
                endDate = dateFormat.parse(etEndDate.getText().toString());
            } catch (NumberFormatException | ParseException e) {
                Toast.makeText(this, "Neispravan format za ponavljajući zadatak.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Date executionTime = null;
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            executionTime = timeFormat.parse(etExecutionTime.getText().toString());
        } catch (ParseException e) {
            Toast.makeText(this, "Neispravan format za vreme.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kreiranje novog zadatka
        Task newTask;

        if (selectedCategory != null) {
            newTask = new Task(
                    0,
                    name,
                    description,
                    selectedCategory, // Prosleđivanje celog Category objekta
                    frequency,
                    interval,
                    intervalUnit,
                    startDate,
                    endDate,
                    executionTime,
                    difficulty,
                    importance
            );
        } else {
            // Ako nema izabrane kategorije, kreiraj zadatak bez nje
            // Ili prikaži poruku o grešci
            Toast.makeText(this, "Izaberite kategoriju.", Toast.LENGTH_SHORT).show();
            return;
        }

// Čuvanje zadatka u bazi
        long newTaskId = taskRepository.insertTask(newTask);

        if (newTaskId != -1) {
            Toast.makeText(this, "Zadatak uspešno kreiran!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Greška prilikom kreiranja zadatka.", Toast.LENGTH_SHORT).show();
        }
    }

    // Pomoćna funkcija koja simulira dobijanje kategorija iz baze
    private List<Category> getDummyCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(1, "Zdravlje", R.color.red));
        categories.add(new Category(2, "Ucenje", R.color.blue));
        categories.add(new Category(3, "Sređivanje", R.color.green));
        return categories;
    }
}