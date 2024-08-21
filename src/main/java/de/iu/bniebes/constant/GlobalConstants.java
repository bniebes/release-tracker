package de.iu.bniebes.constant;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class GlobalConstants {

    public static class Markers {
        public static final Marker APPLICATION = MarkerFactory.getMarker("application");
    }

    public static class Directories {
        public static final String SECRET = "/run/secrets";
    }
}
