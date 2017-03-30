package uk.ac.napier.audiogarden;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.skyfishjy.library.RippleBackground;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    private Double maxDistance = -70.0;

    private List<String> scanFilters;
    private Map<String, List<Double>> noiseFilter;

    private int voicePosition;
    private int bgPosition;

    private ImageView foundDevice;
    private ImageView scanning;
    private Intent serviceIntent;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        scanFilters = new ArrayList<>();
        noiseFilter = new HashMap<String, List<Double>>();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

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
                startAnimation();
            }
        });
        setLocation();
        setupReceiver();
        setView();
        startAnimation();
        checkBluetoothState(); // check if bluetooth available
    }

    private void checkBluetoothState() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported", Toast.LENGTH_SHORT).show();
            finish();
        } else if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            mLEScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
            scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            mLEScanner.startScan(null, scanSettings, mScanCallback);
        }
    }

    private void setupReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION.STOP_ACTION);
        filter.addAction(Constants.ACTION.PLAY_ACTION);
        filter.addAction(Constants.ACTION.PAUSE_ACTION);
        filter.addAction(Constants.ACTION.RESET_ACTION);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Constants.ACTION.STOP_ACTION)) {
                    stopSounds();
                } else if (action.equals(Constants.ACTION.PLAY_ACTION)) {
                    resumeSounds();
                } else if (action.equals(Constants.ACTION.PAUSE_ACTION)) {
                    pauseSounds();
                } else if (action.equals(Constants.ACTION.RESET_ACTION)) {
                    rePlaySounds();
                }
            }
        };
        registerReceiver(receiver,filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            checkBluetoothState();
        }
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

    //start notification service
    public void startService() {
        serviceIntent = new Intent(LocationActivity.this, NotificationService.class);
        serviceIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        startService(serviceIntent);
    }

    private void setView() {
        getSupportActionBar().setTitle(location.getLocationName());
    }

    private void setTransmittersIds(JSONArray jsonArray) throws JSONException {
        List<String> locations = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            locations.add(jsonArray.getString(i));
            scanFilters.add(jsonArray.getString(i)); // set transmitters ids
            noiseFilter.put(jsonArray.getString(i), new ArrayList<Double>());   // initialise noise filter with transmitters ids(key) and empty arrays of double
                                                                                // arrays will store distance samples for every transmitters
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

            // validate device id
            if (!scanFilters.contains(newId)) {
                return;
            }

            boolean inRange = calculateDistance(result.getScanRecord().getTxPowerLevel(), result.getRssi(), newId);

            if (inRange) {
                if (volume <= 0 || volume >= 1) {
                    scanFilters.remove(newId); // remove currently played device from list
                    stopAnimation();
                    startService();
                    resetNoiseFilter();
                    playTracks(bgMP, vMP, Integer.toString(location.getId()), newId);
                }
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

    //start ripple animation
    public void startAnimation() {
        final RippleBackground rippleBackground=(RippleBackground)findViewById(R.id.scanning);
        scanning();
        rippleBackground.startRippleAnimation();
        TextView text = (TextView) findViewById(R.id.scanText);
        text.setText(R.string.strSearching);
    }

    //stop ripple animation
    public void stopAnimation() {
        startService();
        final RippleBackground rippleBackground=(RippleBackground)findViewById(R.id.scanning);
        foundDevice();
        rippleBackground.stopRippleAnimation();
        TextView text = (TextView) findViewById(R.id.scanText);
        text.setText(R.string.strFound);
    }

    //set centre image to bluetooth found with pop animation
    private void foundDevice(){
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(400);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        ArrayList<Animator> animatorList=new ArrayList<Animator>();
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(foundDevice, "ScaleX", 0f, 1.2f, 1f);
        animatorList.add(scaleXAnimator);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(foundDevice, "ScaleY", 0f, 1.2f, 1f);
        animatorList.add(scaleYAnimator);
        animatorSet.playTogether(animatorList);
        foundDevice.setVisibility(View.VISIBLE);
        scanning.setVisibility(View.INVISIBLE);
        animatorSet.start();
    }

    //set centre image to bluetooth scanning with pop animation
    private void scanning(){
        foundDevice=(ImageView)findViewById(R.id.foundDevice);
        scanning=(ImageView)findViewById(R.id.centerImage);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(400);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        ArrayList<Animator> animatorList=new ArrayList<Animator>();
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(scanning, "ScaleX", 0f, 1.2f, 1f);
        animatorList.add(scaleXAnimator);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(scanning, "ScaleY", 0f, 1.2f, 1f);
        animatorList.add(scaleYAnimator);
        animatorSet.playTogether(animatorList);
        scanning.setVisibility(View.VISIBLE);
        foundDevice.setVisibility(View.INVISIBLE);
        animatorSet.start();
    }


    private boolean calculateDistance(float txPower, double rssi, String transmitterId) {
        double distance = 0;

        if (rssi == 0) {
            return false;
        }

//        double ratio = rssi * 1.0 / txPower;
//
//        if (ratio < 1.0) {
//            distance = Math.pow(ratio, 10);
//        } else {
//             distance = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
//        }

        noiseFilter.get(transmitterId).add(rssi);
        List<Double> samples = noiseFilter.get(transmitterId);

        if (samples.size() == 10) {
            double sum = 0;
            for (Double sample : samples) {
                sum += sample;
            }

            double resDistance = sum / samples.size();
            Toast.makeText(getApplicationContext(), Double.toString(resDistance), Toast.LENGTH_SHORT).show();
            noiseFilter.get(transmitterId).clear();
            Log.e("distance", Double.toString(resDistance));
            return resDistance > maxDistance;
        }

        return false;
    }

    // remove distance samples for each transmitter
    private void resetNoiseFilter() {
        Iterator it = noiseFilter.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            pair.setValue(new ArrayList<Double>());
            it.remove();
        }
    }

    private void pauseSounds() {
        vMP.pause();
        bgMP.pause();

        voicePosition = vMP.getCurrentPosition();
        bgPosition = bgMP.getCurrentPosition();
    }

    private void resumeSounds() {
        bgMP.seekTo(bgPosition);
        vMP.seekTo(voicePosition);

        bgMP.start();
        vMP.start();
    }

    private void stopSounds() {
        vMP.stop();
        bgMP.stop();
    }

    private void rePlaySounds() {
        bgMP.seekTo(0);
        vMP.seekTo(0);

        bgMP.start();
        vMP.start();
    }

    private void restartLocation() {
        setLocation();
    }

    @Override
    protected void onDestroy() {
        stopSounds();
        serviceIntent = new Intent(LocationActivity.this, NotificationService.class);
        serviceIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        startService(serviceIntent);
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }
}
