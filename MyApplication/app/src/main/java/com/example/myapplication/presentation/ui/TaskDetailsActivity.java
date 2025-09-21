package com.example.myapplication.presentation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.database.LevelingSystemHelper;
import com.example.myapplication.data.database.TaskRepositoryFirebaseImpl;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.DifficultyType;
import com.example.myapplication.domain.models.ImportanceType; // Importovan da se izbegne puna putanja
import com.example.myapplication.domain.models.Task;
import com.example.myapplication.domain.models.TaskStatus;
import com.example.myapplication.domain.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TaskDetailsActivity extends AppCompatActivity {

    private static final String TAG = "TaskDetailsActivity";
    private Task task;
    private TaskRepository taskRepository;
    private UserRepository userRepository;
    private String userId;

    private TextView tvTaskName, tvTaskDescription, tvCategory, tvFrequency, tvDates, tvExecutionTime, tvDifficulty, tvImportance;
    private View vCategoryColor;
    private Button btnEdit, btnDelete;
    private Button btnComplete, btnCancel, btnPause, btnActivate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Korisnik nije prijavljen.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        taskRepository = new TaskRepositoryFirebaseImpl();
        userRepository = new UserRepositoryFirebaseImpl();

        String taskId = getIntent().getStringExtra("taskId");
        if (taskId != null) {
            loadTaskDetails(taskId);
        }
    }

    // Metoda za učitavanje podataka, izdvojena radi preglednosti
    private void loadTaskDetails(String taskId) {
        taskRepository.checkAndDeactivateExpiredTasks(userId, new TaskRepository.OnTaskUpdatedListener() {
            @Override
            public void onSuccess() {
                taskRepository.getTaskById(taskId, userId, new TaskRepository.OnTaskLoadedListener() {
                    @Override
                    public void onSuccess(Task fetchedTask) {
                        if (fetchedTask != null) {
                            task = fetchedTask;
                            // Inicijalizacija view-ova se radi samo jednom
                            if (tvTaskName == null) {
                                initializeViews();
                            }
                            displayTaskDetails(task);
                            updateStatusButtons();
                        } else {
                            Toast.makeText(TaskDetailsActivity.this, "Zadatak nije pronađen.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Greška pri učitavanju detalja zadatka.", e);
                        Toast.makeText(TaskDetailsActivity.this, "Greška pri učitavanju detalja zadatka.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Greška pri proveri isteklih zadataka.", e);
                Toast.makeText(TaskDetailsActivity.this, "Greška pri proveri statusa zadataka.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews() {
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

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditTaskActivity.class);
            intent.putExtra("taskId", task.getId());
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> deleteTask());

        btnComplete.setOnClickListener(v -> completeTask()); // Povezano sa novom metodom
        btnCancel.setOnClickListener(v -> updateTaskStatus(TaskStatus.OTKAZAN));
        btnPause.setOnClickListener(v -> updateTaskStatus(TaskStatus.PAUZIRAN));
        btnActivate.setOnClickListener(v -> updateTaskStatus(TaskStatus.AKTIVAN));
    }

    private void displayTaskDetails(Task task) {
        tvTaskName.setText(task.getName());
        tvTaskDescription.setText(task.getDescription());
        if (task.getCategory() != null) {
            vCategoryColor.setBackgroundColor(task.getCategory().getColor());
            tvCategory.setText(String.format("Kategorija: %s", task.getCategory().getName()));
        }

        String frequencyText = "Jednokratan";
        if ("recurring".equals(task.getFrequency())) {
            frequencyText = String.format("Ponavljajući (svaki %d. %s)", task.getInterval(), task.getIntervalUnit());
        }
        tvFrequency.setText(String.format("Učestalost: %s", frequencyText));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String startDate = task.getStartDate() != null ? dateFormat.format(task.getStartDate()) : "N/A";
        String endDate = task.getEndDate() != null ? dateFormat.format(task.getEndDate()) : "N/A";
        tvDates.setText(String.format("Datum: %s - %s", startDate, endDate));

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String executionTime = task.getExecutionTime() != null ? timeFormat.format(task.getExecutionTime()) : "N/A";
        tvExecutionTime.setText(String.format("Vreme izvršenja: %s", executionTime));

        DifficultyType difficultyType = task.getDifficultyType();
        if (difficultyType != null) {
            tvDifficulty.setText(String.format("Težina: %s (%d XP)", difficultyType.getSerbianName(), difficultyType.getXpValue()));
        }

        ImportanceType importanceType = task.getImportanceType();
        if (importanceType != null) {
            tvImportance.setText(String.format("Bitnost: %s (%d XP)", importanceType.getSerbianName(), importanceType.getXpValue()));
        }
    }

    private void deleteTask() {
        if (task != null && task.getStatus() != TaskStatus.URAĐEN && task.getStatus() != TaskStatus.NEURAĐEN) {
            taskRepository.deleteTask(task.getId(), userId, new TaskRepository.OnTaskDeletedListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(TaskDetailsActivity.this, "Zadatak uspešno obrisan.", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Greška pri brisanju zadatka", e);
                    Toast.makeText(TaskDetailsActivity.this, "Greška pri brisanju zadatka.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateTaskStatus(TaskStatus newStatus) {
        if (task.getStatus() == TaskStatus.URAĐEN || task.getStatus() == TaskStatus.NEURAĐEN) return;

        task.setStatus(newStatus);
        taskRepository.updateTask(task, userId, new TaskRepository.OnTaskUpdatedListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(TaskDetailsActivity.this, "Status zadatka promenjen.", Toast.LENGTH_SHORT).show();
                displayTaskDetails(task);
                updateStatusButtons();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Greška pri ažuriranju statusa zadatka", e);
                Toast.makeText(TaskDetailsActivity.this, "Neuspešno ažuriranje statusa.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Kompletno ispravljena metoda za završavanje zadatka.
     */
    private void completeTask() {
        if (task.getStatus() == TaskStatus.URAĐEN) {
            Toast.makeText(this, "Zadatak je već završen.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Pozivamo repozitorijum koji će odraditi transakciju i vratiti tačan XP
        taskRepository.updateTaskStatusToDone(task.getId(), userId, new TaskRepository.OnTaskCompletedListener() {
            @Override
            public void onSuccess(int awardedXp) {
                // 2. Kada dobijemo tačan XP, ažuriramo korisnika
                updateUserWithAwardedXp(awardedXp);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Greška pri ažuriranju statusa zadatka na 'URAĐEN'.", e);
                Toast.makeText(TaskDetailsActivity.this, "Neuspešno označavanje zadatka kao završenog.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserWithAwardedXp(int awardedXp) {
        userRepository.getUserById(userId, new UserRepository.OnCompleteListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    Toast.makeText(TaskDetailsActivity.this, "Greška: Korisnik nije pronađen.", Toast.LENGTH_SHORT).show();
                    return;
                }

                int previousLevel = user.getLevel();
                // Koristimo awardedXp direktno, ne moramo više da računamo
                user.setXp(user.getXp() + awardedXp);

                // Petlja za proveru level-up-a
                while (true) {
                    int requiredXp = LevelingSystemHelper.getRequiredXpForNextLevel(user.getLevel());
                    if (user.getXp() < requiredXp) {
                        break;
                    }
                    int newLevel = user.getLevel() + 1;
                    user.setLevel(newLevel);
                    user.setXp(user.getXp() - requiredXp);
                    user.setTitle(LevelingSystemHelper.getTitleForLevel(newLevel));
                    user.setPowerPoints(user.getPowerPoints() + LevelingSystemHelper.getPowerPointsRewardForLevel(newLevel));
                }

                userRepository.updateUser(user, new UserRepository.OnCompleteListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Ažuriramo lokalni objekat zadatka i UI
                        task.setStatus(TaskStatus.URAĐEN);
                        task.setXpValue(awardedXp);

                        if (awardedXp > 0) {
                            Toast.makeText(TaskDetailsActivity.this, "Zadatak završen! Dodeljeno " + awardedXp + " XP.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TaskDetailsActivity.this, "Zadatak završen, ali je kvota za XP ispunjena.", Toast.LENGTH_SHORT).show();
                        }
                        if (user.getLevel() > previousLevel) {
                            // POZIVAMO NOVU METODU UMESTO TOAST-A
                            showBossFightDialog(user.getLevel());
                        } else {
                            // Ako nema level-upa, samo osveži UI
                            loadTaskDetails(task.getId());
                        }

                        displayTaskDetails(task);
                        updateStatusButtons();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Greška pri ažuriranju korisnika.", e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Greška pri dohvatanju korisnika.", e);
            }
        });
    }


    private void updateStatusButtons() {
        if (task == null) return;

        boolean isRecurring = "recurring".equals(task.getFrequency());
        boolean isEditable = !isTaskTimeFinished(task); // Zadatak se može menjati samo ako mu nije prošao rok

        // Sakrij sve dugmiće pa prikaži samo potrebne
        btnComplete.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        btnPause.setVisibility(View.GONE);
        btnActivate.setVisibility(View.GONE);
        btnDelete.setVisibility(View.VISIBLE);
        btnEdit.setVisibility(View.VISIBLE);

        // Edit i Delete su uvek (ne)dostupni u zavisnosti od roka
        btnEdit.setEnabled(isEditable);
        btnDelete.setEnabled(isEditable);

        switch (task.getStatus()) {
            case AKTIVAN:
                btnComplete.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                if (isRecurring) {
                    btnPause.setVisibility(View.VISIBLE);
                }
                break;
            case PAUZIRAN:
                btnActivate.setVisibility(View.VISIBLE);
                break;
            case NEURAĐEN:
            case OTKAZAN:
            case URAĐEN:
                // Za završene, neurađene ili otkazane zadatke, sakrivamo sve akcione dugmiće
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Osveži podatke o zadatku pri povratku na Activity (npr. nakon izmene)
        if (task != null && task.getId() != null) {
            loadTaskDetails(task.getId());
        }
    }

    /**
     * Proverava da li je trenutno vreme posle krajnjeg datuma zadatka.
     */
    private boolean isTaskTimeFinished(Task task) {
        if (task.getEndDate() == null) {
            return false; // Ako nema krajnji datum, nikad ne ističe
        }
        // Zadatak ističe na kraju dana definisanog sa endDate
        Calendar taskEnd = Calendar.getInstance();
        taskEnd.setTime(task.getEndDate());
        taskEnd.set(Calendar.HOUR_OF_DAY, 23);
        taskEnd.set(Calendar.MINUTE, 59);
        taskEnd.set(Calendar.SECOND, 59);

        return Calendar.getInstance().after(taskEnd);
    }
    private void showBossFightDialog(int newLevel) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Novi Nivo!")
                .setMessage("Čestitamo, dostigli ste " + newLevel + ". nivo!\n\nPojavio se novi Bos. Da li želite da se borite sada?")
                .setCancelable(false)
                .setPositiveButton("Bori se", (dialog, which) -> {
                    // Pokreni BossFightActivity
                    Intent intent = new Intent(TaskDetailsActivity.this, BossFightActivity.class);
                    startActivity(intent);
                    finish(); // Zatvori trenutni ekran
                })
                .setNegativeButton("Kasnije", (dialog, which) -> {
                    // Ako korisnik odbije, samo osveži trenutni ekran
                    dialog.dismiss();
                    loadTaskDetails(task.getId());
                })
                .show();
    }
}