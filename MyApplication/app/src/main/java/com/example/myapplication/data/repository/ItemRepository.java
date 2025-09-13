package com.example.myapplication.data.repository;

import com.example.myapplication.domain.models.Badge;
import com.example.myapplication.domain.models.Equipment;
import com.example.myapplication.domain.models.EquipmentType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        allEquipment.add(new Equipment("Minor Potion of Power", "A one-time potion that increases power by 20%.", "potion_power_minor", EquipmentType.POTION, 0.20));
        // Napitak za jednokratnu snagu (+40% PP)
        allEquipment.add(new Equipment("Greater Potion of Power", "A one-time potion that increases power by 40%.", "potion_power_greater", EquipmentType.POTION, 0.40));
        // Napitak za trajno povecanje snage (+5% PP)
        allEquipment.add(new Equipment("Elixir of Might", "A potion that permanently increases power by 5%.", "elixir_might", EquipmentType.POTION, 0.05));
        // Napitak za trajno povecanje snage (+10% PP)
        allEquipment.add(new Equipment("Elixir of Greatness", "A potion that permanently increases power by 10%.", "elixir_greatness", EquipmentType.POTION, 0.10));

        // CLOTHING (Odjeca)
        // Rukavice (+10% povecanje snage)
        allEquipment.add(new Equipment("Gloves of Strength", "Gloves that increase power by 10% for two battles.", "clothing_gloves", EquipmentType.CLOTHING, 0.10));
        // Stit (+10% povecanje sanse uspjesnog napada)
        allEquipment.add(new Equipment("Shield of Fortune", "A shield that increases successful attack chance by 10% for two battles.", "clothing_shield", EquipmentType.CLOTHING, 0.10));
        // Cizme (+40% povecanja broja napada)
        allEquipment.add(new Equipment("Boots of Swiftness", "Boots that give a 40% chance for an extra attack.", "clothing_boots", EquipmentType.CLOTHING, 0.40));

        // WEAPONS (Oruzje)
        // Mac (+5% trajno povecanje snage)
        allEquipment.add(new Equipment("Iron Sword", "A sturdy sword that permanently increases power by 5%.", "weapon_sword", EquipmentType.WEAPON, 0.05));
        // Luk i strijela (+5% stalno povecanje dobijenog novca)
        allEquipment.add(new Equipment("Bow and Arrow", "Increases gained coins by 5%.", "weapon_bow", EquipmentType.WEAPON, 0.05));

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
}