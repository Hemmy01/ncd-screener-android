package com.example.ncdscreener.utils;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * Utility class for accessibility improvements
 */
public class AccessibilityHelper {

    /**
     * Set content description for better accessibility
     */
    public static void setContentDescription(View view, String description) {
        if (view != null && description != null) {
            view.setContentDescription(description);
        }
    }

    /**
     * Increase touch target size for better accessibility
     */
    public static void increaseTouchTarget(View view, int minSize) {
        if (view != null) {
            view.setMinimumWidth(minSize);
            view.setMinimumHeight(minSize);
        }
    }

    /**
     * Make view more accessible for elderly users
     */
    public static void optimizeForElderly(View view) {
        if (view != null) {
            // Increase touch target to 48dp minimum (recommended by Material Design)
            int minTouchTarget = (int) (48 * view.getContext().getResources().getDisplayMetrics().density);
            increaseTouchTarget(view, minTouchTarget);
        }
    }
}
