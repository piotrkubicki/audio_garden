package uk.ac.napier.audiogarden;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public RelativeLayout btnGarden;
    public RelativeLayout btnHelp;
    public RelativeLayout btnMap;

    private ShowcaseView showcaseGuide;
    private int showcaseCounter;

    static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        btnGarden = (RelativeLayout) findViewById(R.id.btnGarden);
        btnGarden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LocationsActivity.class);
                startActivity(intent);
            }
        });

        btnHelp = (RelativeLayout) findViewById(R.id.btnHelp);
        btnHelp.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
               startActivity(intent);
           }
        });

        btnMap = (RelativeLayout) findViewById(R.id.btnMap);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Constants.getUserGuideStatus(this, getString(R.string.user_guide_settings_main))) {
            showcaseGuide = new ShowcaseView.Builder(this)
                    .setTarget(Target.NONE)
                    .setContentTitle(R.string.guide_main_welcome_title)
                    .setContentText(R.string.guide_main_welcome_text)
                    .setStyle(R.style.ShowcaseStyleBlue)
                    .setOnClickListener(this)
                    .build();

            showcaseGuide.setButtonText(getString(R.string.guide_next_btn));
            showcaseCounter = Constants.getUserGuidePage(this, getString(R.string.user_guide_settings_main));

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
        Constants.setUserGuidePage(this, getString(R.string.user_guide_settings_main), showcaseCounter);
    }

    @Override
    public void onClick(View view) {
        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
        lps.setMargins(margin, margin, margin, margin);

        switch (showcaseCounter) {
            case 0: showcaseGuide.setTarget(new ViewTarget(R.id.helpImage, this));
                showcaseGuide.setContentTitle(getString(R.string.guide_main_help_btn_title));
                showcaseGuide.setContentText(getString(R.string.guide_main_help_btn_text));
                showcaseGuide.setButtonPosition(lps);
                break;
            case 1: showcaseGuide.setTarget(new ViewTarget(R.id.mapImage, this));
                showcaseGuide.setContentTitle(getString(R.string.guide_main_map_btn_title));
                showcaseGuide.setContentText(getString(R.string.guide_main_map_btn_text));
                lps.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                showcaseGuide.setButtonPosition(lps);
                break;
            case 2: showcaseGuide.setTarget(new ViewTarget(R.id.locationsImage, this));
                showcaseGuide.setContentTitle(getString(R.string.guide_main_locations_btn_title));
                showcaseGuide.setContentText(getString(R.string.guide_main_locations_btn_text));
                showcaseGuide.setButtonText(getString(R.string.guide_close_btn));
                break;
            case 3:
                showcaseGuide.hide();
                Constants.setUserGuideStatus(this, getString(R.string.user_guide_settings_main), false);
                break;
        }
        showcaseCounter++;
    }

}
