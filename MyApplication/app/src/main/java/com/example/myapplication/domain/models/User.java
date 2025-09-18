package com.example.myapplication.domain.models;

import com.example.myapplication.data.database.LevelingSystemHelper;
import com.example.myapplication.data.repository.ItemRepository;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userId;
    private String username;
    private String email;
    private String password;
    private String avatar;
    private int xp;
    private int level;
    private String title;
    private int powerPoints;
    private int coins;
    private List<String> badges;
    private List<UserEquipment> equipment;
    private List<String> friends;
    private List<String> friendRequestsSent;
    private List<String> friendRequestsReceived;
    private String allianceId;
    private List<String> allianceInvitationsSent;
    private List<String> allianceInvitationsReceived;

    public User() {
    }

    public User(String username, String email, String password, String avatar) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.avatar = avatar;
        this.xp = 0;
        this.level = 0;
        this.title = "Beginner";
        this.powerPoints = 0;
        this.coins = 0;
        this.badges = new ArrayList<>();
        this.equipment = new ArrayList<>();
    }

    public User(String username, String email, String password, String avatar, int level, String title, int powerPoints, int xp, int coins) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.avatar = avatar;
        this.level = level;
        this.title = title;
        this.powerPoints = powerPoints;
        this.xp = xp;
        this.coins = coins;
        this.badges = new ArrayList<>();
        this.equipment = new ArrayList<>();
    }

    public User(String userId, String username, String email, String avatar, int xp, int level, String title, int powerPoints, int coins, List<String> friends, List<String> friendRequestsSent, List<String> friendRequestsReceived, String allianceId) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.avatar = avatar;
        this.xp = xp;
        this.level = level;
        this.title = title;
        this.powerPoints = powerPoints;
        this.coins = coins;
        this.badges = new ArrayList<>();
        this.equipment = new ArrayList<>();
        this.friends = friends;
        this.friendRequestsSent = friendRequestsSent;
        this.friendRequestsReceived = friendRequestsReceived;
        this.allianceId = allianceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public List<String> getFriendRequestsSent() {
        return friendRequestsSent;
    }

    public void setFriendRequestsSent(List<String> friendRequestsSent) {
        this.friendRequestsSent = friendRequestsSent;
    }

    public List<String> getFriendRequestsReceived() {
        return friendRequestsReceived;
    }

    public void setFriendRequestsReceived(List<String> friendRequestsReceived) {
        this.friendRequestsReceived = friendRequestsReceived;
    }

    public String getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }

    public int getPowerPoints() {
        return powerPoints;
    }
    public void setPowerPoints(int powerPoints) {
        this.powerPoints = powerPoints;
    }
    @Exclude
    public int getTotalPowerPoints() {
        int totalPower = this.powerPoints;
        if (this.equipment != null) {
            for (UserEquipment item : this.equipment) {
                // Proverite da li je item null
                if (item != null) {
                    Equipment equipmentDetails = ItemRepository.getEquipmentById(item.getEquipmentId());
                    // Dodatna provera da li su detalji opreme null
                    if (equipmentDetails != null && item.isActive()) {
                        totalPower += (int) equipmentDetails.getBonusValue();
                    }
                }
            }
        }
        return totalPower;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public List<String> getBadges() {
        return badges;
    }

    public void setBadges(List<String> badges) {
        this.badges = badges;
    }

    @PropertyName("equipment")
    public List<UserEquipment> getEquipment() {
        return equipment;
    }

    @PropertyName("equipment")
    public void setEquipment(List<UserEquipment> equipment) {
        this.equipment = equipment;
    }

    public void addEquipment(UserEquipment item) {
        if (this.equipment == null) {
            this.equipment = new ArrayList<>();
        }
        this.equipment.add(item);
    }

    public void addXp(int amount) {
        this.xp += amount;
        int requiredXp = LevelingSystemHelper.getRequiredXpForNextLevel(this.level);
        while (this.xp >= requiredXp) {
            this.level++;
            this.title = LevelingSystemHelper.getTitleForLevel(this.level);
            this.powerPoints += LevelingSystemHelper.getPowerPointsRewardForLevel(this.level);
            this.xp -= requiredXp;
            if (this.level > 0) {
                requiredXp = LevelingSystemHelper.getRequiredXpForNextLevel(this.level);
            } else {
                break;
            }
        }
    }

    public void addFriend(String friendId) {
        if (this.friends == null) {
            this.friends = new ArrayList<>();
        }
        this.friends.add(friendId);
    }
    public void removeFriend(String friendId) {
        if (this.friends != null) {
            this.friends.remove(friendId);
        }
    }
    public void addSentRequest(String userId) {
        if (this.friendRequestsSent == null) {
            this.friendRequestsSent = new ArrayList<>();
        }
        this.friendRequestsSent.add(userId);
    }
    public void addReceivedRequest(String userId) {
        if (this.friendRequestsReceived == null) {
            this.friendRequestsReceived = new ArrayList<>();
        }
        this.friendRequestsReceived.add(userId);
    }
    public void removeSentRequest(String userId) {
        if (this.friendRequestsSent != null) {
            this.friendRequestsSent.remove(userId);
        }
    }
    public void removeReceivedRequest(String userId) {
        if (this.friendRequestsReceived != null) {
            this.friendRequestsReceived.remove(userId);
        }
    }

    public List<String> getAllianceInvitationsSent() { return allianceInvitationsSent; }
    public List<String> getAllianceInvitationsReceived() { return allianceInvitationsReceived; }
    public void setAllianceInvitationsSent(List<String> allianceInvitationsSent) { this.allianceInvitationsSent = allianceInvitationsSent; }
    public void setAllianceInvitationsReceived(List<String> allianceInvitationsReceived) { this.allianceInvitationsReceived = allianceInvitationsReceived; }
}