package de.iu.bniebes.model;

public enum OptInfo {
    RELEASE_NAME("release-name"),
    DESCRIPTION("description"),
    CHANGES("changes"),
    RESPONSIBILITY("responsibility"),
    BUILD_LOCATION("build-location");

    public final String label;

    OptInfo(final String label) {
        this.label = label;
    }
}
