package com.example.myapplication.presentation.ui;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.Alliance;
import com.example.myapplication.domain.models.MissionProgressItem;
import com.example.myapplication.domain.models.SpecialMissionProgress;
import com.example.myapplication.domain.models.User;
import com.example.myapplication.presentation.ui.adapters.MissionProgressAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SpecialMissionActivity extends AppCompatActivity {

    private TextView tvTimeRemaining, tvBossHp,tvPersonalDamage;
    private ProgressBar progressBossHp;
    private UserRepository userRepository;
    private String allianceId;
    private CountDownTimer countDownTimer;
    private RecyclerView membersProgressRecyclerView;
    private MissionProgressAdapter progressAdapter;
    private String currentUserId;
    private ImageView bossImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_mission);
        bossImage = findViewById(R.id.bossImage);

        // Dobijamo ID alijanse koji je poslat iz AllianceActivity
        allianceId = getIntent().getStringExtra("allianceId");
        if (allianceId == null || allianceId.isEmpty()) {
            Toast.makeText(this, "Greška: ID Alijanse nije pronađen.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicijalizacija
        userRepository = new UserRepositoryFirebaseImpl();
        currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        tvTimeRemaining = findViewById(R.id.tvTimeRemaining);
        tvBossHp = findViewById(R.id.tvBossHp);
        progressBossHp = findViewById(R.id.progressBossHp);
        progressBossHp.setVisibility(ProgressBar.GONE);

        tvPersonalDamage = findViewById(R.id.tvPersonalDamage);
        membersProgressRecyclerView = findViewById(R.id.membersProgressRecyclerView);
        membersProgressRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressAdapter = new MissionProgressAdapter(new ArrayList<>());
        membersProgressRecyclerView.setAdapter(progressAdapter);

        tvPersonalDamage.setVisibility(TextView.GONE);
        membersProgressRecyclerView.setVisibility(RecyclerView.GONE);
        findViewById(R.id.tvPersonalProgressTitle).setVisibility(TextView.GONE);
        findViewById(R.id.tvMembersProgressTitle).setVisibility(TextView.GONE);

        bossImage.setBackgroundResource(R.drawable.boss_idle_animation);
        AnimationDrawable idleAnimation = (AnimationDrawable) bossImage.getBackground();
        idleAnimation.start();

        // Pozivamo metodu za ucitavanje podataka
        loadMissionData();
    }

    // Metoda za ucitavanje podataka o misiji iz baze
    private void loadMissionData() {
        userRepository.getAllianceById(allianceId, new UserRepository.OnCompleteListener<Alliance>() {
            @Override
            public void onSuccess(Alliance alliance) {
                if (alliance != null && alliance.isSpecialMissionActive()) {
                    progressBossHp.setVisibility(ProgressBar.VISIBLE);
                    tvPersonalDamage.setVisibility(TextView.VISIBLE);
                    membersProgressRecyclerView.setVisibility(RecyclerView.VISIBLE);
                    findViewById(R.id.tvPersonalProgressTitle).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.tvMembersProgressTitle).setVisibility(TextView.VISIBLE);

                    updateBossHpUI(alliance.getSpecialMissionBossHp(), alliance.getSpecialMissionBossMaxHp());
                    startTimer(alliance.getSpecialMissionStartTime());
                    loadMissionProgress(alliance.getMemberIds(), alliance.getLeaderId());
                } else {
                    progressBossHp.setVisibility(ProgressBar.GONE);
                    tvPersonalDamage.setVisibility(TextView.GONE);
                    membersProgressRecyclerView.setVisibility(RecyclerView.GONE);
                    findViewById(R.id.tvPersonalProgressTitle).setVisibility(TextView.GONE);
                    findViewById(R.id.tvMembersProgressTitle).setVisibility(TextView.GONE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                progressBossHp.setVisibility(ProgressBar.GONE);
                Toast.makeText(SpecialMissionActivity.this, "Greška pri učitavanju misije.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Pomocna metoda za azuriranje prikaza HP-a
    private void updateBossHpUI(int currentHp, int maxHp) {
        tvBossHp.setText(String.format("Boss HP: %d / %d", currentHp, maxHp));
        progressBossHp.setMax(maxHp);
        progressBossHp.setProgress(currentHp);
    }

    // Metoda koja pokrece tajmer
    private void startTimer(Date startTime) {
        // Misija traje 14 dana
        long missionDurationMillis = TimeUnit.DAYS.toMillis(14);
        long missionEndTimeMillis = startTime.getTime() + missionDurationMillis;
        long remainingTimeMillis = missionEndTimeMillis - System.currentTimeMillis();

        if (remainingTimeMillis <= 0) {
            tvTimeRemaining.setText("Misija je završena.");
            return;
        }

        countDownTimer = new CountDownTimer(remainingTimeMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished);
                long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;

                String timeText = String.format("Ostalo još: %d dana %02d:%02d:%02d", days, hours, minutes, seconds);
                tvTimeRemaining.setText(timeText);
            }

            @Override
            public void onFinish() {
                tvTimeRemaining.setText("Misija je završena.");
            }
        }.start();
    }

    //  Obavezno zaustavi tajmer kada se aktivnost unisti da se izbegnu problemi
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
    private void loadMissionProgress(List<String> memberIds, String leaderId) {
        // Kreiranje liste svih ID-jeva (članovi + vođa)
        List<String> allUserIds = new ArrayList<>(memberIds);
        if (leaderId != null && !allUserIds.contains(leaderId)) {
            allUserIds.add(0, leaderId);
        }

        // 1. Učitavanje podataka o napretku (SpecialMissionProgress)
        userRepository.getMissionProgressForUsers(allianceId, allUserIds, new UserRepository.OnCompleteListener<List<SpecialMissionProgress>>() {
            @Override
            public void onSuccess(List<SpecialMissionProgress> progressList) {
                final HashMap<String, SpecialMissionProgress> progressMap = new HashMap<>();
                // Azuriranje licnog napretka
                for (SpecialMissionProgress progress : progressList) {
                    if (progress.getUserId().equals(currentUserId)) {
                        // Koristimo damageDealt za prikaz licnog doprinosa
                        tvPersonalDamage.setText(String.format("Nanesena šteta: %d", progress.getDamageDealt()));
                        break;
                    }
                }

                // 2. Ucitavanje korisnickih imena za sve ID-jeve
                userRepository.getUsersByIds(allUserIds, new UserRepository.OnCompleteListener<List<User>>() {
                    @Override
                    public void onSuccess(List<User> users) {
                        // Spajanje User objekata sa njihovim SpecialMissionProgress podacima
                        List<MissionProgressItem> combinedList = new ArrayList<>();

                        for (User user : users) {
                            for (SpecialMissionProgress progress : progressList) {
                                if (user.getUserId().equals(progress.getUserId())) {
                                    // Spajamo ime korisnika i nanesenu štetu
                                    combinedList.add(new MissionProgressItem(user.getUsername(), progress.getDamageDealt()));
                                    break;
                                }
                            }
                        }
                        // Azuriranje RecyclerView-a (Napredak celog saveza po članu)
                        progressAdapter.setItems(combinedList);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(SpecialMissionActivity.this, "Greška pri učitavanju imena članova.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SpecialMissionActivity.this, "Greška pri učitavanju napretka misije.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void playBossHitAnimation() {
        bossImage.setBackgroundResource(R.drawable.boss_hit_animation);
        AnimationDrawable hitAnimation = (AnimationDrawable) bossImage.getBackground();
        hitAnimation.start();

        // vrati se na idle nakon završetka
        bossImage.postDelayed(() -> {
            bossImage.setBackgroundResource(R.drawable.boss_idle_animation);
            AnimationDrawable idleAnimation = (AnimationDrawable) bossImage.getBackground();
            idleAnimation.start();
        }, getAnimationDuration(hitAnimation));
    }

    // izracunavanje ukupnog trajanja
    private long getAnimationDuration(AnimationDrawable anim) {
        long duration = 0;
        for (int i = 0; i < anim.getNumberOfFrames(); i++) {
            duration += anim.getDuration(i);
        }
        return duration;
    }

}