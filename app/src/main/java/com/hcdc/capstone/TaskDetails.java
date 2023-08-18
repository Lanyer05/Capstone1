package com.hcdc.capstone;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

                // Create a batch object
                WriteBatch batch = firestore.batch();

                // Update the corresponding task's isAccepted field in the "tasks" collection
                firestore.collection("tasks")
                        .whereEqualTo("taskName", tskTitle.getText().toString())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                                // Update the isAccepted field using the batch
                                batch.update(documentSnapshot.getReference(), "isAccepted", true);

                                // Store accepted task details in user_acceptedTask collection
                                Map<String, Object> userTaskAccepted = new HashMap<>();
                                userTaskAccepted.put("taskName", tskTitle.getText().toString());
                                userTaskAccepted.put("description", tskDesc.getText().toString());
                                userTaskAccepted.put("location", tskLoc.getText().toString());
                                userTaskAccepted.put("points", tskPoint.getText().toString());
                                userTaskAccepted.put("isAccepted", true);
                                userTaskAccepted.put("acceptedBy", uID);

                                if (documentSnapshot.contains("timeFrame")) {
                                    userTaskAccepted.put("timeFrame", documentSnapshot.get("timeFrame"));
                                }

                                // Add the userTaskAccepted document to the batch
                                batch.set(firestore.collection("user_acceptedTask").document(), userTaskAccepted);

                                // Commit the batch
                                batch.commit()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Batch write successful");

                                            // After batch write, you can update your UI or take further actions
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Batch write failed", e);
                                        });
                            }

                            Toast.makeText(getApplicationContext(),"Task Accepted",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(),Task.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error getting tasks for update", e);
                            Toast.makeText(getApplicationContext(),"Error in accepting task! Contact Developer: JEKZXC",Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}
