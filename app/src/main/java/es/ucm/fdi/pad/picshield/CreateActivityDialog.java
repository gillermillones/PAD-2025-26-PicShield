package es.ucm.fdi.pad.picshield;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class CreateActivityDialog extends DialogFragment {

    private EditText etTitle, etDate;
    private OnActivityCreatedListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_create_activity, null);

        etTitle = view.findViewById(R.id.etDialogTitle);
        etDate = view.findViewById(R.id.etDialogDate);

        // Al tocar el campo de fecha, se abre el selector
        etDate.setOnClickListener(v -> openDatePicker());

        builder.setView(view)
                .setTitle(R.string.dialog_create_title)
                .setPositiveButton(R.string.dialog_btn_create, (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String date = etDate.getText().toString().trim();

                    // Validamos que haya título
                    if (!title.isEmpty() && listener != null) {

                        listener.onActivityCreated(title, "", date);
                    }
                })
                .setNegativeButton(R.string.dialog_btn_cancel, null);

        return builder.create();
    }

    private void openDatePicker() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                (view, y, m, d) -> {
                    // Formato dd/mm/aaaa (Sumamos 1 al mes porque Enero es 0)
                    String formattedDate = String.format("%02d/%02d/%04d", d, m + 1, y);
                    etDate.setText(formattedDate);
                },
                year, month, day
        );
        datePicker.show();
    }

    public interface OnActivityCreatedListener {
        // Mantenemos los 3 parámetros, aunque 'description' llegará siempre vacío.
        void onActivityCreated(String title, String description, String date);
    }

    public void setOnActivityCreatedListener(OnActivityCreatedListener l) {
        this.listener = l;
    }
}