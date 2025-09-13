package com.example.myapplication.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.myapplication.domain.models.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyProjectDatabase.db";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_AVATAR = "avatar";
    public static final String COLUMN_LEVEL = "level";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_POWER_POINTS = "power_points";
    public static final String COLUMN_XP = "xp";
    public static final String COLUMN_COINS = "coins";
    public static final String COLUMN_EQUIPMENT = "equipment";

    // SQL izraz za kreiranje tabele "users"
    private static final String SQL_CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USERNAME + " TEXT," +
                    COLUMN_EMAIL + " TEXT," +
                    COLUMN_PASSWORD + " TEXT," +
                    COLUMN_AVATAR + " TEXT," +
                    COLUMN_LEVEL + " INTEGER," +
                    COLUMN_TITLE + " TEXT," +
                    COLUMN_POWER_POINTS + " INTEGER," +
                    COLUMN_XP + " INTEGER," +
                    COLUMN_COINS + " INTEGER," +
                    COLUMN_EQUIPMENT + " TEXT)"; // Nova kolona

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public boolean addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_USERNAME, user.getUsername());
        cv.put(COLUMN_EMAIL, user.getEmail());
        cv.put(COLUMN_PASSWORD, user.getPassword());
        cv.put(COLUMN_AVATAR, user.getAvatar());
        cv.put(COLUMN_LEVEL, user.getLevel());
        cv.put(COLUMN_TITLE, user.getTitle());
        cv.put(COLUMN_POWER_POINTS, user.getPowerPoints());
        cv.put(COLUMN_XP, user.getXp());
        cv.put(COLUMN_COINS, user.getCoins());
        cv.put(COLUMN_EQUIPMENT, new Gson().toJson(user.getEquipment()));

        long insert = db.insert(TABLE_USERS, null, cv);
        db.close();

        return insert != -1;
    }

    public boolean checkUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = { COLUMN_EMAIL };
        String selection = COLUMN_EMAIL + " =?";
        String[] selectionArgs = { email };
        String limit = "1";

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null, limit);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public User getUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        String[] columns = {
                COLUMN_ID,
                COLUMN_USERNAME,
                COLUMN_EMAIL,
                COLUMN_PASSWORD,
                COLUMN_AVATAR,
                COLUMN_LEVEL,
                COLUMN_TITLE,
                COLUMN_POWER_POINTS,
                COLUMN_XP,
                COLUMN_COINS,
                COLUMN_EQUIPMENT
        };
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
            int usernameIndex = cursor.getColumnIndexOrThrow(COLUMN_USERNAME);
            int emailIndex = cursor.getColumnIndexOrThrow(COLUMN_EMAIL);
            int passwordIndex = cursor.getColumnIndexOrThrow(COLUMN_PASSWORD);
            int avatarIndex = cursor.getColumnIndexOrThrow(COLUMN_AVATAR);
            int levelIndex = cursor.getColumnIndexOrThrow(COLUMN_LEVEL);
            int titleIndex = cursor.getColumnIndexOrThrow(COLUMN_TITLE);
            int powerPointsIndex = cursor.getColumnIndexOrThrow(COLUMN_POWER_POINTS);
            int xpIndex = cursor.getColumnIndexOrThrow(COLUMN_XP);
            int coinsIndex = cursor.getColumnIndexOrThrow(COLUMN_COINS);
            int equipmentIndex = cursor.getColumnIndexOrThrow(COLUMN_EQUIPMENT);

            // Čitanje podataka
            String username = cursor.getString(usernameIndex);
            String userEmail = cursor.getString(emailIndex);
            String userPassword = cursor.getString(passwordIndex);
            String userAvatar = cursor.getString(avatarIndex);
            int level = cursor.getInt(levelIndex);
            String title = cursor.getString(titleIndex);
            int powerPoints = cursor.getInt(powerPointsIndex);
            int xp = cursor.getInt(xpIndex);
            int coins = cursor.getInt(coinsIndex);
            String equipmentJson = cursor.getString(equipmentIndex);

            // Kreiranje User objekta
            user = new User(username, userEmail, userPassword, userAvatar, level, title, powerPoints, xp, coins);

            // Postavljanje opreme iz JSON-a
            List<String> equipment = new Gson().fromJson(equipmentJson, new TypeToken<List<String>>(){}.getType());
            user.setEquipment(equipment);
        }

        if (cursor != null) {
            cursor.close();
        }
        //db.close();
        return user;
    }

    public void updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_AVATAR, user.getAvatar());
        values.put(COLUMN_LEVEL, user.getLevel());
        values.put(COLUMN_TITLE, user.getTitle());
        values.put(COLUMN_POWER_POINTS, user.getPowerPoints());
        values.put(COLUMN_XP, user.getXp());
        values.put(COLUMN_COINS, user.getCoins());
        values.put(COLUMN_EQUIPMENT, new Gson().toJson(user.getEquipment()));

        db.update(TABLE_USERS, values, "email = ?", new String[]{user.getEmail()});
        db.close();
    }
}