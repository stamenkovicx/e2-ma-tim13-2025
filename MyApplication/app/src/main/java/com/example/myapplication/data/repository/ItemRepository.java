package com.example.myapplication.data.repository;

import com.example.myapplication.domain.models.Badge;
import com.example.myapplication.domain.models.Equipment;
import com.example.myapplication.domain.models.EquipmentType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ItemRepository {

    private static final List<Badge> ALL_BADGES;
    private static final List<Equipment> ALL_EQUIPMENT;

    static {
        List<Badge> allBadges = new ArrayList<>();
        allBadges.add(new Badge("First Login", "Awarded for logging in for the first time.", "badge_first_login"));
        allBadges.add(new Badge("First Mission", "Awarded for completing your first mission.", "badge_first_mission"));
        allBadges.add(new Badge("Special Mission I", "Awarded for completing a special mission.", "badge_special_mission_1"));
        // Add all other badges here if needed
        ALL_BADGES = Collections.unmodifiableList(allBadges);

        List<Equipment> allEquipment = new ArrayList<>();
        // POTIONS (Napici)
        // Napitak za jednokratnu snagu (+20% PP)
        allEquipment.add(new Equipment(1,"Minor Potion of Power", "A one-time potion that increases power by 20%.", "potion_power_minor", EquipmentType.POTION, 0.20, 1));
        // Napitak za jednokratnu snagu (+40% PP)
        allEquipment.add(new Equipment(2,"Greater Potion of Power", "A one-time potion that increases power by 40%.", "potion_power_greater", EquipmentType.POTION, 0.40, 1));
        // Napitak za trajno povecanje snage (+5% PP)
        allEquipment.add(new Equipment(3,"Elixir of Might", "A potion that permanently increases power by 5%.", "elixir_might", EquipmentType.POTION, 0.05, 0));
        // Napitak za trajno povecanje snage (+10% PP)
        allEquipment.add(new Equipment(4,"Elixir of Greatness", "A potion that permanently increases power by 10%.", "elixir_greatness", EquipmentType.POTION, 0.10, 0));

        // CLOTHING (Odjeca)
        // Rukavice (+10% povecanje snage)
        allEquipment.add(new Equipment(5,"Gloves of Strength", "Gloves that increase power by 10% for two battles.", "clothing_gloves", EquipmentType.CLOTHING, 0.10, 2));
        // Stit (+10% povecanje sanse uspjesnog napada)
        allEquipment.add(new Equipment(6,"Shield of Fortune", "A shield that increases successful attack chance by 10% for two battles.", "clothing_shield", EquipmentType.CLOTHING, 0.10, 2));
        // Cizme (+40% povecanja broja napada)
        allEquipment.add(new Equipment(7,"Boots of Swiftness", "Boots that give a 40% chance for an extra attack.", "clothing_boots", EquipmentType.CLOTHING, 0.40, 2));

        // WEAPONS (Oruzje)
        // Mac (+5% trajno povecanje snage)
        allEquipment.add(new Equipment(8,"Iron Sword", "A sturdy sword that permanently increases power by 5%.", "weapon_sword", EquipmentType.WEAPON, 0.05, 0));
        // Luk i strijela (+5% stalno povecanje dobijenog novca)
        allEquipment.add(new Equipment(9,"Bow and Arrow", "Increases gained coins by 5%.", "weapon_bow", EquipmentType.WEAPON, 0.05, 0));

        //  DODAVANJE BEDŽEVA
        allEquipment.add(new Equipment(201, "Bronzani bedž misije", "Za učešće u specijalnoj misiji.", "bronze_badge", EquipmentType.BADGE, 0, 0));
        allEquipment.add(new Equipment(202, "Srebrni bedž misije", "Za značajan doprinos u specijalnoj misiji.", "silver_badge", EquipmentType.BADGE, 0, 0));
        allEquipment.add(new Equipment(203, "Zlatni bedž misije", "Za izvanredan doprinos u specijalnoj misiji.", "medal", EquipmentType.BADGE, 0, 0));

        ALL_EQUIPMENT = Collections.unmodifiableList(allEquipment);
    }
    public static List<Badge> getAllBadges() {
        return ALL_BADGES;
    }
    public static List<Equipment> getAllEquipment() {
        return ALL_EQUIPMENT;
    }
    public static Badge getBadgeByResourceId(String resourceId) {
        for (Badge badge : ALL_BADGES) {
            if (badge.getIconResourceId().equals(resourceId)) {
                return badge;
            }
        }
        return null;
    }
    public static Equipment getEquipmentByResourceId(String resourceId) {
        for (Equipment equipment : ALL_EQUIPMENT) {
            if (equipment.getIconResourceId().equals(resourceId)) {
                return equipment;
            }
        }
        return null;
    }

    public static Equipment getEquipmentById(int id) {
        for (Equipment equipment : getAllEquipment()) {
            if (equipment.getId() == id) {
                return equipment;
            }
        }
        return null;
    }
    // METODE ZA NAGRADE

    public static Equipment getRandomClothing() {
        List<Equipment> clothingItems = new ArrayList<>();
        for (Equipment item : ALL_EQUIPMENT) {
            if (item.getType() == EquipmentType.CLOTHING) {
                clothingItems.add(item);
            }
        }
        if (clothingItems.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return clothingItems.get(random.nextInt(clothingItems.size()));
    }

    public static Equipment getRandomWeapon() {
        List<Equipment> weaponItems = new ArrayList<>();
        for (Equipment item : ALL_EQUIPMENT) {
            if (item.getType() == EquipmentType.WEAPON) {
                weaponItems.add(item);
            }
        }
        if (weaponItems.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return weaponItems.get(random.nextInt(weaponItems.size()));
    }
    public static Equipment getRandomPotion() {
        List<Equipment> potionItems = new ArrayList<>();
        for (Equipment item : ALL_EQUIPMENT) {
            if (item.getType() == EquipmentType.POTION) {
                potionItems.add(item);
            }
        }
        if (potionItems.isEmpty()) {
            return null; // Vraća null ako nema definisanih napitaka
        }
        Random random = new Random();
        return potionItems.get(random.nextInt(potionItems.size()));
    }
    public static Equipment getBadgeForMissionCount(int missionCount) {
        // Vraća bedž na osnovu ukupnog broja završenih misija
        if (missionCount == 1) {
            return getEquipmentById(201); // Bronzani bedž za prvu završenu misiju
        } else if (missionCount == 2) {
            return getEquipmentById(202); // Srebrni bedž za drugu
        } else { // Za 3 ili više misija
            return getEquipmentById(203); // Zlatni bedž
        }
    }
}