package com.example.myapplication.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;

import com.example.myapplication.domain.models.Equipment;
import com.example.myapplication.domain.models.User;
import com.example.myapplication.domain.models.UserEquipment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import com.example.myapplication.data.repository.ItemRepository;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyProjectDatabase.db";
    private static final int DATABASE_VERSION = 5;
    private Context context;


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

    //******************************************************************************//
    // Dodaj nove konstante ispod postojećih (npr. posle COLUMN_EQUIPMENT)
    public static final String TABLE_TASKS = "tasks";
    public static final String COLUMN_TASK_ID = "_id";
    public static final String COLUMN_TASK_NAME = "name";
    public static final String COLUMN_TASK_DESCRIPTION = "description";
    public static final String COLUMN_TASK_CATEGORY_ID = "category_id";
    public static final String COLUMN_TASK_FREQUENCY = "frequency";
    public static final String COLUMN_TASK_INTERVAL = "interval";
    public static final String COLUMN_TASK_INTERVAL_UNIT = "interval_unit";
    public static final String COLUMN_TASK_START_DATE = "start_date";
    public static final String COLUMN_TASK_END_DATE = "end_date";
    public static final String COLUMN_TASK_EXECUTION_TIME = "execution_time";
    public static final String COLUMN_TASK_DIFFICULTY = "difficulty";
    public static final String COLUMN_TASK_IMPORTANCE = "importance";
    public static final String COLUMN_TASK_XP_VALUE = "xp_value";
    public static final String COLUMN_TASK_STATUS = "status";
    public static final String COLUMN_TASK_COMPLETION_DATE = "completion_date";
    public static final String COLUMN_TASK_CATEGORY_COLOR = "category_color";


    //*******************************************************************//
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COLUMN_CATEGORY_ID = "_id";
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_COLOR = "color";

    //*******************************************************************//

    private static final String SQL_CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USERNAME + " TEXT," +
                    COLUMN_EMAIL + " TEXT UNIQUE," + // email jedinstven, ali ne primarni ključ
                    COLUMN_PASSWORD + " TEXT," +
                    COLUMN_AVATAR + " TEXT," +
                    COLUMN_LEVEL + " INTEGER," +
                    COLUMN_TITLE + " TEXT," +
                    COLUMN_POWER_POINTS + " INTEGER," +
                    COLUMN_XP + " INTEGER," +
                    COLUMN_COINS + " INTEGER," +
                    COLUMN_EQUIPMENT + " TEXT)";

    private static final String SQL_CREATE_TASKS_TABLE =
            "CREATE TABLE " + TABLE_TASKS + " ("
                    + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TASK_NAME + " TEXT NOT NULL,"
                    + COLUMN_TASK_DESCRIPTION + " TEXT,"
                    + COLUMN_TASK_CATEGORY_ID + " INTEGER NOT NULL,"
                    + COLUMN_TASK_FREQUENCY + " TEXT NOT NULL,"
                    + COLUMN_TASK_INTERVAL + " INTEGER,"
                    + COLUMN_TASK_INTERVAL_UNIT + " TEXT,"
                    + COLUMN_TASK_START_DATE + " TEXT,"
                    + COLUMN_TASK_END_DATE + " TEXT,"
                    + COLUMN_TASK_EXECUTION_TIME + " TEXT,"
                    + COLUMN_TASK_DIFFICULTY + " TEXT NOT NULL,"
                    + COLUMN_TASK_IMPORTANCE + " TEXT NOT NULL,"
                    + COLUMN_TASK_XP_VALUE + " INTEGER NOT NULL,"
                    + COLUMN_TASK_STATUS + " TEXT NOT NULL,"
                    + COLUMN_TASK_COMPLETION_DATE + " TEXT,"
                    + COLUMN_TASK_CATEGORY_COLOR + " INTEGER)";
    ;


    private static final String SQL_CREATE_CATEGORIES_TABLE =
            "CREATE TABLE " + TABLE_CATEGORIES + " ("
                    + COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_CATEGORY_NAME + " TEXT NOT NULL,"
                    + COLUMN_CATEGORY_COLOR + " INTEGER NOT NULL);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS_TABLE);
        db.execSQL(SQL_CREATE_TASKS_TABLE);
        db.execSQL(SQL_CREATE_CATEGORIES_TABLE);

        onCreateCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES); // Dodaj ovu liniju
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
        cv.put(COLUMN_POWER_POINTS, user.getBasePowerPoints());
        cv.put(COLUMN_XP, user.getXp());
        cv.put(COLUMN_COINS, user.getCoins());

        Type type = new TypeToken<List<UserEquipment>>() {}.getType();
        String equipmentJson = new Gson().toJson(user.getUserEquipmentList(), type);
        cv.put(COLUMN_EQUIPMENT, equipmentJson);

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
         //   int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
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

            user = new User(username, userEmail, userPassword, userAvatar, level, title, powerPoints, xp, coins);

            Type type = new TypeToken<List<UserEquipment>>() {}.getType();
            List<UserEquipment> userEquipmentList = new Gson().fromJson(equipmentJson, type);
            if (userEquipmentList != null) {
                user.setUserEquipmentList(userEquipmentList);
            }
        }

        if (cursor != null) {
            cursor.close();
        }
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
        values.put(COLUMN_POWER_POINTS, user.getBasePowerPoints());
        values.put(COLUMN_XP, user.getXp());
        values.put(COLUMN_COINS, user.getCoins());

        Type type = new TypeToken<List<UserEquipment>>() {}.getType();
        String equipmentJson = new Gson().toJson(user.getUserEquipmentList(), type);
        values.put(COLUMN_EQUIPMENT, equipmentJson);

        db.update(TABLE_USERS, values, "email = ?", new String[]{user.getEmail()});
        db.close();
    }

    public List<Equipment> getUserEquipment(String userEmail) {
        List<Equipment> userEquipment = new ArrayList<>();
        User user = getUser(userEmail);

        if (user != null && user.getUserEquipmentList() != null) {
            for (UserEquipment item : user.getUserEquipmentList()) {
                Equipment equipment = ItemRepository.getEquipmentById(item.getEquipmentId());
                if (equipment != null) {
                    equipment.setActive(item.isActive());
                    equipment.setDuration(item.getDuration());
                    userEquipment.add(equipment);
                }
            }
        }
        return userEquipment;
    }


    private void onCreateCategories(SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        // Hard-kodovane kategorije sa realnim bojama
        values.put(COLUMN_CATEGORY_NAME, "Zdravlje");
        values.put(COLUMN_CATEGORY_COLOR, Color.RED);
        db.insert(TABLE_CATEGORIES, null, values);

        values.clear();
        values.put(COLUMN_CATEGORY_NAME, "Ucenje");
        values.put(COLUMN_CATEGORY_COLOR, Color.BLUE);
        db.insert(TABLE_CATEGORIES, null, values);

        values.clear();
        values.put(COLUMN_CATEGORY_NAME, "Sredjivanje");
        values.put(COLUMN_CATEGORY_COLOR, Color.GREEN);
        db.insert(TABLE_CATEGORIES, null, values);
    }
}