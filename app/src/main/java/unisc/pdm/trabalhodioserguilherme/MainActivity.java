package unisc.pdm.trabalhodioserguilherme;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper helper;
    private Button botaoIniciarGravacao, botaoEncerrarGravacao;
    private EditText descricaoAudio;
    private String caminhoSalvarAudioDispositivo = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio_hora_gravar.3gp";
    private MediaRecorder gravadorMidia;
    public static final int solicitarCodigoPermissao = 1;
    private FileInputStream fileInputStream;
    private byte[] buffer;
    private int read;
    private SQLiteDatabase db;
    private ContentValues values;
    private String data_completa;
    private ByteArrayOutputStream baos;
    private byte[] fileByteArray;
    private FusedLocationProviderClient mFusedLocationClient;
    private int PERMISSION_ID = 44;
    private TextView latTextView, lonTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        botaoIniciarGravacao = findViewById(R.id.botaoIniciarGravacao);

        botaoEncerrarGravacao = findViewById(R.id.botaoEncerrarGravacao);
        botaoEncerrarGravacao.setEnabled(false);

        descricaoAudio = findViewById(R.id.descricaoAudio);

        latTextView = findViewById(R.id.textViewLatitude);
        lonTextView = findViewById(R.id.textViewLongitude);

        helper = new DatabaseHelper(getApplicationContext());
        Log.d("Nome do Banco de Dados", helper.getDatabaseName());

        botaoIniciarGravacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checarPermissoes()) {

                    getLastLocation();

                    if(isLocationEnabled() == false) {
                        Toast.makeText(getApplicationContext(), "Ative a Localização!", Toast.LENGTH_SHORT).show();
                    }

                    //------------------------------------ Data e Hora ------------------------------------
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    Date data = new Date();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(data);
                    Date data_atual = cal.getTime();
                    data_completa = dateFormat.format(data_atual);

                    GravadorMidiaPronto();

                    try {
                        gravadorMidia.prepare();
                        gravadorMidia.start();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    botaoIniciarGravacao.setEnabled(false);
                    botaoEncerrarGravacao.setEnabled(true);

                    Toast.makeText(getApplicationContext(), "Gravação Iniciada", Toast.LENGTH_LONG).show();

                } else {
                    solicitarPermissoes();
                }
            }
        });

        botaoEncerrarGravacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gravadorMidia.stop();

                botaoEncerrarGravacao.setEnabled(false);
                botaoIniciarGravacao.setEnabled(true);

                Toast.makeText(getApplicationContext(), "Registro Concluído", Toast.LENGTH_LONG).show();

                //------------------------------------ Buffer ------------------------------------
                try {
                    fileInputStream = new FileInputStream(caminhoSalvarAudioDispositivo);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                baos = new ByteArrayOutputStream();
                buffer = new byte[1024];
                read = 0;
                try {
                    while ((read = fileInputStream.read(buffer)) != -1) {
                        baos.write(buffer, 0, read);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    baos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fileByteArray = baos.toByteArray();

                //------------------------------------ SQLite ------------------------------------
                db = helper.getWritableDatabase();

                values = new ContentValues();
                values.put("descricao", descricaoAudio.getText().toString());
                values.put("audio", fileByteArray);
                values.put("data_hora", data_completa);
                values.put("latitude", latTextView.getText().toString());
                values.put("longitude", lonTextView.getText().toString());

                long resultado = db.insert("dados", null, values);
                if(resultado != -1) {
                    Toast.makeText(getApplicationContext(), "Registro Salvo com Sucesso", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Fracasso ao Salvar", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public boolean checarPermissoes() {

        int permissao_1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int permissao_2 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int permissao_3 = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);
        int permissao_4 = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);

        return permissao_1 == PackageManager.PERMISSION_GRANTED && permissao_2 == PackageManager.PERMISSION_GRANTED && permissao_3 == PackageManager.PERMISSION_GRANTED && permissao_4 == PackageManager.PERMISSION_GRANTED;

    }

    public void GravadorMidiaPronto(){
        gravadorMidia = new MediaRecorder();
        gravadorMidia.setAudioSource(MediaRecorder.AudioSource.MIC);
        gravadorMidia.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        gravadorMidia.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        gravadorMidia.setOutputFile(caminhoSalvarAudioDispositivo);
    }

    private void solicitarPermissoes() {

        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}, solicitarCodigoPermissao);

    }

    public void RelatorioLista(View view) {
        Intent intent = new Intent(getApplicationContext(), ActivityRelatorioLista.class);
        startActivity(intent);
    }

    private boolean isLocationEnabled(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latTextView.setText(mLastLocation.getLatitude()+"");
            lonTextView.setText(mLastLocation.getLongitude()+"");
        }
    };

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    latTextView.setText(location.getLatitude()+"");
                                    lonTextView.setText(location.getLongitude()+"");
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    public void RelatorioMapa(View view) {
        Intent intent = new Intent(getApplicationContext(), ActivityRelatorioMapa.class);
        startActivity(intent);
    }

    public void RelatorioMapa2(View view) {
        Intent intent = new Intent(getApplicationContext(), ActivityRelatorioMapa2.class);
        startActivity(intent);
    }
}