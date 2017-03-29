package uk.ac.napier.audiogarden;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SplashActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SplashActivity.GetLocations().execute();
    }

    private class GetLocations extends AsyncTask<String, Void, String> {

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
                } finally {
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
            if (response == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.server_error), Toast.LENGTH_SHORT).show();
            } else {
                // get shared preferences and store data
                SharedPreferences prefs = getSharedPreferences(getString(R.string.locations_storage), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(getString(R.string.locations_storage), response);
                editor.commit();
            }

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
