package es.ucm.fdi.pad.picshield;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ActivityViewHolder> {

    public interface OnActivityClickListener {
        void onActivityClick(String activityId);
    }

    private List<ActivityItem> activities;
    private OnActivityClickListener listener;

    public ActivitiesAdapter(List<ActivityItem> activities, OnActivityClickListener listener) {
        this.activities = activities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityItem item = activities.get(position);
        holder.title.setText("📁 " + item.getTitle());

        holder.itemView.setOnClickListener(v -> listener.onActivityClick(item.getId()));
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvActivityTitle);
        }
    }
}
