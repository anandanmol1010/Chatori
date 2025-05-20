package com.app.chatori.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.chatori.R;
import com.app.chatori.model.Review;
import com.app.chatori.ui.stall.StallDetailActivity;
import com.app.chatori.utils.DateUtils;
import com.app.chatori.utils.UIUtils;

import java.util.List;

/**
 * Adapter for displaying reviews in a RecyclerView.
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final Context context;
    private List<Review> reviews;
    private boolean showStallName = true;

    public ReviewAdapter(Context context, List<Review> reviews) {
        this.context = context;
        this.reviews = reviews;
    }

    public ReviewAdapter(Context context, List<Review> reviews, boolean showStallName) {
        this.context = context;
        this.reviews = reviews;
        this.showStallName = showStallName;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        
        // Load user profile image
        UIUtils.loadProfileImage(context, review.getUserProfileImageUrl(), holder.ivUserProfile);
        
        // Set user name
        holder.tvUserName.setText(review.getUserName());
        
        // Set rating
        holder.ratingBar.setRating(review.getRating());
        
        // Set review text
        holder.tvReviewText.setText(review.getText());
        
        // Set review date
        holder.tvDate.setText(DateUtils.formatDate(review.getCreatedAt()));
        
        // Set stall name if needed
        if (showStallName && review.getStallName() != null) {
            holder.tvStallName.setText(review.getStallName());
            holder.tvStallName.setVisibility(View.VISIBLE);
        } else {
            holder.tvStallName.setVisibility(View.GONE);
        }
        
        // Set click listener to navigate to stall detail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StallDetailActivity.class);
            intent.putExtra("stall_id", review.getStallId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    /**
     * Updates the reviews list and refreshes the adapter
     */
    public void updateReviews(List<Review> newReviews) {
        this.reviews = newReviews;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for review items
     */
    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserProfile;
        TextView tvUserName, tvStallName, tvReviewText, tvDate;
        RatingBar ratingBar;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserProfile = itemView.findViewById(R.id.iv_user_profile);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvStallName = itemView.findViewById(R.id.tv_stall_name);
            tvReviewText = itemView.findViewById(R.id.tv_comment);
            tvDate = itemView.findViewById(R.id.tv_date);
            ratingBar = itemView.findViewById(R.id.rating_bar);
        }
    }
}
