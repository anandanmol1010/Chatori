package com.app.chatori.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for date-related operations.
 */
public class DateUtils {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
    
    /**
     * Formats a date to a readable string
     * @param date Date to format
     * @return Formatted date string
     */
    public static String formatDate(Date date) {
        if (date == null) return "";
        
        long diffInMillis = System.currentTimeMillis() - date.getTime();
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
        long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
        long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        
        if (diffInMinutes < 1) {
            return "Just now";
        } else if (diffInMinutes < 60) {
            return diffInMinutes + " min ago";
        } else if (diffInHours < 24) {
            return diffInHours + " hr ago";
        } else if (diffInDays < 7) {
            return diffInDays + " day" + (diffInDays > 1 ? "s" : "") + " ago";
        } else {
            return DATE_FORMAT.format(date);
        }
    }
    
    /**
     * Formats a date to a readable time string
     * @param date Date to format
     * @return Formatted time string
     */
    public static String formatTime(Date date) {
        if (date == null) return "";
        return TIME_FORMAT.format(date);
    }
    
    /**
     * Formats a date to a readable date and time string
     * @param date Date to format
     * @return Formatted date and time string
     */
    public static String formatDateTime(Date date) {
        if (date == null) return "";
        return DATE_TIME_FORMAT.format(date);
    }
}
