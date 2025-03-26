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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class DashboardActivity extends AppCompatActivity {

    private Button btnPremium;
    private ViewFlipper viewFlipper;
    private LinearLayout alumniListLayout;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        btnPremium = findViewById(R.id.btnPremium);
        viewFlipper = findViewById(R.id.viewFlipper);
        alumniListLayout = findViewById(R.id.alumniListLayout);

        firestore = FirebaseFirestore.getInstance();

        btnPremium.setOnClickListener(v ->
                Toast.makeText(this, "Premium features coming soon!", Toast.LENGTH_SHORT).show());

        loadSessions();
        loadAlumniList();
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

                            // Inflate session layout
                            View sessionView = getLayoutInflater().inflate(R.layout.session_item, viewFlipper, false);

                            TextView tvSessionTitle = sessionView.findViewById(R.id.tvSessionTitle);
                            Button btnJoinNow = sessionView.findViewById(R.id.btnJoinNow);

                            // Set session title
                            tvSessionTitle.setText(title);

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
                }).addOnFailureListener(e -> {
                    Toast.makeText(DashboardActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }



    // Method to Join Google Meet
    private void joinGoogleMeet(String meetLink) {
        // Ensure the URL is correctly formatted
        if (!meetLink.startsWith("http://") && !meetLink.startsWith("https://")) {
            meetLink = "https://" + meetLink;
        }

        // Append `?hs=1` to force web browser usage
        if (!meetLink.contains("?")) {
            meetLink += "?hs=1";
        } else {
            meetLink += "&hs=1";
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(meetLink));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);  // Ensures it targets web browsers only
        intent.setPackage("com.android.chrome"); // Forces Chrome if installed

        try {
            startActivity(intent);  // Attempt to open in Chrome
        } catch (Exception e) {
            // Fallback: Try to open in any available browser
            intent.setPackage(null);
            startActivity(intent);
        }
    }



    // Load Alumni List
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


}
