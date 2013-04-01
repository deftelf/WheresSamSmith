package uk.co.deftelf.wheressamsmith;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Toast;

public class MainActivity extends Activity {

    MapFragment map;

    ArrayList<Pub> pubs;

    long lastUpdate;

    private static final long UPDATE_PERIOD = 1000L * 60 * 5; // 5min

    private static final String DOWLOADED_DATA_KEY = "data";

    private static final String DOWLOADED_TIME_KEY = "last";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        map = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map.getMap().setMyLocationEnabled(true);

        map.getMap().setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

            public void onInfoWindowClick(Marker arg0) {
                navTo(arg0.getPosition().latitude, arg0.getPosition().longitude);
            }

        });

        map.getMap().setOnMyLocationChangeListener(new OnMyLocationChangeListener() {

            public void onMyLocationChange(Location arg0) {
                if (arg0 != null) {
                    showRegion(arg0);
                    map.getMap().setOnMyLocationChangeListener(null);
                }
            }
        });

        pubs = parseStoredPubs();
        putPubsOnMap();

        if ((System.currentTimeMillis() - getSharedPreferences("", MODE_APPEND).getLong(DOWLOADED_TIME_KEY, 0L)) > UPDATE_PERIOD) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        URL url = new URL("http://www.jamesgretton.co.uk/samuelsmiths/json");
                        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                        String str;
                        StringBuilder wholeStr = new StringBuilder();
                        while ((str = in.readLine()) != null) {
                            wholeStr.append(str + "\n");
                        }
                        in.close();

                        if (wholeStr.length() > 100) {
                            getSharedPreferences("", MODE_APPEND).edit()
                                    .putString(DOWLOADED_DATA_KEY, wholeStr.toString())
                                    .putLong(DOWLOADED_TIME_KEY, System.currentTimeMillis()).commit();
                            final ArrayList<Pub> newpubs = parseStoredPubs();
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    pubs = newpubs;
                                    putPubsOnMap();
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }
    }

    private void putPubsOnMap() {
        map.getMap().clear();
        for (Pub p : pubs) {
            map.getMap().addMarker(
                    new MarkerOptions().position(new LatLng(p.lat, p.lon)).title(p.name).snippet(p.address1));
        }
    }

    private ArrayList<Pub> parseStoredPubs() {

        String data = getSharedPreferences("", MODE_PRIVATE).getString(DOWLOADED_DATA_KEY, null);
        InputStream open = null;

        try {
            if (data != null) {
                open = new ByteArrayInputStream(data.getBytes());
            } else {
                open = getAssets().open("data.json");
            }
            ArrayList<Pub> pubs = parseFromStream(open);
            return pubs;
        } catch (Exception ex) {
            Log.d("", ex.getMessage());
            getSharedPreferences("", MODE_APPEND).edit().putString(DOWLOADED_DATA_KEY, null).commit();
        } finally {
            if (open != null) {
                try {
                    open.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private ArrayList<Pub> parseFromStream(InputStream in) throws Exception {
        ArrayList<Pub> pubs = new ArrayList<Pub>();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder dataStr = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            dataStr.append(line);
        }
        JSONArray dataJson = new JSONArray(dataStr.toString());
        for (int i = 0; i < dataJson.length(); i++) {
            try {
                Pub pub = new Pub(dataJson.getJSONObject(i));
                pubs.add(pub);
            } catch (Exception ex) {
                Log.d("failed", ex.getMessage() + " str: " + line);
            }
        }
        return pubs;
    }

    private void navTo(double lat, double lon) {
        String url = "http://maps.google.com/maps?daddr=" + lat + "," + lon + "&dirflg=r";
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void showRegion(Location location) {
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        map.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.showMe).setOnMenuItemClickListener(new OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {
                showRegion(map.getMap().getMyLocation());
                return true;
            }

        });

        menu.findItem(R.id.navToClosest).setOnMenuItemClickListener(new OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {

                Pub.compareFromLat = map.getMap().getMyLocation().getLatitude();
                Pub.compareFromLon = map.getMap().getMyLocation().getLongitude();
                Collections.sort(pubs);
                Pub pub = pubs.get(0);
                Toast.makeText(MainActivity.this, "Navigating to your nearest Sam Smith's, which is " + pub.name,
                        Toast.LENGTH_LONG).show();
                navTo(pub.lat, pub.lon);
                return true;
            }
        });
        
        menu.findItem(R.id.about).setOnMenuItemClickListener(new OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                return true;
            }

        });
        
        return true;
    }

}
