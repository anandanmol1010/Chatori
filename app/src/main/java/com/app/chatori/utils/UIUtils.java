package com.app.chatori.utils;

import android.content.Context;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.chatori.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

/**
 * Utility class for UI-related operations.
 */
public class UIUtils {
    
    /**
     * Shows a short toast message
     * @param context Application context
     * @param message Message to display
     */
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Shows a long toast message
     * @param context Application context
     * @param message Message to display
     */
    public static void showLongToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Loads an image into an ImageView using Glide
     * @param context Application context
     * @param imageUrl URL of the image to load
     * @param imageView ImageView to load the image into
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.placeholder_image);
        }
    }
    
    /**
     * Loads a profile image into a CircleImageView using Glide
     * @param context Application context
     * @param imageUrl URL of the image to load
     * @param imageView ImageView to load the image into
     */
    public static void loadProfileImage(Context context, String imageUrl, ImageView imageView) {
        // Check for null context or ImageView to prevent crashes
        if (context == null || imageView == null) {
            return;
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                Glide.with(context)
                        .load(imageUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(imageView);
            } catch (Exception e) {
                // Fallback to default image if Glide fails
                imageView.setImageResource(R.drawable.default_profile);
            }
        } else {
            imageView.setImageResource(R.drawable.default_profile);
        }
    }
    
    /**
     * Converts dp to pixels
     * @param context Application context
     * @param dp Value in dp
     * @return Value in pixels
     */
    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
