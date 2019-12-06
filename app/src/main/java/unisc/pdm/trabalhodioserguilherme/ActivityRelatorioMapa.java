package unisc.pdm.trabalhodioserguilherme;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public class ActivityRelatorioMapa extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private DatabaseHelper helper = new DatabaseHelper(this);
    private String query = "";
    private Cursor cursor;
    private SQLiteDatabase db;
    private String latitude;
    private String longitude;
    private OutputStream outStream = null;
    private ByteArrayOutputStream byteOutStream = null;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_relatorio_mapa);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                query = "SELECT * FROM dados";

                db = helper.getReadableDatabase();
                cursor = db.rawQuery(query,null);

                cursor.moveToFirst();

                //só faça isso se houver no mínimo um registro no Banco de Dados
                if(cursor.isFirst()) {

                    do {
                        HashMap<String, Object> item = new HashMap<String, Object>();

                        String descricao = cursor.getString(cursor.getColumnIndex("descricao"));
                        String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
                        String longitude = cursor.getString(cursor.getColumnIndex("longitude"));

                        LatLng sydney = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                        googleMap.addMarker(new MarkerOptions().position(sydney).title(descricao));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                    } while (cursor.moveToNext());

                    cursor.close();

                }

                // Listener para quando clicar no mapa
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        /*if(marker.getTitle().equals("Ponto A")){
                            // ABRE ACTIVITY PONTO A
                        }else if(marker.getTitle().equals("Ponto B")){
                            // ABRE ACTIVITY PONTO B
                        }*/
                        //o atributo Title do Marker traz a descrição do Registro
                        /*Toast.makeText(getApplicationContext(), marker.getTitle(),
                                Toast.LENGTH_SHORT).show();*/
                        //marker.showInfoWindow();
                        /*Toast.makeText(getApplicationContext(), marker.getPosition().latitude,
                                Toast.LENGTH_SHORT).show();*/
                        helper = new DatabaseHelper(getApplicationContext());

                        db = helper.getReadableDatabase();

                        latitude = String.valueOf(marker.getPosition().latitude);
                        longitude = String.valueOf(marker.getPosition().longitude);

                        cursor = db.rawQuery("SELECT audio FROM dados " +
                                        "WHERE latitude = ? AND longitude = ?",
                                new String[]{latitude, longitude});

                        cursor.moveToFirst();

                        //a coluna 2 é o áudio BLOB
                        try {
                            outStream = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio_mapa.3gp");
                            byteOutStream = new ByteArrayOutputStream();
                            // writing bytes in to byte output stream
                            byteOutStream.write(cursor.getBlob(0));
                            byteOutStream.writeTo(outStream);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                outStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        /*long resultado = db.("dados", values, "id = ?", where);
                        if (resultado != -1){
                            Toast.makeText(this,"Registro Alterado com Sucesso", Toast.LENGTH_SHORT).show();
                            super.onBackPressed();
                        }else {
                            Toast.makeText(this,"Fracasso ao Alterar", Toast.LENGTH_SHORT).show();
                        }*/

                        //reproducao audio
                        setVolumeControlStream(AudioManager.STREAM_MUSIC);
                        mediaPlayer = new MediaPlayer();

                        try {
                            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio_mapa.3gp");
                            mediaPlayer.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio_mapa.3gp"));

                        mediaPlayer.start();

                        Toast.makeText(getApplicationContext(), "Reprodução de Gravação", Toast.LENGTH_LONG).show();

                        return true;
                    }
                });
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        query = "SELECT * FROM dados";

        SQLiteDatabase db = helper.getReadableDatabase();
        cursor = db.rawQuery(query,null);

        cursor.moveToFirst();

        //só faça isso se houver no mínimo um registro no Banco de Dados
        if(cursor.isFirst()) {

            do {
                HashMap<String, Object> item = new HashMap<String, Object>();

                String descricao = cursor.getString(cursor.getColumnIndex("descricao"));
                String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
                String longitude = cursor.getString(cursor.getColumnIndex("longitude"));

                LatLng sydney = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                googleMap.addMarker(new MarkerOptions().position(sydney).title(descricao));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            } while (cursor.moveToNext());

            cursor.close();

        }
    }
}
