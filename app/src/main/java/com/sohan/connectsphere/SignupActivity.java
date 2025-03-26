package com.sohan.connectsphere;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnSignup, btnLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance(); // 🔥 Firestore Initialized

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignup = findViewById(R.id.btnSignup);
        btnLogin = findViewById(R.id.btnLogin);

        btnSignup.setOnClickListener(v -> createStudentAccount());

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, MainActivity.class));
            finish();
        });
    }

    private void createStudentAccount() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserData(user.getUid(), name, email);  // Save user data in Firestore
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(SignupActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e("SIGNUP_ERROR", "Error creating user: " + errorMessage);
                    }
                });
    }

    private void saveUserData(String userId, String name, String email) {
        // Firestore Data Format
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("userType", "Student");
        userData.put("isPremium",false);

        firestore.collection("Users")
                .document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignupActivity.this, "Failed to save user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("DATABASE_ERROR", "Failed to save user data for UID: " + userId, e);
                });
    }
}
