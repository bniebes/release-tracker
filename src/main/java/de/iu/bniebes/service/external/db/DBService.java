package de.iu.bniebes.service.external.db;

import de.iu.bniebes.service.external.db.release.ReleaseDBService;
import de.iu.bniebes.service.external.db.release.ReleaseOptInfoService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DBService {

    public final ReleaseDBService releaseDBService;
    public final ReleaseOptInfoService releaseOptInfoService;

    public DBService(final DBClientService dbClientService) {
        this.releaseDBService = new ReleaseDBService(dbClientService.jdbi);
        this.releaseOptInfoService = new ReleaseOptInfoService(dbClientService.jdbi);
    }
}
