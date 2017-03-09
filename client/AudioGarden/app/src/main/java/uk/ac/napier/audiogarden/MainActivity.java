package uk.ac.napier.audiogarden;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String BASE_URL = "http://192.168.0.9:5000/";
    public RelativeLayout btnGarden;
    public RelativeLayout btnHelp;
    public RelativeLayout btnSettings;
    public RelativeLayout btnMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        btnSettings = (RelativeLayout) findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
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

    /**
     * Created by pz on 23/02/17.
     */

    public static class Location {
        private int id;
        private String name;
        private List<Integer> position;
        private List<String> transmitters;

        public Location(int id, String name, List<Integer> position, List<String> transmitters) {
            setId(id);
            setName(name);
            setPosition(position);
            setTransmitters(transmitters);
        }

        public Location() {};

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public List<Integer> getPosition() {
            return position;
        }

        public void setPosition(List<Integer> position) {
            this.position = position;
        }

        public List<String> getTransmitters() {
            return transmitters;
        }

        public void setTransmitters(List<String> transmitters) {
            this.transmitters = transmitters;
        }
    }
}
