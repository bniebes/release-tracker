package de.iu.bniebes.service.web;

import static de.iu.bniebes.constant.GlobalConstants.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.iu.bniebes.service.internal.InputSanitizationService;
import de.iu.bniebes.util.TimestampUtils;
import io.helidon.http.HeaderNames;
import io.helidon.http.HeaderValues;
import io.helidon.http.Status;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import java.time.Instant;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UtilHttpServiceV1 implements HttpService {

    private static final String MEDIA_TYPE_JSON = HeaderValues.CONTENT_TYPE_JSON.values();

    private record ZuluEpochMicrosResponse(String zuluEpochMicros) {}

    private final ObjectMapper mapper = new ObjectMapper();

    private final InputSanitizationService inputSanitizationService;

    @Override
    public void routing(final HttpRules httpRules) {
        httpRules.get("/zeu/now", this::now).get("/zeu/{datetime}", this::convert);
    }

    private void now(final ServerRequest request, final ServerResponse response) {
        respondZuluEpochMicros(request, response, Instant.now());
    }

    private void convert(final ServerRequest request, final ServerResponse response) {
        try {
            final var parameters = request.path().pathParameters();
            final var maybeDateTime = inputSanitizationService.offsetDateTime(parameters.get("datetime"));
            if (maybeDateTime.isEmpty()) {
                response.status(Status.BAD_REQUEST_400).send();
                return;
            }
            respondZuluEpochMicros(request, response, maybeDateTime.get());
        } catch (NoSuchElementException nseEx) {
            log.atError().addMarker(Markers.HTTP).setCause(nseEx).log();
            response.status(Status.INTERNAL_SERVER_ERROR_500).send();
        }
    }

    private void respondZuluEpochMicros(
            final ServerRequest request, final ServerResponse response, final Instant instant) {
        final var micros = TimestampUtils.zuluEpochMicrosOf(instant);
        if (acceptJson(request)) {
            try {
                response.header(HeaderNames.CONTENT_TYPE, MEDIA_TYPE_JSON)
                        .send(mapper.writeValueAsString(new ZuluEpochMicrosResponse(micros.toString())));
            } catch (JsonProcessingException e) {
                response.status(Status.INTERNAL_SERVER_ERROR_500).send();
            }
        } else {
            response.send(micros.toString());
        }
    }

    private boolean acceptJson(final ServerRequest request) {
        return request.headers()
                .value(HeaderNames.ACCEPT)
                .map(value -> value.equalsIgnoreCase(MEDIA_TYPE_JSON))
                .orElse(false);
    }
}
