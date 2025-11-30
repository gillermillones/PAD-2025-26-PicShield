package es.ucm.fdi.pad.picshield;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.bumptech.glide.Glide;

public class FullScreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        ConstraintLayout rootLayout = findViewById(R.id.rootLayout);
        ImageView ivFullScreen = findViewById(R.id.ivFullScreen);
        ImageButton btnClose = findViewById(R.id.btnClose);

        String imageUrl = getIntent().getStringExtra("image_url");

        if (imageUrl != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .into(ivFullScreen);
        }

        // Opción 1: Cerrar con la X
        btnClose.setOnClickListener(v -> finish());

        // Opción 2: Cerrar tocando el fondo negro (o la imagen)
        // Al hacer click en cualquier parte del contenedor principal, cerramos.
        rootLayout.setOnClickListener(v -> finish());
    }
}