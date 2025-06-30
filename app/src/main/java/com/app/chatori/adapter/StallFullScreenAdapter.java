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
import com.app.chatori.model.Stall;
import com.app.chatori.ui.stall.StallDetailActivity;
import com.app.chatori.utils.UIUtils;

import java.util.List;

/**
 * Adapter for displaying stalls in a RecyclerView.
 */
public class StallFullScreenAdapter extends RecyclerView.Adapter<StallFullScreenAdapter.StallViewHolder> {

    private final Context context;
    private List<Stall> stalls;

    public StallFullScreenAdapter(Context context, List<Stall> stalls) {
        this.context = context;
        this.stalls = stalls;
    }

    @NonNull
    @Override
    public StallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_stall, parent, false);
        return new StallViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StallViewHolder holder, int position) {
        Stall stall = stalls.get(position);
        
        // Set stall name
        holder.tvStallName.setText(stall.getName());
        
        // Set dish type
        holder.tvDishType.setText(stall.getDishType());
        
        // Set area
        holder.tvArea.setText(stall.getArea());
        
        // Set rating
        holder.ratingBar.setRating(stall.getRating());
        holder.tvRating.setText(String.format("%.1f", stall.getRating()));
        holder.tvNumRatings.setText(String.format("(%d)", stall.getNumRatings()));
        
        // Load stall image
        if (stall.getImages() != null && !stall.getImages().isEmpty()) {
            UIUtils.loadImage(context, stall.getImages().get(0), holder.ivStallImage);
        } else {
            holder.ivStallImage.setImageResource(R.drawable.placeholder_image);
        }
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StallDetailActivity.class);
            intent.putExtra("stall_id", stall.getStallId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return stalls.size();
    }

    /**
     * Updates the stalls list and refreshes the adapter
     */
    public void updateStalls(List<Stall> newStalls) {
        this.stalls = newStalls;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for stall items
     */
    static class StallViewHolder extends RecyclerView.ViewHolder {
        ImageView ivStallImage;
        TextView tvStallName, tvDishType, tvArea, tvRating, tvNumRatings;
        RatingBar ratingBar;

        StallViewHolder(@NonNull View itemView) {
            super(itemView);
            ivStallImage = itemView.findViewById(R.id.iv_stall_image);
            tvStallName = itemView.findViewById(R.id.tv_stall_name);
            tvDishType = itemView.findViewById(R.id.tv_dish_type);
            tvArea = itemView.findViewById(R.id.tv_area);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvNumRatings = itemView.findViewById(R.id.tv_num_ratings);
            ratingBar = itemView.findViewById(R.id.rating_bar);
        }
    }
}
