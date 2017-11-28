package br.com.conseng.bollyfilmes;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import br.com.conseng.bollyfilmes.sync.FilmesSyncAdapter;

public class MainActivity extends AppCompatActivity implements MainFragment.Callback {

    public static final String FILME_DETALHE_URI = "FILME";

    private boolean isTablet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (null != findViewById(R.id.fragment_filme_detalhe)) {

            if (null == savedInstanceState) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_filme_detalhe, new FilmeDetalheFragment())
                        .commit();
            }
            isTablet = true;
        } else {
            isTablet = false;
        }

        MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        mainFragment.setUseFilmeDestaque(!isTablet);

        FilmesSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public void onItemSelected(Uri uri) {
        assert(null != uri);
        if (isTablet) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            FilmeDetalheFragment detalheFragment = new FilmeDetalheFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(MainActivity.FILME_DETALHE_URI, uri);
            detalheFragment.setArguments(bundle);

            fragmentTransaction.replace(R.id.fragment_filme_detalhe, detalheFragment);
            fragmentTransaction.commit();
        } else {
            Intent intent = new Intent(this, FilmeDetalheActivity.class);
            intent.setData(uri);
            startActivity(intent);
        }
    }
}
