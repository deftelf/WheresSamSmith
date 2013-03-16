package uk.co.deftelf.wheressamsmith;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.location.Location;

public class Pub implements Comparable<Pub> {
    
    public static double compareFromLat, compareFromLon;

    public double lat, lon;

    public String name;

    public String address1;

    public Pub(String csv) {
        String[] items = csv.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        name = items[1];
        address1 = items[4];
        lat = Double.parseDouble(items[7]);
        lon = Double.parseDouble(items[8]);
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
