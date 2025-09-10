package com.example.myapplication.data.local.datebase;

// Nalazi se u paketu: com.example.myapplication.data.local.database

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.myapplication.data.local.dao.TaskDao;
import com.example.myapplication.data.local.dao.UserDao;
import com.example.myapplication.data.model.Task;
import com.example.myapplication.data.model.User;

// Navodimo sve klase koje su tabele (entities) i verziju baze.
// exportSchema = false služi da izbegnemo warning prilikom bildovanja.
@Database(entities = {User.class, Task.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // Apstraktne metode za svaki DAO koji imamo
    public abstract UserDao userDao();
    public abstract TaskDao taskDao();

    // Singleton patern - osigurava da postoji samo jedna instanca baze u celoj aplikaciji
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "rpg_navike_database") // Ovo je ime fajla baze podataka
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}