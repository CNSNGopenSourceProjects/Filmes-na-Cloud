package br.com.conseng.bollyfilmes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.com.conseng.bollyfilmes.data.FilmesContract;
import br.com.conseng.bollyfilmes.data.FilmesDBHelper;
import br.com.conseng.bollyfilmes.sync.FilmesSyncAdapter;


public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private int posicaoItem = ListView.INVALID_POSITION;

    private static final String KEY_POSICAO = "SELECIONADO";

    private ListView listaFilmes;
    private FilmesAdapter adapter;

    private boolean useFilmeDestaque = false;

    private static final int FILMES_LOADER = 0;

    private ProgressDialog progressDialog;

    public void setUseFilmeDestaque(boolean useFilmeDestaque) {
        this.useFilmeDestaque = useFilmeDestaque;
        if (null != adapter) {
            adapter.setUseFilmeDestaque(useFilmeDestaque);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        listaFilmes = view.findViewById(R.id.lista_de_filmes);

        adapter = new FilmesAdapter(getContext(), null);
        adapter.setUseFilmeDestaque(useFilmeDestaque);

        listaFilmes.setAdapter(adapter);

        listaFilmes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = FilmesContract.FilmeEntry.buildUriForFilmes(id);
                Callback callback = (Callback) getActivity();
                callback.onItemSelected(uri);
                posicaoItem = position;
            }
        });

        if ((null != savedInstanceState) && (savedInstanceState.containsKey(KEY_POSICAO))) {
            posicaoItem = savedInstanceState.getInt(KEY_POSICAO);
        }

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(getString(R.string.pd_carregando_titulo));
        progressDialog.setMessage(getString(R.string.pd_carregando_mensagem));
        progressDialog.setCancelable(false);

        getLoaderManager().initLoader(FILMES_LOADER, null, this);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (ListView.INVALID_POSITION != posicaoItem) {
            outState.putInt(KEY_POSICAO, posicaoItem);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (null != savedInstanceState) {
            listaFilmes.smoothScrollToPosition(savedInstanceState.getInt(KEY_POSICAO));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_atualizar:
                FilmesSyncAdapter.syncImmediately(getContext());
                Toast.makeText(getContext(), "Atualizando os filmes...", Toast.LENGTH_LONG).show();
                return true;
            case R.id.menu_config:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(FILMES_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        progressDialog.show();

        String[] projection = {
                FilmesContract.FilmeEntry.COLUMN_ID,
                FilmesContract.FilmeEntry.COLUMN_TITULO,
                FilmesContract.FilmeEntry.COLUMN_DESCRICAO,
                FilmesContract.FilmeEntry.COLUMN_POSTER_PATH,
                FilmesContract.FilmeEntry.COLUMN_CAPA_PATH,
                FilmesContract.FilmeEntry.COLUMN_AVALIACAO,
                FilmesContract.FilmeEntry.COLUMN_DATA_LANCAMENTO,
                FilmesContract.FilmeEntry.COLUMN_POPULARIDADE
        };

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String popularValue = getResources().getStringArray(R.array.prefs_ordem_values)[0];
        String ordem = preferences.getString(getString(R.string.prefs_ordem_key), getString(R.string.prefs_ordem_default_value));

        String orderBy = null;
        if (ordem.equals(popularValue)) {
            orderBy = FilmesContract.FilmeEntry.COLUMN_POPULARIDADE + " DESC";
        } else {
            orderBy = FilmesContract.FilmeEntry.COLUMN_AVALIACAO + " DESC";
        }

        return new CursorLoader(getContext(), FilmesContract.FilmeEntry.CONTENT_URI,
                projection, null, null, orderBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        progressDialog.dismiss();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public interface Callback {
        void onItemSelected(Uri uri);
    }
}
