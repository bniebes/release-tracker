package de.iu.bniebes.service.web;

import de.iu.bniebes.constant.GlobalConstants;
import de.iu.bniebes.service.internal.InputSanitizationService;
import de.iu.bniebes.service.internal.ReleaseCreationService;
import io.helidon.http.Status;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ReleaseHttpServiceV1 implements HttpService {

    private final InputSanitizationService inputSanitizationService;
    private final ReleaseCreationService releaseCreationService;

    @Override
    public void routing(final HttpRules httpRules) {
        httpRules.post("/{app}/{env}/{ver}", this::create);
    }

    private void create(final ServerRequest request, final ServerResponse response) {
        try {
            final var parameters = request.path().pathParameters();
            final var maybeApp = inputSanitizationService.safeString(parameters.get("app"));
            final var maybeEnv = inputSanitizationService.safeString(parameters.get("env"));
            final var maybeVer = inputSanitizationService.safeString(parameters.get("ver"));

            if (maybeApp.isEmpty() || maybeEnv.isEmpty() || maybeVer.isEmpty()) {
                response.status(Status.BAD_REQUEST_400).send();
                return;
            }

            final var maybeCreateResult = releaseCreationService.create(maybeApp.get(), maybeEnv.get(), maybeVer.get());
            if (maybeCreateResult.isEmpty()) {
                log.atError()
                        .addMarker(GlobalConstants.Markers.HTTP)
                        .setMessage("Could not create a release")
                        .log();
                response.status(Status.INTERNAL_SERVER_ERROR_500).send();
                return;
            }

            response.status(Status.CREATED_201).send(maybeCreateResult.get());
        } catch (NoSuchElementException nseEx) {
            onNoSuchElementException(nseEx, response);
        }
    }

    private void onNoSuchElementException(final NoSuchElementException nseEx, final ServerResponse response) {
        log.atError().addMarker(GlobalConstants.Markers.HTTP).setCause(nseEx).log();
        response.status(Status.INTERNAL_SERVER_ERROR_500).send();
    }
}
