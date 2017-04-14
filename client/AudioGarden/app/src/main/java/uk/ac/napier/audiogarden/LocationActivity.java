package uk.ac.napier.audiogarden;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.skyfishjy.library.RippleBackground;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class LocationActivity extends AppCompatActivity implements View.OnClickListener {

    private FloatingActionButton pausePlayBtn, stopReplayBtn, resetLocBtn;
    private Location location;
    private MediaPlayer bgMP, vMP, introMP;
    private SharedPreferences sharedPreferences;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner mLEScanner;
    private final int REQUEST_ENABLE_BT = 1;
    private ScanSettings scanSettings;

    private final int FADE_DURATION = 5000;         // duration of fade effects
    private final int FADE_INTERVAL = 250;
    private final int MAX_VOLUME = 1;
    private float volume = 0;
    private Double maxDistance = 0.0;         // distance read that trigger server audio stream requests

    private List<String> scanFilters;               // list of valid devices
    private Map<String, List<Double>> noiseFilter;  // stores distance samples of each valid device
    private String lastTrack = null;

    private int voicePosition, bgPosition;

    private ImageView foundDevice;
    private ImageView scanning;

    private boolean firstPlay = true;               // used to pause scanning when pause btn pressed for the first time

    private Intent serviceIntent;
    private BroadcastReceiver receiver;

    private ShowcaseView showcaseGuide;
    private int showcaseCounter;

    public enum AnimMode {
        FOUND, REPLAY, PAUSE, DESTROY, PLAY, NO_MORE_DEVICES
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        scanFilters = new ArrayList<>();
        noiseFilter = new HashMap<String, List<Double>>();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        setupReceiver();
        startService();

        maxDistance = Double.parseDouble(getString(R.string.max_distance));

        bgMP= new MediaPlayer();
        vMP = new MediaPlayer();

        bgMP.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                fadeIn();
                mp.start();
                pausePlayBtn.setImageDrawable(getDrawable(R.drawable.ic_pause_black_52dp));
                stopReplayBtn.setImageDrawable(getDrawable(R.drawable.ic_stop_black_52dp));

                // prevent error when second media player is not set
                try {
                    vMP.prepareAsync();     // prepare voice media player
                } catch (IllegalStateException e) {
                    return;
                }
            }
        });

        vMP.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                synchronized (mp) {
                    try {
                        // allow background sound build up and run voice media player
                        mp.wait(FADE_DURATION + 1000);
                        mp.start();
                        serviceIntent = new Intent(LocationActivity.this, NotificationService.class);
                        serviceIntent.setAction(Constants.ACTION.ENABLE_STOP_REPLAY);
                        startService(serviceIntent);
                        stopReplayBtn.setEnabled(true);

                        serviceIntent = new Intent(LocationActivity.this, NotificationService.class);
                        serviceIntent.setAction(Constants.ACTION.ENABLE_PLAY_PAUSE);
                        startService(serviceIntent);
                        pausePlayBtn.setEnabled(true);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        vMP.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (!firstPlay) runScanner();
                else firstPlay = false;
                stopReplayBtn.setImageDrawable(getDrawable(R.drawable.ic_replay_black_52dp));
            }
        });

        pausePlayBtn = (FloatingActionButton) findViewById(R.id.pause_play_btn);
        pausePlayBtn.setImageDrawable(getDrawable(R.drawable.ic_play_arrow_black_52dp));
        pausePlayBtn.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.fab_ripple_color));
        pausePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bgMP.isPlaying()) {
                    pauseSounds();
                } else {
                    resumeSounds();
                }
            }
        });

        serviceIntent = new Intent(LocationActivity.this, NotificationService.class);
        serviceIntent.setAction(Constants.ACTION.DISABLE_STOP_REPLAY);
        startService(serviceIntent);

        stopReplayBtn = (FloatingActionButton) findViewById(R.id.stop_replay_btn);
        stopReplayBtn.setImageDrawable(getDrawable(R.drawable.ic_replay_black_52dp));
        stopReplayBtn.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.fab_ripple_color));
        stopReplayBtn.setEnabled(false);
        stopReplayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bgMP.isPlaying()) {
                    stopSounds();
                } else {
                    replaySounds();
                }
            }
        });

        resetLocBtn = (FloatingActionButton) findViewById(R.id.reset_loc_btn);
        resetLocBtn.setImageDrawable(getDrawable(R.drawable.ic_restore_black_52dp));
        resetLocBtn.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.fab_ripple_color));
        resetLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartLocation();
            }
        });
        setLocation();
        setView();

        // don't run if first use
        if (!Constants.getUserGuideStatus(this, getString(R.string.user_guide_settings_location))) {
            checkBluetoothState(); // check if bluetooth available
            playIntro(bgMP, Integer.toString(location.getId()));
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        if (Constants.getUserGuideStatus(this, getString(R.string.user_guide_settings_location))) {
            showcaseGuide = new ShowcaseView.Builder(this)
                    .setTarget(Target.NONE)
                    .setContentTitle(R.string.guide_location_title)
                    .setContentText(R.string.guide_location_text)
                    .setStyle(R.style.ShowcaseStyleBlue)
                    .setOnClickListener(this)
                    .build();

            showcaseGuide.setButtonText(getString(R.string.guide_next_btn));
            showcaseCounter = Constants.getUserGuidePage(this, getString(R.string.user_guide_settings_location));

            if (showcaseCounter > -1) {
                Button btn = (Button) findViewById(R.id.showcase_button);
                btn.performClick();
            } else {
                showcaseCounter++;
            }
        }
    }

    @Override
    public void onClick(View view) {
        //change guide button position
        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
        lps.setMargins(margin, margin, margin, margin);

        switch (showcaseCounter) {
            case 0:
                showcaseGuide.setTarget(new ViewTarget(R.id.foundDevice, this));
                showcaseGuide.setContentTitle(getString(R.string.guide_location_scanner_status_indicator_title));
                showcaseGuide.setContentText(getString(R.string.guide_location_scanner_status_indicator_text));

                checkBluetoothState();
                playIntro(bgMP, Integer.toString(location.getId()));
                break;
            case 1:
                showcaseGuide.setTarget(new ViewTarget(R.id.stop_replay_btn, this));
                showcaseGuide.setContentTitle(getString(R.string.guide_location_stop_replay_btn_title));
                showcaseGuide.setContentText(getString(R.string.guide_location_stop_replay_btn_text));
                break;
            case 2:
                showcaseGuide.setTarget(new ViewTarget(R.id.pause_play_btn, this));
                showcaseGuide.setContentTitle(getString(R.string.guide_location_pause_start_btn_title));
                showcaseGuide.setContentText(getString(R.string.guide_location_pause_start_btn_text));
                break;
            case 3:
                showcaseGuide.setTarget(new ViewTarget(R.id.reset_loc_btn, this));
                showcaseGuide.setContentTitle(getString(R.string.guide_location_reload_loc_btn_title));
                showcaseGuide.setContentText(getString(R.string.guide_location_reload_loc_btn_text));
                showcaseGuide.setButtonPosition(lps);
                break;
            case 4:
                showcaseGuide.setTarget(Target.NONE);
                showcaseGuide.setContentTitle(getString(R.string.guide_location_notification_bar_title));
                showcaseGuide.setContentText(getString(R.string.guide_location_notification_bar_text));
                showcaseGuide.setButtonText(getString(R.string.guide_close_btn));
                lps.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                showcaseGuide.setButtonPosition(lps);
                break;
            case 5:
                showcaseGuide.hide();
                Constants.setUserGuideStatus(this, getString(R.string.user_guide_settings_location), false);
                break;
        }
        showcaseCounter++;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (showcaseGuide != null) showcaseGuide.hide();
        Constants.setUserGuidePage(this, getString(R.string.user_guide_settings_location), showcaseCounter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bgMP.isPlaying()) {
            bgMP.stop();
            bgMP.reset();
            bgMP.release();
            bgMP = null;
        }

        if (vMP.isPlaying()) {
            vMP.stop();
            vMP.reset();
            vMP.release();
            vMP = null;
        }

        serviceIntent = new Intent(LocationActivity.this, NotificationService.class);
        serviceIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        startService(serviceIntent);
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }

        stopScanner(AnimMode.DESTROY);
    }

    private  void runScanner() {
        if (scanFilters.size() < 1) {
            stopScanner(AnimMode.NO_MORE_DEVICES);
            return;
        }

        mLEScanner.startScan(null, scanSettings, mScanCallback);
        startAnimation();
    }

    private void stopScanner(AnimMode mode) {
        try {
            mLEScanner.stopScan(mScanCallback);
            stopAnimation(mode);
        } catch (NullPointerException e) {
            return;
        }
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
            runScanner();
        }
    }

    private void setupReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION.STOP_ACTION);
        filter.addAction(Constants.ACTION.REPLAY_ACTION);
        filter.addAction(Constants.ACTION.PLAY_ACTION);
        filter.addAction(Constants.ACTION.PAUSE_ACTION);
        filter.addAction(Constants.ACTION.RESET_ACTION);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Constants.ACTION.STOP_ACTION)) {
                    stopSounds();
                } else if (action.equals(Constants.ACTION.REPLAY_ACTION)) {
                    replaySounds();
                } else if (action.equals(Constants.ACTION.PLAY_ACTION)) {
                    resumeSounds();
                } else if (action.equals(Constants.ACTION.PAUSE_ACTION)) {
                    pauseSounds();
                } else if (action.equals(Constants.ACTION.RESET_ACTION)) {
                    restartLocation();
                }
            }
        };
        registerReceiver(receiver,filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == 0) {
                Intent intent = new Intent(getApplicationContext(), LocationsActivity.class); //go back if user select no
                startActivity(intent);
                finish();
            } else {
                checkBluetoothState();
            }
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

    private void playIntro(MediaPlayer introMp, String locationID) {
        String path = getString(R.string.base_url) + "locations/" + locationID + "/intro";

        if (introMp.isPlaying()) {
            return;     // abort if currently playing
        }

        try {
            introMp.stop();
            introMp.reset();
            introMp.setDataSource(getApplicationContext(), Uri.parse(path));
            introMp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            introMp.setLooping(true);
            introMp.prepareAsync();
        } catch (IOException e) {
            return;
        }
    }

    private void playTracks(MediaPlayer backgroundMP, MediaPlayer voiceMP, String locationID, String transmitterID, AnimMode animMode) {
        String path = getString(R.string.base_url) + "locations/" + locationID + "/"+ transmitterID;
        stopScanner(animMode);
        serviceIntent = new Intent(LocationActivity.this, NotificationService.class);
        serviceIntent.setAction(Constants.ACTION.DISABLE_PLAY_PAUSE);
        startService(serviceIntent);
        pausePlayBtn.setEnabled(false); //prevent user from stop until voice mp ready

        serviceIntent = new Intent(LocationActivity.this, NotificationService.class);
        serviceIntent.setAction(Constants.ACTION.DISABLE_STOP_REPLAY);
        startService(serviceIntent);
        stopReplayBtn.setEnabled(false);

        try {
            // Request new sounds only if voice record is not playing
            if (!voiceMP.isPlaying()) {
                // fadeout background music if playing
                if (backgroundMP.isPlaying()) {
                    fadeOut();
                    synchronized (backgroundMP) {
                        backgroundMP.wait(FADE_DURATION + 1000);
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
        if (bgMP != null) {
            try {
                bgMP.setVolume(volume, volume);
                volume += deltaVolume;
            } catch (IllegalStateException e) {
                return;
            }
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if(result == null || result.getDevice() == null)
                return;

            String newId = result.getDevice().getAddress().toString();

            // validate device id
            if (!scanFilters.contains(newId)) {
                return;
            }

            boolean inRange = calculateDistance(result.getScanRecord().getTxPowerLevel(), result.getRssi(), newId);

            if (inRange) {
                if (volume <= 0 || volume >= 1) {
                    scanFilters.remove(newId); // remove currently played device from list
                    resetNoiseFilter();
                    lastTrack = newId;
                    playTracks(bgMP, vMP, Integer.toString(location.getId()), newId, AnimMode.FOUND);
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

    public void stopAnimation(AnimMode mode) {
        final RippleBackground rippleBackground=(RippleBackground)findViewById(R.id.scanning);
        foundDevice();
        rippleBackground.stopRippleAnimation();
        TextView text = (TextView) findViewById(R.id.scanText);

        if (mode == AnimMode.FOUND) {
            text.setText(R.string.strFound);
        } else if (mode == AnimMode.REPLAY) {
            text.setText(R.string.strReplay);
        } else if (mode == AnimMode.PAUSE) {
            text.setText(R.string.strPaused);
        } else if (mode == AnimMode.PLAY) {
            text.setText(R.string.strPlay);
        } else if (mode == AnimMode.NO_MORE_DEVICES) {
            text.setText(R.string.strRestartLoc);
        }
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

        if (rssi == 0) {
            return false;
        }
        
        List<Double> samples = noiseFilter.get(transmitterId);

        if (samples != null) {
            samples.add(rssi);

            if (samples.size() == 10) {
                double sum = 0;
                for (Double sample : samples) {
                    sum += sample;
                }

                double resDistance = sum / samples.size();
                Toast.makeText(getApplicationContext(), Double.toString(resDistance), Toast.LENGTH_SHORT).show();
                noiseFilter.get(transmitterId).clear();

                return resDistance > maxDistance;
            }
        }

        return false;
    }

    // remove distance samples for each transmitter
    private void resetNoiseFilter() {
        for (List<Double> l : noiseFilter.values()) {
            l.clear();
        }
    }

    private void pauseSounds() {
        vMP.pause();
        bgMP.pause();
        voicePosition = vMP.getCurrentPosition();
        bgPosition = bgMP.getCurrentPosition();
        stopScanner(AnimMode.PAUSE);

        FloatingActionButton playBtn = (FloatingActionButton) findViewById(R.id.pause_play_btn);
        playBtn.setImageDrawable(getDrawable(R.drawable.ic_play_arrow_black_52dp));
        FloatingActionButton resumeBtn = (FloatingActionButton) findViewById(R.id.stop_replay_btn);
        resumeBtn.setImageDrawable(getDrawable(R.drawable.ic_replay_black_52dp));

        serviceIntent = new Intent(LocationActivity.this, NotificationService.class);
        serviceIntent.setAction(Constants.ACTION.SEND_PAUSE_ACTION);
        startService(serviceIntent);
    }

    private void resumeSounds() {
        bgMP.seekTo(bgPosition);
        vMP.seekTo(voicePosition);

        bgMP.start();
        vMP.start();

        if (vMP.isPlaying()) {
            stopScanner(AnimMode.PLAY);
        } else {
            runScanner();
        }

        FloatingActionButton pauseBtn = (FloatingActionButton) findViewById(R.id.pause_play_btn);
        pauseBtn.setImageDrawable(getDrawable(R.drawable.ic_pause_black_52dp));
        FloatingActionButton stopBtn = (FloatingActionButton) findViewById(R.id.stop_replay_btn);
        stopBtn.setImageDrawable(getDrawable(R.drawable.ic_stop_black_52dp));

        serviceIntent = new Intent(LocationActivity.this, NotificationService.class);
        serviceIntent.setAction(Constants.ACTION.SEND_PLAY_ACTION);
        startService(serviceIntent);
    }

    private void stopSounds() {
        vMP.stop();
        bgMP.stop();
        runScanner();

        FloatingActionButton replayBtn = (FloatingActionButton) findViewById(R.id.stop_replay_btn);
        replayBtn.setImageDrawable(getDrawable(R.drawable.ic_replay_black_52dp));
        FloatingActionButton playBtn = (FloatingActionButton) findViewById(R.id.pause_play_btn);
        playBtn.setEnabled(false);

        serviceIntent = new Intent(LocationActivity.this, NotificationService.class);
        serviceIntent.setAction(Constants.ACTION.SEND_STOP_ACTION);
        startService(serviceIntent);

        serviceIntent = new Intent(LocationActivity.this, NotificationService.class);
        serviceIntent.setAction(Constants.ACTION.DISABLE_PLAY_PAUSE);
        startService(serviceIntent);
    }

    private void replaySounds() {
        if (lastTrack != null) {
            playTracks(bgMP, vMP, Integer.toString(location.getId()), lastTrack, AnimMode.REPLAY);

            FloatingActionButton btn = (FloatingActionButton) findViewById(R.id.stop_replay_btn);
            btn.setImageDrawable(getDrawable(R.drawable.ic_stop_black_52dp));
            FloatingActionButton pauseBtn = (FloatingActionButton) findViewById(R.id.pause_play_btn);
            pauseBtn.setImageDrawable(getDrawable(R.drawable.ic_pause_black_52dp));

            serviceIntent = new Intent(LocationActivity.this, NotificationService.class);
            serviceIntent.setAction(Constants.ACTION.SEND_REPLAY_ACTION);
            startService(serviceIntent);
        }
    }

    private void restartLocation() {
        setLocation();
        serviceIntent = new Intent(LocationActivity.this, NotificationService.class);
        serviceIntent.setAction(Constants.ACTION.SEND_RESET_ACTION);
        startService(serviceIntent);
        runScanner();
    }
}
