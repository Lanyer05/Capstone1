package com.hcdc.capstone;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class Task extends BaseActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private final String[] tabTitles = new String[]{"Tab 1", "Tab 2"};

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        viewPager.setAdapter(new TabPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])).attach();

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation item clicks
                // ...
                return true;
            }
        });
    }

    private class TabPagerAdapter extends FragmentStateAdapter {

        public TabPagerAdapter(Task activity) {
            super(activity);
        }

        @Override
        public int getItemCount() {
            return tabTitles.length;
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new taskFragment();
                case 1:
                    return new userTaskFragment();
                default:
                    return null;
            }
        }
    }
}
