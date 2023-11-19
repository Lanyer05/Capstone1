package com.hcdc.capstone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hcdc.capstone.adapters.AnnouncementsAdapter;
import com.hcdc.capstone.rewardprocess.Reward;
import com.hcdc.capstone.taskprocess.Task;
import com.hcdc.capstone.taskprocess.TaskProgress;
import com.hcdc.capstone.transactionprocess.Transaction;

import java.util.ArrayList;
import java.util.Map;

public class Homepage extends BaseActivity {

    private static final String TIMER_PREFS = "TimerPrefs";
    private static final String PREF_TIMER_RUNNING = "timerRunning";

    private BottomNavigationView bottomNavigationView;
    private TextView pointsSystemTextView;
    private ImageView profileImageView;

    private RecyclerView recyclerView;
    private AnnouncementsAdapter adapter;
    private FirebaseFirestore db;

    private boolean isLastItemReached = false;
    private ArrayList<AnnouncementModel> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Check if the timer is running
        boolean isTimerRunning = checkTimerRunning();

        recyclerView = findViewById(R.id.recycler_announce);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        arrayList = new ArrayList<>();
        adapter = new AnnouncementsAdapter(this,arrayList);
        adapter.setRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);
        adapter.startAutoScroll();
        db = FirebaseFirestore.getInstance();

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        pointsSystemTextView = findViewById(R.id.points_system);
        profileImageView = findViewById(R.id.profile);

        bottomNavigationView.setSelectedItemId(R.id.action_home);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.action_home) {
                    return true;

                } else if (itemId == R.id.action_task) {
                    navigateToActivity(Task.class);
                    return true;
                } else if (itemId == R.id.action_reward) {
                    navigateToActivity(Reward.class);
                    return true;
                } else if (itemId == R.id.action_transaction) {
                    navigateToActivity(Transaction.class);
                    return true;
                }

                return false;
            }
        });

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToActivity(Profile_Activity.class);
            }
        });

        if (isTimerRunning) {
            navigateToTaskProgress();
        } else {
            fetchAndDisplayCurrentUserPoints();
        }
        retrieveAndStoreFCMToken();
        fetchAnnouncements();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount();

                if (lastVisibleItemPosition == totalItemCount - 1) {
                    // Last item reached, set the flag
                    isLastItemReached = true;
                } else {
                    // Reset the flag when scrolling to other items
                    isLastItemReached = false;
                }
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopNotificationTimer();
        adapter.stopAutoScroll();
    }
    private void stopNotificationTimer() {
        Intent stopTimerIntent = new Intent("StopTimerService");
        sendBroadcast(stopTimerIntent);
    }
    private boolean checkTimerRunning() {
        SharedPreferences sharedPreferences = getSharedPreferences(TIMER_PREFS, MODE_PRIVATE);
        return sharedPreferences.getBoolean(PREF_TIMER_RUNNING, false);
    }

    private void navigateToTaskProgress() {
        if (checkTimerRunning()) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String currentUserUID = auth.getCurrentUser().getUid();

            db.collection("user_acceptedTask")
                    .whereEqualTo("acceptedBy", currentUserUID)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                // Retrieve the accepted task details
                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                String taskName = documentSnapshot.getString("taskName");
                                Long taskPointsLong = documentSnapshot.getLong("points");
                                String taskPoints = String.valueOf(taskPointsLong) + " points";
                                String taskDescription = documentSnapshot.getString("description");
                                String taskLocation = documentSnapshot.getString("location");
                                int finalTaskHours = 0;
                                int finalTaskMinutes = 0;

                                if (documentSnapshot.contains("timeFrame")) {
                                    Map<String, Object> timeFrameMap = (Map<String, Object>) documentSnapshot.get("timeFrame");

                                    if (timeFrameMap != null && timeFrameMap.containsKey("hours") && timeFrameMap.containsKey("minutes")) {
                                        finalTaskHours = ((Long) timeFrameMap.get("hours")).intValue();
                                        finalTaskMinutes = ((Long) timeFrameMap.get("minutes")).intValue();
                                    }
                                }
                                Long maxUsers = documentSnapshot.getLong("maxUsers");

                                // Pass data to the TaskProgress activity
                                Intent intent = new Intent(Homepage.this, TaskProgress.class);
                                intent.putExtra("taskName", taskName);
                                intent.putExtra("taskPoints", taskPoints);
                                intent.putExtra("taskDescription", taskDescription);
                                intent.putExtra("taskLocation", taskLocation);
                                intent.putExtra("timeFrameHours", finalTaskHours);
                                intent.putExtra("timeFrameMinutes", finalTaskMinutes);
                                intent.putExtra("maxUsers", maxUsers);

                                startActivity(intent);
                            } else {
                                Log.d("user no task","have no tasks");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Homepage.this, " Failed to retrieve task details. ", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
        }
    }

    private void fetchAndDisplayCurrentUserPoints() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference userRef = firestore.collection("users").document(currentUserId);

        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getApplicationContext(), "Error Occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    if (documentSnapshot.contains("userpoints")) {
                        Long userPoints = documentSnapshot.getLong("userpoints");
                        if (userPoints != null) {
                            pointsSystemTextView.setText("" + userPoints);
                        }
                    }
                }
            }
        });
    }

    private void retrieveAndStoreFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        updateFCMTokenInFirestore(token);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error Occurred while retrieving FCM token", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateFCMTokenInFirestore(String token) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(currentUserId);
        userRef.update("fcmToken", token)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error Occurred while storing FCM token", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private void fetchAnnouncements() {
        db.collection("announcements").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Clear the list and add the fetched data
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            AnnouncementModel announcement = document.toObject(AnnouncementModel.class);
                            arrayList.add(announcement);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the failure to fetch data
                    }
                });

    }

}