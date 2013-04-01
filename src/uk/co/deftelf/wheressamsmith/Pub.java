package uk.co.deftelf.wheressamsmith;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

public class Pub implements Comparable<Pub> {
    
    public static double compareFromLat, compareFromLon;

    public double lat, lon;

    public String name;

    public String address1;

    public Pub(JSONObject csv) throws Exception {
        name = csv.getString("name");
        address1 = csv.getString("location");
        JSONArray coordinates = csv.getJSONArray("coordinates");
        lat = coordinates.getDouble(1);
        lon = coordinates.getDouble(0);
    }

    @Override
    public String toString() {
        return name;
    }

    public int compareTo(Pub another) {
        float[] resultThis = new float[1];
        float[] resultOther = new float[1];
        Location.distanceBetween(compareFromLat, compareFromLon, lat, lon, resultThis);
        Location.distanceBetween(compareFromLat, compareFromLon, another.lat, another.lon, resultOther);
        return (resultOther[0] > resultThis[0] ? -1 : 1);
    }

}
