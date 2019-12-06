package unisc.pdm.trabalhodioserguilherme;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String BANCO_DADOS = "trabalho_pdm";
    private static int VERSAO = 2;

    public DatabaseHelper(Context context) {
        super(context, BANCO_DADOS, null, VERSAO);
        Log.d("versao", String.valueOf(VERSAO));
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE dados (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "descricao TEXT, " +
                "audio BLOB, " +
                "data_hora TEXT, " +
                "latitude TEXT, " +
                "longitude TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS dados");

        db.execSQL("CREATE TABLE dados (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "descricao TEXT, " +
                "audio BLOB, " +
                "data_hora TEXT, " +
                "latitude TEXT, " +
                "longitude TEXT);");
    }
}
