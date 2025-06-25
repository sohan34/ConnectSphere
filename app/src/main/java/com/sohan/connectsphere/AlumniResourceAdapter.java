package com.sohan.connectsphere;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class AlumniResourceAdapter extends RecyclerView.Adapter<AlumniResourceAdapter.ViewHolder> {

    private final List<Map<String, Object>> alumniList;
    private final Context context;
    private final OnAlumniClickListener listener;

    public interface OnAlumniClickListener {
        void onAlumniClick(String alumniId, String alumniName);
    }

    public AlumniResourceAdapter(Context context, List<Map<String, Object>> alumniList, OnAlumniClickListener listener) {
        this.context = context;
        this.alumniList = alumniList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.alumni_resource_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> alumni = alumniList.get(position);
        
        holder.tvAlumniName.setText((String) alumni.get("name"));
        holder.tvAlumniCompany.setText((String) alumni.get("company"));
        
        Long resourceCount = (Long) alumni.get("resourceCount");
        if (resourceCount != null) {
            holder.tvResourceCount.setText(resourceCount + " Resources");
            holder.tvResourceCount.setVisibility(View.VISIBLE);
        } else {
            holder.tvResourceCount.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            String alumniId = (String) alumni.get("id");
            String alumniName = (String) alumni.get("name");
            if (alumniId != null && listener != null) {
                listener.onAlumniClick(alumniId, alumniName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return alumniList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAlumniName;
        TextView tvAlumniCompany;
        TextView tvResourceCount;

        ViewHolder(View itemView) {
            super(itemView);
            tvAlumniName = itemView.findViewById(R.id.tvAlumniName);
            tvAlumniCompany = itemView.findViewById(R.id.tvAlumniCompany);
            tvResourceCount = itemView.findViewById(R.id.tvResourceCount);
        }
    }
} 