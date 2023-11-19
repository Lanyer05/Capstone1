package com.hcdc.capstone.accounthandling;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.PasswordTransformationMethod;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hcdc.capstone.BaseActivity;
import com.hcdc.capstone.Homepage;
import com.hcdc.capstone.R;

public class LoginActivity extends BaseActivity {

    private FirebaseAuth auth;
    private Button loginBttn;
    private EditText loginEmail;
    private TextView signupRedirect;
    private FirebaseFirestore fstore;
    private ProgressDialog progressDialog;

    ToggleButton togglePassword ;
    EditText passwordEditText ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        togglePassword = findViewById(R.id.togglePassword);
        passwordEditText = findViewById(R.id.login_password);
        loginEmail = findViewById(R.id.login_email);

        signupRedirect = findViewById(R.id.singupRedirect);
        loginBttn = findViewById(R.id.loginbtn);
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage(" Logging in... ");
        progressDialog.setCancelable(false);

        loginBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                String email = loginEmail.getText().toString();
                String pass = passwordEditText.getText().toString();

                if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (!pass.isEmpty()) {
                        auth.signInWithEmailAndPassword(email, pass)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        // Check if the user is approved and email is verified
                                        FirebaseUser user = auth.getCurrentUser();
                                        if (user != null && user.isEmailVerified()) {
                                            progressDialog.dismiss();
                                            Toast.makeText(LoginActivity.this, " Login Successful ", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(LoginActivity.this, Homepage.class));
                                            finish();
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(LoginActivity.this, " Your email is not verified. Please check your email for verification. ", Toast.LENGTH_SHORT).show();
                                            auth.signOut();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, " Please enter correct password ", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        progressDialog.dismiss();
                        passwordEditText.setError(" Password cannot be empty ");
                    }
                } else if (email.isEmpty()) {
                    progressDialog.dismiss();
                    loginEmail.setError(" Email Cannot be empty ");
                } else {
                    progressDialog.dismiss();
                    loginEmail.setError(" Please enter valid email ");
                }
            }
        });

        signupRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        togglePassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            // Move the cursor to the end of the text to maintain cursor position
            passwordEditText.setSelection(passwordEditText.getText().length());
        });
    }
}
