package com.hcdc.capstone;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class TaskDetails extends BaseActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private TextView tskTitle, tskPoint, tskDesc, tskLoc;

    private String uID;

    Button acceptTask, cancelTask;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        auth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth
        firestore = FirebaseFirestore.getInstance(); // Initialize FirebaseFirestore

        tskTitle = findViewById(R.id.tdTitle);
        tskDesc = findViewById(R.id.tdDesc);
        tskPoint = findViewById(R.id.tdPoints);
        tskLoc = findViewById(R.id.tdLocation);

        acceptTask = findViewById(R.id.tdAccept);
        cancelTask = findViewById(R.id.tdCancel);

        Bundle extra = getIntent().getExtras();
        String tTitle = extra.getString("tasktitle");
        String tDesc = extra.getString("taskdetails");
        String tPoints = extra.getString("taskpoint");
        String tLoc = extra.getString("tasklocation");

        tskTitle.setText(tTitle);
        tskDesc.setText(tDesc);
        tskLoc.setText(tLoc);
        tskPoint.setText(tPoints);

        cancelTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Task.class);
                startActivity(intent);
                finish();
            }
        });

        acceptTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uID = auth.getCurrentUser().getUid();

                // Check if the user has already accepted a task
                firestore.collection("user_acceptedTask")
                        .whereEqualTo("acceptedBy", uID)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                // User already has an accepted task, show a message or take appropriate action
                                Toast.makeText(TaskDetails.this, "  You have already accepted a task.  Complete it before accepting a new task.  ", Toast.LENGTH_SHORT).show();
                            } else {
                                firestore.collection("user_acceptedTask")
                                        .whereEqualTo("taskName", tskTitle.getText().toString())
                                        .whereEqualTo("acceptedBy", uID)
                                        .get()
                                        .addOnSuccessListener(querySnapshot -> {
                                            if (!querySnapshot.isEmpty()) {
                                                // User has already accepted this task, show a message or take appropriate action
                                                // For example: Toast.makeText(TaskDetails.this, "You have already accepted this task.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // User hasn't accepted this task yet, proceed to accept the new task
                                                Map<String, Object> userTaskAccepted = new HashMap<>();
                                                userTaskAccepted.put("taskName", tskTitle.getText().toString());
                                                userTaskAccepted.put("description", tskDesc.getText().toString());
                                                userTaskAccepted.put("location", tskLoc.getText().toString());
                                                userTaskAccepted.put("points", tskPoint.getText().toString());
                                                userTaskAccepted.put("isAccepted", true);
                                                userTaskAccepted.put("acceptedBy", uID);

                                                firestore.collection("tasks")
                                                        .whereEqualTo("taskName", tskTitle.getText().toString())
                                                        .whereEqualTo("isAccepted", false)
                                                        .get()
                                                        .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                                            if (!queryDocumentSnapshots2.isEmpty()) {
                                                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots2.getDocuments().get(0);
                                                                Map<String, Object> taskData = documentSnapshot.getData();

                                                                if (taskData.containsKey("timeFrame")) {
                                                                    userTaskAccepted.put("timeFrame", taskData.get("timeFrame"));
                                                                }

                                                                WriteBatch batch = firestore.batch();

                                                                // Add to user_acceptedTask collection
                                                                batch.set(firestore.collection("user_acceptedTask").document(), userTaskAccepted);

                                                                // Delete from tasks collection
                                                                batch.delete(documentSnapshot.getReference());

                                                                batch.commit()
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                Log.d(TAG, "Batch write successful");
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                Log.e(TAG, "Error executing batch write", e);
                                                                            }
                                                                        });
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e(TAG, "Error getting tasks for update", e);
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error checking accepted tasks", e);
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error checking accepted tasks", e);
                        });
            }
        });
    }
}
