package br.com.conseng.bollyfilmes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.SimpleFormatter;

import static android.provider.Settings.Global.getString;

/**
 * Created by Qin on 14/11/2017.
 * Exemplo do JSON
 * {
 * "poster_path":"/2tOgiY533JSFp7OrVlkeRJvsZpI.jpg",
 * "adult":false,
 * "overview":"Following the events of Age of Ultron, the collective governments of the world pass an act designed to regulate all superhuman activity. This polarizes opinion amongst the Avengers, causing two factions to side with Iron Man or Captain America, which causes an epic battle between former allies.",
 * "release_date":"2016-04-27",
 * "genres":[  {"id":12,"name":"Adventure"},
 * {"id":28,"name":"Action"},
 * {"id":878,"name":"Science Fiction"}],
 * "id":271110,
 * "original_title":"Captain America: Civil War",
 * "original_language":"en",
 * "title":"Captain America: Civil War",
 * "backdrop_path":"/m5O3SZvQ6EgD5XXXLPIP1wLppeW.jpg",
 * "popularity":105.335692,
 * "vote_count":7856,
 * "video":false,
 * "vote_average":7.1
 * }
 */

public class ItemFilme implements Serializable {
    private long id;
    private String titulo;
    private String descricao;
    private String dataLancamento;
    private String posterPath;
    private String capaPath;
    private float avaliacao;
    private float popularidade;

    public ItemFilme(long id, String titulo, String descricao, String dataLancamento, String posterPath, String capaPath, float avaliacao, float popularidade) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        setDataLancamento(dataLancamento);
        this.avaliacao = avaliacao;
        // http://image.tmdb.org/t/p/w185/<nome do arquivo com o poster.jpg>
        this.posterPath = posterPath;
        // http://image.tmdb.org/t/p/w185/<nome do arquivo com a capa.jpg>
        this.capaPath = capaPath;
        this.popularidade = popularidade;
    }

    public ItemFilme(JSONObject jsonObject) throws JSONException {
        /**
         *      "id":271110,
         *      "title":"Captain America: Civil War",
         *      "overview":"Following the events of Age of Ultron, the collective governments of the world pass an act designed to regulate all superhuman activity. This polarizes opinion amongst the Avengers, causing two factions to side with Iron Man or Captain America, which causes an epic battle between former allies.",
         *      "release_date":"2016-04-27",
         *      "poster_path":"/2tOgiY533JSFp7OrVlkeRJvsZpI.jpg",
         *      "backdrop_path":"/m5O3SZvQ6EgD5XXXLPIP1wLppeW.jpg",
         *      "vote_average":7.1
         *      "popularity":105.335692,
         */
        this.id = jsonObject.getLong("id");
        this.titulo = jsonObject.getString("title");
        this.descricao = jsonObject.getString("overview");
        setDataLancamento(jsonObject.getString("release_date"));
        this.posterPath = jsonObject.getString("poster_path");
        this.capaPath = jsonObject.getString("backdrop_path");
        this.avaliacao = (float) jsonObject.getDouble("vote_average") / 2;
        this.popularidade = (float) jsonObject.getDouble("popularity");
    }

    private String buildPath(String imageWidth, String imagePath) {
        StringBuilder builder = new StringBuilder();
        builder.append("http://image.tmdb.org/t/p/")
                .append(imageWidth)
                .append(imagePath);
        return builder.toString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDataLancamento() {
        return dataLancamento;
    }

    public void setDataLancamento(String dataLancamento) {

        if ((null == dataLancamento) || (dataLancamento.length() == 0))
            dataLancamento = "";
        else {
            Locale locale = new Locale("pt", "BR");
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd", locale).parse(dataLancamento);
                dataLancamento = new SimpleDateFormat("dd/MMM/yyyy", locale).format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        this.dataLancamento = dataLancamento;
    }

    public String getPosterPath() {
        return buildPath("w500", posterPath);
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getCapaPath() {
        return buildPath("w780", capaPath);
    }

    public void setCapaPath(String capaPath) {
        this.capaPath = capaPath;
    }

    public float getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(float avaliacao) {
        this.avaliacao = avaliacao;
    }

    public float getPopularidade() {
        return popularidade;
    }

    public void setPopularidade(float popularidade) {
        this.popularidade = popularidade;
    }
}
