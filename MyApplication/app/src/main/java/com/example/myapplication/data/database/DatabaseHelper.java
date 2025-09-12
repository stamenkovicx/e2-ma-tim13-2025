package com.example.myapplication.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.myapplication.domain.models.User;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyProjectDatabase.db";
    private static final int DATABASE_VERSION = 2;

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
                    COLUMN_COINS + " INTEGER)";

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

        long insert = db.insert(TABLE_USERS, null, cv);
        db.close();

        return insert != -1;
    }

    // Provjera da li vec postoji korisnik sa odredjenim email-om
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
                COLUMN_COINS
        };
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME);
            int emailIndex = cursor.getColumnIndex(COLUMN_EMAIL);
            int passwordIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
            int avatarIndex = cursor.getColumnIndex(COLUMN_AVATAR);
            int levelIndex = cursor.getColumnIndex(COLUMN_LEVEL);
            int titleIndex = cursor.getColumnIndex(COLUMN_TITLE);
            int powerPointsIndex = cursor.getColumnIndex(COLUMN_POWER_POINTS);
            int xpIndex = cursor.getColumnIndex(COLUMN_XP);
            int coinsIndex = cursor.getColumnIndex(COLUMN_COINS);

            int id = (idIndex != -1) ? cursor.getInt(idIndex) : 0;
            String username = (usernameIndex != -1) ? cursor.getString(usernameIndex) : "";
            String userEmail = (emailIndex != -1) ? cursor.getString(emailIndex) : "";
            String userPassword = (passwordIndex != -1) ? cursor.getString(passwordIndex) : "";
            String userAvatar = (avatarIndex != -1) ? cursor.getString(avatarIndex) : "";
            int level = (levelIndex != -1) ? cursor.getInt(levelIndex) : 1;
            String title = (titleIndex != -1) ? cursor.getString(titleIndex) : "Beginner";
            int powerPoints = (powerPointsIndex != -1) ? cursor.getInt(powerPointsIndex) : 100;
            int xp = (xpIndex != -1) ? cursor.getInt(xpIndex) : 0;
            int coins = (coinsIndex != -1) ? cursor.getInt(coinsIndex) : 0;

            user = new User(username, userEmail, userPassword, userAvatar, level, title, powerPoints, xp, coins);        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return user;
    }
}