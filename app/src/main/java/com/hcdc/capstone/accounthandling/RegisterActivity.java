package com.hcdc.capstone.accounthandling;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hcdc.capstone.BaseActivity;
import com.hcdc.capstone.R;

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
    int userpoints;

    ToggleButton firstpassword, secondpassword;

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
        firstpassword = findViewById(R.id.togglePasswordFirst);
        secondpassword = findViewById(R.id.togglePasswordSecond);

        firstpassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                regPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                regPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            // Move the cursor to the end of the text to maintain cursor position
            regPassword.setSelection(regPassword.getText().length());
        });

        secondpassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                regConfirmpass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                regConfirmpass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            // Move the cursor to the end of the text to maintain cursor position
            regConfirmpass.setSelection(regConfirmpass.getText().length());
        });



        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Rname = regName.getText().toString();
                String Remail = regEmail.getText().toString().trim();
                String Rbrgy = regBrgy.getText().toString();
                String Rpass = regPassword.getText().toString().trim();
                String CRpass = regConfirmpass.getText().toString().trim();

                if (Rpass.equals(CRpass)) {
                auth.createUserWithEmailAndPassword(Remail, Rpass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, " Registration Successful ", Toast.LENGTH_SHORT).show();
                            userID = auth.getCurrentUser().getUid();
                            DocumentReference documentReference = fstore.collection("users").document(userID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("name", regName.getText().toString());
                            user.put("Barangay", regBrgy.getText().toString());
                            user.put("email", regEmail.getText().toString());
                            user.put("Uid", userID);
                            user.put("userpoints", userpoints);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "Success: user profile is created for" + userID);
                                }
                            });
                            sendEmailVerification();

                            // Show a message to the user that their registration is pending approval
                            Toast.makeText(RegisterActivity.this, " Registration request sent for approval. Please check your email for verification. ", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        } else {
                            Toast.makeText(RegisterActivity.this, " Sign Up Failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
                else if (Rname.isEmpty() || Rbrgy.isEmpty() || Remail.isEmpty() || Rpass.isEmpty() || CRpass.isEmpty())
                {
                    regName.setError("required");
                    regEmail.setError("required");
                    regBrgy.setError("required");
                    regPassword.setError("required");
                    regConfirmpass.setError("required");
                }
                else {
                    regPassword.setError("Passwords do not match");
                    regConfirmpass.setError("Passwords do not match");
                    Toast.makeText(getApplicationContext(),"Passwords not matched",Toast.LENGTH_SHORT).show();
                }
            }
        });

        regRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            DocumentReference registrationRequestRef = fstore.collection("users").document(currentUser.getUid());
            registrationRequestRef.update("isApproved", true)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG, "isApproved field updated to true");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error updating isApproved field: " + e.getMessage());
                        }
                    });
        }
    }

    // Function to send email verification
    private void sendEmailVerification() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Email verification sent.");
                            } else {
                                Log.e(TAG, "Error sending email verification", task.getException());
                            }
                        }
                    });
        }
    }
}
