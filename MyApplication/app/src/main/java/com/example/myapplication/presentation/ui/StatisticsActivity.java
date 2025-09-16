// StatisticsActivity.java

package com.example.myapplication.presentation.ui;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


import com.example.myapplication.data.database.CategoryRepositorySQLiteImpl;
import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.DifficultyType;
import com.example.myapplication.domain.models.User;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StatisticsActivity extends AppCompatActivity {

    private ActivityStatisticsBinding binding;
    private TaskRepositorySQLiteImpl taskRepository;
    private UserRepository userRepository;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        CategoryRepository categoryRepository = new CategoryRepositorySQLiteImpl(this);
        taskRepository = new TaskRepositorySQLiteImpl(this, categoryRepository);
        userRepository = new UserRepositoryFirebaseImpl();
        mAuth = FirebaseAuth.getInstance();

        loadStatistics();
    }

    private void loadStatistics() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userRepository.getUserById(currentUser.getUid(), new UserRepository.OnCompleteListener<User>() {
                @Override
                public void onSuccess(User user) {
                    if (user != null) {
                        int totalActiveDays = taskRepository.getTotalActiveDays();
                        int longestStreak = taskRepository.getLongestConsecutiveDays();
                        double averageXp = taskRepository.getAverageDifficultyXp();

                        binding.tvDifficultySummary.setText(getDifficultySummaryText(averageXp));
                        binding.tvTotalActiveDays.setText(String.valueOf(totalActiveDays));
                        binding.tvLongestStreak.setText(String.valueOf(longestStreak));

                        setupPieChart();
                        setupBarChart();
                        setupAverageXpLineChart();

                        setupXpLast7DaysChart(user.getLevel());
                    } else {
                        // poruka ako korisnik nije pronađen u bazi
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    // poruka o grešci pri dohvatanju podataka
                }
            });
        } else {
            // poruka ako korisnik nije prijavljen
        }
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

    private void setupXpLast7DaysChart(int userLevel) {
        // Dohvatanje ukupnog XP-a po danu iz baze (zadnjih 7 dana)
        Map<String, Double> xpPerDay = taskRepository.getXpLast7Days(userLevel);

        List<Entry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>(xpPerDay.keySet());

        // Priprema podataka za grafikon (Entry(xIndex, yValue))
        for (int i = 0; i < dates.size(); i++) {
            Double xpValue = xpPerDay.get(dates.get(i));
            entries.add(new Entry(i, xpValue != null ? xpValue.floatValue() : 0f));
        }

        LineDataSet dataSet = new LineDataSet(entries, "XP per day (last 7 days)");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setCircleColor(Color.parseColor("#4CAF50"));
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        binding.chartXpLast7Days.setData(lineData);

        // X osa: datumi
        binding.chartXpLast7Days.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dates));
        binding.chartXpLast7Days.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.chartXpLast7Days.getXAxis().setGranularity(1f);
        binding.chartXpLast7Days.getXAxis().setDrawGridLines(false);
        binding.chartXpLast7Days.getXAxis().setLabelCount(dates.size());
        binding.chartXpLast7Days.getXAxis().setLabelRotationAngle(-45f);

        // Y osa: ukupni XP, minimalno 0
        binding.chartXpLast7Days.getAxisLeft().setAxisMinimum(0f);
        binding.chartXpLast7Days.getAxisRight().setEnabled(false);

        // Opšte postavke
        binding.chartXpLast7Days.getDescription().setEnabled(false);
        binding.chartXpLast7Days.animateX(1200);
        binding.chartXpLast7Days.invalidate();
    }

    private void setupAverageXpLineChart() {
        Map<String, Double> avgDifficultyXpPerDay = taskRepository.getAverageDifficultyXpLast7Days();

        List<Entry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>(avgDifficultyXpPerDay.keySet());

        for (int i = 0; i < dates.size(); i++) {
            Double value = avgDifficultyXpPerDay.get(dates.get(i));
            entries.add(new Entry(i, value != null ? value.floatValue() : 0f));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Average Difficulty XP");
        dataSet.setColor(Color.parseColor("#FF9800"));
        dataSet.setCircleColor(Color.parseColor("#FF9800"));
        dataSet.setLineWidth(2f);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        binding.chartAverageDifficultyXp.setData(lineData);

        binding.chartAverageDifficultyXp.getAxisLeft().setAxisMinimum(1f);
        binding.chartAverageDifficultyXp.getAxisLeft().setAxisMaximum(25f);

        LimitLine veryEasy = new LimitLine(1f, "Very Easy");
        veryEasy.setLineColor(Color.GREEN);
        veryEasy.setLineWidth(1f);

        LimitLine easy = new LimitLine(3f, "Easy");
        easy.setLineColor(Color.BLUE);
        easy.setLineWidth(1f);

        LimitLine hard = new LimitLine(7f, "Hard");
        hard.setLineColor(Color.MAGENTA);
        hard.setLineWidth(1f);

        LimitLine extreme = new LimitLine(20f, "Extremely Hard");
        extreme.setLineColor(Color.RED);
        extreme.setLineWidth(1f);

        binding.chartAverageDifficultyXp.getAxisLeft().addLimitLine(veryEasy);
        binding.chartAverageDifficultyXp.getAxisLeft().addLimitLine(easy);
        binding.chartAverageDifficultyXp.getAxisLeft().addLimitLine(hard);
        binding.chartAverageDifficultyXp.getAxisLeft().addLimitLine(extreme);

        binding.chartAverageDifficultyXp.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dates));
        binding.chartAverageDifficultyXp.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.chartAverageDifficultyXp.getXAxis().setGranularity(1f);

        binding.chartAverageDifficultyXp.getAxisRight().setEnabled(false);
        binding.chartAverageDifficultyXp.getDescription().setEnabled(false);
        binding.chartAverageDifficultyXp.animateX(1500);
        binding.chartAverageDifficultyXp.invalidate();
    }

    private String getDifficultySummaryText(double avgDifficultyXp) {
        DifficultyType closest = DifficultyType.VERY_EASY;
        double minDiff = Double.MAX_VALUE;

        for (DifficultyType type : DifficultyType.values()) {
            double diff = Math.abs(avgDifficultyXp - type.getXpValue());
            if (diff < minDiff) {
                minDiff = diff;
                closest = type;
            }
        }

        switch (closest) {
            case VERY_EASY:
                return "The user mostly completes very easy tasks.";
            case EASY:
                return "The user mostly completes easy tasks.";
            case HARD:
                return "The user mostly completes hard tasks.";
            case EXTREMELY_HARD:
                return "The user mostly completes extremely hard tasks.";
            default:
                return "The user has a mixed difficulty task pattern.";
        }
    }
}