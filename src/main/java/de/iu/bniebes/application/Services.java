package de.iu.bniebes.application;

import static de.iu.bniebes.constant.GlobalConstants.*;

import de.iu.bniebes.service.external.db.DBClientService;
import de.iu.bniebes.service.external.db.ReleaseDBService;
import de.iu.bniebes.service.external.db.ReleaseOptInfoDBService;
import de.iu.bniebes.service.internal.InputSanitizationService;
import de.iu.bniebes.service.internal.ReleaseAccessService;
import de.iu.bniebes.service.internal.ReleaseCreationService;
import de.iu.bniebes.service.internal.ReleaseOptInfoService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Services implements AutoCloseable {

    public final DBClientService dbClientService;

    public final ReleaseDBService releaseDBService;
    public final ReleaseOptInfoDBService releaseOptInfoDBService;
    public final InputSanitizationService inputSanitizationService;
    public final ReleaseCreationService releaseCreationService;
    public final ReleaseAccessService releaseAccessService;
    public final ReleaseOptInfoService releaseOptInfoService;

    public Services(final Configuration configuration) {
        log.atInfo().addMarker(Markers.APPLICATION).setMessage("Set-Up").log();
        this.dbClientService = new DBClientService(configuration.dbConfiguration);

        this.releaseDBService = new ReleaseDBService(dbClientService.jdbi);
        this.releaseOptInfoDBService = new ReleaseOptInfoDBService(dbClientService.jdbi);
        this.inputSanitizationService = new InputSanitizationService();
        this.releaseCreationService =
                new ReleaseCreationService(releaseDBService, releaseOptInfoDBService, inputSanitizationService);
        this.releaseAccessService = new ReleaseAccessService(releaseDBService, releaseOptInfoDBService);
        this.releaseOptInfoService = new ReleaseOptInfoService(releaseDBService, releaseOptInfoDBService);
    }

    @Override
    public void close() throws Exception {
        dbClientService.close();
    }
}
