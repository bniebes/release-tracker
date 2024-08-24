package de.iu.bniebes.constant;

import java.math.BigInteger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class GlobalConstants {

    public static class Markers {
        public static final Marker APPLICATION = MarkerFactory.getMarker("application");
        public static final Marker DB = MarkerFactory.getMarker("db");
        public static final Marker TEST = MarkerFactory.getMarker("test");
        public static final Marker HTTP = MarkerFactory.getMarker("http");
        public static final Marker SERVICE = MarkerFactory.getMarker("service");
    }

    public static class Directories {
        public static final String SECRET = "/run/secrets";
    }

    public static class Conversion {
        public static final BigInteger SECONDS_TO_NANOS_MULTIPLIER = BigInteger.valueOf(1_000_000_000L);
    }
}
