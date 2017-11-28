package br.com.conseng.bollyfilmes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qin on 17/11/2017.
 * Array JSON:
 * {
 * "page":1,
 * "total_results":19798,
 * "total_pages":990,
 * "results":[]
 * }
 */

public class JsonUtil {
    public static List<ItemFilme> fromJsonToList(String json) {
        List<ItemFilme> ListaFilmes = new ArrayList<>();
        try {
            JSONObject jsonBase = new JSONObject(json);
            JSONArray resultados = jsonBase.getJSONArray("results");
            for (int i = 0; i < resultados.length(); i++) {
                JSONObject filmeOject = resultados.getJSONObject(i);
                ItemFilme itemFilme = new ItemFilme(filmeOject);
                ListaFilmes.add(itemFilme);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ListaFilmes;
    }
}
