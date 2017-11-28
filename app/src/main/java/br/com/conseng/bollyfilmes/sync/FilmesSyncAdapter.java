package br.com.conseng.bollyfilmes.sync;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import br.com.conseng.bollyfilmes.BuildConfig;
import br.com.conseng.bollyfilmes.FilmeDetalheActivity;
import br.com.conseng.bollyfilmes.ItemFilme;
import br.com.conseng.bollyfilmes.JsonUtil;
import br.com.conseng.bollyfilmes.R;
import br.com.conseng.bollyfilmes.data.FilmesContract;

/**
 * Created by Qin on 23/11/2017.
 */

public class FilmesSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final int SYNC_INTERVAL = 60 * 60 * 12;           // intervalo máximo em segundos (=12 horas)

    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;      // intervalo mínimo em segundos

    public static final int NOTIFICATION_FILMES_ID = 1001;

    public FilmesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String ordem = preferences.getString(getContext().getString(R.string.prefs_ordem_key),
                getContext().getString(R.string.prefs_ordem_default_value));
        String idioma = preferences.getString(getContext().getString(R.string.prefs_idioma_key),
                getContext().getString(R.string.prefs_idioma_default_value));

        try {
            String urlBase = "https://api.themoviedb.org/3/movie/" + ordem + "?";
            String apiKey = "api_key";
            String language = "language";

            Uri uriApi = Uri.parse(urlBase).buildUpon()
                    .appendQueryParameter(apiKey, BuildConfig.TMB_API_KEY)
                    .appendQueryParameter(language, idioma)
                    .build();
            URL url = new URL(uriApi.toString());
            assert (null != url);

            urlConnection = (HttpURLConnection) url.openConnection();
            assert (null != urlConnection);

            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            assert (null != inputStream);
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String linha;
            StringBuffer buffer = new StringBuffer();
            while ((linha = reader.readLine()) != null) {
                buffer.append(linha);
                buffer.append("\n");
            }
            List<ItemFilme> itemFilmes = JsonUtil.fromJsonToList(buffer.toString());
            if (null == itemFilmes) return;

            for (ItemFilme itemFilme : itemFilmes) {
                ContentValues values = new ContentValues();
                values.put(FilmesContract.FilmeEntry.COLUMN_ID, itemFilme.getId());
                values.put(FilmesContract.FilmeEntry.COLUMN_TITULO, itemFilme.getTitulo());
                values.put(FilmesContract.FilmeEntry.COLUMN_DESCRICAO, itemFilme.getDescricao());
                values.put(FilmesContract.FilmeEntry.COLUMN_POSTER_PATH, itemFilme.getPosterPath());
                values.put(FilmesContract.FilmeEntry.COLUMN_CAPA_PATH, itemFilme.getCapaPath());
                values.put(FilmesContract.FilmeEntry.COLUMN_DATA_LANCAMENTO, itemFilme.getDataLancamento());
                values.put(FilmesContract.FilmeEntry.COLUMN_AVALIACAO, itemFilme.getAvaliacao());
                values.put(FilmesContract.FilmeEntry.COLUMN_POPULARIDADE, itemFilme.getPopularidade());

                int update = getContext().getContentResolver().update(FilmesContract.FilmeEntry.buildUriForFilmes(itemFilme.getId()),
                        values, null, null);

                if (0 == update) {
                    getContext().getContentResolver().insert(FilmesContract.FilmeEntry.CONTENT_URI, values);
                    notify(itemFilme);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != urlConnection) {
                urlConnection.disconnect();
            }
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void notify(ItemFilme itemFilme) {

        assert (null != itemFilme);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String notifyPrefKey = getContext().getString(R.string.prefs_notif_filmes_key);
        String notifyPrefDefault = getContext().getString(R.string.prefs_notif_filmes_default);
        boolean notifyPrefs = sharedPreferences.getBoolean(notifyPrefKey, Boolean.parseBoolean(notifyPrefDefault));

        if (notifyPrefs) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext()).
                    setSmallIcon(R.mipmap.ic_launcher).
                    setContentTitle(itemFilme.getTitulo()).
                    setContentText(itemFilme.getDescricao());

            Intent intent = new Intent(getContext(), FilmeDetalheActivity.class);
            Uri uri = FilmesContract.FilmeEntry.buildUriForFilmes(itemFilme.getId());
            intent.setData(uri);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
            stackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_FILMES_ID, builder.build());
        }
    }

    public static void configurePeriodicSync(@NonNull Context context, int syncInterval, int flexTime) {
        assert (null != context);
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest syncRequest = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(syncRequest);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }

    public static void syncImmediately(@NonNull Context context) {
        assert (null != context);
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), bundle);
    }

    @Nullable
    public static Account getSyncAccount(@NonNull Context context) {
        assert (null != context);
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account account = new Account(context.getString(R.string.app_name),
                context.getString(R.string.sync_account_type));

        assert (null != account);
        if (null == accountManager.getPassword(account)) {
            if (!accountManager.addAccountExplicitly(account, "", null)) {
                return null;
            }
            onAccountCreated(account, context);
        }
        return account;
    }

    private static void onAccountCreated(@NonNull Account account, @NonNull Context context) {
        assert (null != account);
        assert (null != context);
        String authority = context.getString(R.string.content_authority);

        FilmesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        ContentResolver.setSyncAutomatically(account, authority, true);

        syncImmediately(context);
    }

    public static void initializeSyncAdapter(@NonNull Context context) {
        assert (null != context);
        getSyncAccount(context);
    }
}
