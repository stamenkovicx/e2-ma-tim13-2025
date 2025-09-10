package com.example.myapplication.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.myapplication.data.model.User;

// @Dao anotacija je obavezna
@Dao
public interface UserDao {

    // OnConflictStrategy.REPLACE znači da ako pokušaš da uneseš korisnika
    // koji već postoji (sa istim ID-jem), stari će biti zamenjen novim.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Query("SELECT * FROM user_table WHERE uid = :uid LIMIT 1")
    User getUserByUid(String uid);

    @Query("SELECT * FROM user_table WHERE id = :id LIMIT 1")
    User getUserById(int id);
}