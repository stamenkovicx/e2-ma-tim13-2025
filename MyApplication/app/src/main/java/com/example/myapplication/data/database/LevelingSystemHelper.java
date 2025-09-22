package com.example.myapplication.data.database;

import com.example.myapplication.domain.models.DifficultyType;
import com.example.myapplication.domain.models.ImportanceType;

public class LevelingSystemHelper {

    private static final int INITIAL_XP_REQUIRED = 200;
    private static final int INITIAL_PP_REWARD = 40;

    // Vraca potrebnu kolicinu XP-a za dostizanje sljedeceg nivoa
    public static int getRequiredXpForNextLevel(int currentLevel) {
        if (currentLevel == 0) {
            return INITIAL_XP_REQUIRED;
        }

        int requiredXp = INITIAL_XP_REQUIRED;
        for (int i = 1; i < currentLevel; i++) {
            requiredXp = (int) (requiredXp * 2.0 + (double) requiredXp / 2.0);
        }

        double requiredXpDouble = requiredXp * 2.0 + (double) requiredXp / 2.0;
        return (int) (Math.ceil(requiredXpDouble / 100.0) * 100);
    }

    // Vraca broj Power Points-a koje korisnik dobija po prelasku na novi nivo
    public static int getPowerPointsRewardForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level == 1) {
            return INITIAL_PP_REWARD;
        }

        int ppReward = INITIAL_PP_REWARD;
        for (int i = 2; i <= level; i++) {
            ppReward = (int) Math.round(ppReward + 0.75 * ppReward);
        }
        return ppReward;
    }

    // Vraca titulu za dati nivo
    public static String getTitleForLevel(int level) {
        switch (level) {
            case 0:
                return "Beginner";
            case 1:
                return "Apprentice";
            case 2:
                return "Wanderer";
            case 3:
                return "Adventurer";
            default:
                return "Hero of the Realm";
        }
    }

    // Izracunava bonus XP za bitnost na osnovu nivoa korisnika
    public static int getXpForImportance(int baseXpForImportance, int userLevel) {
        if (userLevel < 1) {
            return baseXpForImportance;
        }
        int calculatedXp = baseXpForImportance;
        for (int i = 1; i <= userLevel; i++) {
            // ISPRAVLJENA LINIJA: Koristimo množenje sa 1.5 i Math.round za pravilno zaokruživanje.
            calculatedXp = (int) Math.round(calculatedXp * 1.5);
        }
        return calculatedXp;
    }

    // Izracunava bonus XP za tezinu na osnovu nivoa korisnika
    public static int getXpForDifficulty(int baseXpForDifficulty, int userLevel) {
        return baseXpForDifficulty;
    }
    // Izracunava konacnu XP vrijednost na osnovu osnovnog XP-a i nivoa korisnika
    public static int calculateFinalXp(ImportanceType importance, DifficultyType difficulty, int userLevel) {

        // Osnovne vrijednosti XP-a za bitnost, prije nego sto se primijeni bonus
        int baseXpForImportance;
        switch (importance) {
            case NORMAL:
                baseXpForImportance = 1;
                break;
            case IMPORTANT:
                baseXpForImportance = 3;
                break;
            case EXTREMELY_IMPORTANT:
                baseXpForImportance = 10;
                break;
            case SPECIAL:
                baseXpForImportance = 100;
                break;
            default:
                baseXpForImportance = 0;
        }

        // Osnovne vrijednosti XP-a za tezinu, pijre nego sto se primijeni bonus
        int baseXpForDifficulty;
        switch (difficulty) {
            case VERY_EASY:
                baseXpForDifficulty = 1;
                break;
            case EASY:
                baseXpForDifficulty = 3;
                break;
            case HARD:
                baseXpForDifficulty = 7;
                break;
            case EXTREMELY_HARD:
                baseXpForDifficulty = 20;
                break;
            default:
                baseXpForDifficulty = 0;
        }

        // Racunanje finalne vrijednosti XP-a za bitnost
        int finalXpImportance = getXpForImportance(baseXpForImportance, userLevel);

        // Racunanje finalne vrijednosti XP-a za tezinu
        int finalXpDifficulty = getXpForDifficulty(baseXpForDifficulty, userLevel);

        // Konacan XP - zbir finalnih vrijednosti za bitnost i tezinu
        return finalXpImportance + finalXpDifficulty;
    }
    // Odredi nivo na osnovu ukupnog XP-a (bez resetovanja XP-a)
    public static int calculateLevelFromXp(int totalXp) {
        int level = 0;
        int requiredXpForNext = getRequiredXpForNextLevel(level);

        while (totalXp >= requiredXpForNext) {
            level++;
            requiredXpForNext = getRequiredXpForNextLevel(level);
        }

        return level;
    }
}