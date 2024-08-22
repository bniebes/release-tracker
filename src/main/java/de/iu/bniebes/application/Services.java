package de.iu.bniebes.application;

import static de.iu.bniebes.constant.GlobalConstants.*;

import de.iu.bniebes.service.external.db.DBClientService;
import de.iu.bniebes.service.external.db.DBService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Services implements AutoCloseable {

    public final DBClientService dbClientService;

    public Services(final Configuration configuration) {
        log.atInfo().addMarker(Markers.APPLICATION).setMessage("Set-Up").log();
        this.dbClientService = new DBClientService(configuration.dbConfiguration);

        final var dbService = new DBService(dbClientService);
    }

    @Override
    public void close() throws Exception {
        dbClientService.close();
    }
}
