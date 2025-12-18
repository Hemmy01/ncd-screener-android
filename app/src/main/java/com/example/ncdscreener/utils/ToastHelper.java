package com.example.ncdscreener.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ncdscreener.R;
import com.google.android.material.card.MaterialCardView;

/**
 * Utility class for displaying colored toast messages
 */
public class ToastHelper {

    // Toast types for different message categories
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_ERROR = 1;
    public static final int TYPE_WARNING = 2;
    public static final int TYPE_INFO = 3;

    /**
     * Shows a colored toast message with a border around the screen
     * 
     * @param context The context to use
     * @param message The message to display
     * @param type The type of toast (SUCCESS, ERROR, WARNING, INFO)
     * @param duration Toast.LENGTH_SHORT or Toast.LENGTH_LONG
     */
    public static void showToast(Context context, String message, int type, int duration) {
        if (context == null) return;

        // Create custom toast layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        MaterialCardView cardView = layout.findViewById(R.id.toast_card);
        TextView textView = layout.findViewById(R.id.toast_text);

        // Set message
        textView.setText(message);

        // Set colors based on type
        int backgroundColor;
        int borderColor;
        int textColor = Color.WHITE;

        switch (type) {
            case TYPE_SUCCESS:
                backgroundColor = context.getResources().getColor(R.color.success, null);
                borderColor = context.getResources().getColor(R.color.success, null);
                break;
            case TYPE_ERROR:
                backgroundColor = context.getResources().getColor(R.color.error, null);
                borderColor = context.getResources().getColor(R.color.error, null);
                break;
            case TYPE_WARNING:
                backgroundColor = context.getResources().getColor(R.color.warning, null);
                borderColor = context.getResources().getColor(R.color.warning, null);
                textColor = Color.BLACK;
                break;
            case TYPE_INFO:
            default:
                backgroundColor = context.getResources().getColor(R.color.info, null);
                borderColor = context.getResources().getColor(R.color.info, null);
                break;
        }

        // Apply colors
        cardView.setCardBackgroundColor(backgroundColor);
        cardView.setStrokeColor(borderColor);
        cardView.setStrokeWidth(4); // Border width
        textView.setTextColor(textColor);

        // Create and show toast
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();
    }

    /**
     * Shows a success toast message
     */
    public static void showSuccess(Context context, String message) {
        showToast(context, message, TYPE_SUCCESS, Toast.LENGTH_SHORT);
    }

    /**
     * Shows an error toast message
     */
    public static void showError(Context context, String message) {
        showToast(context, message, TYPE_ERROR, Toast.LENGTH_SHORT);
    }

    /**
     * Shows a warning toast message
     */
    public static void showWarning(Context context, String message) {
        showToast(context, message, TYPE_WARNING, Toast.LENGTH_SHORT);
    }

    /**
     * Shows an info toast message
     */
    public static void showInfo(Context context, String message) {
        showToast(context, message, TYPE_INFO, Toast.LENGTH_SHORT);
    }

    /**
     * Shows a success toast message with long duration
     */
    public static void showSuccessLong(Context context, String message) {
        showToast(context, message, TYPE_SUCCESS, Toast.LENGTH_LONG);
    }

    /**
     * Shows an error toast message with long duration
     */
    public static void showErrorLong(Context context, String message) {
        showToast(context, message, TYPE_ERROR, Toast.LENGTH_LONG);
    }

    /**
     * Shows a warning toast message with long duration
     */
    public static void showWarningLong(Context context, String message) {
        showToast(context, message, TYPE_WARNING, Toast.LENGTH_LONG);
    }
}
