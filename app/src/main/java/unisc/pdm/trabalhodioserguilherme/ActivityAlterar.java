package unisc.pdm.trabalhodioserguilherme;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityAlterar extends AppCompatActivity {

    private String id, descricao, data_hora, latitude, longitude;
    private TextView textViewId;
    private EditText editTextDescricao, editTextDataHora, editTextLatitude,
            editTextLongitude;
    private DatabaseHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar);

        helper = new DatabaseHelper(getApplicationContext());

        textViewId = findViewById(R.id.textViewId);
        editTextDescricao = findViewById(R.id.editTextDescricao);
        editTextDataHora = findViewById(R.id.editTextDataHora);
        editTextLatitude = findViewById(R.id.editTextLatitude);
        editTextLongitude = findViewById(R.id.editTextLongitude);

        Bundle extras = getIntent().getExtras();
        id =  extras.getString("id");
        descricao =  extras.getString("descricao");
        data_hora =  extras.getString("data_hora");
        latitude =  extras.getString("latitude");
        longitude =  extras.getString("longitude");

        textViewId.setText(id);
        editTextDescricao.setText(descricao);
        editTextDataHora.setText(data_hora);
        editTextLatitude.setText(latitude);
        editTextLongitude.setText(longitude);
    }

    public void atualizarInformacoes(View view) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("descricao", editTextDescricao.getText().toString());
        values.put("data_hora",editTextDataHora.getText().toString());
        values.put("latitude", editTextLatitude.getText().toString());
        values.put("longitude", editTextLongitude.getText().toString());

        String where [] = new String[]{id};
        long resultado = db.update("dados", values, "id = ?", where);
        if (resultado != -1){
            Toast.makeText(this,"Registro Alterado com Sucesso", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        }else {
            Toast.makeText(this,"Fracasso ao Alterar", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
