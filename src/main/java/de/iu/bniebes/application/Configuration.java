package de.iu.bniebes.application;

import static de.iu.bniebes.constant.GlobalConstants.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Configuration {

    public Configuration(final EnvironmentAccessor environmentAccessor) {
        log.atInfo()
                .addMarker(Markers.APPLICATION)
                .setMessage("Load configuration")
                .log();
    }
}
