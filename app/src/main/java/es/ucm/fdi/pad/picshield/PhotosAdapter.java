package es.ucm.fdi.pad.picshield;

import android.content.Context;
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
    private List<String> urls;

    public PhotosAdapter(Context context, List<String> urls) {
        this.context = context;
        this.urls = urls;
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
        Glide.with(context).load(urls.get(position)).into(holder.imgPhoto);
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPhoto;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
        }
    }
}
