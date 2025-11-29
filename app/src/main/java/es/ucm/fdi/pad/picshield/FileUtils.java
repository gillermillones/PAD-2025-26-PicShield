package es.ucm.fdi.pad.picshield;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
    public static File getFileFromUri(Context context, Uri uri) {
        try {
            File destinationFilename = new File(context.getCacheDir(), getFileName(context, uri));
            try (InputStream ins = context.getContentResolver().openInputStream(uri);
                 OutputStream os = new FileOutputStream(destinationFilename)) {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = ins.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            }
            return destinationFilename;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if(index >= 0) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }
}