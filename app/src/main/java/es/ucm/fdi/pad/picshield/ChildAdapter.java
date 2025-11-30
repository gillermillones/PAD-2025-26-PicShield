package es.ucm.fdi.pad.picshield;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ChildViewHolder> {

    private List<Child> children;
    private Context context;

    public ChildAdapter(List<Child> children, Context context) {
        this.children = children;
        this.context = context;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el XML nuevo (tarjeta bonita)
        View view = LayoutInflater.from(context).inflate(R.layout.item_child, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Child child = children.get(position);

        // Ponemos Nombre y DNI
        holder.tvName.setText(child.getFirstName() + " " + child.getLastName());
        holder.tvDni.setText(child.getDni());

        // Cargamos la foto con Glide
        if (child.getPhotoUrl() != null && !child.getPhotoUrl().isEmpty()) {
            Glide.with(context).load(child.getPhotoUrl()).into(holder.ivPhoto);
        }

        // --- LÓGICA DEL SEMÁFORO (Nuevo) ---
        if (child.isAllowPhotos()) {
            // Si permite fotos -> Icono VERDE
            holder.ivPhotoStatus.setImageResource(android.R.drawable.ic_menu_camera);
            holder.ivPhotoStatus.setColorFilter(Color.parseColor("#4CAF50")); // Verde Material Design
        } else {
            // Si NO permite fotos -> Icono ROJO (o candado/prohibido)
            // Usamos el mismo icono de cámara pero en rojo para indicar "cuidado"
            holder.ivPhotoStatus.setImageResource(android.R.drawable.ic_menu_camera);
            holder.ivPhotoStatus.setColorFilter(Color.parseColor("#D32F2F")); // Rojo Material Design
        }
    }

    @Override
    public int getItemCount() {
        return children.size();
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDni;
        ImageView ivPhoto, ivPhotoStatus; // Añadido ivPhotoStatus

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvChildName);
            tvDni = itemView.findViewById(R.id.tvChildDni);
            ivPhoto = itemView.findViewById(R.id.ivChildPhoto);

            // Enlazamos el icono del semáforo
            ivPhotoStatus = itemView.findViewById(R.id.ivPhotoStatus);
        }
    }
}