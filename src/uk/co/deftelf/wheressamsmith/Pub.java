package uk.co.deftelf.wheressamsmith;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pub {

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

}
