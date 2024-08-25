package de.iu.bniebes.service.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.iu.bniebes.util.TimestampUtils;
import io.helidon.http.HeaderNames;
import io.helidon.http.Status;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import java.time.Instant;

public class UtilHttpServiceV1 implements HttpService {

    private static final String MEDIA_TYPE_JSON = "application/json";

    private record NowResponse(String zuluEpochMicros) {}

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void routing(final HttpRules httpRules) {
        httpRules.get("/zeu/now", this::now);
    }

    private void now(final ServerRequest request, final ServerResponse response) {
        final var micros = TimestampUtils.zuluEpochMicrosOf(Instant.now());
        final var maybeAcceptHeader = request.headers().value(HeaderNames.ACCEPT);
        if (maybeAcceptHeader.isPresent() && maybeAcceptHeader.get().equalsIgnoreCase(MEDIA_TYPE_JSON)) {
            try {
                response.send(mapper.writeValueAsString(new NowResponse(micros.toString())));
            } catch (JsonProcessingException e) {
                response.status(Status.INTERNAL_SERVER_ERROR_500).send();
            }
            return;
        }
        response.send(micros.toString());
    }
}
