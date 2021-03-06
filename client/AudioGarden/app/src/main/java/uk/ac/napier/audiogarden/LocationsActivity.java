package uk.ac.napier.audiogarden;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.provider.SyncStateContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

public class LocationsActivity extends AppCompatActivity implements View.OnClickListener {
    private ShowcaseView showcaseGuide;
    private int showcaseCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);
        setupActionBar();
        makeButtons();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(getApplicationContext(), "App requires location permissions, please enable these in settings", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Constants.getUserGuideStatus(this, getString(R.string.user_guide_settings_locations))) {
            showcaseGuide = new ShowcaseView.Builder(this)
                    .setTarget(Target.NONE)
                    .setContentTitle(R.string.guide_locations_location_btn_title)
                    .setContentText(R.string.guide_locations_location_btn_text)
                    .setStyle(R.style.ShowcaseStyleBlue)
                    .setOnClickListener(this)
                    .build();

            showcaseGuide.setButtonText(getString(R.string.guide_close_btn));
            showcaseCounter = Constants.getUserGuidePage(this, getString(R.string.user_guide_settings_locations));

            if (showcaseCounter > -1) {
                Button btn = (Button) findViewById(R.id.showcase_button);
                btn.performClick();
            } else {
                showcaseCounter++;
            }
        }

    }

    @Override
    public void onStop() {
        super.onStop();

        if (showcaseGuide != null) showcaseGuide.hide();
        Constants.setUserGuidePage(this, getString(R.string.user_guide_settings_locations), showcaseCounter);
    }

    @Override
    public void onClick(View view) {
        switch (showcaseCounter){
            case 0: showcaseGuide.hide();
                Constants.setUserGuideStatus(this, getString(R.string.user_guide_settings_locations), false);
                break;
        }
        showcaseCounter++;
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void makeButtons() {
        // make buttons for all locations
        SharedPreferences prefs = getSharedPreferences(getString(R.string.locations_storage), Context.MODE_PRIVATE);
        try {
            JSONObject jsonResponse = new JSONObject(prefs.getString(getString(R.string.locations_storage), ""));

            JSONArray jsonArray = jsonResponse.getJSONArray("locations");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                Button button = new Button(getApplicationContext());
                button.setText(obj.getString("name"));
                RelativeLayout rl = (RelativeLayout)findViewById(R.id.activity_locations);
                RelativeLayout.LayoutParams lParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

                if (i > 0) {
                    lParams.addRule(RelativeLayout.BELOW, i);
                }
                lParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                lParams.setMargins(0, 0, 0, 10);

                button.setLayoutParams(lParams);
                button.setId(i + 1);
                button.setBackgroundResource(R.drawable.buttonstyle);
                rl.addView(button);

                final int location_id = obj.getInt("location_id");

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), LocationActivity.class);
                        intent.putExtra("id", location_id);
                        startActivity(intent);
                    }
                });
            }
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Locations data cannot be found!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }
}
