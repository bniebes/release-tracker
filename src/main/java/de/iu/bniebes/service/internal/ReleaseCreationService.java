package de.iu.bniebes.service.internal;

import de.iu.bniebes.constant.GlobalConstants;
import de.iu.bniebes.service.external.db.DBService;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ReleaseCreationService {

    private final DBService dbService;

    public Optional<String> create(final String app, final String env, final String version) {
        final var timestamp = Instant.now(); // FIXME use zulu epoch second instead of timestamp
        final var maybeId = dbService.releaseDBService.insert(app, env, version, Instant.now());
        if (maybeId.isEmpty()) return Optional.empty();

        try {
            return Optional.of(timestamp.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        } catch (DateTimeException ex) {
            log.atError()
                    .addMarker(GlobalConstants.Markers.SERVICE)
                    .setCause(ex)
                    .log();
            return Optional.empty();
        }
    }
}
