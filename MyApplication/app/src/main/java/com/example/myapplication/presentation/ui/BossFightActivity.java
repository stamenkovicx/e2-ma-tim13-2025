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
import com.example.myapplication.domain.models.Boss;

import java.util.Random;

public class BossFightActivity extends AppCompatActivity {

    private Boss boss;
    private ProgressBar bossHpBar;
    private TextView bossHpText, attemptsText, successChanceText;
    private Button attackButton;
    private AnimationDrawable bossAnimation;
    private ImageView bossImage;

    private int attemptsLeft = 5;
    private int userPP = 50; // primer, ovo ćeš kasnije vući iz User klase
    private int userSuccessChance = 70; // npr. 70% šanse da pogodi

    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss_fight);

        // inicijalizacija bosa (level 1, nema prethodnog HP-a)
        boss = new Boss(1, 0);

        bossHpBar = findViewById(R.id.bossHpBar);
        bossHpText = findViewById(R.id.bossHpText);
        attemptsText = findViewById(R.id.attemptsText);
        successChanceText = findViewById(R.id.successChanceText);
        attackButton = findViewById(R.id.attackButton);
        bossImage = findViewById(R.id.bossImage);

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


}