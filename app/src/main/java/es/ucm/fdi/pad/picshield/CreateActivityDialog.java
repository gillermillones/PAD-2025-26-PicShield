package es.ucm.fdi.pad.picshield;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateActivityDialog extends DialogFragment {

    private EditText etTitle, etDescription, etDate;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        etTitle = new EditText(getContext());
        etTitle.setHint("Título de la actividad");

        etDescription = new EditText(getContext());
        etDescription.setHint("Descripción");

        etDate = new EditText(getContext());
        etDate.setHint("Fecha");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(etTitle);
        layout.addView(etDescription);
        layout.addView(etDate);

        return new AlertDialog.Builder(requireContext())
                .setTitle("Crear actividad")
                .setView(layout)
                .setPositiveButton("Crear", (dialog, which) -> saveActivity())
                .setNegativeButton("Cancelar", null)
                .create();
    }

    public interface OnActivityCreatedListener {
        void onActivityCreated(String title, String description, String date);
    }

    private OnActivityCreatedListener listener;
    public void setOnActivityCreatedListener(OnActivityCreatedListener l) {
        this.listener = l;
    }

    private void saveActivity() {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        if (title.isEmpty()) return;

        Map<String, Object> activity = new HashMap<>();
        activity.put("title", title);
        activity.put("description", desc);
        activity.put("date", date);

        FirebaseFirestore.getInstance()
                .collection("activities")
                .add(activity);

        listener.onActivityCreated(title, desc, date);
        dismiss();

    }



}

