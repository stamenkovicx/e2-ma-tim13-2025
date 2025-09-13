package com.example.myapplication.presentation.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.myapplication.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TaskViewerActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_viewer);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        TaskPagerAdapter adapter = new TaskPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Lista");
                            break;
                        case 1:
                            tab.setText("Kalendar");
                            break;
                        case 2:
                            tab.setText("Pojedinačni zadatak");
                            break;
                    }
                }).attach();
    }

    private static class TaskPagerAdapter extends FragmentStateAdapter {

        public TaskPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new TaskListFragment(); // Prikazuje listu svih zadataka
                case 1:
                    return new TaskCalendarFragment();
                case 2:
                    // Prazan fragment za sada
                    return new Fragment();
                default:
                    return new Fragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3; // Broj tabova
        }
    }
}