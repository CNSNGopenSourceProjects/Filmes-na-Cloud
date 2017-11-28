package br.com.conseng.bollyfilmes.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Qin on 21/11/2017.
 */

public class FilmesProvider extends ContentProvider {

    private static final UriMatcher URI_MATCHER = buildUriMatcher();

    private FilmesDBHelper dbHelper;

    private static final int FILMES = 100;
    private static final int FILME_ID = 101;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(FilmesContract.CONTENT_AUTHORITY, FilmesContract.PATH_FILMES, FILMES);
        uriMatcher.addURI(FilmesContract.CONTENT_AUTHORITY, FilmesContract.PATH_FILMES + "/#", FILME_ID);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new FilmesDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        assert(null != uri);

        SQLiteDatabase readableDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        switch (URI_MATCHER.match(uri)) {
            case FILME_ID:      // redefine a clausula de WHERE para identificar um registro
                selection = FilmesContract.FilmeEntry.COLUMN_ID + "=?";
                selectionArgs = new String[]{String.valueOf(FilmesContract.FilmeEntry.getIdFromUri(uri))};
            case FILMES:        // continua FILME_ID
                cursor = readableDatabase.query(FilmesContract.FilmeEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("URI não identificada: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        assert(null != uri);

        String me = null;

        switch (URI_MATCHER.match(uri)) {
            case FILME_ID:
                me = FilmesContract.FilmeEntry.CONTENT_ITEM_TYPE;
                break;
            case FILMES:
                me = FilmesContract.FilmeEntry.CONTENT_TYPE;
                break;
            default:
                throw new IllegalArgumentException("URI não identificada: " + uri);
        }

        return me;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        assert(null != uri);

        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
        long id;
        Uri resultado = null;

        switch (URI_MATCHER.match(uri)) {
            case FILMES:
                id = writableDatabase.insert(FilmesContract.FilmeEntry.TABLE_NAME, null, values);
                if (id >= 0) {
                    resultado = FilmesContract.FilmeEntry.buildUriForFilmes(id);
                }
                break;
            default:
                throw new IllegalArgumentException("URI não identificada: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return resultado;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        assert(null != uri);

        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
        int removeu = 0;

        switch (URI_MATCHER.match(uri)) {
            case FILME_ID:      // redefine a clausula de WHERE para identificar um registro
                selection = FilmesContract.FilmeEntry.COLUMN_ID + "=?";
                selectionArgs = new String[]{String.valueOf(FilmesContract.FilmeEntry.getIdFromUri(uri))};
            case FILMES:        // continua FILME_ID
                removeu = writableDatabase.delete(FilmesContract.FilmeEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("URI não identificada: " + uri);
        }

        if (removeu > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return removeu;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        assert(null != uri);

        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
        int atualizou = 0;

        switch (URI_MATCHER.match(uri)) {
            case FILME_ID:      // redefine a clausula de WHERE para identificar um registro
                selection = FilmesContract.FilmeEntry.COLUMN_ID + "=?";
                selectionArgs = new String[]{String.valueOf(FilmesContract.FilmeEntry.getIdFromUri(uri))};
            case FILMES:        // continua FILME_ID
                atualizou = writableDatabase.update(FilmesContract.FilmeEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("URI não identificada: " + uri);
        }

        if (atualizou > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return atualizou;
    }
}
