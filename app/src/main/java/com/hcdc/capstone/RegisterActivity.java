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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    private FirebaseAuth auth;
    private FirebaseFirestore fstore;

    private EditText regName, regEmail, regBrgy, regPassword, regConfirmpass;

    private Button regButton;
    private TextView regRedirect;
    private  String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        regName= findViewById(R.id.register_username);
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
                    regName.setError("Name cannot be empty");
                    return;
                }
                if (Remail.isEmpty()) {
                    regEmail.setError("Email cannot be empty");
                    return;
                }
                if (Rbrgy.isEmpty()) {
                    regBrgy.setError("Barangay cannot be empty");
                    return;
                }
                if (Rpass.isEmpty()) {
                    regPassword.setError("Password cannot be empty");
                    return;
                }
                if (!Rpass.equals(regConfirmpass.getText().toString())) {
                    regConfirmpass.setError("Passwords do not match");
                    return;
                }

                auth.createUserWithEmailAndPassword(Remail, Rpass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Register Successful", Toast.LENGTH_SHORT).show();
                            userID = auth.getCurrentUser().getUid();
                            DocumentReference documentReference = fstore.collection("users").document(userID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("name", regName);
                            user.put("Barangay", regBrgy);
                            user.put("email", regEmail);
                            user.put("Uid", userID);

                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "onSuccess: user profile is created for" + userID);
                                }
                            });
                            startActivity(new Intent(RegisterActivity.this, Homepage.class));
                        } else {
                            Toast.makeText(RegisterActivity.this, "Sign Up Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
            }
        }

