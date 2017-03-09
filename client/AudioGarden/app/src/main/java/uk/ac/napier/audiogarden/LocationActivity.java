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
import android.os.ParcelUuid;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LocationActivity extends AppCompatActivity {

    private Location location;
    private MediaPlayer bgMP, vMP;
    private SharedPreferences sharedPreferences;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner mLEScanner;
    private final int REQUEST_ENABLE_BT = 1;
    private ScanSettings scanSettings;

    private final int FADE_DURATION = 5000;
    private final int FADE_INTERVAL = 250;
    private final int MAX_VOLUME = 1;
    private float volume = 0;

    private List<String> scanFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        scanFilters = new ArrayList<>();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }


        bgMP= new MediaPlayer();
        vMP = new MediaPlayer();

        bgMP.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                fadeIn();
                vMP.prepareAsync();     // prepare voice media player
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

        vMP.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mLEScanner.startScan(null, scanSettings, mScanCallback);
            }
        });

        mLEScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

        setLocation();
        setView();

        mLEScanner.startScan(null, scanSettings, mScanCallback);
    }

    private void setLocation() {
        location = new Location();
        sharedPreferences = getSharedPreferences(getString(R.string.locations_storage), Context.MODE_PRIVATE);
        String locations = sharedPreferences.getString(getString(R.string.locations_storage), "0");
        JSONObject jsonObject;

        if (locations != null) {
            try {
                jsonObject = new JSONObject(locations);
                Intent intent = getIntent();
                int location_id = intent.getIntExtra("id", 0);
                JSONArray jsonArray = jsonObject.getJSONArray("locations");
                List<Double> position = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject temp_location = jsonArray.getJSONObject(i);

                    if (temp_location.getInt("location_id") == location_id) {
                        location.setId(location_id);
                        location.setLocationName(temp_location.getString("name"));

                        JSONObject cord = temp_location.getJSONObject("position");
                        position.add(cord.getDouble("longitude"));
                        position.add(cord.getDouble("latitude"));
                        location.setPosition(position);

                        JSONArray transmittersArray = temp_location.getJSONArray("transmitters");
                        setTransmittersIds(transmittersArray);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void setView() {
        getSupportActionBar().setTitle(location.getLocationName());
    }

    private void setTransmittersIds(JSONArray jsonArray) throws JSONException {
        List<String> locations = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            locations.add(jsonArray.getString(i));
            scanFilters.add(jsonArray.getString(i));
        }
        location.setTransmittersIds(locations);
    }

    private void playTracks(MediaPlayer backgroundMP, MediaPlayer voiceMP, String locationID, String transmitterID) {
        String path = getString(R.string.base_url) + "locations/" + locationID + "/"+ transmitterID;
        mLEScanner.stopScan(mScanCallback);

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

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            if(result == null || result.getDevice() == null)
                return;

            //String newId = result.getScanRecord().getServiceUuids().get(0).toString();
            String newId = result.getDevice().getAddress().toString();

            if (!scanFilters.contains(newId)) {
                return;
            }

            if (volume <= 0 || volume >= 1) {
                scanFilters.remove(newId); // remove currently played device from list
                playTracks(bgMP, vMP, Integer.toString(location.getId()), newId);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e( "BLE", "Discovery onScanFailed: " + errorCode );
            super.onScanFailed(errorCode);
        }
    };
}
