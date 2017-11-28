package br.com.conseng.bollyfilmes.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Qin on 23/11/2017.
 */

public class FilmesSyncService extends Service {

    private static FilmesSyncAdapter filmesSyncAdapter = null;

    private static final Object lock = new Object();

    @Override
    public void onCreate() {
        synchronized (lock) {
            if (null == filmesSyncAdapter) {
                filmesSyncAdapter = new FilmesSyncAdapter(getApplicationContext(), true);
                assert (null != filmesSyncAdapter);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return filmesSyncAdapter.getSyncAdapterBinder();
    }
}
