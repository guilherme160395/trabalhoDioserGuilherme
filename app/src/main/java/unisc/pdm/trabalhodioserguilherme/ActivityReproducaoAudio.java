/*
    https://stackoverflow.com/questions/24939090/how-to-store-3gp-audio-file-in-sqlite-android

    https://stackoverflow.com/questions/17488534/create-a-file-from-a-bytearrayoutputstream

    http://codeinventions.blogspot.com/2014/08/creating-file-from-bytearrayoutputstrea.html

    http://android-er.blogspot.com/2015/02/create-audio-visualizer-for-mediaplayer.html

    https://www.androdocs.com/java/getting-current-location-latitude-longitude-in-android-using-java.html

    https://stackoverflow.com/questions/40624869/how-to-display-marker-on-given-latitude-and-longitude-values-using-google-map-in
*/

package unisc.pdm.trabalhodioserguilherme;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ActivityReproducaoAudio extends AppCompatActivity {

    private String query;
    private DatabaseHelper helper;
    private Cursor cursor;
    private TextView textViewId, textViewDescricao, textViewDataHora, textViewLatitude, textViewLongitude;
    private Button buttonPlay, buttonStop;
    private ByteArrayOutputStream byteOutStream = null;
    private OutputStream outStream = null;
    private MediaPlayer mediaPlayer;
    private VisualizerView visualizerView;
    private Visualizer visualizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reproducao_audio);

        setTitle("Activity Reprodução Áudio");

        Bundle extra = getIntent().getExtras();

        helper = new DatabaseHelper(getApplicationContext());

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(String.format("SELECT * FROM dados WHERE id = %s", String.valueOf(extra.getInt("idItem"))), null);
        cursor.moveToFirst();

        textViewId = findViewById(R.id.textViewId);
        textViewDescricao = findViewById(R.id.textViewDescricao);
        textViewDataHora = findViewById(R.id.textViewDataHora);
        textViewLatitude = findViewById(R.id.textViewLatitude);
        textViewLongitude = findViewById(R.id.textViewLongitude);
        buttonPlay = findViewById(R.id.botaoPlay);
        buttonStop = findViewById(R.id.botaoStop);
        visualizerView = findViewById(R.id.myvisualizerview);
        /*tvId = (TextView) findViewById(R.id.textViewId);
        modelo = (EditText) findViewById(R.id.editTextModelo);
        valor  = (EditText) findViewById(R.id.editTextValor);
        ano    = (EditText) findViewById(R.id.editTextAno);*/

        textViewId.setText(String.valueOf(cursor.getInt(0)));
        textViewDescricao.setText(cursor.getString(1));

        //a coluna 2 é o áudio BLOB
        try {
            outStream = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio_hora_reproduzir.3gp");
            byteOutStream = new ByteArrayOutputStream();
            // writing bytes in to byte output stream
            byteOutStream.write(cursor.getBlob(2));
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

        textViewDataHora.setText(cursor.getString(3));
        textViewLatitude.setText(cursor.getString(4));
        textViewLongitude.setText(cursor.getString(5));
        /*tvId.setText(String.valueOf(cursor.getInt(0)));
        modelo.setText(cursor.getString(1));
        ano.setText(String.valueOf(cursor.getInt(2)));
        valor.setText(String.valueOf(cursor.getInt(3)));*/

        cursor.close();

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setVolumeControlStream(AudioManager.STREAM_MUSIC);

                buttonStop.setEnabled(true);
                buttonPlay.setEnabled(false);

                mediaPlayer = new MediaPlayer();

                try {
                    mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio_hora_reproduzir.3gp");
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio_hora_reproduzir.3gp"));
                setupVisualizerFxAndUI();
                visualizer.setEnabled(true);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        visualizer.setEnabled(false);
                    }
                });

                mediaPlayer.start();

                Toast.makeText(getApplicationContext(), "Reprodução de Gravação", Toast.LENGTH_LONG).show();

            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                buttonStop.setEnabled(false);
                buttonPlay.setEnabled(true);

                if(mediaPlayer != null){

                    mediaPlayer.stop();
                    mediaPlayer.release();

                }


            }
        });

    }

    public void retornarLista(View view) {
        Intent intent = new Intent(getApplicationContext(), ActivityRelatorioLista.class);
        startActivity(intent);
    }

    private void setupVisualizerFxAndUI() {

        // Create the Visualizer object and attach it to our media player.
        visualizer = new Visualizer(mediaPlayer.getAudioSessionId());
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        visualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                        visualizerView.updateVisualizer(bytes);
                    }

                    public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false);
    }

    public void alterarRegistro(View view) {
        Intent intent = new Intent(getApplicationContext(), ActivityAlterar.class);
        intent.putExtra("id", textViewId.getText().toString());
        intent.putExtra("descricao", textViewDescricao.getText().toString());
        intent.putExtra("data_hora", textViewDataHora.getText().toString());
        intent.putExtra("latitude", textViewLatitude.getText().toString());
        intent.putExtra("longitude", textViewLongitude.getText().toString());
        startActivity(intent);
    }

    public void deletarRegistro(View view) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String where [] = new String[] {textViewId.getText().toString()};

        long resultado = db.delete("dados", "id = ?", where);
        if (resultado != -1){
            Toast.makeText(this, "Registro Excluído com Sucesso", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        }else{
            Toast.makeText(this, "Fracasso ao Excluir", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
