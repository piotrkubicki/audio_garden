package uk.ac.napier.audiogarden;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

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

public class LocationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);

        new GetLocations().execute();
    }

    private  class GetLocations extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            try {
                url = new URL(getString(R.string.base_url) + "locations");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                try {
                    connection.setRequestMethod("GET");

                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }

                    br.close();

                    return result.toString();
                }
                finally {
                    connection.disconnect();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray jsonArray = jsonResponse.getJSONArray("locations");

                // make buttons for all locations
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject obj = jsonArray.getJSONObject(i);

                    Button button = new Button(getApplicationContext());
                    button.setText(obj.getString("name"));
                    RelativeLayout rl = (RelativeLayout)findViewById(R.id.activity_locations);
                    RelativeLayout.LayoutParams lParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);

                    if (i > 0) {
                        lParams.addRule(RelativeLayout.BELOW, i);
                    }
                    lParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

                    button.setLayoutParams(lParams);
                    button.setId(i + 1);

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
                e.printStackTrace();
            }
            // get shared preferences and store data
            SharedPreferences prefs = getSharedPreferences(getString(R.string.locations_storage), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.locations_storage), response);
            editor.commit();

        }
    }
}
