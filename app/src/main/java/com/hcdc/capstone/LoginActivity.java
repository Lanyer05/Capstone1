package com.hcdc.capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.text.method.PasswordTransformationMethod;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button loginBttn, login_gmail;
    private EditText loginEmail, loginPassword;
    private TextView signupRedirect;
    private FirebaseFirestore fstore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        signupRedirect = findViewById(R.id.singupRedirect);
        loginBttn = findViewById(R.id.loginbtn);

        loginPassword.setTransformationMethod(new PasswordTransformationMethod());
        loginBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = loginEmail.getText().toString();
                String pass = loginPassword.getText().toString();

                if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (!pass.isEmpty()) {
                        auth.signInWithEmailAndPassword(email, pass)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        // Check if the user is approved
                                        DocumentReference userRef = fstore.collection("users").document(auth.getCurrentUser().getUid());
                                        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document != null && document.exists()) {
                                                        Boolean isApproved = document.getBoolean("isApproved");
                                                        if (isApproved != null && isApproved.booleanValue()) {
                                                            // User is approved, allow login
                                                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(LoginActivity.this, Homepage.class));
                                                            finish();
                                                        } else {
                                                            // User is not approved yet
                                                            Toast.makeText(LoginActivity.this, "Your registration is pending approval by the admin", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        // Document does not exist or is null, handle accordingly
                                                        // For example, if the document doesn't exist, the user may not be registered properly.
                                                        Toast.makeText(LoginActivity.this, "User not found or registration data missing. Please register first.", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    // Task failed with an exception, handle accordingly
                                                    Toast.makeText(LoginActivity.this, "Error fetching user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(LoginActivity.this, "Please enter correct password", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        loginPassword.setError("Password cannot be empty");
                    }
                } else if (email.isEmpty()) {
                    loginEmail.setError("Email Cannot be empty");
                } else {
                    loginEmail.setError("Please enter valid email");
                }

            }
        });
        signupRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }
}
