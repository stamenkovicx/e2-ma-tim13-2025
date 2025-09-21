package com.example.myapplication.data.database;

import com.example.myapplication.data.repository.BossRepository;
import com.example.myapplication.domain.models.Boss;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BossRepositoryFirebaseImpl implements BossRepository {

    private final CollectionReference bossCollection;

    public BossRepositoryFirebaseImpl() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.bossCollection = db.collection("bosses");
    }

    @Override
    public void getBoss(String bossId, OnBossLoadedListener listener) {
        bossCollection.document(bossId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boss boss = documentSnapshot.toObject(Boss.class);
                        listener.onSuccess(boss);
                    } else {
                        listener.onSuccess(null); // Šef ne postoji
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }

    @Override
    public void saveBoss(Boss boss, OnBossSavedListener listener) {
        // Koristi se document id korisnika, da bi svakom korisniku pripadao samo 1 boss
        DocumentReference bossRef = bossCollection.document(boss.getId());
        bossRef.set(boss)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }
}