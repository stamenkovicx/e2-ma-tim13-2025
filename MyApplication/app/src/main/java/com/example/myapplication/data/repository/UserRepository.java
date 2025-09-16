package com.example.myapplication.data.repository;

import com.example.myapplication.domain.models.User;

import java.util.List;

public interface UserRepository {
    void sendFriendRequest(String senderUserId, String receiverUserId, OnCompleteListener<Void> onCompleteListener);
    void acceptFriendRequest(String currentUserId, String senderUserId, OnCompleteListener<Void> onCompleteListener);
    void rejectFriendRequest(String currentUserId, String senderUserId, OnCompleteListener<Void> onCompleteListener);
    // Metoda za pretragu korisnika po korisnickom imenu
    void searchUsers(String username, OnCompleteListener<List<User>> onCompleteListener);

    // Pomocni interfejs za asinhronu komunikaciju
    interface OnCompleteListener<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
    void getUserById(String userId, OnCompleteListener<User> onCompleteListener);
}