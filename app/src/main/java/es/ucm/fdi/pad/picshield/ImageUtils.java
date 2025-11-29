package es.ucm.fdi.pad.picshield;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ImageUtils {
    public static Bitmap pixelateFaces(Bitmap original, JSONArray faces) {
        if (original == null) return null;
        Bitmap mutableBitmap = original.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);

        for (int i = 0; i < faces.length(); i++) {
            try {
                JSONObject face = faces.getJSONObject(i);
                JSONObject rect = face.getJSONObject("face_rectangle");
                int rawLeft = rect.getInt("left");
                int rawTop = rect.getInt("top");
                int rawWidth = rect.getInt("width");
                int rawHeight = rect.getInt("height");

                float expansionFactor = 1.4f;
                int centerX = rawLeft + rawWidth / 2;
                int centerY = rawTop + rawHeight / 2;
                int newWidth = (int) (rawWidth * expansionFactor);
                int newHeight = (int) (rawHeight * expansionFactor);
                int left = centerX - newWidth / 2;
                int top = centerY - newHeight / 2;
                int width = newWidth;
                int height = newHeight;

                if (left < 0) left = 0;
                if (top < 0) top = 0;
                if (left + width > mutableBitmap.getWidth()) width = mutableBitmap.getWidth() - left;
                if (top + height > mutableBitmap.getHeight()) height = mutableBitmap.getHeight() - top;
                if (width <= 0 || height <= 0) continue;

                Bitmap faceCrop = Bitmap.createBitmap(mutableBitmap, left, top, width, height);
                int blurIntensity = 6;
                Bitmap small = Bitmap.createScaledBitmap(faceCrop, blurIntensity, blurIntensity, true);
                Bitmap blurred = Bitmap.createScaledBitmap(small, width, height, true);
                canvas.drawBitmap(blurred, left, top, paint);
            } catch (JSONException e) { e.printStackTrace(); }
        }
        return mutableBitmap;
    }
}