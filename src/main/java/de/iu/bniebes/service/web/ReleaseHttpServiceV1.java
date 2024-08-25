package de.iu.bniebes.service.web;

import de.iu.bniebes.constant.GlobalConstants;
import de.iu.bniebes.service.internal.InputSanitizationService;
import de.iu.bniebes.service.internal.ReleaseAccessService;
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
    private final ReleaseAccessService releaseAccessService;

    @Override
    public void routing(final HttpRules httpRules) {
        httpRules
                .get("/", this::all)
                .post("/{app}/{env}/{ver}", this::create)
                .get("/{app}/{env}/{ver}/{zet}", this::get);
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
            if (maybeCreateResult.notPresent()) {
                onErrorResult("Could not create a release", response);
                response.status(Status.INTERNAL_SERVER_ERROR_500).send();
                return;
            }

            response.status(Status.CREATED_201).send(maybeCreateResult.get());
        } catch (NoSuchElementException nseEx) {
            onNoSuchElementException(nseEx, response);
        }
    }

    private void get(final ServerRequest request, final ServerResponse response) {
        try {
            final var parameters = request.path().pathParameters();
            final var maybeApp = inputSanitizationService.safeString(parameters.get("app"));
            final var maybeEnv = inputSanitizationService.safeString(parameters.get("env"));
            final var maybeVer = inputSanitizationService.safeString(parameters.get("ver"));
            final var maybeZet = inputSanitizationService.bigInteger(parameters.get("zet"));

            if (maybeApp.isEmpty() || maybeEnv.isEmpty() || maybeVer.isEmpty() || maybeZet.isEmpty()) {
                response.status(Status.BAD_REQUEST_400).send();
                return;
            }

            final var maybeResponse =
                    releaseAccessService.get(maybeApp.get(), maybeEnv.get(), maybeVer.get(), maybeZet.get());
            if (maybeResponse.isEmpty()) {
                response.status(Status.NOT_FOUND_404).send();
                return;
            }
            if (maybeResponse.isError()) {
                response.status(Status.INTERNAL_SERVER_ERROR_500).send();
                return;
            }

            response.send(maybeResponse.get());
        } catch (NoSuchElementException nseEx) {
            onNoSuchElementException(nseEx, response);
        }
    }

    private void all(final ServerRequest request, final ServerResponse response) {
        final var maybeAllResponse = releaseAccessService.all();
        if (maybeAllResponse.notPresent()) {
            onErrorResult("Could not retrieve releases", response);
            return;
        }
        response.send(maybeAllResponse.get());
    }

    private void onNoSuchElementException(final NoSuchElementException nseEx, final ServerResponse response) {
        log.atError().addMarker(GlobalConstants.Markers.HTTP).setCause(nseEx).log();
        response.status(Status.INTERNAL_SERVER_ERROR_500).send();
    }

    private void onErrorResult(final String message, final ServerResponse response) {
        log.atError()
                .addMarker(GlobalConstants.Markers.HTTP)
                .setMessage(message)
                .log();
        response.status(Status.INTERNAL_SERVER_ERROR_500).send();
    }
}
