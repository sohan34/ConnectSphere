package com.sohan.connectsphere;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AlumniDashboardActivity extends AppCompatActivity {

    private Button btnGroupChat, btnCreateSession;
    private EditText etSessionTitle, etSessionStartTime;
    private LinearLayout sessionListLayout;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alumni_dashboard);

        btnGroupChat = findViewById(R.id.btnGroupChat);
        btnCreateSession = findViewById(R.id.btnCreateSession);
        etSessionTitle = findViewById(R.id.etSessionTitle);
        etSessionStartTime = findViewById(R.id.etSessionStartTime);
        sessionListLayout = findViewById(R.id.sessionListLayout);

        firestore = FirebaseFirestore.getInstance();

        btnGroupChat.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));
        btnCreateSession.setOnClickListener(v -> createSession());

        etSessionStartTime.setOnClickListener(v -> showTimePickerDialog());

        loadSessions();
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
            etSessionStartTime.setText(formattedTime);
        }, hour, minute, true).show();
    }

    private void createSession() {
        String title = etSessionTitle.getText().toString().trim();
        String startTime = etSessionStartTime.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(startTime)) {
            Toast.makeText(this, "Please enter both title and start time", Toast.LENGTH_SHORT).show();
            return;
        }

        String meetLink = "https://meet.google.com/" + generateUniqueMeetCode();

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("title", title);
        sessionData.put("isLive", false);
        sessionData.put("link", meetLink);
        sessionData.put("startTime", startTime);

        firestore.collection("Sessions")
                .add(sessionData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Session created successfully!", Toast.LENGTH_SHORT).show();
                    loadSessions();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to create session", Toast.LENGTH_SHORT).show());
    }

    private String generateUniqueMeetCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            code.append(characters.charAt((int) (Math.random() * characters.length())));
        }
        return code.toString();
    }

    private void loadSessions() {
        sessionListLayout.removeAllViews();

        firestore.collection("Sessions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String sessionId = document.getId();
                        String title = document.getString("title");
                        String startTime = document.getString("startTime");
                        boolean isLive = Boolean.TRUE.equals(document.getBoolean("isLive"));
                        String link = document.getString("link");

                        View sessionView = getLayoutInflater().inflate(R.layout.session_item2, sessionListLayout, false);

                        TextView tvSessionTitle = sessionView.findViewById(R.id.tvSessionTitle);
                        TextView tvSessionStartTime = sessionView.findViewById(R.id.tvSessionStartTime);
                        Button btnStartSession = sessionView.findViewById(R.id.btnStartSession);
                        Button btnStopSession = sessionView.findViewById(R.id.btnStopSession);

                        tvSessionTitle.setText(title);
                        tvSessionStartTime.setText("Start Time: " + startTime);

                        if (isLive) {
                            btnStartSession.setVisibility(View.GONE);
                            btnStopSession.setVisibility(View.VISIBLE);

                            btnStopSession.setOnClickListener(v -> stopSession(sessionId));
                        } else {
                            btnStartSession.setVisibility(View.VISIBLE);
                            btnStopSession.setVisibility(View.GONE);

                            btnStartSession.setOnClickListener(v -> startSession(sessionId, link));
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
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Session started", Toast.LENGTH_SHORT).show();
                    startMeeting(link);
                    loadSessions();
                });
    }

    private void stopSession(String sessionId) {
        firestore.collection("Sessions").document(sessionId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Session stopped successfully", Toast.LENGTH_SHORT).show();
                    loadSessions();
                });
    }

    private void startMeeting(String meetLink) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(meetLink));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        startActivity(intent);
    }
}
