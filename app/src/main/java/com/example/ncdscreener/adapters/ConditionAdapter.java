package com.example.ncdscreener.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ncdscreener.R;
import com.example.ncdscreener.model.Condition;

import java.util.List;

/**
 * RecyclerView Adapter for displaying conditions
 */
public class ConditionAdapter extends RecyclerView.Adapter<ConditionAdapter.ConditionViewHolder> {

    private List<Condition> conditions;

    public ConditionAdapter(List<Condition> conditions) {
        this.conditions = conditions;
    }

    @NonNull
    @Override
    public ConditionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_condition, parent, false);
        return new ConditionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConditionViewHolder holder, int position) {
        Condition condition = conditions.get(position);
        holder.bind(condition);
    }

    @Override
    public int getItemCount() {
        return conditions != null ? conditions.size() : 0;
    }

    public void updateConditions(List<Condition> newConditions) {
        this.conditions = newConditions;
        notifyDataSetChanged();
    }

    class ConditionViewHolder extends RecyclerView.ViewHolder {
        private TextView textConditionName;
        private TextView textConditionCode;

        public ConditionViewHolder(@NonNull View itemView) {
            super(itemView);
            textConditionName = itemView.findViewById(R.id.text_condition_name);
            textConditionCode = itemView.findViewById(R.id.text_condition_code);
        }

        public void bind(Condition condition) {
            textConditionName.setText(condition.getConditionName());
            textConditionCode.setText("Code: " + condition.getConditionCode());
            
            if (condition.isCritical()) {
                textConditionName.setTextColor(itemView.getContext().getColor(R.color.risk_high));
            }
        }
    }
}

