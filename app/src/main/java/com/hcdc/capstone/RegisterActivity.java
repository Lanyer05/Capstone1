package com.hcdc.capstone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends BaseActivity {

    public static final String TAG = "TAG";
    private FirebaseAuth auth;
    private FirebaseFirestore fstore;

    private EditText regName, regEmail, regBrgy, regPassword, regConfirmpass;

    private Button regButton;
    private TextView regRedirect;
    private String userID;

    private RegistrationApprovalManager approvalManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        regName = findViewById(R.id.register_username);
        regEmail = findViewById(R.id.register_email);
        regBrgy = findViewById(R.id.register_barangay);
        regPassword = findViewById(R.id.register_password);
        regConfirmpass = findViewById(R.id.confirmReg_password);

        regButton = findViewById(R.id.registerbtn);
        regRedirect = findViewById(R.id.registerRedirect);

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Rname = regName.getText().toString();
                String Remail = regEmail.getText().toString().trim();
                String Rbrgy = regBrgy.getText().toString();
                String Rpass = regPassword.getText().toString().trim();

                if (Rname.isEmpty()) {
                    regName.setError(" Name cannot be empty ");
                    return;
                }
                if (Remail.isEmpty()) {
                    regEmail.setError(" Email cannot be empty ");
                    return;
                }
                if (Rbrgy.isEmpty()) {
                    regBrgy.setError(" Barangay cannot be empty ");
                    return;
                }
                if (Rpass.isEmpty()) {
                    regPassword.setError(" Password cannot be empty ");
                    return;
                }
                if (!Rpass.equals(regConfirmpass.getText().toString())) {
                    regConfirmpass.setError(" Passwords do not match ");
                    return;
                }
                if(!Remail.contains("@gmail.com")){
                    regEmail.setError("  Only accepts with gmail only  ");
                    return;
                }

                auth.createUserWithEmailAndPassword(Remail, Rpass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Register Successful", Toast.LENGTH_SHORT).show();
                            userID = auth.getCurrentUser().getUid();

                            // Initialize a WriteBatch
                            WriteBatch batch = fstore.batch();

                            // Create a new user document
                            DocumentReference userRef = fstore.collection("users").document(userID);
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", Rname);
                            userData.put("Barangay", Rbrgy);
                            userData.put("email", Remail);
                            userData.put("isApproved", false);
                            batch.set(userRef, userData);

                            // Create a registration request document
                            DocumentReference registrationRequestRef = fstore.collection("registration_requests").document(userID);
                            Map<String, Object> registrationRequestData = new HashMap<>();
                            registrationRequestData.put("name", Rname);
                            registrationRequestData.put("Barangay", Rbrgy);
                            registrationRequestData.put("email", Remail);
                            registrationRequestData.put("isApproved", false);
                            registrationRequestData.put("Uid", userID);
                            batch.set(registrationRequestRef, registrationRequestData);

                            // Commit the batch
                            batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "Batch write successful: user registration request saved for " + userID);
                                    // Show a message to the user that their registration is pending approval
                                    Toast.makeText(RegisterActivity.this, "Registration request sent for approval", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "Batch write failed: " + e.getMessage());
                                    // Handle batch write failure if needed
                                }
                            });

                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        } else {
                            Toast.makeText(RegisterActivity.this, "Sign Up Failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        regRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        // Start listening for registration approvals
        approvalManager = new RegistrationApprovalManager();
        approvalManager.startListeningForApprovals();
    }
}

