package de.iu.bniebes.util;

import de.iu.bniebes.constant.GlobalConstants;
import de.iu.bniebes.model.result.Result;
import io.helidon.http.HeaderNames;
import io.helidon.http.HeaderValues;
import io.helidon.http.Status;
import io.helidon.webserver.http.ServerResponse;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResponseUtil {

    private ResponseUtil() {}

    public static void onNoSuchElementException(final NoSuchElementException nseEx, final ServerResponse response) {
        log.atError().addMarker(GlobalConstants.Markers.HTTP).setCause(nseEx).log();
        response.status(Status.INTERNAL_SERVER_ERROR_500).send();
    }

    public static void onErrorResult(final String message, final ServerResponse response) {
        log.atError()
                .addMarker(GlobalConstants.Markers.HTTP)
                .setMessage(message)
                .log();
        response.status(Status.INTERNAL_SERVER_ERROR_500).send();
    }

    public static void respondAccordingToResult(
            final Result<String> result, final ServerResponse response, final String errorMsg) {
        if (result.isEmpty()) {
            response.status(Status.NOT_FOUND_404).send();
            return;
        }
        if (result.isError()) {
            onErrorResult(errorMsg, response);
            return;
        }
        response.header(HeaderNames.CONTENT_TYPE, HeaderValues.CONTENT_TYPE_JSON.get())
                .send(result.get());
    }
}
