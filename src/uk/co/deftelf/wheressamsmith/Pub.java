package uk.co.deftelf.wheressamsmith;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

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

    public static boolean sanityCheck(ArrayList<Pub> newpubs) {
        int good = 0;
        int bad = 0;
        for (Pub pub : newpubs) {
            if (!TextUtils.isEmpty(pub.name) &&
                    pub.lat > 48 && pub.lat < 60 &&
                    pub.lon > -8 && pub.lon < 3) {
                good++;
            }
            else {
                bad++;
            }
        }
        MainActivity.log("Sanity check downloaded pubs: good = " + good + " bad = " + bad);
        return good > bad;
    }

}
