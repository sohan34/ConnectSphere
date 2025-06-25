package com.sohan.connectsphere;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private Button btnPremium, btnLogout;
    private ViewFlipper viewFlipper;
    private LinearLayout alumniListLayout;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private List<ListenerRegistration> listeners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        btnPremium = findViewById(R.id.btnPremium);
        btnLogout = findViewById(R.id.btnLogout);
        viewFlipper = findViewById(R.id.viewFlipper);
        alumniListLayout = findViewById(R.id.alumniListLayout);

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        listeners = new ArrayList<>();

        btnPremium.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ResourcesActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> logoutUser());

        setupRealTimeListeners();
    }

    private void setupRealTimeListeners() {
        // Listen for session changes
        ListenerRegistration sessionsListener = firestore.collection("Sessions")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error listening to sessions", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        viewFlipper.removeAllViews();
                        loadSessions();
                    }
                });
        listeners.add(sessionsListener);

        // Listen for alumni changes
        ListenerRegistration alumniListener = firestore.collection("Users")
                .whereEqualTo("userType", "Alumni")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error listening to alumni", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        alumniListLayout.removeAllViews();
                        loadAlumniList();
                    }
                });
        listeners.add(alumniListener);
    }

    private void logoutUser() {
        mAuth.signOut();
        // Remove all listeners
        for (ListenerRegistration listener : listeners) {
            listener.remove();
        }
        // Clear all activities and go to MainActivity
        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Load Sessions with Google Meet Integration
    private void loadSessions() {
        firestore.collection("Sessions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            boolean isLive = Boolean.TRUE.equals(document.getBoolean("isLive"));
                            String link = document.getString("link");
                            String alumniId = document.getString("alumniId");

                            // Inflate session layout
                            View sessionView = getLayoutInflater().inflate(R.layout.session_item, viewFlipper, false);

                            TextView tvSessionTitle = sessionView.findViewById(R.id.tvSessionTitle);
                            TextView tvAlumniName = sessionView.findViewById(R.id.tvAlumniName);
                            Button btnJoinNow = sessionView.findViewById(R.id.btnJoinNow);

                            // Set session title
                            tvSessionTitle.setText(title);

                            // Fetch and set alumni name
                            if (alumniId != null) {
                                firestore.collection("Users").document(alumniId)
                                        .get()
                                        .addOnSuccessListener(userDoc -> {
                                            String alumniName = userDoc.getString("name");
                                            if (alumniName != null) {
                                                tvAlumniName.setText("By " + alumniName);
                                            }
                                        });
                            }

                            // Show join button if live
                            if (isLive) {
                                btnJoinNow.setVisibility(View.VISIBLE);
                                btnJoinNow.setOnClickListener(v -> joinGoogleMeet(link));
                            }

                            viewFlipper.addView(sessionView);
                        }
                    } else {
                        Toast.makeText(DashboardActivity.this, "Failed to load sessions", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to Join Google Meet
    private void joinGoogleMeet(String meetLink) {
        if (!meetLink.startsWith("http://") && !meetLink.startsWith("https://")) {
            meetLink = "https://" + meetLink;
        }

        if (!meetLink.contains("?")) {
            meetLink += "?hs=1";
        } else {
            meetLink += "&hs=1";
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(meetLink));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setPackage("com.android.chrome");

        try {
            startActivity(intent);
        } catch (Exception e) {
            intent.setPackage(null);
            startActivity(intent);
        }
    }

    // Load Alumni List
    private void loadAlumniList() {
        firestore.collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot userSnapshot : task.getResult()) {
                    String userType = userSnapshot.getString("userType");

                    if ("Alumni".equals(userType)) {
                        String name = userSnapshot.getString("name");
                        String company = userSnapshot.getString("company");
                        String alumniId = userSnapshot.getId();

                        // Inflate the custom alumni layout
                        View alumniView = getLayoutInflater().inflate(R.layout.alumni_item, alumniListLayout, false);

                        TextView tvAlumniName = alumniView.findViewById(R.id.tvAlumniName);
                        TextView tvAlumniCompany = alumniView.findViewById(R.id.tvAlumniCompany);
                        Button btnChatAlumni = alumniView.findViewById(R.id.btnChatAlumni);

                        tvAlumniName.setText(name);
                        tvAlumniCompany.setText(company);

                        btnChatAlumni.setOnClickListener(v -> {
                            Intent intent = new Intent(DashboardActivity.this, ChatActivity.class);
                            intent.putExtra("alumniId", alumniId);
                            intent.putExtra("alumniName", name);
                            startActivity(intent);
                        });

                        alumniListLayout.addView(alumniView);
                    }
                }
            } else {
                Toast.makeText(DashboardActivity.this, "Failed to load alumni", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove all listeners when activity is destroyed
        for (ListenerRegistration listener : listeners) {
            listener.remove();
        }
    }
}
