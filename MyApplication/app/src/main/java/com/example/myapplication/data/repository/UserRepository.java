package com.example.myapplication.data.repository;

import com.example.myapplication.domain.models.Alliance;
import com.example.myapplication.domain.models.User;
import com.example.myapplication.domain.models.Notification;

import java.util.List;

public interface UserRepository {
    void sendFriendRequest(String senderUserId, String receiverUserId, OnCompleteListener<Void> onCompleteListener);
    void acceptFriendRequest(String currentUserId, String senderUserId, OnCompleteListener<Void> onCompleteListener);
    void rejectFriendRequest(String currentUserId, String senderUserId, OnCompleteListener<Void> onCompleteListener);
    // Metoda za pretragu korisnika po korisnickom imenu
    void searchUsers(String username, OnCompleteListener<List<User>> onCompleteListener);
    void getUsersByIds(List<String> userIds, OnCompleteListener<List<User>> onCompleteListener);

    // Pomocni interfejs za asinhronu komunikaciju
    interface OnCompleteListener<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
    void getUserById(String userId, OnCompleteListener<User> onCompleteListener);
    void updateUser(User user, OnCompleteListener<Void> onCompleteListener);
    void createAlliance(Alliance alliance, OnCompleteListener<String> listener);
    void updateUserAllianceId(String userId, String allianceId, OnCompleteListener<Void> listener);
    void sendAllianceInvitation(String allianceId, String invitedUserId, OnCompleteListener<Void> listener);
    void getAlliancesByIds(List<String> allianceIds, OnCompleteListener<List<Alliance>> onCompleteListener);
    void acceptAllianceInvitation(String currentUserId, String allianceId, OnCompleteListener<Void> listener);
    void rejectAllianceInvitation(String currentUserId, String allianceId, OnCompleteListener<Void> listener);
    public void switchAlliance(String userId, String oldAllianceId, String newAllianceId, OnCompleteListener<Void> listener);
    public void getAllianceById(String allianceId, OnCompleteListener<Alliance> listener);
    public void leaveAlliance(String userId, String allianceId, OnCompleteListener<Void> listener);
    public void disbandAlliance(String allianceId, OnCompleteListener<Void> listener);
    public void addNotification(Notification notification, OnCompleteListener<Void> listener);
    public void getUnreadNotificationsCount(String userId, OnCompleteListener<Integer> listener);
    public void getAllNotifications(String userId, OnCompleteListener<List<Notification>> listener);
    public void markNotificationAsRead(String notificationId, OnCompleteListener<Void> listener);
    void startSpecialMission(String allianceId, OnCompleteListener<Void> listener);

}