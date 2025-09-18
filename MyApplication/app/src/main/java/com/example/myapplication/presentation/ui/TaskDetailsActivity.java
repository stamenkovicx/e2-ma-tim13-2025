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
import com.example.myapplication.data.database.TaskRepositoryFirebaseImpl;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.DifficultyType;
import com.example.myapplication.domain.models.Task;
import com.example.myapplication.domain.models.TaskStatus;
import com.example.myapplication.domain.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.myapplication.data.database.LevelingSystemHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaskDetailsActivity extends AppCompatActivity {

    private Task task;
    private TaskRepository taskRepository;
    private UserRepository userRepository;
    private String userId;

    private TextView tvTaskName, tvTaskDescription, tvCategory, tvFrequency, tvDates, tvExecutionTime, tvDifficulty, tvImportance;
    private View vCategoryColor;
    private Button btnEdit, btnDelete;
    private Button btnComplete, btnCancel, btnPause, btnActivate;
    private static final String TAG = "TaskDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Instanciramo Firebase repozitorijume
        taskRepository = new TaskRepositoryFirebaseImpl();
        userRepository = new UserRepositoryFirebaseImpl();

        String taskId = getIntent().getStringExtra("taskId");
        if (taskId != null) {
            // Prvo proveravamo da li je zadatak istekao pre nego što ga prikažemo
            taskRepository.checkAndDeactivateExpiredTasks(userId, new TaskRepository.OnTaskUpdatedListener() {
                @Override
                public void onSuccess() {
                    // Nakon provere, dohvatamo zadatak
                    taskRepository.getTaskById(taskId, userId, new TaskRepository.OnTaskLoadedListener() {
                        @Override
                        public void onSuccess(Task fetchedTask) {
                            if (fetchedTask != null) {
                                task = fetchedTask;
                                initializeViews();
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
                            finish();
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Greška pri proveri isteklih zadataka.", e);
                    Toast.makeText(TaskDetailsActivity.this, "Greška pri proveri statusa zadataka.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
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

        btnComplete.setOnClickListener(v -> updateTaskStatusAndXP(TaskStatus.URAĐEN));
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
        } else {
            vCategoryColor.setBackgroundColor(0);
            tvCategory.setText("Kategorija: Nije postavljeno");
        }

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

        DifficultyType difficultyType = task.getDifficultyType();
        if (difficultyType != null) {
            tvDifficulty.setText(String.format("Težina: %s (%d XP)", difficultyType.getSerbianName(), difficultyType.getXpValue()));
        } else {
            tvDifficulty.setText("Težina: Nije postavljeno");
        }

        // Ispravite ImportanceType
        com.example.myapplication.domain.models.ImportanceType importanceType = task.getImportanceType();
        if (importanceType != null) {
            tvImportance.setText(String.format("Bitnost: %s (%d XP)", importanceType.getSerbianName(), importanceType.getXpValue()));
        } else {
            tvImportance.setText("Bitnost: Nije postavljeno");
        }
    }

    private void deleteTask() {
        if (task != null) {
            if (task.getStatus() == TaskStatus.URAĐEN || task.getStatus() == TaskStatus.NEURAĐEN) {
                Toast.makeText(this, "Ne možete obrisati završen ili neurađen zadatak.", Toast.LENGTH_SHORT).show();
            } else {
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
    }

    private void updateTaskStatus(TaskStatus newStatus) {
        if (task.getStatus() == TaskStatus.URAĐEN || task.getStatus() == TaskStatus.NEURAĐEN) {
            Toast.makeText(this, "Ne možete menjati status završenog ili neurađenog zadatka.", Toast.LENGTH_SHORT).show();
            return;
        }

        task.setStatus(newStatus);
        taskRepository.updateTask(task, userId, new TaskRepository.OnTaskUpdatedListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(TaskDetailsActivity.this, "Status zadatka promenjen u " + newStatus.name().toLowerCase(Locale.ROOT) + ".", Toast.LENGTH_SHORT).show();
                    displayTaskDetails(task);
                    updateStatusButtons();
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Greška pri ažuriranju statusa zadatka", e);
                runOnUiThread(() -> Toast.makeText(TaskDetailsActivity.this, "Neuspešno ažuriranje statusa zadatka.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateTaskStatusAndXP(TaskStatus newStatus) {
        if (task.getStatus() == TaskStatus.URAĐEN) {
            Toast.makeText(this, "Zadatak je već završen.", Toast.LENGTH_SHORT).show();
            return;
        }

        taskRepository.updateTaskStatusToDone(task.getId(), userId, new TaskRepository.OnTaskUpdatedListener() {
            @Override
            public void onSuccess() {
                taskRepository.getTaskById(task.getId(), userId, new TaskRepository.OnTaskLoadedListener() {
                    @Override
                    public void onSuccess(Task updatedTask) {
                        if (updatedTask != null) {
                            task = updatedTask;

                            userRepository.getUserById(userId, new UserRepository.OnCompleteListener<User>() {
                                @Override
                                public void onSuccess(User user) {
                                    if (user != null) {
                                        int previousLevel = user.getLevel();
                                        int totalXpGained = task.getXpValue(); // Vrednost može biti 0 zbog kvote
                                        user.setXp(user.getXp() + totalXpGained);

                                        while (true) {
                                            int requiredXp = LevelingSystemHelper.getRequiredXpForNextLevel(user.getLevel());
                                            if (user.getXp() < requiredXp) {
                                                break;
                                            }
                                            int newLevel = user.getLevel() + 1;
                                            user.setLevel(newLevel);
                                            int remainingXp = user.getXp() - requiredXp;
                                            user.setXp(remainingXp);
                                            user.setTitle(LevelingSystemHelper.getTitleForLevel(newLevel));
                                            user.setPowerPoints(user.getPowerPoints() + LevelingSystemHelper.getPowerPointsRewardForLevel(newLevel));
                                        }

                                        userRepository.updateUser(user, new UserRepository.OnCompleteListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                runOnUiThread(() -> {
                                                    if (totalXpGained > 0) {
                                                        Toast.makeText(TaskDetailsActivity.this, "Zadatak završen! Dodeljeno " + totalXpGained + " XP.", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(TaskDetailsActivity.this, "Zadatak završen, ali je kvota za XP ispunjena.", Toast.LENGTH_SHORT).show();
                                                    }
                                                    if (user.getLevel() > previousLevel) {
                                                        Toast.makeText(TaskDetailsActivity.this, "Čestitamo! Podigli ste nivo na " + user.getLevel() + "!", Toast.LENGTH_LONG).show();
                                                    }
                                                    displayTaskDetails(task);
                                                    updateStatusButtons();
                                                });
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                Log.e(TAG, "Greška pri ažuriranju korisnika nakon zadatka", e);
                                                runOnUiThread(() -> Toast.makeText(TaskDetailsActivity.this, "Zadatak završen, ali je došlo do greške pri ažuriranju XP-a.", Toast.LENGTH_SHORT).show());
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e(TAG, "Greška pri dohvatanju podataka korisnika", e);
                                    runOnUiThread(() -> Toast.makeText(TaskDetailsActivity.this, "Zadatak završen, ali je došlo do greške pri dohvatanju korisničkih podataka.", Toast.LENGTH_SHORT).show());
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Greška pri dohvatanju ažuriranog zadatka", e);
                        runOnUiThread(() -> Toast.makeText(TaskDetailsActivity.this, "Zadatak završen, ali je došlo do greške pri osvežavanju.", Toast.LENGTH_SHORT).show());
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Greška pri ažuriranju statusa zadatka na 'URAĐEN'.", e);
                runOnUiThread(() -> Toast.makeText(TaskDetailsActivity.this, "Neuspešno označavanje zadatka kao završenog.", Toast.LENGTH_SHORT).show());
            }
        });
    }


    private void updateStatusButtons() {
        boolean isRecurring = "recurring".equals(task.getFrequency());
        boolean isTimeFinished = isTaskTimeFinished(task);
        boolean isCompletedOrUnfinished = task.getStatus() == TaskStatus.URAĐEN || task.getStatus() == TaskStatus.NEURAĐEN;

        switch (task.getStatus()) {
            case AKTIVAN:
                btnComplete.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                btnActivate.setVisibility(View.GONE);
                btnDelete.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.VISIBLE);
                btnEdit.setEnabled(!isTimeFinished && !isCompletedOrUnfinished);
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
                btnEdit.setEnabled(!isTimeFinished && !isCompletedOrUnfinished);
                break;

            case NEURAĐEN:
            case OTKAZAN:
            case URAĐEN:
            default:
                btnComplete.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                btnPause.setVisibility(View.GONE);
                btnActivate.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (task != null && task.getId() != null) {
            taskRepository.getTaskById(task.getId(), userId, new TaskRepository.OnTaskLoadedListener() {
                @Override
                public void onSuccess(Task updatedTask) {
                    if (updatedTask != null) {
                        TaskDetailsActivity.this.task = updatedTask;
                        displayTaskDetails(TaskDetailsActivity.this.task);
                        updateStatusButtons();
                    } else {
                        runOnUiThread(() -> Toast.makeText(TaskDetailsActivity.this, "Task not found.", Toast.LENGTH_SHORT).show());
                        finish();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to refresh task data.", e);
                    runOnUiThread(() -> Toast.makeText(TaskDetailsActivity.this, "Failed to load task details.", Toast.LENGTH_SHORT).show());
                }
            });
        }
    }
    private boolean isTaskTimeFinished(Task task) {
        if (task.getEndDate() == null) return false;

        Calendar now = Calendar.getInstance();
        Calendar taskEnd = Calendar.getInstance();
        taskEnd.setTime(task.getEndDate());
        taskEnd.set(Calendar.HOUR_OF_DAY, 23);
        taskEnd.set(Calendar.MINUTE, 59);
        taskEnd.set(Calendar.SECOND, 59);
        taskEnd.set(Calendar.MILLISECOND, 999);

        Calendar taskCreated = Calendar.getInstance();
        taskCreated.setTime(task.getStartDate()); // pretpostavljam da task ima createdDate

        boolean isSameDayAsCreated = taskCreated.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                && taskCreated.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR);

        // Ako je zadatak kreiran danas, ne smemo ga disejblovati
        if (isSameDayAsCreated) return false;

        return now.after(taskEnd);
    }


}