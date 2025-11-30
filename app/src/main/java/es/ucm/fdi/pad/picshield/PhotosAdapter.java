package es.ucm.fdi.pad.picshield;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.PhotoViewHolder> {

    private Context context;
    private List<Object> items; // Puede contener String (URL) o Uri (Local)

    public PhotosAdapter(Context context, List<Object> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_gallery, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Object item = items.get(position);

        // Cargar imagen pequeña
        Glide.with(context)
                .load(item)
                .centerCrop()
                .into(holder.imgPhoto);

        // --- NUEVO: CLICK PARA ABRIR EN GRANDE ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullScreenImageActivity.class);
            // Convertimos el objeto (sea Uri o String) a String para pasarlo
            intent.putExtra("image_url", item.toString());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPhoto;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
        }
    }
}