package com.sohan.connectsphere;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AlumniDashboardActivity extends AppCompatActivity {

    private EditText etSessionTitle, etMeetLink;
    private EditText etResourceTitle, etResourceDescription, etResourceLink;
    private Button btnStartSession, btnStopSession, btnAddResource, btnPublicChat, btnLogout;
    private Spinner spinnerResourceType;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String currentSessionId;
    private LinearLayout sessionListLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alumni_dashboard);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews();
        setupResourceTypeSpinner();
        setupClickListeners();
        loadSessions();
    }

    private void initializeViews() {
        etSessionTitle = findViewById(R.id.etSessionTitle);
        etMeetLink = findViewById(R.id.etMeetLink);
        etResourceTitle = findViewById(R.id.etResourceTitle);
        etResourceDescription = findViewById(R.id.etResourceDescription);
        etResourceLink = findViewById(R.id.etResourceLink);
        spinnerResourceType = findViewById(R.id.spinnerResourceType);
        btnStartSession = findViewById(R.id.btnStartSession);
        btnStopSession = findViewById(R.id.btnStopSession);
        btnAddResource = findViewById(R.id.btnAddResource);
        btnPublicChat = findViewById(R.id.btnPublicChat);
        btnLogout = findViewById(R.id.btnLogout);
        sessionListLayout = findViewById(R.id.sessionListLayout);
    }

    private void setupResourceTypeSpinner() {
        String[] resourceTypes = {"Document", "Video", "Link", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, resourceTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerResourceType.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnStartSession.setOnClickListener(v -> startSession());
        btnStopSession.setOnClickListener(v -> stopSession());
        btnAddResource.setOnClickListener(v -> addResource());
        btnPublicChat.setOnClickListener(v -> openPublicChat());
        btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void loadSessions() {
        sessionListLayout.removeAllViews();
        String currentUserId = mAuth.getCurrentUser().getUid();

        firestore.collection("Sessions")
            .whereEqualTo("alumniId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String sessionId = document.getId();
                        String title = document.getString("title");
                    String link = document.getString("link");
                        boolean isLive = Boolean.TRUE.equals(document.getBoolean("isLive"));

                        View sessionView = getLayoutInflater().inflate(R.layout.session_item2, sessionListLayout, false);
                        TextView tvSessionTitle = sessionView.findViewById(R.id.tvSessionTitle);
                        Button btnStartSession = sessionView.findViewById(R.id.btnStartSession);
                        Button btnStopSession = sessionView.findViewById(R.id.btnStopSession);
                    Button btnDeleteSession = sessionView.findViewById(R.id.btnDeleteSession);

                        tvSessionTitle.setText(title);

                        if (isLive) {
                            btnStartSession.setVisibility(View.GONE);
                            btnStopSession.setVisibility(View.VISIBLE);
                        btnDeleteSession.setVisibility(View.GONE);

                            btnStopSession.setOnClickListener(v -> stopSession(sessionId));
                        } else {
                            btnStartSession.setVisibility(View.VISIBLE);
                            btnStopSession.setVisibility(View.GONE);
                        btnDeleteSession.setVisibility(View.VISIBLE);

                            btnStartSession.setOnClickListener(v -> startSession(sessionId, link));
                        btnDeleteSession.setOnClickListener(v -> deleteSession(sessionId));
                        }

                        sessionListLayout.addView(sessionView);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load sessions", Toast.LENGTH_SHORT).show());
    }

    private void startSession(String sessionId, String link) {
        firestore.collection("Sessions").document(sessionId)
                .update("isLive", true)
            .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Session started", Toast.LENGTH_SHORT).show();
                    loadSessions();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Failed to start session", Toast.LENGTH_SHORT).show());
    }

    private void stopSession(String sessionId) {
        firestore.collection("Sessions").document(sessionId)
            .update("isLive", false)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Session stopped", Toast.LENGTH_SHORT).show();
                loadSessions();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Failed to stop session", Toast.LENGTH_SHORT).show());
    }

    private void deleteSession(String sessionId) {
        firestore.collection("Sessions").document(sessionId)
                .delete()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Session deleted", Toast.LENGTH_SHORT).show();
                loadSessions();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Failed to delete session", Toast.LENGTH_SHORT).show());
    }

    private void startSession() {
        String title = etSessionTitle.getText().toString().trim();
        String meetLink = etMeetLink.getText().toString().trim();

        if (title.isEmpty() || meetLink.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> session = new HashMap<>();
        session.put("title", title);
        session.put("link", meetLink);
        session.put("alumniId", mAuth.getCurrentUser().getUid());
        session.put("isLive", true);

        firestore.collection("Sessions")
            .add(session)
            .addOnSuccessListener(documentReference -> {
                currentSessionId = documentReference.getId();
                btnStartSession.setVisibility(View.GONE);
                btnStopSession.setVisibility(View.VISIBLE);
                etSessionTitle.setText("");
                etMeetLink.setText("");
                Toast.makeText(this, "Session started successfully", Toast.LENGTH_SHORT).show();
                loadSessions();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Failed to start session", Toast.LENGTH_SHORT).show());
    }

    private void stopSession() {
        if (currentSessionId == null) return;

        firestore.collection("Sessions").document(currentSessionId)
            .update("isLive", false)
            .addOnSuccessListener(aVoid -> {
                currentSessionId = null;
                btnStartSession.setVisibility(View.VISIBLE);
                btnStopSession.setVisibility(View.GONE);
                etSessionTitle.setText("");
                etMeetLink.setText("");
                Toast.makeText(this, "Session stopped", Toast.LENGTH_SHORT).show();
                    loadSessions();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Failed to stop session", Toast.LENGTH_SHORT).show());
    }

    private void addResource() {
        String title = etResourceTitle.getText().toString().trim();
        String description = etResourceDescription.getText().toString().trim();
        String link = etResourceLink.getText().toString().trim();
        String type = spinnerResourceType.getSelectedItem().toString();

        if (title.isEmpty() || description.isEmpty() || link.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> resource = new HashMap<>();
        resource.put("title", title);
        resource.put("description", description);
        resource.put("link", link);
        resource.put("type", type);
        resource.put("alumniId", mAuth.getCurrentUser().getUid());
        resource.put("timestamp", System.currentTimeMillis());
        resource.put("status", "active");
        resource.put("createdBy", mAuth.getCurrentUser().getUid());

        firestore.collection("Resources")
            .add(resource)
            .addOnSuccessListener(documentReference -> {
                etResourceTitle.setText("");
                etResourceDescription.setText("");
                etResourceLink.setText("");
                Toast.makeText(this, "Resource added successfully", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to add resource: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("AlumniDashboard", "Error adding resource: " + e.getMessage());
            });
    }

    private void openPublicChat() {
        Intent intent = new Intent(this, PublicChatActivity.class);
        startActivity(intent);
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
