package br.com.conseng.bollyfilmes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by Qin on 18/11/2017.
 */

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    private ImageView imageView = null;

    public DownloadImageTask(@NonNull ImageView imageView) {
        if (null == imageView) {
            throw new NullPointerException("Chamou DownloadImageTask sem declara o ImageView.");
        }
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap bitmap = null;
        String url = params[0];

        try {
            InputStream in = new URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        try {
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
