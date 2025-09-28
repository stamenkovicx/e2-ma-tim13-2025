package com.example.myapplication.presentation.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.ItemRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.Alliance;
import com.example.myapplication.domain.models.Equipment;
import com.example.myapplication.domain.models.MissionProgressItem;
import com.example.myapplication.domain.models.SpecialMissionProgress;
import com.example.myapplication.domain.models.User;
import com.example.myapplication.domain.models.UserEquipment;
import com.example.myapplication.presentation.ui.adapters.MissionProgressAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SpecialMissionActivity extends AppCompatActivity {

    private TextView tvTimeRemaining, tvBossHp, tvMembersProgressTitle;
    private ProgressBar progressBossHp;
    private RecyclerView membersProgressRecyclerView;
    private MissionProgressAdapter progressAdapter;
    private ImageView bossImage;
    private ListenerRegistration missionProgressRegistration;
    private UserRepository userRepository;
    private String allianceId;
    private String currentUserId;
    private CountDownTimer countDownTimer;

    // Promenljive za logiku nagrada i animaciju
    private final HashMap<String, Boolean> missionEndedMap = new HashMap<>();
    private Group missionLayoutGroup;
    private ImageView chestImage;
    private TextView tvRewardCoins;
    private LinearLayout layoutRewardItems;
    private ImageView ivRewardPotion, ivRewardClothing, ivRewardBadge;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_mission);

        allianceId = getIntent().getStringExtra("allianceId");
        currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        userRepository = new UserRepositoryFirebaseImpl();

        initializeViews();
        setupRecyclerView();
        loadMissionData();
    }

    private void initializeViews() {
        missionLayoutGroup = findViewById(R.id.missionLayoutGroup);
        bossImage = findViewById(R.id.bossImage);
        tvTimeRemaining = findViewById(R.id.tvTimeRemaining);
        tvBossHp = findViewById(R.id.tvBossHp);
        progressBossHp = findViewById(R.id.progressBossHp);
        membersProgressRecyclerView = findViewById(R.id.membersProgressRecyclerView);
       // tvPersonalProgressTitle = findViewById(R.id.tvPersonalProgressTitle);
        tvMembersProgressTitle = findViewById(R.id.tvMembersProgressTitle);

        chestImage = findViewById(R.id.chestImage);
        tvRewardCoins = findViewById(R.id.tvRewardCoins);
        layoutRewardItems = findViewById(R.id.layoutRewardItems);
        ivRewardPotion = findViewById(R.id.ivRewardPotion);
        ivRewardClothing = findViewById(R.id.ivRewardClothing);
        ivRewardBadge = findViewById(R.id.ivRewardBadge);

        bossImage.setBackgroundResource(R.drawable.boss_idle_animation);
        AnimationDrawable idleAnimation = (AnimationDrawable) bossImage.getBackground();
        idleAnimation.start();
    }

    private void setupRecyclerView() {
        progressAdapter = new MissionProgressAdapter(new ArrayList<>());
        membersProgressRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        membersProgressRecyclerView.setAdapter(progressAdapter);
    }

    private void loadMissionData() {
        userRepository.getAllianceById(allianceId, new UserRepository.OnCompleteListener<Alliance>() {
            @Override
            public void onSuccess(Alliance alliance) {
                if (alliance != null) {
                    if (alliance.isSpecialMissionActive()) {
                        missionLayoutGroup.setVisibility(View.VISIBLE);
                        updateBossHpUI(alliance.getSpecialMissionBossHp(), alliance.getSpecialMissionBossMaxHp());
                        startTimer(alliance.getSpecialMissionStartTime());
                        startMissionProgressListener(alliance.getMemberIds(), alliance.getLeaderId());
                        checkForCompletionAndGiveRewards(alliance);
                    } else {
                        tvTimeRemaining.setText("Misija je završena.");
                        checkForCompletionAndGiveRewards(alliance);
                    }
                    checkForUserRewardStatus();
                }
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SpecialMissionActivity.this, "Greška pri učitavanju misije.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startTimer(Date startTime) {
        if (countDownTimer != null) countDownTimer.cancel();
        long missionDurationMillis = TimeUnit.DAYS.toMillis(14);
        long missionEndTimeMillis = startTime.getTime() + missionDurationMillis;
        long remainingTimeMillis = missionEndTimeMillis - System.currentTimeMillis();

        if (remainingTimeMillis <= 0) {
            tvTimeRemaining.setText("Misija je završena.");
            loadMissionData();
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
                loadMissionData();
            }
        }.start();
    }

    private void startMissionProgressListener(List<String> memberIds, String leaderId) {

        List<String> allUserIds = new ArrayList<>();
        if (leaderId != null) {
            allUserIds.add(leaderId);
        }
        for (String memberId : memberIds) {
            if (!memberId.equals(leaderId)) {
                allUserIds.add(memberId);
            }
        }
        if (allUserIds.isEmpty()) return;
        missionProgressRegistration = userRepository.observeMissionProgress(
                allianceId,
                allUserIds,
                new UserRepository.MissionProgressListener<List<SpecialMissionProgress>>() {
                    @Override
                    public void onProgressChange(List<SpecialMissionProgress> progressList) {
                        // OVAJ BLOK SE IZVRŠAVA SVAKI PUT KADA SE PODACI PROMENE!

                        final HashMap<String, SpecialMissionProgress> progressMap = new HashMap<>();
                        // 1. Priprema mape i Ažuriranje ličnog doprinosa
                        for (SpecialMissionProgress progress : progressList) {
                            progressMap.put(progress.getUserId(), progress);
                            Log.d("SpecialMission", "User: " + progress.getUserId() + ", Damage: " + progress.getDamageDealt());
                        }
                        // 2. Ažuriranje liste članova (dohvat korisničkih imena)
                        userRepository.getUsersByIds(allUserIds, new UserRepository.OnCompleteListener<List<User>>() {
                            @Override
                            public void onSuccess(List<User> users) {
                                List<MissionProgressItem> combinedList = new ArrayList<>();
                                for (User user : users) {
                                    SpecialMissionProgress progress = progressMap.get(user.getUserId());
                                    if (progress != null) {
                                        combinedList.add(new MissionProgressItem(user.getUsername(), progress.getDamageDealt()));
                                    }
                                }
                                // AŽURIRANJE LISTE U REALNOM VREMENU
                                progressAdapter.setItems(combinedList);
                                // Dodatno: Ažuriranje HP Bosa
                                refreshBossHp();
                            }
                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(SpecialMissionActivity.this, "Greška pri učitavanju imena članova.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(SpecialMissionActivity.this, "Greška pri slušanju napretka misije.", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void updateBossHpUI(int currentHp, int maxHp) {
        tvBossHp.setText(String.format("Boss HP: %d / %d", currentHp, maxHp));
        progressBossHp.setMax(maxHp);
        progressBossHp.setProgress(currentHp);
    }

    public void refreshBossHp() {
        userRepository.getAllianceById(allianceId, new UserRepository.OnCompleteListener<Alliance>() {
            @Override
            public void onSuccess(Alliance alliance) {
                if (alliance != null) {
                    updateBossHpUI(alliance.getSpecialMissionBossHp(), alliance.getSpecialMissionBossMaxHp());
                    checkForCompletionAndGiveRewards(alliance);
                }
            }
            @Override
            public void onFailure(Exception e) { }
        });
    }

    private void checkForCompletionAndGiveRewards(Alliance alliance) {
        if (alliance == null) return;

        boolean bossDefeated = alliance.getSpecialMissionBossHp() <= 0;
        Boolean userMissionEnded = missionEndedMap.get(currentUserId);

        if (bossDefeated && (userMissionEnded == null || !userMissionEnded)) {
            missionEndedMap.put(currentUserId, true); // Postavi samo za ovog korisnika
            if (countDownTimer != null) countDownTimer.cancel();
            tvTimeRemaining.setText("Misija Uspešno Završena!");

            distributeRewards(); // Ako je pobeđen, pokreni dodelu nagrada
        }
    }


    // pokreće dodelu nagrada za sve
    private void distributeRewards() {
        // Prvo dohvatamo podatke o savezu da bismo dobili listu svih članova
        userRepository.getAllianceById(allianceId, new UserRepository.OnCompleteListener<Alliance>() {
            @Override
            public void onSuccess(Alliance alliance) {
                if (alliance == null) return;

                List<String> allMemberIds = new ArrayList<>(alliance.getMemberIds());
                // Osiguraj da je i vođa na listi
                if (alliance.getLeaderId() != null && !allMemberIds.contains(alliance.getLeaderId())) {
                    allMemberIds.add(alliance.getLeaderId());
                }

                // Petlja koja prolazi kroz SVE članove
                for (String memberId : allMemberIds) {
                    giveRewardToMember(memberId); // Pozivamo pomoćnu metodu za svakog člana
                }

                // Označi misiju kao neaktivnu za ceo savez NAKON što smo pokrenuli dodelu nagrada
                userRepository.endSpecialMission(allianceId, new UserRepository.OnCompleteListener<Void>() {
                    @Override public void onSuccess(Void aVoid) {}
                    @Override public void onFailure(Exception e) {}
                });
            }
            @Override public void onFailure(Exception e) {
                Toast.makeText(SpecialMissionActivity.this, "Greška pri dohvatanju saveza za dodelu nagrada.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // dodeljuje nagradu JEDNOM članu
    private void giveRewardToMember(String memberId) {
        // Prvo proveravamo napredak za OVU misiju da vidimo da li je nagrada već pokupljena
        userRepository.getSpecialMissionProgress(memberId, allianceId, new UserRepository.OnCompleteListener<SpecialMissionProgress>() {
            @Override
            public void onSuccess(SpecialMissionProgress progress) {
                // Ako progres ne postoji ili je nagrada već pokupljena, ne radi ništa
                if (progress == null || progress.areRewardsClaimed()) {
                    return;
                }

                // ko nije, ODMAH "lupamo pečat" da sprečimo duple pozive
                progress.setRewardsClaimed(true);
                userRepository.updateSpecialMissionProgress(progress, new UserRepository.OnCompleteListener<Void>() {
                    @Override public void onSuccess(Void aVoid) {}
                    @Override public void onFailure(Exception e) {}
                });

                // Sada nastavljamo sa dodelom nagrada korisniku
                userRepository.getUserById(memberId, new UserRepository.OnCompleteListener<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (user == null) return;

                        // Povećaj UKUPAN broj završenih misija
                        // Povećaj UKUPAN broj završenih misija i odmah loguj
                        int oldMissionCount = user.getSpecialMissionsCompleted();
                        int newMissionCount = oldMissionCount + 1;
                        user.setSpecialMissionsCompleted(newMissionCount);

                        Log.d("SpecialMission", "Updating specialMissionsCompleted for user " + memberId +
                                ": old=" + oldMissionCount + ", new=" + newMissionCount);

                        // Dodeljujemo ostale nagrade
                        int coinReward = (int) (0.5 * Math.round(240 * Math.pow(1.2, user.getLevel() - 1)));
                        user.setCoins(user.getCoins() + coinReward);

                        Equipment potion = ItemRepository.getRandomPotion();
                        Equipment clothing = ItemRepository.getRandomClothing();
                        user.addEquipment(new UserEquipment(potion.getId(), false, potion.getDuration()));
                        user.addEquipment(new UserEquipment(clothing.getId(), false, clothing.getDuration()));

                        // Dodeljujemo bedž na osnovu ukupnog broja pobeda
                        Equipment badge = ItemRepository.getBadgeForMissionCount(newMissionCount);
                        user.addEquipment(new UserEquipment(badge.getId(), false, badge.getDuration()));

                        // Čuvamo izmene za korisnika
                        userRepository.updateUser(user, new UserRepository.OnCompleteListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("SpecialMission", "Updated specialMissionsCompleted successfully for user " + memberId);
                                if (memberId.equals(currentUserId)) {
                                    showRewardAnimation(coinReward, potion, clothing, badge);
                                }
                            }
                            @Override public void onFailure(Exception e) {
                                Log.e("DistributeRewards", "Failed to update user: " + memberId, e);
                            }
                        });
                    }
                    @Override public void onFailure(Exception e) {
                        Log.e("DistributeRewards", "Failed to get user: " + memberId, e);
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("DistributeRewards", "Failed to get progress for user: " + memberId, e);
            }
        });
    }


    private void showRewardAnimation(int coinReward, Equipment potion, Equipment clothing, Equipment badge) {
        chestImage.setVisibility(View.VISIBLE);
        chestImage.setImageResource(R.drawable.chest_open_animation);
        AnimationDrawable chestAnimation = (AnimationDrawable) chestImage.getDrawable();
        if (chestAnimation != null) chestAnimation.start();
        MediaPlayer.create(this, R.raw.cup).start(); // Pretpostavljam da je zvuk 'cup' otvaranje kovčega

        // Tekst nagrade
        tvRewardCoins.setText("+" + coinReward + " coins");
        tvRewardCoins.setVisibility(View.VISIBLE);

        // Određujemo koliko nisko se inicijalno nalaze (npr. direktno iznad kovčega)
        // I koliko visoko treba da idu (npr. samo malo iznad početne pozicije)
        // Ove vrednosti su relativne u odnosu na njihovu trenutnu poziciju definisanu u XML-u.
        ObjectAnimator coinAnimator = ObjectAnimator.ofFloat(tvRewardCoins, "translationY", 0f, -10f);        coinAnimator.setDuration(1500);

        // Layout za iteme
        layoutRewardItems.setVisibility(View.VISIBLE);
        ivRewardPotion.setImageResource(getResources().getIdentifier(potion.getIconResourceId(), "drawable", getPackageName()));
        ivRewardClothing.setImageResource(getResources().getIdentifier(clothing.getIconResourceId(), "drawable", getPackageName()));
        ivRewardBadge.setImageResource(getResources().getIdentifier(badge.getIconResourceId(), "drawable", getPackageName()));

        AnimatorSet itemsAnimatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(layoutRewardItems, "scaleX", 0.3f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(layoutRewardItems, "scaleY", 0.3f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(layoutRewardItems, "alpha", 0f, 1f);
        ObjectAnimator translateY = ObjectAnimator.ofFloat(layoutRewardItems, "translationY", 50f, 300f); // Pomeri se 100dp gore

        itemsAnimatorSet.playTogether(scaleX, scaleY, alpha, translateY);
        itemsAnimatorSet.setDuration(1200);

        AnimatorSet allAnimations = new AnimatorSet();
        allAnimations.playTogether(coinAnimator, itemsAnimatorSet);
        allAnimations.start();
    }

    private void playBossHitAnimation() {
        bossImage.setBackgroundResource(R.drawable.boss_hit_animation);
        AnimationDrawable hitAnimation = (AnimationDrawable) bossImage.getBackground();
        hitAnimation.start();

        bossImage.postDelayed(() -> {
            bossImage.setBackgroundResource(R.drawable.boss_idle_animation);
            AnimationDrawable idleAnimation = (AnimationDrawable) bossImage.getBackground();
            idleAnimation.start();
        }, getAnimationDuration(hitAnimation));
    }

    private long getAnimationDuration(AnimationDrawable anim) {
        long duration = 0;
        for (int i = 0; i < anim.getNumberOfFrames(); i++) {
            duration += anim.getDuration(i);
        }
        return duration;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        if (missionProgressRegistration != null) missionProgressRegistration.remove();
    }
    private void checkForUserRewardStatus() {
        userRepository.getSpecialMissionProgress(currentUserId, allianceId, new UserRepository.OnCompleteListener<SpecialMissionProgress>() {
            @Override
            public void onSuccess(SpecialMissionProgress progress) {
                if (progress != null && progress.areRewardsClaimed()) {
                    // Ako je nagrada već upisana u bazu → ponovo pokreni animaciju nagrada
                    userRepository.getUserById(currentUserId, new UserRepository.OnCompleteListener<User>() {
                        @Override
                        public void onSuccess(User user) {
                            if (user == null) return;

                            // Ovde možeš izračunati nagradu isto kao u giveRewardToMember
                            int coinReward = (int) (0.5 * Math.round(240 * Math.pow(1.2, user.getLevel() - 1)));

                            Equipment potion = ItemRepository.getRandomPotion();
                            Equipment clothing = ItemRepository.getRandomClothing();
                            Equipment badge = ItemRepository.getBadgeForMissionCount(user.getSpecialMissionsCompleted());

                            // Pokreni animaciju nagrada
                            showRewardAnimation(coinReward, potion, clothing, badge);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("RewardCheck", "Greška pri dohvatanju korisnika za prikaz nagrade", e);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("RewardCheck", "Greška pri proveri statusa nagrade", e);
            }
        });
    }


}