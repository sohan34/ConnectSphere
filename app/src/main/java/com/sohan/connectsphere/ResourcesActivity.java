package com.sohan.connectsphere;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourcesActivity extends AppCompatActivity implements AlumniResourceAdapter.OnAlumniClickListener {

    private RecyclerView alumniRecyclerView;
    private ScrollView resourcesScrollView;
    private LinearLayout resourcesContainer;
    private FirebaseFirestore firestore;
    private String currentAlumniName;
    private List<ListenerRegistration> listeners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Alumni Resources");
        }

        alumniRecyclerView = findViewById(R.id.alumniRecyclerView);
        resourcesScrollView = findViewById(R.id.resourcesScrollView);
        resourcesContainer = findViewById(R.id.resourcesContainer);
        firestore = FirebaseFirestore.getInstance();
        listeners = new ArrayList<>();

        // Set up RecyclerView
        alumniRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadAlumniList();
    }

    private void loadAlumniList() {
        List<Map<String, Object>> alumniList = new ArrayList<>();
        
        // Add real-time listener for alumni
        ListenerRegistration alumniListener = firestore.collection("Users")
                .whereEqualTo("userType", "Alumni")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading alumni list", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (value != null) {
                        alumniList.clear();
                        for (QueryDocumentSnapshot document : value) {
                            Map<String, Object> alumni = new HashMap<>();
                            alumni.put("id", document.getId());
                            alumni.put("name", document.getString("name"));
                            alumni.put("company", document.getString("company"));

                            // Get resource count for this alumni
                            firestore.collection("Resources")
                                    .whereEqualTo("alumniId", document.getId())
                                    .get()
                                    .addOnSuccessListener(resources -> {
                                        alumni.put("resourceCount", (long) resources.size());
                                        alumniList.add(alumni);
                                        
                                        // Update adapter when we have the resource count
                                        alumniRecyclerView.setAdapter(new AlumniResourceAdapter(this, alumniList, this));
                                    });
                        }
                    }
                });
        listeners.add(alumniListener);
    }

    @Override
    public void onAlumniClick(String alumniId, String alumniName) {
        currentAlumniName = alumniName;
        alumniRecyclerView.setVisibility(View.GONE);
        resourcesScrollView.setVisibility(View.VISIBLE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(alumniName + "'s Resources");
        }
        loadResources(alumniId);
    }

    private void loadResources(String alumniId) {
        resourcesContainer.removeAllViews();
        
        // Add real-time listener for resources
        ListenerRegistration resourcesListener = firestore.collection("Resources")
                .whereEqualTo("alumniId", alumniId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading resources", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    resourcesContainer.removeAllViews();
                    
                    if (value != null && !value.isEmpty()) {
                        for (QueryDocumentSnapshot document : value) {
                            String title = document.getString("title");
                            String description = document.getString("description");
                            String link = document.getString("link");
                            String type = document.getString("type");

                            View resourceView = getLayoutInflater().inflate(R.layout.resource_item, resourcesContainer, false);

                            TextView tvResourceTitle = resourceView.findViewById(R.id.tvResourceTitle);
                            TextView tvResourceDescription = resourceView.findViewById(R.id.tvResourceDescription);
                            TextView tvResourceType = resourceView.findViewById(R.id.tvResourceType);
                            TextView tvAlumniName = resourceView.findViewById(R.id.tvAlumniName);
                            CardView cardView = resourceView.findViewById(R.id.resourceCard);

                            tvResourceTitle.setText(title);
                            tvResourceDescription.setText(description);
                            tvResourceType.setText(type);
                            tvAlumniName.setText("Shared by: " + currentAlumniName);

                            cardView.setOnClickListener(v -> {
                                if (link != null && !link.isEmpty()) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(this, "Resource link not available", Toast.LENGTH_SHORT).show();
                                }
                            });

                            resourcesContainer.addView(resourceView);
                        }
                    } else {
                        showEmptyState();
                    }
                });
        listeners.add(resourcesListener);
    }

    private void showEmptyState() {
        View emptyView = getLayoutInflater().inflate(R.layout.empty_state, resourcesContainer, false);
        TextView tvEmptyMessage = emptyView.findViewById(R.id.tvEmptyMessage);
        tvEmptyMessage.setText("No resources shared yet");
        resourcesContainer.addView(emptyView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (resourcesScrollView.getVisibility() == View.VISIBLE) {
                // If showing resources, go back to alumni list
                resourcesScrollView.setVisibility(View.GONE);
                alumniRecyclerView.setVisibility(View.VISIBLE);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Alumni Resources");
                }
                return true;
            }
            // Otherwise, finish activity
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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