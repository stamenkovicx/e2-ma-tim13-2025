// StatisticsActivity.java

package com.example.myapplication.presentation.ui;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

// Uvezite klase potrebne za grafikone
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.example.myapplication.data.database.TaskRepositorySQLiteImpl;
import com.example.myapplication.domain.models.TaskStatus;
import com.example.myapplication.databinding.ActivityStatisticsBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.github.mikephil.charting.data.Entry;

public class StatisticsActivity extends AppCompatActivity {

    private ActivityStatisticsBinding binding;
    private TaskRepositorySQLiteImpl taskRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        taskRepository = new TaskRepositorySQLiteImpl(this);

        loadStatistics();
    }

    private void loadStatistics() {
        int totalActiveDays = taskRepository.getTotalActiveDays();
        int longestStreak = taskRepository.getLongestConsecutiveDays();
        double averageXp = taskRepository.getAverageDifficultyXp();

        binding.tvTotalActiveDays.setText(String.valueOf(totalActiveDays));
        binding.tvLongestStreak.setText(String.valueOf(longestStreak));

        setupPieChart();
        setupBarChart();
       // setupLineChart();
    }

    private void setupPieChart() {
        // Dohvatanje broja zadataka po statusu
        int completedCount = taskRepository.getTaskCountByStatus(TaskStatus.URAĐEN);
        int inProgressCount = taskRepository.getTaskCountByStatus(TaskStatus.AKTIVAN);
        int canceledCount = taskRepository.getTaskCountByStatus(TaskStatus.OTKAZAN);

        List<PieEntry> entries = new ArrayList<>();
        if (completedCount > 0) entries.add(new PieEntry(completedCount, "Completed"));
        if (inProgressCount > 0) entries.add(new PieEntry(inProgressCount, "In Progress"));
        if (canceledCount > 0) entries.add(new PieEntry(canceledCount, "Canceled"));

        PieDataSet dataSet = new PieDataSet(entries, "Task Status");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // Definisanje boja za segmente grafikona
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#4CAF50")); // Zelena za Completed
        colors.add(Color.parseColor("#2196F3")); // Plava za In Progress
        colors.add(Color.parseColor("#F44336")); // Crvena za Canceled
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        binding.chartTaskStatus.setData(data);
        binding.chartTaskStatus.getDescription().setEnabled(false); // Uklanjanje opisa
        binding.chartTaskStatus.setDrawHoleEnabled(true);
        binding.chartTaskStatus.setHoleColor(Color.TRANSPARENT);
        binding.chartTaskStatus.setTransparentCircleRadius(61f);
        binding.chartTaskStatus.invalidate(); // Osvjezavanje grafikona
    }

    private void setupBarChart() {
        // Dohvatanje podataka o završenim zadacima po kategoriji
        Map<String, Integer> categoryCounts = taskRepository.getCompletedTasksCountByCategory();

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            entries.add(new BarEntry(i, entry.getValue()));
            labels.add(entry.getKey());
            colors.add(getCategoryColor(entry.getKey()));
            i++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Completed Tasks by Category");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        binding.chartCategoryTasks.setData(data);

        // Konfigurisanje X-ose da prikazuje nazive kategorija
        binding.chartCategoryTasks.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.chartCategoryTasks.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.chartCategoryTasks.getXAxis().setGranularity(1f);
        binding.chartCategoryTasks.getXAxis().setDrawGridLines(false);
        binding.chartCategoryTasks.getXAxis().setLabelCount(labels.size());

        // Uklanjanje opisa i animacija
        binding.chartCategoryTasks.getAxisLeft().setAxisMinimum(0f);
        binding.chartCategoryTasks.getAxisRight().setEnabled(false);
        binding.chartCategoryTasks.getDescription().setEnabled(false);
        binding.chartCategoryTasks.animateY(1000);
        binding.chartCategoryTasks.invalidate();
    }

    private int getCategoryColor(String categoryName) {
        if (categoryName.equalsIgnoreCase("Zdravlje")) {
            return Color.parseColor("#F44336");
        } else if (categoryName.equalsIgnoreCase("Ucenje")) {
            return Color.parseColor("#2196F3");
        } else if (categoryName.equalsIgnoreCase("Sredjivanje")) {
            return Color.parseColor("#4CAF50");
        }
        return Color.GRAY;
    }
}