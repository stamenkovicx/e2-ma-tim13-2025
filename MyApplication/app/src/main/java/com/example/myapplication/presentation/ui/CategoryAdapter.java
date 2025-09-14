package com.example.myapplication.presentation.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.database.TaskRepositorySQLiteImpl;
import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.domain.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private List<Integer> availableColors;
    private Context context;
    private CategoryRepository repository;

    public CategoryAdapter(List<Category> categories, List<Integer> availableColors, Context context, CategoryRepository repository) {
        this.categories = categories;
        this.availableColors = availableColors;
        this.context = context;
        this.repository = repository;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.tvCategoryName.setText(category.getName());
        holder.vCategoryColor.setBackgroundColor(category.getColor());

        // Klik na boju za promenu
        holder.vCategoryColor.setOnClickListener(v -> showColorPicker(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        public View vCategoryColor;
        public TextView tvCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            vCategoryColor = itemView.findViewById(R.id.vCategoryColor);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
        }
    }

    private void showColorPicker(Category category) {
        // filtriraj boje koje nisu zauzete
        List<Integer> freeColors = new ArrayList<>();
        for (int color : availableColors) {
            boolean taken = false;
            for (Category c : categories) {
                if (c.getColor() == color && c.getId() != category.getId()) {
                    taken = true;
                    break;
                }
            }
            if (!taken) freeColors.add(color);
        }

        String[] colorNames = new String[freeColors.size()];
        for (int i = 0; i < freeColors.size(); i++) {
            colorNames[i] = String.format("#%06X", (0xFFFFFF & freeColors.get(i)));
        }

        new AlertDialog.Builder(context)
                .setTitle("Izaberite boju")
                .setItems(colorNames, (dialog, which) -> {
                    // Menjamo boju kategorije na izabranu
                    category.setColor(freeColors.get(which));

                    // Ovdje je update u bazi za kategoriju
                    repository.updateCategory(category);

                    // Ovdje je update boje svih zadataka te kategorije
                    TaskRepository taskRepo = new TaskRepositorySQLiteImpl(context);
                    taskRepo.updateTasksColor(category.getId(), category.getColor());

                    // Osvežavamo prikaz
                    notifyDataSetChanged();
                }).show();
    }

    public void updateCategories(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }
}
