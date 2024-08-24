package de.iu.bniebes.service.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.iu.bniebes.constant.GlobalConstants;
import de.iu.bniebes.model.result.ReleaseCreateResult;
import de.iu.bniebes.service.external.db.DBService;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ReleaseCreationService {

    private final DBService dbService;

    private final ObjectMapper mapper = new ObjectMapper();

    public Optional<String> create(final String app, final String env, final String version) {
        try {
            final var timestamp = Instant.now();
            final var maybeId = dbService.releaseDBService.insert(app, env, version, timestamp);
            if (maybeId.isEmpty()) return Optional.empty();

            return Optional.of(mapper.writeValueAsString(ReleaseCreateResult.of(app, env, version, timestamp)));
        } catch (DateTimeException | JsonProcessingException ex) {
            log.atError()
                    .addMarker(GlobalConstants.Markers.SERVICE)
                    .setCause(ex)
                    .log();
            return Optional.empty();
        }
    }
}
