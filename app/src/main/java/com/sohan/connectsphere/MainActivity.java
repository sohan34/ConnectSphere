package com.sohan.connectsphere;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {


    private EditText etEmail, etPassword;
    private Button btnLogin, btnForgotPassword, btnSignUp;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");


        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        btnSignUp = findViewById(R.id.btnSignUp); // Added Sign Up Button

        // Toggle Logic - Show/Hide Sign Up & Forgot Password


        btnLogin.setOnClickListener(v -> loginUser());

        btnForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(MainActivity.this, "Enter email to reset password", Toast.LENGTH_SHORT).show();
                return;
            }
            resetPassword(email);
        });

        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                            firestore.collection("Users").document(user.getUid()).get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful() && task1.getResult().exists()) {
                                            String userType = task1.getResult().getString("userType");
                                            Toast.makeText(MainActivity.this, "Logged in as " + userType, Toast.LENGTH_SHORT).show();
                                            if(userType.equalsIgnoreCase("Student")) {
                                                navigateToDashboard();
                                            }else{
                                                navigateToDashboard1();
                                            }
                                        } else {
                                            Toast.makeText(MainActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(MainActivity.this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Reset link sent to email", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToDashboard() {
        // Navigate to Dashboard
        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
        finish();
    }
    private void navigateToDashboard1() {
        // Navigate to Dashboard
        startActivity(new Intent(MainActivity.this, AlumniDashboardActivity.class));
        finish();
    }
}
