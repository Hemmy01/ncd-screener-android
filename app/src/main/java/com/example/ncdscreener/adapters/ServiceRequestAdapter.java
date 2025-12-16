package com.example.ncdscreener.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ncdscreener.R;
import com.example.ncdscreener.model.ServiceRequest;

import java.util.List;

/**
 * RecyclerView Adapter for displaying service requests (referrals)
 */
public class ServiceRequestAdapter extends RecyclerView.Adapter<ServiceRequestAdapter.ServiceRequestViewHolder> {

    private List<ServiceRequest> serviceRequests;

    public ServiceRequestAdapter(List<ServiceRequest> serviceRequests) {
        this.serviceRequests = serviceRequests;
    }

    @NonNull
    @Override
    public ServiceRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_request, parent, false);
        return new ServiceRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceRequestViewHolder holder, int position) {
        ServiceRequest serviceRequest = serviceRequests.get(position);
        holder.bind(serviceRequest);
    }

    @Override
    public int getItemCount() {
        return serviceRequests != null ? serviceRequests.size() : 0;
    }

    public void updateServiceRequests(List<ServiceRequest> newServiceRequests) {
        this.serviceRequests = newServiceRequests;
        notifyDataSetChanged();
    }

    class ServiceRequestViewHolder extends RecyclerView.ViewHolder {
        private TextView textReferralCode;
        private TextView textReasonText;
        private TextView textStatus;

        public ServiceRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            textReferralCode = itemView.findViewById(R.id.text_referral_code);
            textReasonText = itemView.findViewById(R.id.text_reason_text);
            textStatus = itemView.findViewById(R.id.text_status);
        }

        public void bind(ServiceRequest serviceRequest) {
            textReferralCode.setText("Referral Code: " + serviceRequest.getReferralCode());
            
            String reasonText = serviceRequest.getReasonText();
            if (reasonText != null && !reasonText.isEmpty()) {
                textReasonText.setText(reasonText);
                textReasonText.setVisibility(View.VISIBLE);
            } else {
                textReasonText.setVisibility(View.GONE);
            }
            
            String status = serviceRequest.getStatus();
            String statusDisplay = status != null && !status.isEmpty() ? status.substring(0, 1).toUpperCase() + status.substring(1) : "Pending";
            textStatus.setText("Status: " + statusDisplay);
            
            // Color code status
            int statusColor = getStatusColor(status);
            textStatus.setTextColor(itemView.getContext().getColor(statusColor));
        }

        private int getStatusColor(String status) {
            if (status == null) return R.color.on_surface;
            switch (status.toLowerCase()) {
                case "active":
                    return R.color.risk_moderate;
                case "completed":
                    return R.color.risk_low;
                case "cancelled":
                    return R.color.on_surface_variant;
                case "draft":
                    return R.color.outline;
                default:
                    return R.color.on_surface;
            }
        }
    }
}
