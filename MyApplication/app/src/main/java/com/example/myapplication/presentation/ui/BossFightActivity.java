package com.example.myapplication.presentation.ui;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.database.TaskRepositoryFirebaseImpl;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.Boss;
import com.example.myapplication.domain.models.Task;
import com.example.myapplication.domain.models.TaskStatus;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class BossFightActivity extends AppCompatActivity {

    private Boss boss;
    private ProgressBar bossHpBar,userPPBar;
    private TextView bossHpText, attemptsText, successChanceText,userPPText;
    private Button attackButton;
    private AnimationDrawable bossAnimation;
    private ImageView bossImage;

    private int attemptsLeft = 5;
    private int userPP = 0; // sada će se učitati iz baze
    private int userSuccessChance = 2; //svakako ce promeniti vrednost uzece da gleda iz zads
    private com.example.myapplication.domain.models.User currentUser;


    private Random random = new Random();
    private UserRepository userRepository;
    private TaskRepository taskRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss_fight);

        // inicijalizacija repozitorijuma sa konkretnom implementacijom
        userRepository = new UserRepositoryFirebaseImpl();
        taskRepository = new TaskRepositoryFirebaseImpl();

        // inicijalizacija bosa (level 1, nema prethodnog HP-a)
        boss = new Boss(1, 0);

        bossHpBar = findViewById(R.id.bossHpBar);
        bossHpText = findViewById(R.id.bossHpText);
        attemptsText = findViewById(R.id.attemptsText);
        successChanceText = findViewById(R.id.successChanceText);
        attackButton = findViewById(R.id.attackButton);
        bossImage = findViewById(R.id.bossImage);
        userPPBar = findViewById(R.id.userPPBar);
        userPPText = findViewById(R.id.userPPText);

        attackButton.setEnabled(false); // dok se ne učita user

        // postavljanje idle animacije
        bossImage.setImageResource(R.drawable.boss_idle_animation);

        // startuj animaciju kada ImageView bude spreman
        bossImage.post(() -> {
            bossAnimation = (AnimationDrawable) bossImage.getDrawable();
            bossAnimation.start();
        });

        bossHpBar.setMax(boss.getMaxHp());
        bossHpBar.setProgress(boss.getHp());

        updateUI();

        loadCurrentUser();

        attackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attackBoss();
            }
        });
    }

    private void attackBoss() {
        if (attemptsLeft <= 0) {
            Toast.makeText(this, "Nema više pokušaja!", Toast.LENGTH_SHORT).show();
            return;
        }

        attemptsLeft--;

        int roll = random.nextInt(100); // 0-99
        if (roll < userSuccessChance) {
            boss.takeDamage(userPP);
            playHitAnimation(); // animacija na udarac
            Toast.makeText(this, "Pogodak! -" + userPP + " HP", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Promašaj!", Toast.LENGTH_SHORT).show();
        }

        updateUI();

        if (boss.isDefeated()) {
            attackButton.setEnabled(false);
            Toast.makeText(this, "Pobedio si bosa!", Toast.LENGTH_LONG).show();
        } else if (attemptsLeft == 0) {
            attackButton.setEnabled(false);
            Toast.makeText(this, "Kraj borbe! Bos preživeo.", Toast.LENGTH_LONG).show();
        }
    }

    private void updateUI() {
        bossHpBar.setProgress(boss.getHp());
        bossHpText.setText("HP: " + boss.getHp() + "/" + boss.getMaxHp());
        attemptsText.setText("Pokušaji: " + attemptsLeft + "/5");
        successChanceText.setText("Šansa za pogodak: " + userSuccessChance + "%");
    }

    private void startIdleAnimation() {
        bossImage.setImageResource(R.drawable.boss_idle_animation);
        bossImage.post(() -> {
            bossAnimation = (AnimationDrawable) bossImage.getDrawable();
            bossAnimation.start();
        });
    }

    private void playHitAnimation() {
        bossImage.setImageResource(R.drawable.boss_hit_animation);
        bossImage.post(() -> {
            bossAnimation = (AnimationDrawable) bossImage.getDrawable();
            bossAnimation.start();

            // ukupno trajanje animacije
            int totalDuration = 0;
            for (int i = 0; i < bossAnimation.getNumberOfFrames(); i++) {
                totalDuration += bossAnimation.getDuration(i);
            }

            // Vrati idle animaciju nakon završetka hit animacije
            bossImage.postDelayed(this::startIdleAnimation, totalDuration);
        });
    }
    // metoda za učitavanje PP bodova iz baze
    private void loadCurrentUser() {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
            String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

            userRepository.getUserById(currentUserId, new UserRepository.OnCompleteListener<com.example.myapplication.domain.models.User>() {
                @Override
                public void onSuccess(com.example.myapplication.domain.models.User user) {
                    if (user != null) {
                        currentUser = user;
                        userPP = currentUser.getPowerPoints(); // učitaj PP iz usera
                        int maxPP = currentUser.getTotalPowerPoints(); // ili koliko god korisnik može maksimalno imati
                        userPPBar.setMax(maxPP);
                        userPPBar.setProgress(userPP); // stvarni PP iz baze
                        userPPText.setText("PP: " + userPP + "/"+maxPP); // samo tvoj broj iz baze

                        attackButton.setEnabled(true); // sada korisnik može napadati
                        Toast.makeText(BossFightActivity.this, "Korisnik učitan, PP: " + userPP, Toast.LENGTH_SHORT).show();
                        loadUserSuccessChance(currentUser.getUserId());;
                    } else {
                        Toast.makeText(BossFightActivity.this, "Korisnik nije pronađen", Toast.LENGTH_SHORT).show();
                        userPP = 0;
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(BossFightActivity.this, "Greška pri učitavanju korisnika", Toast.LENGTH_SHORT).show();
                    userPP = 0;
                }
            });
        } else {
            Toast.makeText(this, "Nije prijavljen nijedan korisnik", Toast.LENGTH_SHORT).show();
            userPP = 0;
        }
    }

    //racunannje sanse da korisnikov napad uspe
    private void loadUserSuccessChance(String userId) {
        taskRepository.getAllTasks(userId, new TaskRepository.OnTasksLoadedListener() {
            @Override
            public void onSuccess(List<Task> tasks) {
                if (tasks == null || tasks.isEmpty()) {
                    userSuccessChance = 0;
                    successChanceText.setText("Šansa za pogodak: 0%");
                    return;
                }

                AtomicInteger completed = new AtomicInteger(0);
                AtomicInteger total = new AtomicInteger(0);
                AtomicInteger processedTasks = new AtomicInteger(0);

                for (Task task : tasks) {
                    if (task.getStatus() == TaskStatus.PAUZIRAN || task.getStatus() == TaskStatus.OTKAZAN) {
                        processedTasks.incrementAndGet();
                        continue;
                    }

                    taskRepository.isTaskOverQuota(task, userId, new TaskRepository.OnQuotaCheckedListener() {
                        @Override
                        public void onResult(boolean overQuota) {
                            if (!overQuota) {
                                total.incrementAndGet();
                                if (task.getCompletionDate() != null) {
                                    completed.incrementAndGet();
                                }
                            }

                            if (processedTasks.incrementAndGet() == tasks.size()) {
                                int chance = total.get() == 0 ? 0 : (int)((completed.get() * 100.0) / total.get());
                                userSuccessChance = chance;
                                successChanceText.setText("Šansa za pogodak: " + userSuccessChance + "%");
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            processedTasks.incrementAndGet();
                            if (processedTasks.get() == tasks.size()) {
                                int chance = total.get() == 0 ? 0 : (int)((completed.get() * 100.0) / total.get());
                                userSuccessChance = chance;
                                successChanceText.setText("Šansa za pogodak: " + userSuccessChance + "%");
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                userSuccessChance = 0;
                successChanceText.setText("Šansa za pogodak: 0%");
            }
        });
    }
}