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

    private EditText etTitle, etDescription;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        etTitle = new EditText(getContext());
        etTitle.setHint("Título de la actividad");

        etDescription = new EditText(getContext());
        etDescription.setHint("Descripción");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(etTitle);
        layout.addView(etDescription);

        return new AlertDialog.Builder(requireContext())
                .setTitle("Crear actividad")
                .setView(layout)
                .setPositiveButton("Crear", (dialog, which) -> saveActivity())
                .setNegativeButton("Cancelar", null)
                .create();
    }

    private void saveActivity() {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

        if (title.isEmpty()) return;

        Map<String, Object> activity = new HashMap<>();
        activity.put("title", title);
        activity.put("description", desc);

        FirebaseFirestore.getInstance()
                .collection("activities")
                .add(activity);
    }
}

