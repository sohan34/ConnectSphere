package com.sohan.connectsphere;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class PublicChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etMessage;
    private Button btnSend;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private CollectionReference chatRef;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList, userId);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            // Initialize chat reference with the current user's ID
            chatRef = firestore.collection("Chats").document(userId).collection("Messages");
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnSend.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private void loadMessages() {
        if (chatRef == null) {
            Toast.makeText(this, "Chat reference is invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        chatRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(PublicChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
                Log.e("ChatActivity", "Error loading messages: " + error.getMessage());
                return;
            }

            if (value != null) {
                messageList.clear();

                for (QueryDocumentSnapshot doc : value) {
                    if (doc.exists()) {
                        String text = doc.getString("text");
                        String senderId = doc.getString("senderId");

                        // Handle timestamp properly (supports multiple formats)
                        Object timestampObj = doc.get("timestamp");
                        Long timestamp = getTimestampInMillis(timestampObj);

                        Boolean isAlumni = doc.getBoolean("isAlumni");

                        if (text != null && senderId != null && timestamp != null) {
                            ChatMessage message = new ChatMessage(text, senderId, timestamp, isAlumni != null && isAlumni);
                            messageList.add(message);
                        }
                    }
                }

                // Sort messages manually based on timestamp
                messageList.sort(Comparator.comparingLong(ChatMessage::getTimestamp));

                chatAdapter.notifyDataSetChanged();

                // Auto-scroll to latest message
                if (!messageList.isEmpty()) {
                    recyclerView.post(() -> recyclerView.smoothScrollToPosition(messageList.size() - 1));
                }
            }
        });
    }

    // Method to handle multiple timestamp formats
    private Long getTimestampInMillis(Object timestampObj) {
        if (timestampObj instanceof Timestamp) {
            return ((Timestamp) timestampObj).toDate().getTime();
        } else if (timestampObj instanceof Long) {
            return (Long) timestampObj;
        } else if (timestampObj instanceof String) {
            String timestampStr = (String) timestampObj;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'at' HH:mm:ss 'UTC'X", Locale.ENGLISH);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdf.parse(timestampStr);
                return date != null ? date.getTime() : null;
            } catch (ParseException e) {
                Log.e("ChatActivity", "Invalid timestamp format: " + timestampStr, e);
            }
        }
        return null;
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format timestamp to match the string format you use in `loadMessages()`
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'at' HH:mm:ss 'UTC'X", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedTimestamp = sdf.format(new Date());

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("text", messageText);
        messageData.put("senderId", userId);
        messageData.put("timestamp", formattedTimestamp); // Ensuring consistent timestamp format
        messageData.put("isAlumni", true); // Since this is the alumni's chat, always set isAlumni to true

        chatRef.add(messageData).addOnSuccessListener(documentReference -> {
            etMessage.setText("");  // Clear input field
            recyclerView.scrollToPosition(messageList.size() - 1); // Auto-scroll to latest
        }).addOnFailureListener(e -> {
            Toast.makeText(PublicChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            Log.e("ChatActivity", "Error sending message: " + e.getMessage());
        });
    }
}
