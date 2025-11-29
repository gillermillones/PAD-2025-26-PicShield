package es.ucm.fdi.pad.picshield;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FaceManager {
    private final FaceApiClient apiClient;
    private static final String FACESET_ID = "picshield_restricted_faces"; // ID único para tu app

    public interface ProcessCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface GroupPhotoCallback {
        void onProcessed(Bitmap bitmap);
        void onError(String error);
    }

    public FaceManager(Context context, FaceApiClient apiClient) {
        this.apiClient = apiClient;
        initFaceSet();
    }

    private void initFaceSet() {
        apiClient.createFaceSet(FACESET_ID, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}
            @Override
            public void onResponse(Call call, Response response) { response.close(); }
        });
    }

    public void addIndividualPhoto(File imageFile, ProcessCallback callback) {
        apiClient.detectFace(imageFile, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { callback.onError("Error de red: " + e.getMessage()); }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                response.close();
                try {
                    JSONObject obj = new JSONObject(json);
                    if (obj.has("error_message")) {
                        callback.onError("Error detectando: " + obj.getString("error_message"));
                        return;
                    }
                    JSONArray faces = obj.optJSONArray("faces");
                    if (faces != null && faces.length() > 0) {
                        String faceToken = faces.getJSONObject(0).getString("face_token");
                        addFaceWithRetry(faceToken, callback);
                    } else {
                        callback.onError("No se detectó ninguna cara en la foto. Intenta con otra.");
                    }
                } catch (JSONException e) { callback.onError("Error JSON detect"); }
            }
        });
    }

    private void addFaceWithRetry(String faceToken, ProcessCallback callback) {
        apiClient.addFaceToSet(FACESET_ID, faceToken, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { callback.onError("Error red añadiendo"); }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                response.close();
                try {
                    JSONObject obj = new JSONObject(json);
                    if (obj.has("error_message")) {
                        String error = obj.getString("error_message");
                        if (error.contains("FACESET_NOT_FOUND") || error.contains("INVALID_OUTER_ID")) {
                            apiClient.createFaceSet(FACESET_ID, new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) { callback.onError("Fallo al crear FaceSet"); }
                                @Override
                                public void onResponse(Call call, Response response) {
                                    response.close();
                                    try { Thread.sleep(3000); } catch (InterruptedException e) {}
                                    addFaceWithRetry(faceToken, callback);
                                }
                            });
                        } else if (error.contains("CONCURRENCY_LIMIT_EXCEEDED")) {
                            try { Thread.sleep(3000); } catch (InterruptedException e) {}
                            addFaceWithRetry(faceToken, callback);
                        } else {
                            callback.onError("API Error: " + error);
                        }
                    } else {
                        callback.onSuccess("Cara añadida a lista de privacidad.");
                    }
                } catch (JSONException e) { callback.onError("Error JSON add"); }
            }
        });
    }

    public void processGroupPhoto(File groupFile, GroupPhotoCallback callback) {
        apiClient.detectFace(groupFile, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { callback.onError("Error red grupo"); }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                response.close();
                try {
                    JSONObject obj = new JSONObject(json);
                    if (obj.has("error_message")) {
                        callback.onError("API Error: " + obj.getString("error_message"));
                        return;
                    }
                    JSONArray faces = obj.optJSONArray("faces");
                    if (faces == null || faces.length() == 0) {
                        // Si no hay caras, devolvemos la original
                        Bitmap original;
                        try (FileInputStream fis = new FileInputStream(groupFile)) {
                            original = BitmapFactory.decodeStream(fis);
                        }
                        callback.onProcessed(original);
                        return;
                    }

                    Bitmap original;
                    try (FileInputStream fis = new FileInputStream(groupFile)) {
                        Bitmap temp = BitmapFactory.decodeStream(fis);
                        original = temp.copy(Bitmap.Config.ARGB_8888, true);
                    }

                    // Pausa de seguridad inicial
                    try { Thread.sleep(2000); } catch (InterruptedException e) {}

                    List<JSONObject> facesToPixelate = new ArrayList<>();
                    processNextFace(0, faces, facesToPixelate, original, callback);

                } catch (JSONException e) { callback.onError("Error JSON grupo"); }
            }
        });
    }

    private void processNextFace(int index, JSONArray allFaces, List<JSONObject> matches, Bitmap original, GroupPhotoCallback callback) {
        if (index >= allFaces.length()) {
            if (!matches.isEmpty()) {
                JSONArray finalArray = new JSONArray(matches);
                Bitmap result = ImageUtils.pixelateFaces(original, finalArray);
                callback.onProcessed(result);
            } else {
                callback.onProcessed(original);
            }
            return;
        }

        if (index > 0) {
            try { Thread.sleep(2000); } catch (InterruptedException e) {}
        }

        try {
            JSONObject faceObj = allFaces.getJSONObject(index);
            String faceToken = faceObj.getString("face_token");

            apiClient.searchFace(faceToken, FACESET_ID, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    processNextFace(index + 1, allFaces, matches, original, callback);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String res = response.body().string();
                        JSONObject searchObj = new JSONObject(res);

                        if (!searchObj.has("error_message")) {
                            JSONArray results = searchObj.optJSONArray("results");
                            if (results != null && results.length() > 0) {
                                double confidence = results.getJSONObject(0).getDouble("confidence");
                                if (confidence > 80.0) { // Umbral de coincidencia
                                    matches.add(faceObj);
                                }
                            }
                        } else {
                            String error = searchObj.getString("error_message");
                            if(error.contains("CONCURRENCY")) {
                                try { Thread.sleep(3000); } catch (InterruptedException e) {}
                                processNextFace(index, allFaces, matches, original, callback);
                                return;
                            }
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                    finally {
                        response.close();
                        processNextFace(index + 1, allFaces, matches, original, callback);
                    }
                }
            });
        } catch (JSONException e) {
            processNextFace(index + 1, allFaces, matches, original, callback);
        }
    }

    // --- ESTE ES EL MÉTODO QUE FALTABA ---
    public void deleteFaceSet(ProcessCallback callback) {
        apiClient.deleteFaceSet(FACESET_ID, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Error de red al borrar");
            }

            @Override
            public void onResponse(Call call, Response response) {
                response.close();
                initFaceSet(); // Lo reiniciamos vacío
                callback.onSuccess("FaceSet eliminado correctamente.");
            }
        });
    }
}