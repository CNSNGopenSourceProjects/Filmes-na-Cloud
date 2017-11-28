package br.com.conseng.bollyfilmes;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import br.com.conseng.bollyfilmes.data.FilmesContract;


public class FilmeDetalheFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri filmeUri;

    private TextView tituloView;
    private TextView dataLancamentoView;
    private TextView descricaoView;
    private RatingBar avaliacaoView;
    private ImageView capaView;
    private ImageView posterView;

    private static final int FILME_DETALHE_LOADER = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (null != getArguments()) {
            this.filmeUri = (Uri) getArguments().getParcelable(MainActivity.FILME_DETALHE_URI);
        }

        // TODO: valida a existência da URI antes de carregar o manager
        if (null != filmeUri) {
            getLoaderManager().initLoader(FILME_DETALHE_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_filme_detalhe, container, false);

        this.tituloView = view.findViewById(R.id.item_titulo);
        this.dataLancamentoView = view.findViewById(R.id.item_data);
        this.descricaoView = view.findViewById(R.id.item_desc);
        this.avaliacaoView = view.findViewById(R.id.item_avaliacao);
        this.capaView = view.findViewById(R.id.item_capa);
        this.posterView = view.findViewById(R.id.item_poster); // pode ser nulo

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

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

        assert (null != filmeUri);
        return new CursorLoader(getContext(), filmeUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if ((null != data) && (data.getCount() > 0)) {
            if (data.moveToFirst()) {       // Primeiro registro
                int tituloIndex = data.getColumnIndex(FilmesContract.FilmeEntry.COLUMN_TITULO);
                int descricaoIndex = data.getColumnIndex(FilmesContract.FilmeEntry.COLUMN_DESCRICAO);
                int posterIndex = data.getColumnIndex(FilmesContract.FilmeEntry.COLUMN_POSTER_PATH);
                int capaIndex = data.getColumnIndex(FilmesContract.FilmeEntry.COLUMN_CAPA_PATH);
                int avaliacaoIndex = data.getColumnIndex(FilmesContract.FilmeEntry.COLUMN_AVALIACAO);
                int dataLancamentoIndex = data.getColumnIndex(FilmesContract.FilmeEntry.COLUMN_DATA_LANCAMENTO);

                String titulo = data.getString(tituloIndex);
                String descricao = data.getString(descricaoIndex);
                String poster = data.getString(posterIndex);
                String capa = data.getString(capaIndex);
                float avaliacao = data.getFloat(avaliacaoIndex);
                String dataLancamento = data.getString(dataLancamentoIndex);

                this.tituloView.setText(titulo);
                this.descricaoView.setText(descricao);
                this.dataLancamentoView.setText(dataLancamento);
                this.avaliacaoView.setRating(avaliacao);

                new DownloadImageTask(capaView).execute(capa);
                if (null != posterView) {
                    new DownloadImageTask(posterView).execute(poster);  // Não existe na tela destaque
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
