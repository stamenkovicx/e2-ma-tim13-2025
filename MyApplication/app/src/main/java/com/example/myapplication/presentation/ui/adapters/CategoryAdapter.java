package com.example.myapplication.presentation.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.database.TaskRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.domain.models.Category;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private static final String TAG = "CategoryAdapter";

    private List<Category> categories;
    private Context context;
    private CategoryRepository categoryRepository;
    private TaskRepository taskRepository;
    private String userId;

    public CategoryAdapter(List<Category> categories, Context context, CategoryRepository categoryRepository, String userEmail) {
        this.categories = categories;
        this.context = context;
        this.categoryRepository = categoryRepository;
        this.userId = userEmail;
        this.taskRepository = new TaskRepositoryFirebaseImpl();
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
        holder.tvCategoryIndex.setText((position + 1) + "."); // redni broj
        holder.tvCategoryName.setText(category.getName());
        holder.vCategoryColor.setBackgroundColor(category.getColor());

        holder.vCategoryColor.setOnClickListener(v -> showColorPicker(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        public View vCategoryColor;
        public TextView tvCategoryName;
        public TextView tvCategoryIndex;


        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            vCategoryColor = itemView.findViewById(R.id.vCategoryColor);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryIndex = itemView.findViewById(R.id.tvCategoryIndex);
        }
    }

    private void showColorPicker(Category category) {
        new ColorPickerDialog.Builder(context)
                .setTitle("Choose a color")
                .setPositiveButton("OK", (ColorEnvelopeListener) (envelope, fromUser) -> {
                    int chosenColor = envelope.getColor();

                    boolean taken = false;
                    for (Category c : categories) {
                        if (c.getColor() == chosenColor && !c.getId().equals(category.getId())) {
                            taken = true;
                            break;
                        }
                    }

                    if (taken) {
                        Toast.makeText(context, "This color is already taken!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    category.setColor(chosenColor);

                    // Asinhroni poziv za ažuriranje kategorije
                    categoryRepository.updateCategory(category, userId, new CategoryRepository.OnCategoryUpdatedListener() {
                        @Override
                        public void onSuccess() {
                            // Asinhroni poziv za ažuriranje boje zadataka
                            taskRepository.updateTasksColor(category.getId(), chosenColor, userId, new TaskRepository.OnTaskUpdatedListener() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(context, "Category color and associated tasks updated successfully!", Toast.LENGTH_SHORT).show();
                                    notifyDataSetChanged();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e(TAG, "Failed to update tasks color.", e);
                                    Toast.makeText(context, "Failed to update tasks color.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Failed to update category color.", e);
                            Toast.makeText(context, "Failed to update category color.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .attachAlphaSlideBar(false)
                .attachBrightnessSlideBar(true)
                .show();
    }

    public void updateCategories(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }
}