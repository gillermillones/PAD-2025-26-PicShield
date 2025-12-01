package es.ucm.fdi.pad.picshield;

import android.app.Application;

import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Configuración de Cloudinary
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "degsfj3pv");
        config.put("api_key", "145991673621652");
        config.put("api_secret", "CC3gpzIUi0N1BkL9F_hWjctBs7A");

        MediaManager.init(this, config);
    }
}
