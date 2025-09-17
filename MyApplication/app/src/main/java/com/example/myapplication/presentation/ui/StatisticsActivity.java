package com.example.myapplication.presentation.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.data.database.TaskRepositoryFirebaseImpl;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.DifficultyType;
import com.example.myapplication.domain.models.Task;
import com.example.myapplication.domain.models.TaskStatus;
import com.example.myapplication.domain.models.User;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.myapplication.databinding.ActivityStatisticsBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.github.mikephil.charting.data.Entry;

public class StatisticsActivity extends AppCompatActivity {

    private ActivityStatisticsBinding binding;
    private TaskRepository taskRepository;
    private UserRepository userRepository;
    private FirebaseAuth mAuth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        taskRepository = new TaskRepositoryFirebaseImpl();
        userRepository = new UserRepositoryFirebaseImpl();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            loadStatistics();
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadStatistics() {
        if (userId == null || userId.isEmpty()) {
            return;
        }

        userRepository.getUserById(mAuth.getCurrentUser().getUid(), new UserRepository.OnCompleteListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    taskRepository.getTotalActiveDays(userId, new TaskRepository.OnStatisticsLoadedListener<Integer>() {
                        @Override
                        public void onSuccess(Integer result) {
                            binding.tvTotalActiveDays.setText(String.valueOf(result));
                        }
                        @Override
                        public void onFailure(Exception e) { Log.e("StatsActivity", "Failed to get active days", e); }
                    });

                    taskRepository.getLongestConsecutiveDays(userId, new TaskRepository.OnStatisticsLoadedListener<Integer>() {
                        @Override
                        public void onSuccess(Integer result) {
                            binding.tvLongestStreak.setText(String.valueOf(result));
                        }
                        @Override
                        public void onFailure(Exception e) { Log.e("StatsActivity", "Failed to get longest streak", e); }
                    });

                    taskRepository.getAverageDifficultyXp(userId, new TaskRepository.OnStatisticsLoadedListener<Double>() {
                        @Override
                        public void onSuccess(Double result) {
                            binding.tvDifficultySummary.setText(getDifficultySummaryText(result));
                        }
                        @Override
                        public void onFailure(Exception e) { Log.e("StatsActivity", "Failed to get average XP", e); }
                    });

                    taskRepository.getTaskCountByStatus(userId, TaskStatus.URAĐEN, new TaskRepository.OnStatisticsLoadedListener<Integer>() {
                        @Override
                        public void onSuccess(Integer completedCount) {
                            taskRepository.getTaskCountByStatus(userId, TaskStatus.AKTIVAN, new TaskRepository.OnStatisticsLoadedListener<Integer>() {
                                @Override
                                public void onSuccess(Integer activeCount) {
                                    taskRepository.getTaskCountByStatus(userId, TaskStatus.OTKAZAN, new TaskRepository.OnStatisticsLoadedListener<Integer>() {
                                        @Override
                                        public void onSuccess(Integer canceledCount) {
                                            setupPieChart(completedCount, activeCount, canceledCount);
                                        }
                                        @Override public void onFailure(Exception e) { Log.e("StatsActivity", "Failed to get canceled tasks", e); }
                                    });
                                }
                                @Override public void onFailure(Exception e) { Log.e("StatsActivity", "Failed to get active tasks", e); }
                            });
                        }
                        @Override public void onFailure(Exception e) { Log.e("StatsActivity", "Failed to get completed tasks", e); }
                    });

                    taskRepository.getCompletedTasksCountByCategory(userId, new TaskRepository.OnStatisticsLoadedListener<Map<String, Pair<Integer, Integer>>>() {
                        @Override
                        public void onSuccess(Map<String, Pair<Integer, Integer>> categoryCountsAndColors) {
                            setupBarChart(categoryCountsAndColors);
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Log.e("StatsActivity", "Failed to get tasks by category and color", e);
                        }
                    });

                    taskRepository.getAverageDifficultyXpLast7Days(userId, new TaskRepository.OnStatisticsLoadedListener<Map<String, Double>>() {
                        @Override
                        public void onSuccess(Map<String, Double> avgDifficultyXpPerDay) {
                            setupAverageXpLineChart(avgDifficultyXpPerDay);
                        }
                        @Override
                        public void onFailure(Exception e) { Log.e("StatsActivity", "Failed to get avg XP last 7 days", e); }
                    });

                    taskRepository.getXpLast7Days(userId, user.getLevel(), new TaskRepository.OnStatisticsLoadedListener<Map<String, Double>>() {
                        @Override
                        public void onSuccess(Map<String, Double> xpPerDay) {
                            setupXpLast7DaysChart(xpPerDay);
                        }
                        @Override
                        public void onFailure(Exception e) { Log.e("StatsActivity", "Failed to get XP last 7 days", e); }
                    });

                } else {
                    Toast.makeText(StatisticsActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("StatsActivity", "Failed to get user data", e);
                Toast.makeText(StatisticsActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupPieChart(int completedCount, int inProgressCount, int canceledCount) {
        List<PieEntry> entries = new ArrayList<>();
        if (completedCount > 0) entries.add(new PieEntry(completedCount, "Completed"));
        if (inProgressCount > 0) entries.add(new PieEntry(inProgressCount, "In Progress"));
        if (canceledCount > 0) entries.add(new PieEntry(canceledCount, "Canceled"));

        PieDataSet dataSet = new PieDataSet(entries, "Task Status");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#4CAF50"));
        colors.add(Color.parseColor("#2196F3"));
        colors.add(Color.parseColor("#F44336"));
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        binding.chartTaskStatus.setData(data);
        binding.chartTaskStatus.getDescription().setEnabled(false);
        binding.chartTaskStatus.setDrawHoleEnabled(true);
        binding.chartTaskStatus.setHoleColor(Color.TRANSPARENT);
        binding.chartTaskStatus.setTransparentCircleRadius(61f);
        binding.chartTaskStatus.invalidate();
    }

    private void setupBarChart(Map<String, Pair<Integer, Integer>> categoryCountsAndColors) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String, Pair<Integer, Integer>> entry : categoryCountsAndColors.entrySet()) {
            String categoryName = entry.getKey();
            int count = entry.getValue().first;
            int color = entry.getValue().second;

            entries.add(new BarEntry(i, count));
            labels.add(categoryName);
            colors.add(color);
            i++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        binding.chartCategoryTasks.setData(data);

        binding.chartCategoryTasks.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.chartCategoryTasks.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.chartCategoryTasks.getXAxis().setGranularity(1f);
        binding.chartCategoryTasks.getXAxis().setDrawGridLines(false);
        binding.chartCategoryTasks.getXAxis().setLabelCount(labels.size());
        binding.chartCategoryTasks.getXAxis().setLabelRotationAngle(-45f);

        binding.chartCategoryTasks.getAxisLeft().setAxisMinimum(0f);
        binding.chartCategoryTasks.getAxisRight().setEnabled(false);
        binding.chartCategoryTasks.getDescription().setEnabled(false);
        binding.chartCategoryTasks.animateY(1000);
        binding.chartCategoryTasks.invalidate();
    }

    // --- POMOĆNE METODE ---
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

    // --- METODE ZA LINIJSKE GRAFIKONE ---

    private void setupAverageXpLineChart(Map<String, Double> avgDifficultyXpPerDay) {
        LineChart chart = binding.chartAverageDifficultyXp;

        if (avgDifficultyXpPerDay == null || avgDifficultyXpPerDay.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No data to display.");
            chart.setNoDataTextColor(Color.BLACK);
            chart.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;

        for (Map.Entry<String, Double> entry : avgDifficultyXpPerDay.entrySet()) {
            entries.add(new Entry(i, entry.getValue().floatValue()));
            labels.add(entry.getKey());
            i++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setColor(Color.parseColor("#2196F3"));
        dataSet.setCircleColor(Color.parseColor("#2196F3"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawValues(true);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f);

        // Podešavanje Y-ose
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(25f);

        chart.getAxisLeft().setAxisMinimum(0f);

        LimitLine veryEasy = new LimitLine(2f, "Very Easy");
        veryEasy.setLineColor(Color.GREEN);
        veryEasy.setLineWidth(2f);
        veryEasy.setTextColor(Color.BLACK);

        LimitLine easy = new LimitLine(4f, "Easy");
        easy.setLineColor(Color.BLUE);
        easy.setLineWidth(2f);
        easy.setTextColor(Color.BLACK);

        LimitLine hard = new LimitLine(7f, "Hard");
        hard.setLineColor(Color.MAGENTA);
        hard.setLineWidth(2f);
        hard.setTextColor(Color.BLACK);

        LimitLine extreme = new LimitLine(20f, "Extremely Hard");
        extreme.setLineColor(Color.RED);
        extreme.setLineWidth(2f);
        extreme.setTextColor(Color.BLACK);

        chart.getAxisLeft().addLimitLine(veryEasy);
        chart.getAxisLeft().addLimitLine(easy);
        chart.getAxisLeft().addLimitLine(hard);
        chart.getAxisLeft().addLimitLine(extreme);

        chart.getAxisRight().setEnabled(false);

        chart.getDescription().setEnabled(false);
        chart.animateX(1000);
        chart.invalidate();
    }

    private void setupXpLast7DaysChart(Map<String, Double> xpPerDay) {
        LineChart chart = binding.chartXpLast7Days;

        if (xpPerDay == null || xpPerDay.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No data to display.");
            chart.setNoDataTextColor(Color.BLACK);
            chart.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;

        for (Map.Entry<String, Double> entry : xpPerDay.entrySet()) {
            entries.add(new Entry(i, entry.getValue().floatValue()));
            labels.add(entry.getKey());
            i++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setCircleColor(Color.parseColor("#4CAF50"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawValues(true);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f);

        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false);

        chart.getDescription().setEnabled(false);
        chart.animateX(1000);
        chart.invalidate();
    }
}