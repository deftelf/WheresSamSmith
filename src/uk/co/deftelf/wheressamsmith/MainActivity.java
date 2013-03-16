package uk.co.deftelf.wheressamsmith;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Toast;

public class MainActivity extends Activity {
    
    MapFragment map;
    
    ArrayList<Pub> pubs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        map = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map.getMap().setMyLocationEnabled(true);
        
        pubs = new ArrayList<Pub>();
        
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("loc.csv")));
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    Pub pub = new Pub(line);
                    pubs.add(pub);
                }catch (Exception ex) {
                    Log.d("failed", ex.getMessage() + " str: " + line);
                }
            }
            br.close();
        } catch (Exception ex) {
            Log.d("", ex.getMessage());
        }
        
        for (Pub p : pubs) {
            map.getMap().addMarker(new MarkerOptions().position(new LatLng(p.lat, p.lon)).title(p.name).snippet(p.address1));
        }
        
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
    }

    private void navTo(double lat, double lon) {
        String url = "http://maps.google.com/maps?daddr=" + lat + "," + lon + "&dirflg=r";
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,  Uri.parse(url));
        startActivity(intent);
    }

    private void showRegion(Location location) {
        LatLng latlng= new LatLng(location.getLatitude(), location.getLongitude());
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
                Toast.makeText(MainActivity.this, "Navigating to your nearest Sam Smith's, which is " + pub.name, Toast.LENGTH_LONG).show();
                navTo(pub.lat, pub.lon);
                return true;
            }
        });
        return true;
    }

}
