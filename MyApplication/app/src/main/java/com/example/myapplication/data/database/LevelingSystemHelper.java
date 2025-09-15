package com.example.myapplication.data.database;

import com.example.myapplication.domain.models.DifficultyType;
import com.example.myapplication.domain.models.ImportanceType;
import com.example.myapplication.domain.models.User;
import com.example.myapplication.domain.models.Equipment;
import com.example.myapplication.domain.models.UserEquipment;
import java.util.List;
import java.util.stream.Collectors;

public class LevelingSystemHelper {

    private static final int INITIAL_XP_REQUIRED = 200;
    private static final int INITIAL_PP_REWARD = 40;


     // Vraca potrebnu kolicinu XP-a za dostizanje sljedeceg nivoa
    public static int getRequiredXpForNextLevel(int currentLevel) {
        if (currentLevel == 0) {
            return INITIAL_XP_REQUIRED;
        } else {
            // Formula: XP_prethodnog_nivoa * 2 + XP_prethodnog_nivoa / 2
            // Proracun se zaokruzuje na prvu narednu stotinu
            int prevLevelRequiredXp = getRequiredXpForNextLevel(currentLevel - 1);
            double requiredXp = prevLevelRequiredXp * 2.0 + (double) prevLevelRequiredXp / 2.0;
            return (int) (Math.ceil(requiredXp / 100.0) * 100);
        }
    }

     // Vraca broj Power Points-a koje korisnik dobija po prelasku na novi nivo
    public static int getPowerPointsRewardForLevel(int currentLevel) {
        if (currentLevel == 1) {
            return INITIAL_PP_REWARD;
        } else if (currentLevel > 1) {
            // Formula: PP_prethodnog_nivoa + 3/4 * PP_prethodnog_nivoa
            double prevLevelPPReward = getPowerPointsRewardForLevel(currentLevel - 1);
            double currentLevelPPReward = prevLevelPPReward + 0.75 * prevLevelPPReward;
            return (int) Math.round(currentLevelPPReward);
        }
        return 0;
    }


     // Vraca titulu za dati nivo. Titule su proizvoljno dodijeljene za prva tri nivoa.
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

     // Izracunava bonus XP za bitnost na osnovu nivoa korisnika.
    public static int getXpForImportance(int baseXpForImportance, int userLevel) {
        if (userLevel <= 1) {
            return baseXpForImportance;
        }

        // Formula: XP = XP_prethodni + XP_prethodni / 2
        int calculatedXp = baseXpForImportance;
        for (int i = 2; i <= userLevel; i++) {
            calculatedXp = calculatedXp + calculatedXp / 2;
        }
        return calculatedXp;
    }

    /**
     * Izračunava bonus XP za težinu na osnovu nivoa korisnika.
     * Formula: XP težine za prethodni nivo + XP težine za prethodni nivo / 2
     * @param baseXpForDifficulty Osnovni XP za težinu zadatka
     * @param userLevel Trenutni nivo korisnika
     * @return Ažurirani XP za težinu
     */
    public static int getXpForDifficulty(int baseXpForDifficulty, int userLevel) {
        if (userLevel <= 1) {
            return baseXpForDifficulty;
        }

        // Formula: XP = XP_prethodni + XP_prethodni / 2
        int calculatedXp = baseXpForDifficulty;
        for (int i = 2; i <= userLevel; i++) {
            calculatedXp = calculatedXp + calculatedXp / 2;
        }
        return calculatedXp;
    }
}