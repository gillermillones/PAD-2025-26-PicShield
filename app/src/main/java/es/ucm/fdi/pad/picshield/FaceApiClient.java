package es.ucm.fdi.pad.picshield;

import java.io.File;
import okhttp3.*;

public class FaceApiClient {
    private static final String API_KEY = "35EZbB3qWHQjAz9RVw6YuYKAEDR2m_ty";
    private static final String API_SECRET = "FeeWA23WUT6K_dlGnCPEM4PNOg7QsBTk";
    private static final String BASE_URL = "https://api-us.faceplusplus.com/facepp/v3/";

    private final OkHttpClient client = new OkHttpClient();

    public void createFaceSet(String outerId, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("api_key", API_KEY).add("api_secret", API_SECRET)
                .add("outer_id", outerId).build();
        Request request = new Request.Builder().url(BASE_URL + "faceset/create").post(body).build();
        client.newCall(request).enqueue(callback);
    }

    public void detectFace(File imageFile, Callback callback) {
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("api_key", API_KEY).addFormDataPart("api_secret", API_SECRET)
                .addFormDataPart("return_landmark", "0").addFormDataPart("return_attributes", "none")
                .addFormDataPart("image_file", imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/jpeg"))).build();
        Request request = new Request.Builder().url(BASE_URL + "detect").post(body).build();
        client.newCall(request).enqueue(callback);
    }

    public void addFaceToSet(String outerId, String faceToken, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("api_key", API_KEY).add("api_secret", API_SECRET)
                .add("outer_id", outerId).add("face_tokens", faceToken).build();
        Request request = new Request.Builder().url(BASE_URL + "faceset/addface").post(body).build();
        client.newCall(request).enqueue(callback);
    }

    public void searchFace(String faceToken, String outerId, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("api_key", API_KEY).add("api_secret", API_SECRET)
                .add("face_token", faceToken).add("outer_id", outerId).build();
        Request request = new Request.Builder().url(BASE_URL + "search").post(body).build();
        client.newCall(request).enqueue(callback);
    }

    public void deleteFaceSet(String outerId, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("api_key", API_KEY).add("api_secret", API_SECRET)
                .add("outer_id", outerId).add("check_empty", "0").build();
        Request request = new Request.Builder().url(BASE_URL + "faceset/delete").post(body).build();
        client.newCall(request).enqueue(callback);
    }
}