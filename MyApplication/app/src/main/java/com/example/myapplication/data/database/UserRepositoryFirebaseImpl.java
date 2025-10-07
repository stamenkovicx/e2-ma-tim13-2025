package com.example.myapplication.data.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.Alliance;
import com.example.myapplication.domain.models.MissionStats;
import com.example.myapplication.domain.models.Notification;
import com.example.myapplication.domain.models.SpecialMissionProgress;
import com.example.myapplication.domain.models.TaskStatus;
import com.example.myapplication.domain.models.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepositoryFirebaseImpl implements UserRepository {
    private FirebaseFirestore db;
    private CollectionReference usersCollection;
    private final CollectionReference allianceCollection;
    private final CollectionReference missionProgressCollection;

    public UserRepositoryFirebaseImpl() {
        this.db = FirebaseFirestore.getInstance();
        this.usersCollection = db.collection("users");
        this.allianceCollection = db.collection("alliances");
        this.missionProgressCollection = db.collection("specialMissionProgress");
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
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .whereGreaterThanOrEqualTo("username", username)
                .whereLessThanOrEqualTo("username", username + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> foundUsers = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            // Isključujemo trenutno ulogovanog korisnika iz rezultata pretrage
                            if (!user.getUserId().equals(currentUserId)) {
                                foundUsers.add(user);
                            }
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

    @Override
    public void updateUser(User user, OnCompleteListener<Void> onCompleteListener) {
        if (user == null || user.getUserId() == null || user.getUserId().isEmpty()) {
            onCompleteListener.onFailure(new IllegalArgumentException("User or User ID is invalid."));
            return;
        }

        DocumentReference userRef = usersCollection.document(user.getUserId());

        Map<String, Object> updates = new HashMap<>();
        updates.put("coins", user.getCoins());
        updates.put("equipment", user.getEquipment());
        updates.put("xp", user.getXp());
        updates.put("level", user.getLevel());
        updates.put("powerPoints", user.getPowerPoints());
        updates.put("dateOfLastLevelUp", user.getDateOfLastLevelUp());
        updates.put("specialMissionsCompleted", user.getSpecialMissionsCompleted());
        updates.put("specialMissionsFailed", user.getSpecialMissionsFailed());

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> onCompleteListener.onSuccess(null))
                .addOnFailureListener(e -> onCompleteListener.onFailure(e));
    }

    @Override
    public void getUsersByIds(List<String> userIds, UserRepository.OnCompleteListener<List<User>> onCompleteListener) {
        List<User> friends = new ArrayList<>();
        if (userIds == null || userIds.isEmpty()) {
            onCompleteListener.onSuccess(friends);
            return;
        }

        // Koristi se lista zadataka da se prati kada su svi dohvaćeni
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String userId : userIds) {
            tasks.add(db.collection("users").document(userId).get());
        }

        // Pomocu Task.whenAllSuccess() cekamo da se svi zadaci zavrse
        Task<List<DocumentSnapshot>> allTasks = com.google.android.gms.tasks.Tasks.whenAllSuccess(tasks);

        allTasks.addOnSuccessListener(documentSnapshots -> {
            List<User> friendUsers = new ArrayList<>();
            for (DocumentSnapshot doc : documentSnapshots) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    friendUsers.add(user);
                }
            }
            onCompleteListener.onSuccess(friendUsers);
        }).addOnFailureListener(onCompleteListener::onFailure);
    }

    @Override
    public void createAlliance(Alliance alliance, OnCompleteListener<String> listener) {
        CollectionReference alliancesRef = db.collection("alliances");
        alliancesRef.add(alliance)
                .addOnSuccessListener(documentReference -> {
                    String allianceId = documentReference.getId();
                    documentReference.update("allianceId", allianceId)
                            .addOnSuccessListener(aVoid -> {
                                // Pozivamo listener.onSuccess TEK nakon što je dokument uspešno ažuriran
                                listener.onSuccess(allianceId);
                            })
                            .addOnFailureListener(e -> {
                                listener.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    listener.onFailure(e);
                });
    }

    @Override
    public void updateUserAllianceId(String userId, String allianceId, OnCompleteListener<Void> listener) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.update("allianceId", allianceId)
                .addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(e -> listener.onFailure(e));
    }

    @Override
    public void sendAllianceInvitation(String allianceId, String invitedUserId, OnCompleteListener<Void> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference allianceRef = db.collection("alliances").document(allianceId);
        DocumentReference invitedUserRef = db.collection("users").document(invitedUserId);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Ovo je u redu da ostane van transakcije
        DocumentReference currentUserRef = db.collection("users").document(currentUserId);

        db.runTransaction(transaction -> {
            DocumentSnapshot allianceSnapshot = transaction.get(allianceRef);
            if (!allianceSnapshot.exists()) {
                throw new FirebaseFirestoreException("Alliance not found!", FirebaseFirestoreException.Code.NOT_FOUND);
            }

            DocumentSnapshot invitedUserSnapshot = transaction.get(invitedUserRef);
            if (!invitedUserSnapshot.exists()) {
                throw new FirebaseFirestoreException("Invited user not found!", FirebaseFirestoreException.Code.NOT_FOUND);
            }

            DocumentSnapshot currentUserSnapshot = transaction.get(currentUserRef);
            if (!currentUserSnapshot.exists()) {
                throw new FirebaseFirestoreException("Current user not found!", FirebaseFirestoreException.Code.NOT_FOUND);
            }

            List<String> pendingInvitations = (List<String>) allianceSnapshot.get("pendingInvitations");
            if (pendingInvitations == null) {
                pendingInvitations = new ArrayList<>();
            }
            if (!pendingInvitations.contains(invitedUserId)) {
                pendingInvitations.add(invitedUserId);
                transaction.update(allianceRef, "pendingInvitations", pendingInvitations);
            }

            List<String> allianceInvitationsReceived = (List<String>) invitedUserSnapshot.get("allianceInvitationsReceived");
            if (allianceInvitationsReceived == null) {
                allianceInvitationsReceived = new ArrayList<>();
            }
            if (!allianceInvitationsReceived.contains(allianceId)) {
                allianceInvitationsReceived.add(allianceId);
                transaction.update(invitedUserRef, "allianceInvitationsReceived", allianceInvitationsReceived);
            }

            List<String> allianceInvitationsSent = (List<String>) currentUserSnapshot.get("allianceInvitationsSent");
            if (allianceInvitationsSent == null) {
                allianceInvitationsSent = new ArrayList<>();
            }
            if (!allianceInvitationsSent.contains(invitedUserId)) {
                allianceInvitationsSent.add(invitedUserId);
                transaction.update(currentUserRef, "allianceInvitationsSent", allianceInvitationsSent);
            }

            return null;
        }).addOnSuccessListener(aVoid -> {
            listener.onSuccess(null);
        }).addOnFailureListener(e -> {
            listener.onFailure(e);
        });
    }

    @Override
    public void getAlliancesByIds(List<String> allianceIds, OnCompleteListener<List<Alliance>> onCompleteListener) {
        if (allianceIds == null || allianceIds.isEmpty()) {
            onCompleteListener.onSuccess(new ArrayList<>());
            return;
        }

        List<com.google.android.gms.tasks.Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String allianceId : allianceIds) {
            tasks.add(db.collection("alliances").document(allianceId).get());
        }

        com.google.android.gms.tasks.Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(documentSnapshots -> {
                    List<Alliance> alliances = new ArrayList<>();
                    for (Object snapshot : documentSnapshots) {
                        if (snapshot instanceof DocumentSnapshot) {
                            Alliance alliance = ((DocumentSnapshot) snapshot).toObject(Alliance.class);
                            if (alliance != null) {
                                alliances.add(alliance);
                            }
                        }
                    }
                    onCompleteListener.onSuccess(alliances);
                })
                .addOnFailureListener(e -> {
                    onCompleteListener.onFailure(e);
                });
    }

    @Override
    public void acceptAllianceInvitation(String currentUserId, String allianceId, OnCompleteListener<Void> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference currentUserRef = db.collection("users").document(currentUserId);
        DocumentReference allianceRef = db.collection("alliances").document(allianceId);

        db.runTransaction(transaction -> {
                    // Dohvati podatke korisnika i saveza unutar transakcije
                    DocumentSnapshot currentUserSnapshot = transaction.get(currentUserRef);
                    DocumentSnapshot allianceSnapshot = transaction.get(allianceRef);

                    if (!currentUserSnapshot.exists() || !allianceSnapshot.exists()) {
                        throw new FirebaseFirestoreException("Document not found!", FirebaseFirestoreException.Code.NOT_FOUND);
                    }

                    // Dohvati ID vođe saveza i reference na vođu unutar transakcije
                    String leaderId = (String) allianceSnapshot.get("leaderId");
                    if (leaderId == null) {
                        throw new FirebaseFirestoreException("Alliance leader not found!", FirebaseFirestoreException.Code.NOT_FOUND);
                    }
                    DocumentReference leaderRef = db.collection("users").document(leaderId);
                    DocumentSnapshot leaderSnapshot = transaction.get(leaderRef);

                    if (!leaderSnapshot.exists()) {
                        throw new FirebaseFirestoreException("Leader user not found!", FirebaseFirestoreException.Code.NOT_FOUND);
                    }

                    // 1. Ažuriraj trenutnog korisnika (koji prihvata poziv)
                    List<String> receivedInvitations = (List<String>) currentUserSnapshot.get("allianceInvitationsReceived");
                    if (receivedInvitations != null) {
                        receivedInvitations.remove(allianceId); // Uklanjamo ID saveza
                        transaction.update(currentUserRef, "allianceInvitationsReceived", receivedInvitations);
                    }
                    transaction.update(currentUserRef, "allianceId", allianceId);

                    // 2. Ažuriraj savez
                    List<String> memberIds = (List<String>) allianceSnapshot.get("memberIds");
                    List<String> pendingInvitations = (List<String>) allianceSnapshot.get("pendingInvitations");

                    if (memberIds == null) memberIds = new ArrayList<>();
                    if (pendingInvitations == null) pendingInvitations = new ArrayList<>();

                    if (!memberIds.contains(currentUserId)) {
                        memberIds.add(currentUserId);
                        transaction.update(allianceRef, "memberIds", memberIds);
                    }

                    pendingInvitations.remove(currentUserId);
                    transaction.update(allianceRef, "pendingInvitations", pendingInvitations);

                    // 3. Ažuriraj vođu saveza
                    List<String> allianceInvitationsSent = (List<String>) leaderSnapshot.get("allianceInvitationsSent");
                    if (allianceInvitationsSent != null) {
                        allianceInvitationsSent.remove(currentUserId); // Uklanjamo ID korisnika
                        transaction.update(leaderRef, "allianceInvitationsSent", allianceInvitationsSent);
                    }

                    return null;
                }).addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }

    @Override
    public void rejectAllianceInvitation(String currentUserId, String allianceId, OnCompleteListener<Void> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference currentUserRef = db.collection("users").document(currentUserId);
        DocumentReference allianceRef = db.collection("alliances").document(allianceId);

        db.runTransaction(transaction -> {
                    // Dohvati podatke korisnika i saveza unutar transakcije
                    DocumentSnapshot currentUserSnapshot = transaction.get(currentUserRef);
                    DocumentSnapshot allianceSnapshot = transaction.get(allianceRef);

                    if (!currentUserSnapshot.exists() || !allianceSnapshot.exists()) {
                        throw new FirebaseFirestoreException("Document not found!", FirebaseFirestoreException.Code.NOT_FOUND);
                    }

                    // Dohvati ID vođe saveza i reference na vođu unutar transakcije
                    String leaderId = (String) allianceSnapshot.get("leaderId");
                    if (leaderId == null) {
                        throw new FirebaseFirestoreException("Alliance leader not found!", FirebaseFirestoreException.Code.NOT_FOUND);
                    }
                    DocumentReference leaderRef = db.collection("users").document(leaderId);
                    DocumentSnapshot leaderSnapshot = transaction.get(leaderRef);

                    if (!leaderSnapshot.exists()) {
                        throw new FirebaseFirestoreException("Leader user not found!", FirebaseFirestoreException.Code.NOT_FOUND);
                    }

                    // 1. Ukloni poziv iz korisničkog profila
                    List<String> receivedInvitations = (List<String>) currentUserSnapshot.get("allianceInvitationsReceived");
                    if (receivedInvitations != null) {
                        receivedInvitations.remove(allianceId); // Uklanjamo ID saveza
                        transaction.update(currentUserRef, "allianceInvitationsReceived", receivedInvitations);
                    }

                    // 2. Ukloni poziv iz saveza
                    List<String> pendingInvitations = (List<String>) allianceSnapshot.get("pendingInvitations");
                    if (pendingInvitations != null) {
                        pendingInvitations.remove(currentUserId);
                        transaction.update(allianceRef, "pendingInvitations", pendingInvitations);
                    }

                    // 3. Ukloni poziv iz sent liste vođe saveza
                    List<String> allianceInvitationsSent = (List<String>) leaderSnapshot.get("allianceInvitationsSent");
                    if (allianceInvitationsSent != null) {
                        allianceInvitationsSent.remove(currentUserId); // Uklanjamo ID korisnika
                        transaction.update(leaderRef, "allianceInvitationsSent", allianceInvitationsSent);
                    }

                    return null;
                }).addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }

    @Override
    public void switchAlliance(String userId, String oldAllianceId, String newAllianceId, OnCompleteListener<Void> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);
        DocumentReference oldAllianceRef = db.collection("alliances").document(oldAllianceId);
        DocumentReference newAllianceRef = db.collection("alliances").document(newAllianceId);

        // Dohvati podatke pre transakcije, tako da su reference unutar transakcije ispravne
        db.runTransaction(transaction -> {
                    DocumentSnapshot userSnapshot = transaction.get(userRef);
                    DocumentSnapshot oldAllianceSnapshot = transaction.get(oldAllianceRef);
                    DocumentSnapshot newAllianceSnapshot = transaction.get(newAllianceRef);

                    // Dodatni korak: Dohvati i lidera saveza ovde, na početku transakcije
                    String newAllianceLeaderId = (String) newAllianceSnapshot.get("leaderId");
                    DocumentSnapshot newAllianceLeaderSnapshot = null;
                    if (newAllianceLeaderId != null) {
                        DocumentReference newAllianceLeaderRef = db.collection("users").document(newAllianceLeaderId);
                        newAllianceLeaderSnapshot = transaction.get(newAllianceLeaderRef);
                    }

                    if (!userSnapshot.exists() || !oldAllianceSnapshot.exists() || !newAllianceSnapshot.exists()) {
                        throw new FirebaseFirestoreException("Document not found!", FirebaseFirestoreException.Code.NOT_FOUND);
                    }

                    // 1. Ažuriraj korisnika
                    List<String> receivedInvitations = (List<String>) userSnapshot.get("allianceInvitationsReceived");
                    if (receivedInvitations != null) {
                        receivedInvitations.remove(newAllianceId);
                        transaction.update(userRef, "allianceInvitationsReceived", receivedInvitations);
                    }
                    transaction.update(userRef, "allianceId", newAllianceId);

                    // 2. Ažuriraj stari savez
                    List<String> oldMembers = (List<String>) oldAllianceSnapshot.get("memberIds");
                    if (oldMembers != null) {
                        oldMembers.remove(userId);
                        transaction.update(oldAllianceRef, "memberIds", oldMembers);
                    }

                    // 3. Ažuriraj novi savez
                    List<String> newMembers = (List<String>) newAllianceSnapshot.get("memberIds");
                    List<String> pendingInvitations = (List<String>) newAllianceSnapshot.get("pendingInvitations");

                    if (newMembers == null) newMembers = new ArrayList<>();
                    if (pendingInvitations == null) pendingInvitations = new ArrayList<>();

                    if (!newMembers.contains(userId)) {
                        newMembers.add(userId);
                        transaction.update(newAllianceRef, "memberIds", newMembers);
                    }

                    pendingInvitations.remove(userId);
                    transaction.update(newAllianceRef, "pendingInvitations", pendingInvitations);

                    // 4. Ažuriraj vođu novog saveza (ukloni poziv iz sent liste)
                    if (newAllianceLeaderSnapshot != null && newAllianceLeaderSnapshot.exists()) {
                        List<String> allianceInvitationsSent = (List<String>) newAllianceLeaderSnapshot.get("allianceInvitationsSent");
                        if (allianceInvitationsSent != null) {
                            allianceInvitationsSent.remove(userId);
                            transaction.update(newAllianceLeaderSnapshot.getReference(), "allianceInvitationsSent", allianceInvitationsSent);
                        }
                    }

                    return null;
                }).addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }
    @Override
    public void getAllianceById(String allianceId, OnCompleteListener<Alliance> listener) {
        db.collection("alliances").document(allianceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Alliance alliance = documentSnapshot.toObject(Alliance.class);
                        listener.onSuccess(alliance);
                    } else {
                        listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e));
    }

    @Override
    public void leaveAlliance(String userId, String allianceId, OnCompleteListener<Void> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);
        DocumentReference allianceRef = db.collection("alliances").document(allianceId);

        db.runTransaction(transaction -> {
                    DocumentSnapshot userSnapshot = transaction.get(userRef);
                    DocumentSnapshot allianceSnapshot = transaction.get(allianceRef);

                    if (!userSnapshot.exists() || !allianceSnapshot.exists()) {
                        throw new FirebaseFirestoreException("User or Alliance document not found!", FirebaseFirestoreException.Code.NOT_FOUND);
                    }

                    // 1. Ukloni korisnika iz liste članova saveza
                    List<String> memberIds = (List<String>) allianceSnapshot.get("memberIds");
                    if (memberIds != null) {
                        memberIds.remove(userId);
                        transaction.update(allianceRef, "memberIds", memberIds);
                    }

                    // 2. Ažuriraj korisnikov allianceId na null
                    transaction.update(userRef, "allianceId", null);

                    return null;
                }).addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }

    @Override
    public void disbandAlliance(String allianceId, OnCompleteListener<Void> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference allianceRef = db.collection("alliances").document(allianceId);

        db.runTransaction(transaction -> {
                    DocumentSnapshot allianceSnapshot = transaction.get(allianceRef);

                    if (!allianceSnapshot.exists()) {
                        throw new FirebaseFirestoreException("Alliance not found!", FirebaseFirestoreException.Code.NOT_FOUND);
                    }

                    List<String> memberIds = (List<String>) allianceSnapshot.get("memberIds");
                    String leaderId = (String) allianceSnapshot.get("leaderId");
                    List<String> pendingInvitations = (List<String>) allianceSnapshot.get("pendingInvitations");

                    // 1. Ažuriraj korisnike koji su bili u savezu
                    if (memberIds != null) {
                        for (String memberId : memberIds) {
                            DocumentReference userRef = db.collection("users").document(memberId);
                            transaction.update(userRef, "allianceId", null);
                        }
                    }

                    // 2. Obriši poslate pozive vođe saveza
                    if (leaderId != null) {
                        DocumentReference leaderRef = db.collection("users").document(leaderId);
                        transaction.update(leaderRef, "allianceInvitationsSent", new ArrayList<>());
                    }

                    // 3. Obriši primljene pozive kod korisnika koji su bili pozvani
                    if (pendingInvitations != null) {
                        for (String invitedUserId : pendingInvitations) {
                            DocumentReference invitedUserRef = db.collection("users").document(invitedUserId);
                            DocumentSnapshot invitedUserSnapshot = transaction.get(invitedUserRef);

                            if (invitedUserSnapshot.exists()) {
                                List<String> receivedInvitations = (List<String>) invitedUserSnapshot.get("allianceInvitationsReceived");
                                if (receivedInvitations != null) {
                                    receivedInvitations.remove(allianceId);
                                    transaction.update(invitedUserRef, "allianceInvitationsReceived", receivedInvitations);
                                }
                            }
                        }
                    }

                    // 4. Obriši sam dokument saveza
                    transaction.delete(allianceRef);

                    return null;
                }).addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }

    @Override
    public void addNotification(Notification notification, OnCompleteListener<Void> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }

    @Override
    public void getUnreadNotificationsCount(String userId, OnCompleteListener<Integer> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listener.onSuccess(queryDocumentSnapshots.size());
                })
                .addOnFailureListener(listener::onFailure);
    }

    @Override
    public void getAllNotifications(String userId, OnCompleteListener<List<Notification>> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notification> notifications = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Notification notification = doc.toObject(Notification.class);
                        if (notification != null) {
                            notification.setNotificationId(doc.getId());
                            notifications.add(notification);
                        }
                    }
                    listener.onSuccess(notifications);
                })
                .addOnFailureListener(listener::onFailure);
    }

    @Override
    public void markNotificationAsRead(String notificationId, OnCompleteListener<Void> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications")
                .document(notificationId)
                .update("isRead", true)
                .addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }
    @Override
    public void startSpecialMission(String allianceId, OnCompleteListener<Void> listener) {
        allianceCollection.document(allianceId).get().addOnSuccessListener(documentSnapshot -> {
            Alliance alliance = documentSnapshot.toObject(Alliance.class);
            if (alliance != null && !alliance.isSpecialMissionActive()) {

                List<String> members = alliance.getMemberIds();
                String leaderId = alliance.getLeaderId();
                int memberCount = members.size();
                if (leaderId != null && !members.contains(leaderId)) memberCount++;
                if (members.isEmpty() && leaderId != null) memberCount = 1;

                int bossMaxHp = 100 * memberCount;

                alliance.setSpecialMissionActive(true);
                alliance.setSpecialMissionStartTime(new Date());
                alliance.setSpecialMissionBossMaxHp(bossMaxHp);
                alliance.setSpecialMissionBossHp(bossMaxHp);

                // Kreiranje progress-a za sve članove + vođu
                List<String> allUserIds = new ArrayList<>(members);
                if (leaderId != null && !allUserIds.contains(leaderId)) allUserIds.add(leaderId);

                for (String userId : allUserIds) {
                    SpecialMissionProgress progress = new SpecialMissionProgress(userId, allianceId);
                    String progressDocId = allianceId + "_" + userId; // konzistentan ID
                    missionProgressCollection.document(progressDocId).set(progress);
                }

                allianceCollection.document(allianceId).set(alliance)
                        .addOnSuccessListener(aVoid -> listener.onSuccess(null))
                        .addOnFailureListener(listener::onFailure);

            } else {
                listener.onFailure(new Exception("Misija je već aktivna ili savez ne postoji."));
            }
        }).addOnFailureListener(listener::onFailure);
    }

    @Override
    public void dealDamageToBoss(String allianceId, String userId, int damageAmount, OnCompleteListener<Void> listener) {
        if (damageAmount <= 0) {
            listener.onSuccess(null); // Nema štete za naneti
            return;
        }

        DocumentReference allianceRef = allianceCollection.document(allianceId);
        String progressDocId = allianceId + "_" + userId;
        DocumentReference progressRef = missionProgressCollection.document(progressDocId);

        // Koristimo transakciju za sigurno ažuriranje DVA različita dokumenta
        db.runTransaction((Transaction.Function<Void>) transaction -> {
                    // 1. Čitanje trenutnog stanja
                    DocumentSnapshot allianceSnapshot = transaction.get(allianceRef);
                    DocumentSnapshot progressSnapshot = transaction.get(progressRef);

                    Alliance alliance = allianceSnapshot.toObject(Alliance.class);
                    SpecialMissionProgress progress = progressSnapshot.toObject(SpecialMissionProgress.class);

                    // Provere
                    if (alliance == null || !alliance.isSpecialMissionActive()) {
                        throw new FirebaseFirestoreException("Savez ili misija nisu aktivni.",
                                FirebaseFirestoreException.Code.ABORTED);
                    }
                    if (progress == null) {
                        throw new FirebaseFirestoreException("Progres misije nije pronađen za korisnika.",
                                FirebaseFirestoreException.Code.ABORTED);
                    }

                    // 2. Ažuriranje individualnog napretka (SpecialMissionProgress)
                    int newDamageDealt = progress.getDamageDealt() + damageAmount;
                    progress.setDamageDealt(newDamageDealt);
                    transaction.set(progressRef, progress); // Zapiši ažurirani progres korisnika

                    // 3. Ažuriranje HP-a Bosa (Alliance)
                    int currentBossHp = alliance.getSpecialMissionBossHp();
                    int updatedBossHp = Math.max(0, currentBossHp - damageAmount); // HP ne sme ići ispod 0

                    alliance.setSpecialMissionBossHp(updatedBossHp);
                    transaction.set(allianceRef, alliance); // Zapiši ažurirani HP Bosa

                    // Opciono: Možete dodati proveru za pobedu misije ovde ako je updatedBossHp == 0.

                    return null;
                }).addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }
    @Override
    public ListenerRegistration observeMissionProgress(String allianceId, List<String> userIds, MissionProgressListener<List<SpecialMissionProgress>> listener) {

        return missionProgressCollection
                .whereEqualTo("allianceId", allianceId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        listener.onFailure(error);
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        List<SpecialMissionProgress> progressList = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            SpecialMissionProgress progress = doc.toObject(SpecialMissionProgress.class);

                            // Filtriramo samo one koji su u listi aktivnih clanova
                            // Iako Query već radi filtriranje, ovo je dodatna provera
                            if (progress != null && userIds.contains(progress.getUserId())) {
                                progressList.add(progress);
                            }
                        }
                        listener.onProgressChange(progressList); // Poziva onProgressChange pri promeni
                    }
                });

        // Napomena: Ovo vraća ListenerRegistration objekat.
    }
    @Override
    public void applyDailyMessageBonus(String allianceId, String userId, OnCompleteListener<Void> listener) {
        final int DAMAGE_PER_MESSAGE = 4;
        final int MAX_DAYS = 14;

        DocumentReference allianceRef = allianceCollection.document(allianceId);
        String progressDocId = allianceId + "_" + userId; // Ispravan ID
        DocumentReference progressRef = missionProgressCollection.document(progressDocId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
                    // 1. Čitanje trenutnog stanja unutar transakcije
                    DocumentSnapshot allianceSnapshot = transaction.get(allianceRef);
                    DocumentSnapshot progressSnapshot = transaction.get(progressRef);

                    Alliance alliance = allianceSnapshot.toObject(Alliance.class);
                    SpecialMissionProgress progress = progressSnapshot.toObject(SpecialMissionProgress.class);

                    // Provere
                    if (alliance == null || !alliance.isSpecialMissionActive()) {
                        throw new FirebaseFirestoreException("Misija saveza nije aktivna.", FirebaseFirestoreException.Code.ABORTED);
                    }
                    if (progress == null) {
                        throw new FirebaseFirestoreException("Progres misije nije pronađen.", FirebaseFirestoreException.Code.ABORTED);
                    }

                    // 2. Provera da li je bonus za današnji dan već primenjen
                    String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new Date());
                    if (progress.getDaysMessaged().contains(today) || progress.getDaysMessaged().size() >= MAX_DAYS) {
                        // Ako jeste, prekini transakciju. Ništa se neće desiti, što je ispravno.
                        return null;
                    }

                    // 3. Ažuriranje individualnog napretka
                    progress.getDaysMessaged().add(today);
                    progress.setDamageDealt(progress.getDamageDealt() + DAMAGE_PER_MESSAGE);
                    transaction.set(progressRef, progress);

                    // 4. Ažuriranje HP-a Bosa
                    int newBossHp = Math.max(0, alliance.getSpecialMissionBossHp() - DAMAGE_PER_MESSAGE);
                    transaction.update(allianceRef, "specialMissionBossHp", newBossHp);

                    return null;
                }).addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(e -> {
                    // Ignorišemo ABORTED greške jer one znače da je uslov namerno prekinuo transakciju
                    if (e instanceof FirebaseFirestoreException && ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.ABORTED) {
                        listener.onSuccess(null);
                    } else {
                        listener.onFailure(e);
                    }
                });
    }

    @Override
    public void applyShopPurchaseDamage(String allianceId, String userId, OnCompleteListener<Void> listener) {

        final int DAMAGE_PER_PURCHASE = 2;
        final int PURCHASE_LIMIT = 5;

        DocumentReference allianceRef = allianceCollection.document(allianceId);
        String progressDocId = allianceId + "_" + userId;
        DocumentReference progressRef = missionProgressCollection.document(progressDocId);

        // Koristimo transakciju za sigurno ažuriranje DVA dokumenta
        db.runTransaction((Transaction.Function<Void>) transaction -> {
                    // 1. Čitanje trenutnog stanja
                    DocumentSnapshot allianceSnapshot = transaction.get(allianceRef);
                    DocumentSnapshot progressSnapshot = transaction.get(progressRef);

                    Alliance alliance = allianceSnapshot.toObject(Alliance.class);
                    SpecialMissionProgress progress = progressSnapshot.toObject(SpecialMissionProgress.class);

                    // Provere
                    if (alliance == null || !alliance.isSpecialMissionActive()) {
                        throw new FirebaseFirestoreException("Misija saveza nije aktivna.",
                                FirebaseFirestoreException.Code.ABORTED);
                    }
                    if (progress == null) {
                        // Ako progres ne postoji, kreirajte ga (iako bi startSpecialMission trebalo da ga kreira)
                        progress = new SpecialMissionProgress(userId, allianceId);
                    }

                    // 2. Provera limita kupovine
                    if (progress.getShopPurchases() >= PURCHASE_LIMIT) {
                        // Ako je limit dostignut, prekidamo transakciju bez greške (ne nanosimo štetu)
                        throw new FirebaseFirestoreException("Limit kupovina dostignut.",
                                FirebaseFirestoreException.Code.ABORTED);
                    }

                    // 3. Ažuriranje individualnog napretka (SpecialMissionProgress)

                    // Povećavamo brojač kupovina
                    progress.setShopPurchases(progress.getShopPurchases() + 1);

                    // Povećavamo ukupnu nanesenu štetu korisnika
                    int newDamageDealt = progress.getDamageDealt() + DAMAGE_PER_PURCHASE;
                    progress.setDamageDealt(newDamageDealt);

                    transaction.set(progressRef, progress); // Zapiši ažurirani progres korisnika

                    // 4. Ažuriranje HP-a Bosa (Alliance)
                    int currentBossHp = alliance.getSpecialMissionBossHp();
                    int updatedBossHp = Math.max(0, currentBossHp - DAMAGE_PER_PURCHASE);

                    alliance.setSpecialMissionBossHp(updatedBossHp);
                    transaction.set(allianceRef, alliance); // Zapiši ažurirani HP Bosa

                    return null;

                    // Posebno rukovanje kodom ABORTED da bi se ignorisala poruka "Limit dostignut"
                }).addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseFirestoreException && ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.ABORTED) {
                        // Ignoriši kod ABORTED (korisnik je dostigao limit ili misija nije aktivna)
                        listener.onSuccess(null);
                    } else {
                        listener.onFailure(e);
                    }
                });
    }

    @Override
    public void applyBossHitDamage(String allianceId, String userId, OnCompleteListener<Void> listener) {

        final int DAMAGE_PER_HIT = 2;
        final int HIT_LIMIT = 10; // Maksimalno 10 udaraca

        DocumentReference allianceRef = allianceCollection.document(allianceId);
        String progressDocId = allianceId + "_" + userId;
        DocumentReference progressRef = missionProgressCollection.document(progressDocId);

        db.runTransaction((transaction) -> {
                    DocumentSnapshot allianceSnapshot = transaction.get(allianceRef);
                    DocumentSnapshot progressSnapshot = transaction.get(progressRef);

                    Alliance alliance = allianceSnapshot.toObject(Alliance.class);
                    SpecialMissionProgress progress = progressSnapshot.toObject(SpecialMissionProgress.class);

                    // Provere
                    if (alliance == null || !alliance.isSpecialMissionActive()) {
                        throw new FirebaseFirestoreException("Misija saveza nije aktivna.",
                                FirebaseFirestoreException.Code.ABORTED);
                    }
                    if (progress == null) {
                        throw new FirebaseFirestoreException("Progres misije nije pronađen.",
                                FirebaseFirestoreException.Code.ABORTED);
                    }

                    // 1. Provera limita udaraca
                    if (progress.getRegularBossHits() >= HIT_LIMIT) {
                        throw new FirebaseFirestoreException("Limit udaraca dostignut.",
                                FirebaseFirestoreException.Code.ABORTED);
                    }

                    // 2. Ažuriranje individualnog napretka
                    progress.setRegularBossHits(progress.getRegularBossHits() + 1);

                    int newDamageDealt = progress.getDamageDealt() + DAMAGE_PER_HIT;
                    progress.setDamageDealt(newDamageDealt);

                    transaction.set(progressRef, progress);

                    // 3. Ažuriranje HP-a Bosa saveza
                    int currentBossHp = alliance.getSpecialMissionBossHp();
                    int updatedBossHp = Math.max(0, currentBossHp - DAMAGE_PER_HIT);

                    alliance.setSpecialMissionBossHp(updatedBossHp);
                    transaction.set(allianceRef, alliance);

                    return null;
                }).addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseFirestoreException && ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.ABORTED) {
                        // Ignoriši kod ABORTED (dostignut limit ili misija nije aktivna)
                        listener.onSuccess(null);
                    } else {
                        listener.onFailure(e);
                    }
                });
    }

    @Override
    public void endSpecialMission(String allianceId, OnCompleteListener<Void> listener) {
        // Prvo provjeri da li je boss poražen
        getAllianceById(allianceId, new OnCompleteListener<Alliance>() {
            @Override
            public void onSuccess(Alliance alliance) {
                if (alliance != null) {
                    boolean bossDefeated = alliance.getSpecialMissionBossHp() <= 0;

                    if (!bossDefeated) {
                        // Boss NIJE poražen - misija je neuspješna
                        markMissionAsFailedForAllMembers(allianceId);
                    }

                    // Nastavi sa završetkom misije
                    allianceCollection.document(allianceId).update("specialMissionActive", false)
                            .addOnSuccessListener(aVoid -> listener.onSuccess(null))
                            .addOnFailureListener(listener::onFailure);
                }
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
            }
        });
    }

    private void markMissionAsFailedForAllMembers(String allianceId) {
        getAllianceById(allianceId, new OnCompleteListener<Alliance>() {
            @Override
            public void onSuccess(Alliance alliance) {
                if (alliance != null) {
                    List<String> allMemberIds = new ArrayList<>(alliance.getMemberIds());
                    if (alliance.getLeaderId() != null && !allMemberIds.contains(alliance.getLeaderId())) {
                        allMemberIds.add(alliance.getLeaderId());
                    }

                    for (String memberId : allMemberIds) {
                        incrementFailedMissions(memberId);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Mission", "Failed to get alliance for marking failed mission", e);
            }
        });
    }

    private void incrementFailedMissions(String userId) {
        Log.d("DEBUG_MISSION", "🟡 incrementFailedMissions CALLED for user: " + userId);

        getUserById(userId, new OnCompleteListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    int currentFailed = user.getSpecialMissionsFailed();

                    user.setSpecialMissionsFailed(currentFailed + 1);

                    updateUser(user, new OnCompleteListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            getUserById(userId, new OnCompleteListener<User>() {
                                @Override
                                public void onSuccess(User updatedUser) {
                                    if (updatedUser != null) {
                                        Log.d("DEBUG_MISSION", "CONFIRMED: User " + userId + " now has failed missions: " + updatedUser.getSpecialMissionsFailed());
                                    } else {
                                        Log.e("DEBUG_MISSION", "ONFIRMATION FAILED: User is null");
                                    }
                                }
                                @Override
                                public void onFailure(Exception e) {
                                    Log.e("DEBUG_MISSION", "CONFIRMATION FAILED: " + e.getMessage());
                                }
                            });
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Log.e("DEBUG_MISSION", "FAILED to update user: " + userId, e);
                        }
                    });
                } else {
                    Log.e("DEBUG_MISSION", "User is null!");
                }
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("DEBUG_MISSION", "❌ FAILED to get user: " + userId, e);
            }
        });
    }


    @Override
    public void getSpecialMissionProgress(String userId, String allianceId, OnCompleteListener<SpecialMissionProgress> listener) {
        String progressDocId = allianceId + "_" + userId;
        missionProgressCollection.document(progressDocId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        SpecialMissionProgress progress = documentSnapshot.toObject(SpecialMissionProgress.class);
                        listener.onSuccess(progress);
                    } else {
                        // Ako iz nekog razloga ne postoji, vrati novi prazan objekat da ne bi došlo do greške
                        listener.onSuccess(new SpecialMissionProgress(userId, allianceId));
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }

    @Override
    public void updateSpecialMissionProgress(SpecialMissionProgress progress, OnCompleteListener<Void> listener) {
        String progressDocId = progress.getAllianceId() + "_" + progress.getUserId();
        missionProgressCollection.document(progressDocId).set(progress)
                .addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }
    @Override
    public void handleTaskCompletedForSpecialMission(com.example.myapplication.domain.models.Task completedTask, String userId, OnCompleteListener<Void> listener) {
        getUserById(userId, new OnCompleteListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null || user.getAllianceId() == null || user.getAllianceId().isEmpty()) {
                    listener.onSuccess(null); // Korisnik nije u savezu, ne radi ništa
                    return;
                }
                String allianceId = user.getAllianceId();
                processTaskCompletion(userId, allianceId, completedTask, listener);
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
            }
        });
    }

    private void processTaskCompletion(String userId, String allianceId, com.example.myapplication.domain.models.Task completedTask, OnCompleteListener<Void> listener) {
        final DocumentReference allianceRef = allianceCollection.document(allianceId);
        final String progressDocId = allianceId + "_" + userId;
        final DocumentReference progressRef = missionProgressCollection.document(progressDocId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot allianceSnap = transaction.get(allianceRef);
            DocumentSnapshot progressSnap = transaction.get(progressRef);

            Alliance alliance = allianceSnap.toObject(Alliance.class);
            if (alliance == null || !alliance.isSpecialMissionActive()) {
                return null; // Misija nije aktivna, prekini
            }

            SpecialMissionProgress progress = progressSnap.exists()
                    ? progressSnap.toObject(SpecialMissionProgress.class)
                    : new SpecialMissionProgress(userId, allianceId);

            if (progress == null) throw new FirebaseFirestoreException("Progress object is null", FirebaseFirestoreException.Code.INTERNAL);

            int totalDamageToDeal = 0;

            // Pozivamo metodu iz Task.java koja vraća obe komponente štete
            android.util.Pair<Integer, Integer> damageComponents = completedTask.calculateDamageComponents();
            int simpleDamageCount = damageComponents.first;
            int otherDamage = damageComponents.second;

            // 1. Obračunaj štetu od "jednostavnih" zadataka (max 10)
            if (simpleDamageCount > 0 && progress.getSimpleTasksSolved() < 10) {
                int remainingSimpleSlots = 10 - progress.getSimpleTasksSolved();
                int actualSimpleHits = Math.min(simpleDamageCount, remainingSimpleSlots);
                totalDamageToDeal += actualSimpleHits; // Svaki "hit" je 1 HP
                progress.setSimpleTasksSolved(progress.getSimpleTasksSolved() + actualSimpleHits);
            }

            // 2. Obračunaj štetu od "ostalih" zadataka (max 6)
            if (otherDamage > 0 && progress.getOtherTasksSolved() < 6) {
                int numberOfOtherTasks = otherDamage / 4;
                int remainingOtherSlots = 6 - progress.getOtherTasksSolved();
                int actualOtherTasks = Math.min(numberOfOtherTasks, remainingOtherSlots);
                totalDamageToDeal += actualOtherTasks * 4;
                progress.setOtherTasksSolved(progress.getOtherTasksSolved() + actualOtherTasks);
            }

            // 3. Ažuriraj bazu ako je naneta bilo kakva šteta
            if (totalDamageToDeal > 0) {
                progress.setDamageDealt(progress.getDamageDealt() + totalDamageToDeal);
                alliance.setSpecialMissionBossHp(Math.max(0, alliance.getSpecialMissionBossHp() - totalDamageToDeal));
            }

            transaction.set(progressRef, progress);
            transaction.update(allianceRef, "specialMissionBossHp", alliance.getSpecialMissionBossHp());
            return null;
        }).addOnSuccessListener(aVoid -> {
            checkForNoUnsolvedTasksBonus(userId, allianceId, listener);
        }).addOnFailureListener(listener::onFailure);
    }

    private void checkForNoUnsolvedTasksBonus(String userId, String allianceId, OnCompleteListener<Void> listener) {
        final String progressDocId = allianceId + "_" + userId;
        final DocumentReference progressRef = missionProgressCollection.document(progressDocId);

        // Prvo dohvatamo podatke i o progresu i o savezu
        progressRef.get().addOnSuccessListener(progressSnap -> {
            SpecialMissionProgress progress = progressSnap.toObject(SpecialMissionProgress.class);
            if (progress == null || progress.isNoUnsolvedTasksBonusApplied()) {
                listener.onSuccess(null); // Bonus već primenjen ili nema progresa
                return;
            }

            // Sada dohvatamo savez da bismo znali kada je misija počela
            allianceCollection.document(allianceId).get().addOnSuccessListener(allianceSnapshot -> {
                Alliance alliance = allianceSnapshot.toObject(Alliance.class);
                if (alliance == null || !alliance.isSpecialMissionActive() || alliance.getSpecialMissionStartTime() == null) {
                    listener.onSuccess(null); // Misija nije aktivna ili nema startno vreme
                    return;
                }
                Date missionStartTime = alliance.getSpecialMissionStartTime();
                final int MINIMUM_TASKS_FOR_BONUS = 5; // Minimalan broj rešenih zadataka za bonus

                // Dohvatamo SVE zadatke korisnika da proverimo uslove
                new TaskRepositoryFirebaseImpl().getAllTasks(userId, new TaskRepository.OnTasksLoadedListener() {
                    @Override
                    public void onSuccess(List<com.example.myapplication.domain.models.Task> allTasks) {
                        boolean allTasksAreSolved = true;
                        int tasksSolvedDuringMission = 0;

                        for (com.example.myapplication.domain.models.Task task : allTasks) {
                            // 1. Proveravamo da li postoji ijedan nerešen zadatak
                            if (task.getStatus() != TaskStatus.URAĐEN && task.getStatus() != TaskStatus.OTKAZAN) {
                                allTasksAreSolved = false;
                                break; // Ako nađemo bar jedan, prekidamo proveru
                            }
                            // 2. Brojimo koliko je zadataka rešeno od početka misije
                            if (task.getStatus() == TaskStatus.URAĐEN && task.getCompletionDate() != null && task.getCompletionDate().after(missionStartTime)) {
                                tasksSolvedDuringMission++;
                            }
                        }

                        // FINALNA PROVERA: Da li su SVI zadaci rešeni I da li je rešeno DOVOLJNO zadataka?
                        if (allTasksAreSolved && tasksSolvedDuringMission >= MINIMUM_TASKS_FOR_BONUS) {
                            applyNoUnsolvedBonus(userId, allianceId, progress, listener);
                        } else {
                            listener.onSuccess(null); // Uslovi za bonus nisu ispunjeni
                        }
                    }
                    @Override
                    public void onFailure(Exception e) {
                        listener.onFailure(e);
                    }
                });
            }).addOnFailureListener(listener::onFailure);
        }).addOnFailureListener(listener::onFailure);
    }

    private void applyNoUnsolvedBonus(String userId, String allianceId, SpecialMissionProgress progress, OnCompleteListener<Void> listener) {
        final DocumentReference allianceRef = allianceCollection.document(allianceId);
        final String progressDocId = allianceId + "_" + userId;
        final DocumentReference progressRef = missionProgressCollection.document(progressDocId);

        db.runTransaction(transaction -> {
                    Alliance alliance = transaction.get(allianceRef).toObject(Alliance.class);
                    if (alliance == null || !alliance.isSpecialMissionActive()) return null;

                    int bonusDamage = 10;
                    progress.setNoUnsolvedTasksBonusApplied(true);
                    progress.setDamageDealt(progress.getDamageDealt() + bonusDamage);
                    alliance.setSpecialMissionBossHp(Math.max(0, alliance.getSpecialMissionBossHp() - bonusDamage));

                    transaction.set(progressRef, progress);
                    transaction.update(allianceRef, "specialMissionBossHp", alliance.getSpecialMissionBossHp());
                    return null;
                }).addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }

    @Override
    public void getUserMissionStats(String userId, OnCompleteListener<MissionStats> listener) {
        // Prvo dobijamo korisnika da uzmemo completed i failed misije
        getUserById(userId, new OnCompleteListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    int successfulMissions = user.getSpecialMissionsCompleted();
                    int failedMissions = user.getSpecialMissionsFailed();

                    // Provjeravamo da li korisnik ima aktivnu misiju
                    checkForActiveMission(user, successfulMissions, failedMissions, listener);
                } else {
                    listener.onFailure(new Exception("User not found"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
            }
        });
    }

    private void checkForActiveMission(User user, int successfulMissions, int failedMissions,
                                       OnCompleteListener<MissionStats> listener) {
        // Provjeravamo da li korisnik ima savez
        if (user.getAllianceId() != null && !user.getAllianceId().isEmpty()) {
            // Korisnik je u savezu, provjeravamo da li je misija aktivna
            getAllianceById(user.getAllianceId(), new OnCompleteListener<Alliance>() {
                @Override
                public void onSuccess(Alliance alliance) {
                    int activeMissions = (alliance != null && alliance.isSpecialMissionActive()) ? 1 : 0;
                    MissionStats stats = new MissionStats(successfulMissions, failedMissions, activeMissions);
                    listener.onSuccess(stats);
                }

                @Override
                public void onFailure(Exception e) {
                    // Ako ne možemo dobiti savez, pretpostavljamo da nema aktivne misije
                    MissionStats stats = new MissionStats(successfulMissions, failedMissions, 0);
                    listener.onSuccess(stats);
                }
            });
        } else {
            // Korisnik nije u savezu, nema aktivne misije
            MissionStats stats = new MissionStats(successfulMissions, failedMissions, 0);
            listener.onSuccess(stats);
        }
    }
}