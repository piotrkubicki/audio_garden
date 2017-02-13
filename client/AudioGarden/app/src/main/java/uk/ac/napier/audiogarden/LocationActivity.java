package uk.ac.napier.audiogarden;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

public class LocationActivity extends AppCompatActivity {
    public Button btnStart1, btnStart2;
    private MediaPlayer bgMP, vMP;
    //private BluetoothAdapter bluetoothAdapter;
    //private Handler mHandler;
    //private static final long SCAN_PERIOD = 10000;
    //private BluetoothLeScanner mLEScanner;
    //private ScanSettings scanSettings;
    //private List<ScanFilter> scanFilters;
    //private BluetoothGatt mGatt;
    //private final int REQUEST_ENABLE_BT = 1;
    private String beaconID = "0";

    private final int FADE_DURATION = 5000;
    private final int FADE_INTERVAL = 250;
    private final int MAX_VOLUME = 1;
    private float volume = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        /*mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }*/

        btnStart1 = (Button) findViewById(R.id.btnStart1); // TO DELETE
        btnStart2 = (Button) findViewById(R.id.btnStart2); // TO DELETE

        bgMP= new MediaPlayer();
        vMP = new MediaPlayer();

        // TO DELETE
        btnStart1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ignore beacon if last played or fade in or fade out not finished
                if (beaconID != "1" && (volume <= 0 || volume >= 1)) {
                    playTracks(bgMP, vMP, 1, 1);
                    beaconID = "1";
                }
            }
        });

        // TO DELETE
        btnStart2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ignore beacon if last played or fade in or fade out not finished
                if (beaconID != "2" && (volume <= 0 || volume >= 1)) {
                    playTracks(bgMP, vMP, 1, 2);
                    beaconID = "2";
                }
            }
        });

        bgMP.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                Toast.makeText(getApplicationContext(), "BG Starting", Toast.LENGTH_SHORT).show();

                mp.start();
                fadeIn();

                //vMP.prepareAsync();     // prepare voice media player
            }
        });

        vMP.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                synchronized (mp) {
                    try {
                        // allow background sound build up and run voice media player
                        mp.wait(FADE_DURATION + 1000);
                        mp.start();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }


    private void playTracks(MediaPlayer backgroundMP, MediaPlayer voiceMP, int locationID, int transmitterID) {
        String path = getString(R.string.base_url) + "locations/" + locationID + "/"+ transmitterID;

        try {
            // Request new sounds only if voice record is not playing
            if (!voiceMP.isPlaying()) {
                // fadeout background music if playing
                if (bgMP.isPlaying()) {
                    fadeOut();
                    synchronized (bgMP) {
                        bgMP.wait(FADE_DURATION + 1000);
                    }
                }
                // set backgound media player
                backgroundMP.stop();
                backgroundMP.reset();
                backgroundMP.setDataSource(getApplicationContext(), Uri.parse(path + "/background"));
                backgroundMP.setAudioStreamType(AudioManager.STREAM_MUSIC);
                backgroundMP.setLooping(true);

                // set voice media player
                voiceMP.stop();
                voiceMP.reset();
                voiceMP.setDataSource(getApplicationContext(), Uri.parse(path + "/voice"));
                voiceMP.setAudioStreamType(AudioManager.STREAM_MUSIC);

                backgroundMP.prepareAsync();
            } else {
                Toast.makeText(getApplicationContext(), "Voice is playing", Toast.LENGTH_SHORT).show();
            }
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void fadeIn() {
        int numberOfSteps = FADE_DURATION / FADE_INTERVAL;
        final float deltaVolume = MAX_VOLUME / (float) numberOfSteps;

        final Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                fadeStep(deltaVolume);
                if (volume >= 1f) {
                    timer.cancel();
                    timer.purge();
                }
            }
        };

        timer.schedule(timerTask, FADE_INTERVAL, FADE_INTERVAL);
    }

    private void fadeOut() {
        int numberOfSteps = FADE_DURATION / FADE_INTERVAL;
        final float deltaVolume = MAX_VOLUME / (float) numberOfSteps;

        final Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                fadeStep(-deltaVolume);
                if (volume <= 0) {
                    timer.cancel();
                    timer.purge();
                }
            }
        };

        timer.schedule(timerTask, FADE_INTERVAL, FADE_INTERVAL);
    }

    private void fadeStep(float deltaVolume) {
        bgMP.setVolume(volume, volume);
        volume += deltaVolume;
    }

}
