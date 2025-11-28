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
    private List<Map<String, Object>> activities; // title, date, id

    public GalleryAdapter(Context context, List<Map<String, Object>> activities) {
        this.context = context;
        this.activities = activities;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Map<String, Object> activity = activities.get(position);

        String title = (String) activity.get("title");
        String date = (String) activity.get("date");
        String id = (String) activity.get("id");

        holder.tvTitle.setText(title);
        holder.tvDate.setText("Fecha: " + date);

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

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvActivityTitle);
            tvDate = itemView.findViewById(R.id.tvActivityDate);
        }
    }
}
