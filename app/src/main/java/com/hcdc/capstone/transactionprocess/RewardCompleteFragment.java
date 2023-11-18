package com.hcdc.capstone.transactionprocess;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hcdc.capstone.R;
import com.hcdc.capstone.adapters.RewardCompleteAdapter;
import com.hcdc.capstone.rewardprocess.Coupons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RewardCompleteFragment extends Fragment {

    private RecyclerView recyclerView;
    private RewardCompleteAdapter adapter;
    private FirebaseAuth auth;
    private ArrayList<Coupons> couponList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new RewardCompleteAdapter(requireContext(), new ArrayList<>());
        auth = FirebaseAuth.getInstance();
        couponList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.completedreward_fragment, container, false);

        recyclerView = view.findViewById(R.id.completedrewardfragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        fetchCompletedRewardsFromFirestore();

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchCompletedRewardsFromFirestore() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.e("userCoupons", "User not authenticated");
            return;
        }
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String currentUserUID = auth.getCurrentUser().getUid();
        Context context = getContext();
        if (context == null) {
            Log.e("userCoupons", "Context is null");
            return;
        }
        firestore.collection("coupons")
                .whereEqualTo("isClaimed", true)
                .whereEqualTo("userId", currentUserUID)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("userCoupons", "Listen failed.", error);
                        return;
                    }
                    if (value != null) {
                        for (QueryDocumentSnapshot document : value) {
                            Log.d("userCoupons", "Document data: " + document.getData());
                            Coupons coupon = new Coupons(
                                    document.getString("userId"),
                                    document.getString("couponCode"),
                                    parseSelectedItems(document.get("selectedItems")),
                                    getTimestampFromDocument(document, "claimDateTime")
                            );
                            couponList.add(coupon);
                            Log.d("CouponListSize", "Size: " + couponList.size());
                        }
                        adapter.setFinRewardList(couponList);
                        adapter.notifyDataSetChanged();
                    }
                });
    }


    @NonNull
    private List<Coupons.SelectedItems> parseSelectedItems(Object selectedItemsObj) {
        List<Coupons.SelectedItems> selectedItems = new ArrayList<>();
        if (selectedItemsObj instanceof List) {
            List<Map<String, Object>> selectedItemsList = (List<Map<String, Object>>) selectedItemsObj;
            for (Map<String, Object> itemData : selectedItemsList) {
                String rewardId = (String) itemData.get("rewardId");
                long selectedQuantity = (long) itemData.get("selectedQuantity");
                selectedItems.add(new Coupons.SelectedItems(rewardId, (int) selectedQuantity));
            }
        }
        return selectedItems;
    }

    @Nullable
    private Timestamp getTimestampFromDocument(@NonNull QueryDocumentSnapshot document, String fieldName) {
        Object timestampObj = document.get(fieldName);
        if (timestampObj instanceof Timestamp) {
            return (Timestamp) timestampObj;
        }
        return null;
    }
}
