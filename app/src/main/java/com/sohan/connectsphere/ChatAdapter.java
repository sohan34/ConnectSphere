package com.sohan.connectsphere;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<ChatMessage> messageList;
    private final String currentUserId;
    private final Context context;

    public ChatAdapter(Context context, List<ChatMessage> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        holder.messageText.setText(message.getText());
        holder.senderName.setText(message.getSenderName());

        if (message.isFromAlumni()) {
            holder.messageText.setBackgroundColor(Color.TRANSPARENT);
            holder.senderName.setText("Alumni");//  for alumni
            holder.senderName.setTextColor(Color.WHITE);         // Blue for Alumni name
        } else {
            holder.messageText.setBackgroundColor(Color.TRANSPARENT);
            holder.senderName.setText("Student"); //  for users
            holder.senderName.setTextColor(Color.WHITE);                                                   // White for user name
        }
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, senderName;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tvMessageText);
            senderName = itemView.findViewById(R.id.tvSenderName);
        }
    }
}
