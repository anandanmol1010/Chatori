package com.app.chatori.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.chatori.R;

import java.util.List;

/**
 * Adapter for displaying selected images in a RecyclerView.
 */
public class SelectedImagesAdapter extends RecyclerView.Adapter<SelectedImagesAdapter.ImageViewHolder> {

    private final Context context;
    private final List<Uri> imageUris;
    private final OnImageRemoveListener listener;

    public interface OnImageRemoveListener {
        void onImageRemove(Uri imageUri);
    }

    public SelectedImagesAdapter(Context context, List<Uri> imageUris, OnImageRemoveListener listener) {
        this.context = context;
        this.imageUris = imageUris;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_selected_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        
        // Load image
        holder.imageView.setImageURI(imageUri);
        
        // Set click listener for remove button
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageRemove(imageUri);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView btnRemove;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}
