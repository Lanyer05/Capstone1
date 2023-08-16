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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
                Intent intent = new Intent(getApplicationContext(),Task.class);
                startActivity(intent);
                finish();
            }
        });

        acceptTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uID = auth.getCurrentUser().getUid();
                DocumentReference documentReference = firestore.collection("acceptedTasks").document(uID);
                Map<String, Object> userTaskAccepted = new HashMap<>();
                userTaskAccepted.put("taskTitle", tskTitle.getText().toString());
                userTaskAccepted.put("acceptedBy", uID);

                documentReference.set(userTaskAccepted).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Task Accepted");
                    }
                });
            }
        });
    }
}