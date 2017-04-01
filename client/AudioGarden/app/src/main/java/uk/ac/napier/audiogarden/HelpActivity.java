package uk.ac.napier.audiogarden;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {
    Switch guideAllSwitch, guideMainSwitch, guideLocationsSwitch, guideLocationSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        setSwitches();
    }

    private void setSwitches() {
        guideAllSwitch = (Switch) findViewById(R.id.switch_guide_all);
        guideMainSwitch = (Switch) findViewById(R.id.switch_guide_main);
        guideLocationsSwitch = (Switch) findViewById(R.id.switch_guide_locations);
        guideLocationSwitch = (Switch) findViewById(R.id.switch_guide_location);

        guideMainSwitch.setChecked(Constants.getUserGuideStatus(this, getString(R.string.user_guide_settings_main)));
        guideLocationsSwitch.setChecked(Constants.getUserGuideStatus(this,getString(R.string.user_guide_settings_locations)));
        guideLocationSwitch.setChecked(Constants.getUserGuideStatus(this,getString(R.string.user_guide_settings_location)));

        if (guideMainSwitch.isChecked() && guideLocationsSwitch.isChecked() && guideLocationSwitch.isChecked()) {
            guideAllSwitch.setChecked(true);
        } else {
            guideAllSwitch.setChecked(false);
        }

        guideMainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    Constants.setUserGuideStatus(getApplicationContext(), getString(R.string.user_guide_settings_main), true);
                } else {
                    Constants.setUserGuideStatus(getApplicationContext(), getString(R.string.user_guide_settings_main), false);
                }
            }
        });

        guideLocationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    Constants.setUserGuideStatus(getApplicationContext(), getString(R.string.user_guide_settings_locations), true);
                } else {
                    Constants.setUserGuideStatus(getApplicationContext(), getString(R.string.user_guide_settings_locations), false);
                }
            }
        });

        guideLocationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    Constants.setUserGuideStatus(getApplicationContext(), getString(R.string.user_guide_settings_location), true);
                } else {
                    Constants.setUserGuideStatus(getApplicationContext(), getString(R.string.user_guide_settings_location), false);
                }
            }
        });

        guideAllSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    guideMainSwitch.setChecked(true);
                    guideLocationSwitch.setChecked(true);
                    guideLocationsSwitch.setChecked(true);
                } else {
                    guideMainSwitch.setChecked(false);
                    guideLocationSwitch.setChecked(false);
                    guideLocationsSwitch.setChecked(false);
                }
            }
        });
    }
}
