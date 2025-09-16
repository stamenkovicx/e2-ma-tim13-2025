package com.example.myapplication.data.database;

import androidx.annotation.NonNull;

import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserRepositoryFirebaseImpl implements UserRepository {
    private FirebaseFirestore db;

    public UserRepositoryFirebaseImpl() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void sendFriendRequest(String senderUserId, String receiverUserId, OnCompleteListener<Void> onCompleteListener) {
        // Kreiramo reference na dokumente posiljaoca i primaoca
        DocumentReference senderRef = db.collection("users").document(senderUserId);
        DocumentReference receiverRef = db.collection("users").document(receiverUserId);

        db.runTransaction(transaction -> {
            // Dohvatamo dokumente unutar transakcije
            DocumentSnapshot senderSnapshot = transaction.get(senderRef);
            DocumentSnapshot receiverSnapshot = transaction.get(receiverRef);

            if (!senderSnapshot.exists() || !receiverSnapshot.exists()) {
                throw new FirebaseFirestoreException("User not found!",
                        FirebaseFirestoreException.Code.NOT_FOUND);
            }

            User sender = senderSnapshot.toObject(User.class);
            User receiver = receiverSnapshot.toObject(User.class);

            // Provera da li su vec prijatelji ili je zahtjev vec poslat
            if (sender.getFriends() != null && sender.getFriends().contains(receiverUserId) ||
                    sender.getFriendRequestsSent() != null && sender.getFriendRequestsSent().contains(receiverUserId)) {
                return null;
            }

            // Azuriranje liste poslatih zahtjeva za posiljaoca
            if (sender.getFriendRequestsSent() == null) {
                sender.setFriendRequestsSent(new ArrayList<>());
            }
            sender.getFriendRequestsSent().add(receiverUserId);
            transaction.update(senderRef, "friendRequestsSent", sender.getFriendRequestsSent());

            // Azuriranje liste primljenih zahtjeva za primaoca
            if (receiver.getFriendRequestsReceived() == null) {
                receiver.setFriendRequestsReceived(new ArrayList<>());
            }
            receiver.getFriendRequestsReceived().add(senderUserId);
            transaction.update(receiverRef, "friendRequestsReceived", receiver.getFriendRequestsReceived());

            return null;
        }).addOnSuccessListener(aVoid -> {
            onCompleteListener.onSuccess(null);
        }).addOnFailureListener(e -> {
            onCompleteListener.onFailure(e);
        });
    }

    @Override
    public void acceptFriendRequest(String currentUserId, String senderUserId, OnCompleteListener<Void> onCompleteListener) {
        // Kreiramo reference na dokumente trenutnog korisnika (primaoca) i posiljaoca
        DocumentReference currentUserRef = db.collection("users").document(currentUserId);
        DocumentReference senderRef = db.collection("users").document(senderUserId);

        db.runTransaction(transaction -> {
            // Dohvatamo dokumente unutar transakcije
            DocumentSnapshot currentUserSnapshot = transaction.get(currentUserRef);
            DocumentSnapshot senderSnapshot = transaction.get(senderRef);

            if (!currentUserSnapshot.exists() || !senderSnapshot.exists()) {
                throw new FirebaseFirestoreException("User not found!",
                        FirebaseFirestoreException.Code.NOT_FOUND);
            }

            User currentUser = currentUserSnapshot.toObject(User.class);
            User sender = senderSnapshot.toObject(User.class);

            // Provjeravamo da li je zahtjev za prijateljstvo uopste primljen
            if (currentUser.getFriendRequestsReceived() == null || !currentUser.getFriendRequestsReceived().contains(senderUserId)) {
                return null;
            }

            // 1. Uklanjanje zahtjeva iz liste primljenih zahtjeva kod trenutnog korisnika
            currentUser.getFriendRequestsReceived().remove(senderUserId);
            transaction.update(currentUserRef, "friendRequestsReceived", currentUser.getFriendRequestsReceived());

            // 2. Dodavanje ID-a posiljaoca u listu prijatelja kod trenutnog korisnika
            if (currentUser.getFriends() == null) {
                currentUser.setFriends(new ArrayList<>());
            }
            currentUser.getFriends().add(senderUserId);
            transaction.update(currentUserRef, "friends", currentUser.getFriends());

            // 3. Dodavanje ID-a primaoca u listu prijatelja kod posiljaoca
            if (sender.getFriends() == null) {
                sender.setFriends(new ArrayList<>());
            }
            sender.getFriends().add(currentUserId);
            transaction.update(senderRef, "friends", sender.getFriends());

            // 4. Uklanjanje zahtjeva iz liste poslatih zahtjeva kod posiljaoca
            sender.getFriendRequestsSent().remove(currentUserId);
            transaction.update(senderRef, "friendRequestsSent", sender.getFriendRequestsSent());

            return null;
        }).addOnSuccessListener(aVoid -> {
            onCompleteListener.onSuccess(null);
        }).addOnFailureListener(e -> {
            onCompleteListener.onFailure(e);
        });
    }

    @Override
    public void rejectFriendRequest(String currentUserId, String senderUserId, OnCompleteListener<Void> onCompleteListener) {
        // Kreiramo reference na dokumente trenutnog korisnika (primaoca) i posiljaoca
        DocumentReference currentUserRef = db.collection("users").document(currentUserId);
        DocumentReference senderRef = db.collection("users").document(senderUserId);

        db.runTransaction(transaction -> {
            // Dohvatamo dokumente unutar transakcije
            DocumentSnapshot currentUserSnapshot = transaction.get(currentUserRef);
            DocumentSnapshot senderSnapshot = transaction.get(senderRef);

            if (!currentUserSnapshot.exists() || !senderSnapshot.exists()) {
                throw new FirebaseFirestoreException("User not found!",
                        FirebaseFirestoreException.Code.NOT_FOUND);
            }

            User currentUser = currentUserSnapshot.toObject(User.class);
            User sender = senderSnapshot.toObject(User.class);

            // Provjeravamo da li je zahtjev za prijateljstvo uopste primljen
            if (currentUser.getFriendRequestsReceived() == null || !currentUser.getFriendRequestsReceived().contains(senderUserId)) {
                return null;
            }

            // 1. Uklanjamo zahtjev iz liste primljenih zahtjeva kod trenutnog korisnika
            currentUser.getFriendRequestsReceived().remove(senderUserId);
            transaction.update(currentUserRef, "friendRequestsReceived", currentUser.getFriendRequestsReceived());

            // 2. Uklanjamo zahtjev iz liste poslatih zahtjeva kod posiljaoca
            sender.getFriendRequestsSent().remove(currentUserId);
            transaction.update(senderRef, "friendRequestsSent", sender.getFriendRequestsSent());

            return null;
        }).addOnSuccessListener(aVoid -> {
            onCompleteListener.onSuccess(null);
        }).addOnFailureListener(e -> {
            onCompleteListener.onFailure(e);
        });
    }

    @Override
    public void searchUsers(String username, UserRepository.OnCompleteListener<List<User>> onCompleteListener) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> foundUsers = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            foundUsers.add(user);
                        }
                        onCompleteListener.onSuccess(foundUsers);
                    } else {
                        onCompleteListener.onFailure(task.getException());
                    }
                });
    }

    @Override
    public void getUserById(String userId, OnCompleteListener<User> onCompleteListener) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = task.getResult().toObject(User.class);
                        onCompleteListener.onSuccess(user);
                    } else {
                        onCompleteListener.onFailure(task.getException());
                    }
                });
    }
}