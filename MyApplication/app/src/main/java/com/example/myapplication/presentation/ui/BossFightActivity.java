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
import com.example.myapplication.data.database.TaskRepositoryFirebaseImpl;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
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
    private ProgressBar bossHpBar,userPPBar;
    private TextView bossHpText, attemptsText, successChanceText,userPPText,chestRewardText;
    private Button attackButton;
    private AnimationDrawable bossAnimation,chestAnimation;
    private ImageView bossImage,chestImage;
    private int baseReward = 200;
    private int bossCount = 0; // broj poraženih bosova


    private int attemptsLeft = 5;
    private int userPP = 0; // sada će se učitati iz baze
    private int userSuccessChance = 2; //svakako ce promeniti vrednost uzece da gleda iz zads
    private com.example.myapplication.domain.models.User currentUser;


    private Random random = new Random();
    private UserRepository userRepository;
    private TaskRepository taskRepository;

    private ProgressBar userStageProgressBar;
    private TextView userStageProgressText;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ShakeDetector shakeDetector;
    private int lastReward; // globalno polje klase
    private LinearLayout layoutActiveEquipment;



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
        userStageProgressBar = findViewById(R.id.userStageProgressBar);
        userStageProgressText = findViewById(R.id.userStageProgressText);
        chestImage = findViewById(R.id.chestImage);
        chestImage.setVisibility(View.GONE);
        chestRewardText = findViewById(R.id.chestRewardText);
        layoutActiveEquipment = findViewById(R.id.layoutActiveEquipment);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        shakeDetector = new ShakeDetector(() -> {
            // otvori kovčeg kad korisnik prodrma telefon
            openChestAnimation(lastReward);
        });
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

        //otvaranje dijaloga za aktivaciju opreme
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
            bossCount++;
            giveReward();
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
                       // updateUserStageProgress();
                        updateActiveEquipment(currentUser);

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
  /*  private void updateUserStageProgress() {
        if (currentUser != null) {
            double progress = currentUser.getCurrentStageProgress(); // metoda iz User klase
            userStageProgressBar.setProgress((int) progress);
            userStageProgressText.setText("Napredak etape: " + (int) progress + "%");
        }
    }*/

    private void giveReward() {
        // izračunaj pravi reward
        int reward = (int) (baseReward * Math.pow(1.2, bossCount));

        if (!boss.isDefeated() && boss.getHp() <= boss.getMaxHp() / 2) {
            reward /= 2;
        }

        // povećaj korisniku coins
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

        lastReward = reward; // sačuvaj reward za shake
        chestImage.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Prodrmaj telefon da otvoriš kovčeg!", Toast.LENGTH_LONG).show();
    }


    private void openChestAnimation(int reward) {

        // prvo postavi tekst i učini ga vidljivim
        chestRewardText.setText("+" + reward + " coins");
        chestRewardText.setVisibility(View.VISIBLE);
        chestRewardText.setAlpha(1f); // osiguraj da je vidljiv

        chestImage.setImageResource(R.drawable.chest_open_animation);
        chestAnimation = (AnimationDrawable) chestImage.getDrawable();
        chestAnimation.start();

        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.cup);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mp -> mp.release());

        // animacija letenja teksta nagore
        ObjectAnimator animator = ObjectAnimator.ofFloat(chestRewardText, "translationY", 0f, -200f);
        animator.setDuration(1000);
        animator.start();

        int totalDuration = 0;
        for (int i = 0; i < chestAnimation.getNumberOfFrames(); i++) {
            totalDuration += chestAnimation.getDuration(i);
        }

        chestImage.postDelayed(() -> {
            Toast.makeText(this, "Osvojio si " + reward + " coins!", Toast.LENGTH_LONG).show();
          //  chestRewardText.setVisibility(View.GONE); // sakrij tekst nakon animacije
            chestRewardText.setTranslationY(0f); // resetuj poziciju
        }, totalDuration);
    }



    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
        loadCurrentUser();
        if (currentUser != null) {
            updateActiveEquipment(currentUser);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(shakeDetector);
    }
    private void updateActiveEquipment(User user) {
        layoutActiveEquipment.removeAllViews(); // očisti prethodni prikaz

        if (user.getEquipment() != null && !user.getEquipment().isEmpty()) {
            boolean hasActive = false;

            for (UserEquipment ue : user.getEquipment()) {
                if (ue != null && ue.isActive()) {
                    Equipment eqDetails = ItemRepository.getEquipmentById(ue.getEquipmentId());
                    if (eqDetails != null) {
                        hasActive = true;

                        ImageView icon = new ImageView(this);
                        icon.setLayoutParams(new LinearLayout.LayoutParams(
                                200, // širina ikonice u dp
                                200  // visina ikonice u dp
                        ));

                        // Konvertuj string u int resurs
                        int resId = getResources().getIdentifier(
                                eqDetails.getIconResourceId(), // npr. "sword_icon"
                                "drawable",
                                getPackageName()
                        );

                        if (resId != 0) {
                            icon.setImageResource(resId);
                        } else {
                            icon.setImageResource(R.drawable.elixir_greatness); // fallback ikonica
                        }

                        icon.setPadding(16, 0, 16, 0);

                        // dodaj u layout
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