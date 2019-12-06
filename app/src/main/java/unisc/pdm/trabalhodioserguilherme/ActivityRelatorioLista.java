package unisc.pdm.trabalhodioserguilherme;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActivityRelatorioLista extends ListActivity implements AdapterView.OnItemClickListener {

    private String[] de = {"id", "descricao"};
    private int[] para = {R.id.idRegistro, R.id.descricaoRegistro};
    private List<HashMap<String, Object>> registros = new ArrayList<>();
    private DatabaseHelper helper;
    private String query = "";
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio_lista);

        helper = new DatabaseHelper(getApplicationContext());

        construirLista();

        //nesse caso há registros no Banco de Dados para serem exibidos
        if(registros.size() > 0) {

            SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), registros, R.layout.linha_relatorio_lista, de, para);

            setListAdapter(adapter);
            getListView().setOnItemClickListener(this);

        } else {

            Toast.makeText(getApplicationContext(), "Não há nenhum registro no Banco de Dados!",
                    Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);

        }


    }

    private void construirLista() {

        query = "SELECT * FROM dados";

        SQLiteDatabase db = helper.getReadableDatabase();
        cursor = db.rawQuery(query,null);

        cursor.moveToFirst();

        //só faça isso se houver no mínimo um registro no Banco de Dados
        if(cursor.isFirst()) {

            do {
                HashMap<String, Object> item = new HashMap<String, Object>();

                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String descricao = cursor.getString(cursor.getColumnIndex("descricao"));
                String data_hora = cursor.getString(cursor.getColumnIndex("data_hora"));

                item.put("id", id);
                item.put("descricao", descricao);
                item.put("data_hora", data_hora);

                registros.add(item);
            } while (cursor.moveToNext());

            cursor.close();

        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String, Object> item = registros.get(position);
        int idItem = (int) item.get("id");
        Intent intent = new Intent(getApplicationContext(), ActivityReproducaoAudio.class);
        intent.putExtra("idItem", idItem);
        startActivity(intent);
    }
}
