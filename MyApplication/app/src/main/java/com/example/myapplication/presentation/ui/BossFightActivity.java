package com.example.myapplication.presentation.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaPlayer;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.database.BossRepositoryFirebaseImpl; // Nova klasa
import com.example.myapplication.data.database.TaskRepositoryFirebaseImpl;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.BossRepository; // Novi interfejs
import com.example.myapplication.data.repository.ItemRepository;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.Boss;
import com.example.myapplication.domain.models.Equipment;
import com.example.myapplication.domain.models.ShakeDetector;
import com.example.myapplication.domain.models.Task;
import com.example.myapplication.domain.models.TaskStatus;
import com.example.myapplication.domain.models.User;
import com.example.myapplication.domain.models.UserEquipment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class BossFightActivity extends AppCompatActivity {

    private Boss boss;
    private ProgressBar bossHpBar, userPPBar;
    private TextView bossHpText, attemptsText, successChanceText, userPPText, chestRewardText;
    private Button attackButton;
    private AnimationDrawable bossAnimation, chestAnimation;
    private ImageView bossImage, chestImage;
    private int baseReward = 200;
    // Oprez: bossCount se više neće koristiti direktno za nagradu, već će level bosa biti merilo.
    // Uklonimo je, jer je suvišna kada stanje čuvamo u Boss objektu.

    private int attemptsLeft = 5; // Ostavićemo je ovde za početnu vrednost
    private int userPP = 0;
    private int userSuccessChance = 2;
    private com.example.myapplication.domain.models.User currentUser;

    private Random random = new Random();
    private UserRepository userRepository;
    private TaskRepository taskRepository;
    private BossRepository bossRepository; // Novi repozitorijum
    private String currentUserId; // ID trenutno ulogovanog korisnika

    private ProgressBar userStageProgressBar;
    private TextView userStageProgressText;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ShakeDetector shakeDetector;
    private int lastReward;
    private LinearLayout layoutActiveEquipment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss_fight);

        // Inicijalizacija repozitorijuma
        userRepository = new UserRepositoryFirebaseImpl();
        taskRepository = new TaskRepositoryFirebaseImpl();
        bossRepository = new BossRepositoryFirebaseImpl(); // Inicijalizacija Boss repozitorijuma

        bossHpBar = findViewById(R.id.bossHpBar);
        bossHpText = findViewById(R.id.bossHpText);
        attemptsText = findViewById(R.id.attemptsText);
        successChanceText = findViewById(R.id.successChanceText);
        attackButton = findViewById(R.id.attackButton);
        bossImage = findViewById(R.id.bossImage);
        userPPBar = findViewById(R.id.userPPBar);
        userPPText = findViewById(R.id.userPPText);
        userStageProgressBar = findViewById(R.id.userStageProgressBar);
        userStageProgressText = findViewById(R.id.userStageProgressText);
        chestImage = findViewById(R.id.chestImage);
        chestImage.setVisibility(View.GONE);
        chestRewardText = findViewById(R.id.chestRewardText);
        layoutActiveEquipment = findViewById(R.id.layoutActiveEquipment);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        shakeDetector = new ShakeDetector(() -> {
            if (chestImage.getVisibility() == View.VISIBLE) {
                openChestAnimation(lastReward);
            }
        });
        attackButton.setEnabled(false);

        // Prikaz idle animacije
        bossImage.setImageResource(R.drawable.boss_idle_animation);
        bossImage.post(() -> {
            bossAnimation = (AnimationDrawable) bossImage.getDrawable();
            bossAnimation.start();
        });

        // Početno učitavanje bosa i korisnika
        loadBossAndUser();

        attackButton.setOnClickListener(v -> attackBoss());

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Priprema za borbu")
                .setMessage("Da li želiš da aktiviraš opremu pre borbe?")
                .setCancelable(false)
                .setPositiveButton("Da", (dialog, which) -> {
                    Intent intent = new Intent(BossFightActivity.this, InventoryActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Ne", (dialog, which) -> {
                    Toast.makeText(BossFightActivity.this, "Borba počinje bez dodatne opreme.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void loadBossAndUser() {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Prvo učitaj bosa
            bossRepository.getBoss(currentUserId, new BossRepository.OnBossLoadedListener() {
                @Override
                public void onSuccess(Boss loadedBoss) {
                    if (loadedBoss != null) {
                        boss = loadedBoss;
                    } else {
                        // Kreiraj novog bosa ako ne postoji u bazi
                        boss = new Boss(1, 0);
                        boss.setId(currentUserId);
                        saveBoss();
                    }
                    // Ažuriraj UI sa podacima iz baze
                    bossHpBar.setMax(boss.getMaxHp());
                    bossHpBar.setProgress(boss.getHp());
                    attemptsLeft = boss.getAttemptsLeft();
                    updateUI();

                    // Zatim učitaj korisnika
                    loadCurrentUser();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(BossFightActivity.this, "Greška pri učitavanju bosa", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveBoss() {
        if (boss != null && boss.getId() != null) {
            bossRepository.saveBoss(boss, new BossRepository.OnBossSavedListener() {
                @Override
                public void onSuccess() {
                    // Boss je uspešno sačuvan
                }
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(BossFightActivity.this, "Greška pri čuvanju bosa", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Prilagođena metoda attackBoss() sa ispravljenom logikom
    // Izmenjena metoda attackBoss() sa ispravnom logikom
    private void attackBoss() {
        if (boss.getAttemptsLeft() <= 0) {
            Toast.makeText(this, "Nema više pokušaja!", Toast.LENGTH_SHORT).show();
            return;
        }

        boss.setAttemptsLeft(boss.getAttemptsLeft() - 1);

        int roll = random.nextInt(100);
        if (roll < userSuccessChance) {
            boss.takeDamage(userPP);
            playHitAnimation();
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.crush_boss);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> mp.release());
            Toast.makeText(this, "Pogodak! -" + userPP + " HP", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Promašaj!", Toast.LENGTH_SHORT).show();
        }

        // Provera da li je bos pobeđen nakon napada
        if (boss.getHp() <= 0) {
            // Postavi trajno stanje pobeđenosti na 'true'
            boss.setIsDefeated(true);

            // Zatim pozovi metodu za nagradu
            giveReward();

            attackButton.setEnabled(false);
            Toast.makeText(this, "Pobedio si bosa!", Toast.LENGTH_LONG).show();

            // Priprema za sledećeg bosa
            int nextLevel = boss.getLevel() + 1;
            int previousHp = boss.getMaxHp();
            Boss newBoss = new Boss(nextLevel, previousHp);
            newBoss.setId(currentUserId);
            boss = newBoss;

            // Sačuvaj i trenutnog (pobeđenog) i novog bosa
            saveBoss();

        } else if (boss.getAttemptsLeft() == 0) {
            // Opcionalna nagrada ako je borba završena
            giveReward();
            attackButton.setEnabled(false);
            Toast.makeText(this, "Kraj borbe! Bos preživeo.", Toast.LENGTH_LONG).show();
        }

        // Na kraju, sačuvaj trenutno stanje bosa (HP, attemptsLeft)
        updateUI();
        saveBoss();
    }

    private void updateUI() {
        bossHpBar.setProgress(boss.getHp());
        bossHpText.setText("HP: " + boss.getHp() + "/" + boss.getMaxHp());
        attemptsText.setText("Pokušaji: " + boss.getAttemptsLeft() + "/5");
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

            int totalDuration = 0;
            for (int i = 0; i < bossAnimation.getNumberOfFrames(); i++) {
                totalDuration += bossAnimation.getDuration(i);
            }

            bossImage.postDelayed(this::startIdleAnimation, totalDuration);
        });
    }

    private void loadCurrentUser() {
        if (currentUserId != null) {
            userRepository.getUserById(currentUserId, new UserRepository.OnCompleteListener<com.example.myapplication.domain.models.User>() {
                @Override
                public void onSuccess(com.example.myapplication.domain.models.User user) {
                    if (user != null) {
                        currentUser = user;
                        updateActiveEquipment(currentUser);
                        userPP = currentUser.getPowerPoints();
                        int maxPP = currentUser.getTotalPowerPoints();
                        userPPBar.setMax(maxPP);
                        userPPBar.setProgress(userPP);
                        userPPText.setText("PP: " + userPP + "/" + maxPP);
                        attackButton.setEnabled(true);
                        Toast.makeText(BossFightActivity.this, "Korisnik učitan, PP: " + userPP, Toast.LENGTH_SHORT).show();
                        loadUserSuccessChance(currentUser.getUserId());
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
        }
    }

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

    private void giveReward() {
        // Proveri da li je nagrada već dodeljena
        if (boss.getIsDefeated()) {
            // Puna nagrada, ali samo ako je bos tek poražen
            double rawReward = baseReward * Math.pow(1.2, boss.getLevel());
            int reward = (int) Math.round(rawReward);

            if (reward > 0 && currentUser != null) {
                currentUser.setCoins(currentUser.getCoins() + reward);
                userRepository.updateUser(currentUser, new UserRepository.OnCompleteListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(getApplicationContext(), "User updated!", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getApplicationContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                lastReward = reward;
                chestImage.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Prodrmaj telefon da otvoriš kovčeg!", Toast.LENGTH_LONG).show();

                // Resetuj isDefeated status nakon dodele nagrade da se ne bi ponovo dodeljivala
                boss.setIsDefeated(false);
                saveBoss();
            }
        } else if (boss.getAttemptsLeft() == 0 && boss.getHp() <= boss.getMaxHp() / 2) {
            // Pola nagrade, samo ako se borba završila bez pobede, a HP je nizak
            double rawReward = (baseReward * Math.pow(1.2, boss.getLevel())) / 2.0;
            int reward = (int) Math.round(rawReward);

            if (reward > 0 && currentUser != null) {
                currentUser.setCoins(currentUser.getCoins() + reward);
                userRepository.updateUser(currentUser, new UserRepository.OnCompleteListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(getApplicationContext(), "User updated!", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getApplicationContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                lastReward = reward;
                chestImage.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Prodrmaj telefon da otvoriš kovčeg!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openChestAnimation(int reward) {
        chestRewardText.setText("+" + reward + " coins");
        chestRewardText.setVisibility(View.VISIBLE);
        chestRewardText.setAlpha(1f);

        chestImage.setImageResource(R.drawable.chest_open_animation);
        chestAnimation = (AnimationDrawable) chestImage.getDrawable();
        chestAnimation.start();

        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.cup);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mp -> mp.release());

        ObjectAnimator animator = ObjectAnimator.ofFloat(chestRewardText, "translationY", 0f, -200f);
        animator.setDuration(1000);
        animator.start();

        int totalDuration = 0;
        for (int i = 0; i < chestAnimation.getNumberOfFrames(); i++) {
            totalDuration += chestAnimation.getDuration(i);
        }

        chestImage.postDelayed(() -> {
            Toast.makeText(this, "Osvojio si " + reward + " coins!", Toast.LENGTH_LONG).show();
            chestRewardText.setTranslationY(0f);
        }, totalDuration);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
        loadBossAndUser();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(shakeDetector);
    }

    private void updateActiveEquipment(User user) {
        layoutActiveEquipment.removeAllViews();

        if (user.getEquipment() != null && !user.getEquipment().isEmpty()) {
            boolean hasActive = false;
            for (UserEquipment ue : user.getEquipment()) {
                if (ue != null && ue.isActive()) {
                    Equipment eqDetails = ItemRepository.getEquipmentById(ue.getEquipmentId());
                    if (eqDetails != null) {
                        hasActive = true;
                        ImageView icon = new ImageView(this);
                        icon.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
                        int resId = getResources().getIdentifier(eqDetails.getIconResourceId(), "drawable", getPackageName());
                        if (resId != 0) {
                            icon.setImageResource(resId);
                        } else {
                            icon.setImageResource(R.drawable.elixir_greatness);
                        }
                        icon.setPadding(16, 0, 16, 0);
                        layoutActiveEquipment.addView(icon);
                    }
                }
            }
            if (!hasActive) {
                addNoEquipmentText();
            }
        } else {
            addNoEquipmentText();
        }
    }

    private void addNoEquipmentText() {
        TextView noEq = new TextView(this);
        noEq.setText("Nema aktivirane opreme.");
        noEq.setTextSize(16f);
        noEq.setTextColor(getResources().getColor(android.R.color.darker_gray));
        layoutActiveEquipment.addView(noEq);
    }
}