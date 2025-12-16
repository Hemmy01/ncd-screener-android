package com.example.ncdscreener.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ncdscreener.R;
import com.example.ncdscreener.database.entity.ScreeningEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView Adapter for displaying screening history
 */
public class ScreeningHistoryAdapter extends RecyclerView.Adapter<ScreeningHistoryAdapter.ScreeningViewHolder> {

    private List<ScreeningEntity> screenings;
    private OnScreeningClickListener listener;
    private OnScreeningDeleteListener deleteListener;

    public interface OnScreeningClickListener {
        void onScreeningClick(ScreeningEntity screening);
    }

    public interface OnScreeningDeleteListener {
        void onScreeningDelete(int screeningId);
    }

    public ScreeningHistoryAdapter(List<ScreeningEntity> screenings) {
        this.screenings = screenings;
    }

    public void setOnScreeningClickListener(OnScreeningClickListener listener) {
        this.listener = listener;
    }

    public void setOnScreeningDeleteListener(OnScreeningDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ScreeningViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_screening_history, parent, false);
        return new ScreeningViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScreeningViewHolder holder, int position) {
        ScreeningEntity screening = screenings.get(position);
        holder.bind(screening);
    }

    @Override
    public int getItemCount() {
        return screenings != null ? screenings.size() : 0;
    }

    public void updateScreenings(List<ScreeningEntity> newScreenings) {
        this.screenings = newScreenings;
        notifyDataSetChanged();
    }

    class ScreeningViewHolder extends RecyclerView.ViewHolder {
        private TextView textScreeningDate;
        private TextView textScreeningLocation;
        private TextView textScreeningId;
        private TextView textChwName;
        private ImageButton buttonDelete;

        public ScreeningViewHolder(@NonNull View itemView) {
            super(itemView);
            textScreeningDate = itemView.findViewById(R.id.text_screening_date);
            textScreeningLocation = itemView.findViewById(R.id.text_screening_location);
            textScreeningId = itemView.findViewById(R.id.text_screening_id);
            textChwName = itemView.findViewById(R.id.text_chw_name);
            buttonDelete = itemView.findViewById(R.id.button_delete_screening);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && screenings != null && listener != null) {
                    listener.onScreeningClick(screenings.get(position));
                }
            });

            if (buttonDelete != null) {
                buttonDelete.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && screenings != null && deleteListener != null) {
                        ScreeningEntity screening = screenings.get(position);
                        deleteListener.onScreeningDelete(screening.getScreeningId());
                    }
                });
            }
        }

        public void bind(ScreeningEntity screening) {
            if (screening == null) {
                return;
            }

            // Format date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String dateStr = "Date: " + dateFormat.format(new Date(screening.getScreeningDate()));
            textScreeningDate.setText(dateStr);

            // Location
            String location = screening.getLocation();
            textScreeningLocation.setText(location != null && !location.isEmpty() ? location : "N/A");

            // Screening ID
            textScreeningId.setText("Screening ID: " + screening.getScreeningId());

            // CHW Name
            String chwName = screening.getChwName();
            textChwName.setText("CHW: " + (chwName != null && !chwName.isEmpty() ? chwName : "Unknown"));
        }
    }
}

