package es.ucm.fdi.pad.picshield;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class CreateActivityDialog extends DialogFragment {

    private EditText etTitle, etDescription, etDate;
    private OnActivityCreatedListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        etTitle = new EditText(getContext());
        etTitle.setHint("Título de la actividad");

        etDescription = new EditText(getContext());
        etDescription.setHint("Descripción");

        etDate = new EditText(getContext());
        etDate.setHint("Fecha (dd/mm/aaaa)");

        // ❗ Evitar que salga el teclado y permitir clic
        etDate.setFocusable(false);
        etDate.setClickable(true);

        // 👉 ABRIR DATEPICKER AL TOCAR EL CAMPO
        etDate.setOnClickListener(v -> openDatePicker());

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(etTitle);
        layout.addView(etDescription);
        layout.addView(etDate);

        return new AlertDialog.Builder(requireContext())
                .setTitle("Crear actividad")
                .setView(layout)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String desc = etDescription.getText().toString().trim();
                    String date = etDate.getText().toString().trim();

                    if (!title.isEmpty() && listener != null) {
                        listener.onActivityCreated(title, desc, date);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create();
    }

    // Selector de fecha
    private void openDatePicker() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH); // 0 = enero
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(
                getContext(),
                (view, y, m, d) -> {
                    // m + 1 porque enero = 0
                    String formattedDate = String.format("%02d/%02d/%04d", d, m + 1, y);
                    etDate.setText(formattedDate);
                },
                year, month, day
        );

        datePicker.show();
    }

    public interface OnActivityCreatedListener {
        void onActivityCreated(String title, String description, String date);
    }

    public void setOnActivityCreatedListener(OnActivityCreatedListener l) {
        this.listener = l;
    }
}
