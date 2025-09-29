package com.example.myapplication.presentation.ui;

import android.animation.AnimatorSet;
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
import com.example.myapplication.data.database.BossRepositoryFirebaseImpl;
import com.example.myapplication.data.database.TaskRepositoryFirebaseImpl;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.BossRepository;
import com.example.myapplication.data.repository.ItemRepository;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.*;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class BossFightActivity extends AppCompatActivity {

    // UI Elementi
    private ProgressBar bossHpBar, userPPBar;
    private TextView bossHpText, attemptsText, successChanceText, userPPText, chestRewardText;
    private Button attackButton;
    private ImageView bossImage, chestImage, ivDroppedItemIcon;
    private LinearLayout layoutActiveEquipment;
    private AnimationDrawable bossAnimation, chestAnimation;

    // Modeli i podaci
    private Boss boss;
    private PlayerState playerState;
    private User currentUser;
    private String currentUserId;
    private Equipment lastDroppedItem = null;
    private boolean canAttack = false;
    private Random random = new Random();

    // Repozitorijumi
    private BossRepository bossRepository;
    private UserRepository userRepository;
    private TaskRepository taskRepository;

    // Senzori
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ShakeDetector shakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss_fight);

        // Inicijalizacija
        bossRepository = new BossRepositoryFirebaseImpl();
        userRepository = new UserRepositoryFirebaseImpl();
        taskRepository = new TaskRepositoryFirebaseImpl();

        // Povezivanje UI elemenata
        bossHpBar = findViewById(R.id.bossHpBar);
        bossHpText = findViewById(R.id.bossHpText);
        userPPBar = findViewById(R.id.userPPBar);
        userPPText = findViewById(R.id.userPPText);
        attemptsText = findViewById(R.id.attemptsText);
        successChanceText = findViewById(R.id.successChanceText);
        attackButton = findViewById(R.id.attackButton);
        bossImage = findViewById(R.id.bossImage);
        chestImage = findViewById(R.id.chestImage);
        chestRewardText = findViewById(R.id.chestRewardText);
        ivDroppedItemIcon = findViewById(R.id.ivDroppedItemIcon);
        layoutActiveEquipment = findViewById(R.id.layoutActiveEquipment);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        setupListeners();
        startBossIdleAnimation();
        showEquipmentDialog();
    }

    private void setupListeners() {
        shakeDetector = new ShakeDetector(() -> {
            if (chestImage.getVisibility() == View.VISIBLE) {
                openChestAnimation(playerState.getLastReward(), lastDroppedItem);
            } else if (canAttack) {
                attackBoss();
            }
        });

        attackButton.setOnClickListener(v -> {
            if (canAttack) {
                attackBoss();
            }
        });

        chestImage.setOnClickListener(v -> {
            if (chestImage.getVisibility() == View.VISIBLE) {
                openChestAnimation(playerState.getLastReward(), lastDroppedItem);
            }
        });
    }

    private void startBossIdleAnimation() {
        bossImage.setImageResource(R.drawable.boss_idle_animation);
        bossImage.post(() -> {
            bossAnimation = (AnimationDrawable) bossImage.getDrawable();
            if (bossAnimation != null) bossAnimation.start();
        });
    }

    private void showEquipmentDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Priprema za borbu")
                .setMessage("Da li želiš da aktiviraš opremu pre borbe?")
                .setCancelable(false)
                .setPositiveButton("Da", (dialog, which) -> startActivity(new Intent(BossFightActivity.this, InventoryActivity.class)))
                .setNegativeButton("Ne", (dialog, which) -> Toast.makeText(BossFightActivity.this, "Napadni dugmetom ili protresi telefon!", Toast.LENGTH_LONG).show())
                .show();
    }

    private void loadBossAndUser() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRepository.getUserById(currentUserId, new UserRepository.OnCompleteListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    currentUser = user;
                    bossRepository.getBoss(currentUserId, new BossRepository.OnBossLoadedListener() {
                        @Override
                        public void onSuccess(Boss loadedBoss) {
                            if (loadedBoss != null) {
                                boss = loadedBoss;
                            } else {
                                boss = new Boss(1, 0, currentUser.getLevel());
                                boss.setId(currentUserId);
                                saveBoss();
                            }
                            setupFight();
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(BossFightActivity.this, "Greška pri učitavanju bosa.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(BossFightActivity.this, "Greška pri učitavanju korisnika.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFight() {
        // LOGIKA ZA NEPORAŽENOG BOSA
        if (!boss.getIsDefeated() && currentUser.getLevel() > boss.getUserLevelOnEncounter()) {
            boss.setHp(boss.getMaxHp());
            boss.setAttemptsLeft(5);
            boss.setUserLevelOnEncounter(currentUser.getLevel());
            saveBoss();
            Toast.makeText(BossFightActivity.this, "Neporaženi bos se vratio sa punom snagom!", Toast.LENGTH_LONG).show();
        }

        playerState = new PlayerState(currentUserId, currentUser.getPowerPoints(), 0, 0);

        updateActiveEquipment(currentUser);
        loadUserSuccessChance(currentUserId);
        updateUI();

        canAttack = true;
    }

    private void attackBoss() {
        if (!canAttack || boss.getAttemptsLeft() <= 0) return;

        boss.setAttemptsLeft(boss.getAttemptsLeft() - 1);

        // Generisanje i prikaz nasumičnog broja za napad
        int attackRoll = random.nextInt(100);
        Toast.makeText(this, "Napad: " + attackRoll + " | Šansa: " + playerState.getSuccessChance() + "%", Toast.LENGTH_SHORT).show();

        if (attackRoll < playerState.getSuccessChance()) {
            boss.takeDamage(playerState.getPowerPoints());
            playHitAnimation();
            MediaPlayer.create(this, R.raw.crush_boss).start();
            Toast.makeText(this, "Pogodak! -" + playerState.getPowerPoints() + " HP", Toast.LENGTH_SHORT).show();
            if (currentUser.getAllianceId() != null && !currentUser.getAllianceId().isEmpty()) {
                userRepository.applyBossHitDamage(
                        currentUser.getAllianceId(),
                        currentUserId,
                        new UserRepository.OnCompleteListener<Void>() {
                            @Override public void onSuccess(Void result) {
                                // Šteta registrovana ili je limit dostignut
                            }
                            @Override public void onFailure(Exception e) {
                                // Greška pri primeni štete na specijalnom bosu
                            }
                        }
                );
            }
        } else {
            Toast.makeText(this, "Promašaj!", Toast.LENGTH_SHORT).show();
        }

        updateUI();

        if (boss.getHp() <= 0 || boss.getAttemptsLeft() == 0) {
            canAttack = false;
            boolean bossDefeated = boss.getHp() <= 0;

            if(bossDefeated) boss.setIsDefeated(true);

            giveReward(bossDefeated);

            if (bossDefeated) {
                Toast.makeText(this, "Pobedio si bosa!", Toast.LENGTH_LONG).show();
                int nextLevel = boss.getLevel() + 1;
                int previousHp = boss.getMaxHp();
                Boss newBoss = new Boss(nextLevel, previousHp, currentUser.getLevel());
                newBoss.setId(currentUserId);
                this.boss = newBoss;
            } else {
                Toast.makeText(this, "Kraj borbe! Nema više pokušaja.", Toast.LENGTH_LONG).show();
            }
        }
        saveBoss();
    }

    private void giveReward(boolean isDefeated) {
        if (currentUser == null) return;

        int reward = 0;
        if (isDefeated) {
            reward = (int) Math.round(boss.getBaseReward() * Math.pow(1.2, boss.getLevel() - 1));
        } else if (boss.getHp() <= boss.getMaxHp() / 2) {
            reward = (int) Math.round((boss.getBaseReward() * Math.pow(1.2, boss.getLevel() - 1)) / 2.0);
        }

        if (reward > 0) {
            currentUser.setCoins(currentUser.getCoins() + reward);
            playerState.setLastReward(reward);
        }

        Equipment droppedItem = null;
        int dropChance = 20;
        if (!isDefeated) dropChance /= 2;

        // Prikaz nasumičnog broja za šansu za opremu
        int dropChanceRoll = random.nextInt(100);
        Toast.makeText(this, "Šansa za opremu: " + dropChanceRoll + " | Potrebno: <" + dropChance + "%", Toast.LENGTH_LONG).show();

        if (dropChanceRoll < dropChance) {
            // Prikaz nasumičnog broja za tip opreme
            int itemTypeRoll = random.nextInt(100);
            Toast.makeText(this, "Šansa za tip opreme: " + itemTypeRoll + " | <95% za odeću", Toast.LENGTH_LONG).show();

            if (itemTypeRoll < 95) {
                droppedItem = ItemRepository.getRandomClothing();
            } else {
                droppedItem = ItemRepository.getRandomWeapon();
            }
        }

        if (droppedItem != null) {
            currentUser.addEquipment(new UserEquipment(droppedItem.getId(), false, droppedItem.getDuration()));
            this.lastDroppedItem = droppedItem;
            Toast.makeText(this, "Pronašao si opremu: " + droppedItem.getName() + "!", Toast.LENGTH_LONG).show();
        }

        if (reward > 0 || droppedItem != null) {
            userRepository.updateUser(currentUser, new UserRepository.OnCompleteListener<Void>() {
                @Override public void onSuccess(Void result) {}
                @Override public void onFailure(Exception e) {
                    Toast.makeText(getApplicationContext(), "Greška pri čuvanju nagrade.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (reward > 0 || droppedItem != null) {
            chestImage.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Otvori kovčeg!", Toast.LENGTH_SHORT).show();
        }
    }

    private void openChestAnimation(int reward, Equipment item) {
        chestImage.setOnClickListener(null);
        sensorManager.unregisterListener(shakeDetector);

        chestImage.setImageResource(R.drawable.chest_open_animation);
        chestAnimation = (AnimationDrawable) chestImage.getDrawable();
        if (chestAnimation != null) chestAnimation.start();
        MediaPlayer.create(this, R.raw.cup).start();

        if (reward > 0) {
            chestRewardText.setText("+" + reward + " coins");
            chestRewardText.setVisibility(View.VISIBLE);
            ObjectAnimator coinAnimator = ObjectAnimator.ofFloat(chestRewardText, "translationY", 0f, -200f);
            coinAnimator.setDuration(1500);
            coinAnimator.start();
        }

        if (item != null) {
            int resId = getResources().getIdentifier(item.getIconResourceId(), "drawable", getPackageName());
            if (resId != 0) ivDroppedItemIcon.setImageResource(resId);

            ivDroppedItemIcon.setVisibility(View.VISIBLE);
            ivDroppedItemIcon.setAlpha(0f);

            AnimatorSet itemAnimatorSet = new AnimatorSet();
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivDroppedItemIcon, "scaleX", 0.3f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivDroppedItemIcon, "scaleY", 0.3f, 1f);
            ObjectAnimator translationY = ObjectAnimator.ofFloat(ivDroppedItemIcon, "translationY", 0f, -150f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(ivDroppedItemIcon, "alpha", 0f, 1f);
            itemAnimatorSet.playTogether(scaleX, scaleY, translationY, alpha);
            itemAnimatorSet.setDuration(1200);
            itemAnimatorSet.start();
        }
    }

    private void updateUI() {
        if(boss == null || playerState == null || currentUser == null) return;
        bossHpBar.setMax(boss.getMaxHp());
        bossHpBar.setProgress(boss.getHp());
        bossHpText.setText("HP: " + boss.getHp() + "/" + boss.getMaxHp());
        userPPBar.setMax(currentUser.getTotalPowerPoints());
        userPPBar.setProgress(playerState.getPowerPoints());
        userPPText.setText("PP: " + playerState.getPowerPoints() + "/" + currentUser.getTotalPowerPoints());
        attemptsText.setText("Pokušaji: " + boss.getAttemptsLeft() + "/5");
        successChanceText.setText("Šansa za pogodak: " + playerState.getSuccessChance() + "%");
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

    private void saveBoss() {
        if (boss != null && boss.getId() != null) {
            bossRepository.saveBoss(boss, new BossRepository.OnBossSavedListener() {
                @Override public void onSuccess() {}
                @Override public void onFailure(Exception e) {
                    Toast.makeText(BossFightActivity.this, "Greška pri čuvanju bosa.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void playHitAnimation() {
        bossImage.setImageResource(R.drawable.boss_hit_animation);
        bossImage.post(() -> {
            bossAnimation = (AnimationDrawable) bossImage.getDrawable();
            if (bossAnimation != null) {
                bossAnimation.start();
                int totalDuration = 0;
                for (int i = 0; i < bossAnimation.getNumberOfFrames(); i++) {
                    totalDuration += bossAnimation.getDuration(i);
                }
                bossImage.postDelayed(this::startBossIdleAnimation, totalDuration);
            }
        });
    }

    private void updateActiveEquipment(User user) {
        layoutActiveEquipment.removeAllViews();
        if (user.getEquipment() != null && !user.getEquipment().isEmpty()) {
            boolean hasActive = false;
            for (UserEquipment ue : user.getEquipment()) {
                if (ue != null && ue.isActive()) {
                    hasActive = true;
                    Equipment eqDetails = ItemRepository.getEquipmentById(ue.getEquipmentId());
                    if (eqDetails != null) {
                        ImageView icon = new ImageView(this);
                        int sizeInDp = 90; // nova veličina ikone
                        float scale = getResources().getDisplayMetrics().density;
                        int sizeInPx = (int) (sizeInDp * scale + 0.5f);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
                        params.setMargins(8, 0, 8, 0); // dodaj malo margina između ikona
                        icon.setLayoutParams(params);
                        int resId = getResources().getIdentifier(eqDetails.getIconResourceId(), "drawable", getPackageName());
                        if (resId != 0) icon.setImageResource(resId);
                        layoutActiveEquipment.addView(icon);
                    }
                }
            }
            if (!hasActive) addNoEquipmentText();
        } else {
            addNoEquipmentText();
        }
    }

    private void addNoEquipmentText() {
        TextView noEq = new TextView(this);
        noEq.setText("Nema aktivne opreme.");
        layoutActiveEquipment.addView(noEq);
    }

    private void loadUserSuccessChance(String userId) {
        taskRepository.getAllTasks(userId, new TaskRepository.OnTasksLoadedListener() {
            @Override
            public void onSuccess(List<Task> allTasks) {
                if (allTasks == null || allTasks.isEmpty()) {
                    if (playerState != null) playerState.setSuccessChance(0);
                    updateUI();
                    return;
                }

                AtomicInteger completed = new AtomicInteger(0);
                AtomicInteger total = new AtomicInteger(0);
                int taskCount = allTasks.size();
                AtomicInteger processedCount = new AtomicInteger(0);

                for (Task task : allTasks) {
                    // preskačemo pauzirane i otkazane
                    if (task.getStatus() == TaskStatus.PAUZIRAN || task.getStatus() == TaskStatus.OTKAZAN) {
                        if (processedCount.incrementAndGet() == taskCount) {
                            calculateAndSetChance(completed.get(), total.get());
                        }
                        continue;
                    }

                    // proveravamo da li je preko kvote
                    taskRepository.isTaskOverQuota(task, userId, new TaskRepository.OnQuotaCheckedListener() {
                        @Override
                        public void onResult(boolean overQuota) {
                            if (!overQuota) {
                                total.incrementAndGet();
                                if (task.getCompletionDate() != null) {
                                    completed.incrementAndGet();
                                }
                            }
                            if (processedCount.incrementAndGet() == taskCount) {
                                calculateAndSetChance(completed.get(), total.get());
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            if (processedCount.incrementAndGet() == taskCount) {
                                calculateAndSetChance(completed.get(), total.get());
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (playerState != null) playerState.setSuccessChance(0);
                updateUI();
            }
        });
    }


    private void calculateAndSetChance(int completed, int total) {
        if (playerState != null) {
            int chance = total == 0 ? 0 : (int) ((completed * 100.0) / total);
            playerState.setSuccessChance(chance);
            updateUI();
        }
    }
}