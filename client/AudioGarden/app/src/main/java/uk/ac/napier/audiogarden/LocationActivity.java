package uk.ac.napier.audiogarden;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

public class LocationActivity extends AppCompatActivity {
    public Button btnStart1, btnStart2;
    public MediaPlayer bgMP, vMP;
    public final int VOICE_DELAY = 3000;
    public String base_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        base_url = sharedPreferences.getString("BASE_URL", null);

        btnStart1 = (Button) findViewById(R.id.btnStart1); // TO DELETE
        btnStart2 = (Button) findViewById(R.id.btnStart2); // TO DELETE

        bgMP= new MediaPlayer();
        vMP = new MediaPlayer();

        // TO DELETE
        btnStart1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTracks(1, 1);
            }
        });

        // TO DELETE
        btnStart2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTracks(1, 2);
            }
        });

    }

    public void playTracks(int locationID, int transmitterID) {
        String path = "http://192.168.0.9:5000/locations/"+ locationID + "/"+ transmitterID;

        try {
            if (!vMP.isPlaying()) {
                bgMP.stop();
                bgMP.reset();
                bgMP.setDataSource(getApplicationContext(), Uri.parse(path + "/background"));
                bgMP.setAudioStreamType(AudioManager.STREAM_MUSIC);
                bgMP.setLooping(true);

                vMP.stop();
                vMP.reset();
                vMP.setDataSource(getApplicationContext(), Uri.parse(path + "/voice"));
                vMP.setAudioStreamType(AudioManager.STREAM_MUSIC);

                bgMP.prepareAsync();
            } else {
                Toast.makeText(getApplicationContext(), "Voice is playing", Toast.LENGTH_SHORT).show();
            }

            bgMP.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    Toast.makeText(getApplicationContext(), "BG Starting", Toast.LENGTH_SHORT).show();

                    mp.start();
                    vMP.prepareAsync();
                }
            });

            vMP.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    synchronized (mp) {
                        try {
                            mp.wait(VOICE_DELAY);
                            mp.start();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
