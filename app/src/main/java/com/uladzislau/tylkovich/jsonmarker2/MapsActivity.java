package com.uladzislau.tylkovich.jsonmarker2;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapsActivity extends FragmentActivity {
    private static final String LOG_TAG = "Mymistake";

    private static final String SERVICE_URL = "https://aqueous-depths-77407.herokuapp.com/earthquakes.json";

    protected GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (map == null) {

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(new OnMapReadyCallback() {

                @Override
                public void onMapReady(GoogleMap googlemap) {
                    map = googlemap;
                    if (map != null) {
                        setUpMap();

                        LatLng california = new LatLng(34.049993,-118.246038);

                        CameraPosition target = CameraPosition.builder().target(california).zoom(5).build();
                        map.moveCamera(CameraUpdateFactory.newCameraPosition(target));

                    }
                }
            });
        }
    }

    private void setUpMap() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    retrieveAndAddCities();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Cannot retrive cities", e);
                    return;
                }
            }
        }).start();
    }

    protected void retrieveAndAddCities() throws IOException {
        HttpURLConnection conn = null;
        final StringBuilder json = new StringBuilder();
        try {
            URL url = new URL(SERVICE_URL);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                json.append(buff, 0, read);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to service", e);
            throw new IOException("Error connecting to service", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    createMarkersFromJson(json.toString());
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error processing JSON", e);
                }
            }
        });
    }

    void createMarkersFromJson(String json) throws JSONException {
        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0; i < jsonArray.length(); i++) {
            // Create a marker for each city in the JSON data.
            JSONObject jsonObj = jsonArray.getJSONObject(i);
            map.addMarker(new MarkerOptions()
                    .title(jsonObj.getString("place"))
                    .snippet(Integer.toString(jsonObj.getInt("id")))
                    .position(new LatLng(
                            jsonObj.getDouble("latitude"),
                            jsonObj.getDouble("longitude")
                    ))
            );
        }
    }

}
