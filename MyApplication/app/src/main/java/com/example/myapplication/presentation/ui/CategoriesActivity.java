package com.example.myapplication.presentation.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.data.database.CategoryRepositorySQLiteImpl;
import com.example.myapplication.domain.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private CategoryAdapter adapter;
    private CategoryRepository repository;
    private EditText etCategoryName;
    private Button btnAddCategory;
    private List<Integer> AVAILABLE_COLORS; // umesto fiksnog
    private List<Category> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        repository = new CategoryRepositorySQLiteImpl(this);

        rvCategories = findViewById(R.id.rvCategories);
        etCategoryName = findViewById(R.id.etCategoryName);
        btnAddCategory = findViewById(R.id.btnAddCategory);

        rvCategories.setLayoutManager(new LinearLayoutManager(this));

        categories = repository.getAllCategories();
        // Generiši dovoljno boja (npr. 100)
        AVAILABLE_COLORS = generateColors(50);

        adapter = new CategoryAdapter(categories, AVAILABLE_COLORS, this, repository);
        rvCategories.setAdapter(adapter);

        btnAddCategory.setOnClickListener(v -> {
            String name = etCategoryName.getText().toString().trim();
            if (!name.isEmpty()) {
                int color = AVAILABLE_COLORS.get(categories.size() % AVAILABLE_COLORS.size());
                Category newCategory = new Category(0, name, color);
                repository.insertCategory(newCategory);

                categories.add(newCategory);
                adapter.notifyItemInserted(categories.size() - 1);

                etCategoryName.setText("");
            }
        });
    }

    private List<Integer> generateColors(int n) {
        List<Integer> colors = new ArrayList<>();
        float[] saturations = {0.5f, 0.7f, 0.9f}; // pastelne, srednje, jarke
        float[] values = {0.8f, 0.9f, 1.0f};      // svetla, srednja, tamna

        for (int i = 0; i < n; i++) {
            float hue = (i * 360f / n);             // raspodela kroz ceo spektar
            float saturation = saturations[i % saturations.length];
            float value = values[i % values.length];
            colors.add(Color.HSVToColor(new float[]{hue, saturation, value}));
        }
        return colors;
    }


}