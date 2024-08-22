package de.iu.bniebes.constant;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class GlobalConstants {

    public static class Markers {
        public static final Marker APPLICATION = MarkerFactory.getMarker("application");
        public static final Marker DB = MarkerFactory.getMarker("db");
        public static final Marker TEST = MarkerFactory.getMarker("test");
    }

    public static class Directories {
        public static final String SECRET = "/run/secrets";
    }
}
