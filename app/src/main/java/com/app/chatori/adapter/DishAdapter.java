package com.app.chatori.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.chatori.R;
import com.app.chatori.model.Dish;
import com.app.chatori.utils.UIUtils;

import java.util.List;

/**
 * Adapter for displaying dishes in a RecyclerView.
 */
public class DishAdapter extends RecyclerView.Adapter<DishAdapter.DishViewHolder> {

    private final Context context;
    private List<Dish> dishes;

    public DishAdapter(Context context, List<Dish> dishes) {
        this.context = context;
        this.dishes = dishes;
    }

    @NonNull
    @Override
    public DishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dish, parent, false);
        return new DishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DishViewHolder holder, int position) {
        Dish dish = dishes.get(position);
        
        // Set dish name
        holder.tvDishName.setText(dish.getName());
        
        // Set dish price
        holder.tvPrice.setText(String.format("₹%s", dish.getPrice()));
        
        // Load dish image
        if (dish.getImageUrl() != null && !dish.getImageUrl().isEmpty()) {
            UIUtils.loadImage(context, dish.getImageUrl(), holder.ivDishImage);
        } else {
            holder.ivDishImage.setImageResource(R.drawable.placeholder_image);
        }
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    /**
     * Updates the dishes list and refreshes the adapter
     */
    public void updateDishes(List<Dish> newDishes) {
        this.dishes = newDishes;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for dish items
     */
    static class DishViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDishImage;
        TextView tvDishName, tvPrice;

        DishViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDishImage = itemView.findViewById(R.id.iv_dish_image);
            tvDishName = itemView.findViewById(R.id.tv_dish_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
        }
    }
}
