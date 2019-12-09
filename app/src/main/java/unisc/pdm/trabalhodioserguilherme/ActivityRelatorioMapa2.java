package unisc.pdm.trabalhodioserguilherme;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.HashMap;

public class ActivityRelatorioMapa2 extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private DatabaseHelper helper = new DatabaseHelper(this);
    private String query = "";
    private Cursor cursor;
    private SQLiteDatabase db;
    private String latitude;
    private String longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_relatorio_mapa2);
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

                        cursor = db.rawQuery("SELECT id FROM dados " +
                                        "WHERE latitude = ? AND longitude = ?",
                                new String[]{latitude, longitude});

                        cursor.moveToFirst();

                        Toast.makeText(getApplicationContext(), cursor.getString(0),
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getApplicationContext(),
                                ActivityReproducaoAudio.class);
                        intent.putExtra("idItem", Integer.parseInt(cursor.getString(0)));
                        startActivity(intent);

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
