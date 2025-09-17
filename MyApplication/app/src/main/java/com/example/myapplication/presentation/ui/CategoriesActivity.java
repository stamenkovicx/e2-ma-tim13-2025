package com.example.myapplication.presentation.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.database.CategoryRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.domain.models.Category;
import com.example.myapplication.presentation.ui.adapters.CategoryAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity {

    private static final String TAG = "CategoriesActivity";

    private RecyclerView rvCategories;
    private CategoryAdapter adapter;
    private CategoryRepository repository;
    private EditText etCategoryName;
    private Button btnAddCategory;
    private ImageButton btnSelectColor;

    private List<Category> categories = new ArrayList<>();
    private int selectedColor = Color.GRAY;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        repository = new CategoryRepositoryFirebaseImpl();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rvCategories = findViewById(R.id.rvCategories);
        etCategoryName = findViewById(R.id.etCategoryName);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        btnSelectColor = findViewById(R.id.btnSelectColor);

        rvCategories.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CategoryAdapter(categories, this, repository, userId);
        rvCategories.setAdapter(adapter);

        loadCategories();

        btnSelectColor.setOnClickListener(v -> {
            new ColorPickerDialog.Builder(this)
                    .setTitle("Choose a Color")
                    .setPositiveButton("OK", (ColorEnvelopeListener) (envelope, fromUser) -> {
                        btnSelectColor.setBackgroundColor(envelope.getColor());
                        btnSelectColor.setImageResource(0);
                        selectedColor = envelope.getColor();
                    })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                    .attachAlphaSlideBar(false)
                    .attachBrightnessSlideBar(true)
                    .show();
        });

        btnAddCategory.setOnClickListener(v -> {
            String name = etCategoryName.getText().toString().trim();
            if (!name.isEmpty()) {
                int colorToUse = selectedColor;
                if (colorToUse == Color.GRAY) {
                    Toast.makeText(this, "Please select a color.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Category newCategory = new Category(name, colorToUse);

                repository.insertCategory(newCategory, userId, new CategoryRepository.OnCategoryAddedListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(CategoriesActivity.this, "Category added successfully!", Toast.LENGTH_SHORT).show();
                        etCategoryName.setText("");
                        selectedColor = Color.GRAY;
                        btnSelectColor.setBackgroundColor(Color.TRANSPARENT);
                        loadCategories();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Error adding category", e);
                        Toast.makeText(CategoriesActivity.this, "Error adding category.", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(this, "Category name cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories() {
        repository.getAllCategories(userId, new CategoryRepository.OnCategoriesLoadedListener() {
            @Override
            public void onSuccess(List<Category> loadedCategories) {
                adapter.updateCategories(loadedCategories);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error loading categories", e);
                Toast.makeText(CategoriesActivity.this, "Failed to load categories.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}