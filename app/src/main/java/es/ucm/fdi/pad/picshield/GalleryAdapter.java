package es.ucm.fdi.pad.picshield;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ActivityViewHolder> {

    private Context context;
    private List<Map<String, Object>> activities; // Contiene: title, date, id

    public GalleryAdapter(Context context, List<Map<String, Object>> activities) {
        this.context = context;
        this.activities = activities;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el diseño "currado" (item_activity.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Map<String, Object> activity = activities.get(position);

        String title = (String) activity.get("title");
        String date = (String) activity.get("date");
        String id = (String) activity.get("id");

        // Ponemos el texto. Si es null, ponemos un texto por defecto para que no falle.
        holder.tvTitle.setText(title != null ? title : "Sin Título");
        holder.tvDate.setText(date != null ? "Fecha: " + date : "");

        // Al hacer click en la tarjeta entera
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UploadActivityPhotosActivity.class);
            intent.putExtra("activityId", id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    // La clase interna que busca los elementos en el XML
    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            // Buscamos los IDs que definimos en item_activity.xml
            tvTitle = itemView.findViewById(R.id.tvActivityTitle);
            tvDate = itemView.findViewById(R.id.tvActivityDate);
        }
    }
}