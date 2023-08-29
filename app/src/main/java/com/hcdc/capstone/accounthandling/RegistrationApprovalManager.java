package com.hcdc.capstone.accounthandling;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class RegistrationApprovalManager {

    private FirebaseFirestore firestore;

    public RegistrationApprovalManager() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void startListeningForApprovals() {
        Query query = firestore.collection("registration_requests").whereEqualTo("isApproved", true);
        query.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                // Handle error
                return;
            }

            for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                if (dc.getType() == DocumentChange.Type.ADDED) {
                    moveUserToUsersCollection(dc.getDocument().getId());
                }
            }
        });
    }

    private void moveUserToUsersCollection(String userID) {
        firestore.collection("registration_requests").document(userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    firestore.collection("users").document(userID).set(task.getResult().getData());
                    firestore.collection("registration_requests").document(userID).delete();
                }
            }
        });
    }
}
