package de.iu.bniebes.application;

import static de.iu.bniebes.constant.GlobalConstants.*;

import de.iu.bniebes.configuration.DBConfiguration;
import de.iu.bniebes.configuration.WebServerConfiguration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Configuration {

    public final WebServerConfiguration webServerConfiguration;
    public final DBConfiguration dbConfiguration;

    public Configuration(final EnvironmentAccessor environmentAccessor) {
        log.atInfo()
                .addMarker(Markers.APPLICATION)
                .setMessage("Load configuration")
                .log();
        this.webServerConfiguration = WebServerConfiguration.from(environmentAccessor);
        this.dbConfiguration = DBConfiguration.from(environmentAccessor);
    }
}
