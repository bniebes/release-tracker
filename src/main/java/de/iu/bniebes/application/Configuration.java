package de.iu.bniebes.application;

import static de.iu.bniebes.constant.GlobalConstants.*;

import de.iu.bniebes.configuration.DBConfiguration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Configuration {

    public final DBConfiguration dbConfiguration;

    public Configuration(final EnvironmentAccessor environmentAccessor) {
        log.atInfo()
                .addMarker(Markers.APPLICATION)
                .setMessage("Load configuration")
                .log();
        this.dbConfiguration = DBConfiguration.from(environmentAccessor);
    }
}
