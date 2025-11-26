package es.ucm.fdi.pad.picshield;

import android.content.Context;
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_child, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Child child = children.get(position);
        holder.tvName.setText(child.getFirstName() + " " + child.getLastName());
        holder.tvDni.setText(child.getDni());
        Glide.with(context).load(child.getPhotoUrl()).into(holder.ivPhoto);
    }

    @Override
    public int getItemCount() {
        return children.size();
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDni;
        ImageView ivPhoto;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvChildName);
            tvDni = itemView.findViewById(R.id.tvChildDni);
            ivPhoto = itemView.findViewById(R.id.ivChildPhoto);
        }
    }
}

