package com.hcdc.capstone.rewardprocess;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hcdc.capstone.R;
import com.hcdc.capstone.adapters.RewardCategoryItems;

import java.util.ArrayList;

public class RewardList extends AppCompatActivity {

    RecyclerView recyclerView;
    RewardCategoryItems rewardCategoryItems;
    ArrayList<RewardItems> rewardItemsArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_list);

        recyclerView = findViewById(R.id.rewardrecycler);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        rewardItemsArrayList = new ArrayList<>();
        rewardCategoryItems = new RewardCategoryItems(this, rewardItemsArrayList);
        recyclerView.setAdapter(rewardCategoryItems);

        // Fetch data from Firestore and set the adapter after successful retrieval
        fetchDataFromFirestore();
    }

    private void fetchDataFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference rewardsCollection = db.collection("rewards"); // Replace with your collection name

        rewardsCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Handle successful data retrieval
                    // Convert the QuerySnapshot to a list of RewardItems
                    ArrayList<RewardItems> rewardsList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        RewardItems reward = document.toObject(RewardItems.class);
                        rewardsList.add(reward);
                    }

                    // Update the RecyclerView adapter with the retrieved data
                    rewardCategoryItems.setRewardItems(rewardsList);

                    // Log the count of collected data
                    int itemCount = rewardsList.size();
                    Log.d("Firestore", "Collected " + itemCount + " items from Firestore");
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e("Firestore", "Error fetching data: " + e.getMessage());
                });
    }
}
