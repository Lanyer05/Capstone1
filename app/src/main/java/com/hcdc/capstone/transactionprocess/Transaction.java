package com.hcdc.capstone.transactionprocess;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hcdc.capstone.BaseActivity;
import com.hcdc.capstone.Homepage;
import com.hcdc.capstone.R;
import com.hcdc.capstone.rewardprocess.Reward;
import com.hcdc.capstone.taskprocess.Task;

import java.util.ArrayList;
import java.util.List;

public class Transaction extends BaseActivity {

    private BottomNavigationView bottomNavigationView;
    private ViewPager2 viewPager;
    private MyFragmentStateAdapter fragmentStateAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        viewPager = findViewById(R.id.viewPager);

        fragmentStateAdapter = new MyFragmentStateAdapter(this);
        viewPager.setAdapter(fragmentStateAdapter);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Completed Tasks");
            } else if (position == 1) {
                tab.setText("Completed Rewards");
            }
            // Set icons here if needed: tab.setIcon(R.drawable.ic_tab_icon);
        }).attach();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        navigateToActivity(Homepage.class);
                        return true;

                    case R.id.action_task:
                        navigateToActivity(Task.class);
                        return true;

                    case R.id.action_reward:
                        navigateToActivity(Reward.class);
                        return true;

                    case R.id.action_transaction:
                        return true;
                }
                return false;
            }
        });
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private static class MyFragmentStateAdapter extends FragmentStateAdapter {

        private final List<Fragment> fragments = new ArrayList<>();

        public MyFragmentStateAdapter(FragmentActivity fragmentActivity) {
            super(fragmentActivity);

            // Add your fragments to the list here
            fragments.add(new TaskComplete());
            fragments.add(new RewardComplete());
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }
    }
}
