package com.example.myapplication.presentation.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.data.database.CategoryRepositorySQLiteImpl;
import com.example.myapplication.domain.models.Category;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

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
    private int selectedColor = Color.GRAY; // default boja


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        repository = new CategoryRepositorySQLiteImpl(this);

        rvCategories = findViewById(R.id.rvCategories);
        etCategoryName = findViewById(R.id.etCategoryName);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        ImageButton btnSelectColor = findViewById(R.id.btnSelectColor);

        rvCategories.setLayoutManager(new LinearLayoutManager(this));

        categories = repository.getAllCategories();
        AVAILABLE_COLORS = generateColors(50); // generiši 50 boja

        adapter = new CategoryAdapter(categories, AVAILABLE_COLORS, this, repository);
        rvCategories.setAdapter(adapter);

        // Biranje boje pre dodavanja nove kategorije
        btnSelectColor.setOnClickListener(v -> {
            new ColorPickerDialog.Builder(this)
                    .setTitle("Izaberite boju")
                    .setPositiveButton("OK", (ColorEnvelopeListener) (envelope, fromUser) -> {
                        selectedColor = envelope.getColor();
                    })
                    .setNegativeButton("Otkaži", (dialogInterface, i) -> dialogInterface.dismiss())
                    .attachAlphaSlideBar(false)
                    .attachBrightnessSlideBar(true)
                    .show();
        });

        btnAddCategory.setOnClickListener(v -> {
            String name = etCategoryName.getText().toString().trim();
            if (!name.isEmpty()) {
                // Ako korisnik nije izabrao boju, uzmi prvu slobodnu
                int colorToUse = selectedColor != Color.GRAY ? selectedColor :
                        AVAILABLE_COLORS.get(categories.size() % AVAILABLE_COLORS.size());

                // Provera da boja nije zauzeta
                boolean taken = false;
                for (Category c : categories) {
                    if (c.getColor() == colorToUse) {
                        taken = true;
                        break;
                    }
                }
                if (taken) {
                    colorToUse = AVAILABLE_COLORS.get(categories.size() % AVAILABLE_COLORS.size());
                }

                Category newCategory = new Category(0, name, colorToUse);
                repository.insertCategory(newCategory);

                categories.add(newCategory);
                adapter.notifyItemInserted(categories.size() - 1);

                etCategoryName.setText("");
                selectedColor = Color.GRAY; // reset
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