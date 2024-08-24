package de.iu.bniebes.service.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.iu.bniebes.constant.GlobalConstants;
import de.iu.bniebes.model.result.ReleaseCreateResult;
import de.iu.bniebes.model.result.Result;
import de.iu.bniebes.service.external.db.ReleaseDBService;
import java.time.DateTimeException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ReleaseCreationService {

    private final ReleaseDBService releaseDBService;

    private final ObjectMapper mapper = new ObjectMapper();

    public Result<String> create(final String app, final String env, final String version) {
        try {
            final var timestamp = Instant.now();
            final var maybeId = releaseDBService.insert(app, env, version, timestamp);
            if (maybeId.notPresent()) return Result.error();

            return Result.of(mapper.writeValueAsString(ReleaseCreateResult.of(app, env, version, timestamp)));
        } catch (DateTimeException | JsonProcessingException ex) {
            log.atError()
                    .addMarker(GlobalConstants.Markers.SERVICE)
                    .setCause(ex)
                    .log();
            return Result.error();
        }
    }
}
