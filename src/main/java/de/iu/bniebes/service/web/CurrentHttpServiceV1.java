package de.iu.bniebes.service.web;

import static de.iu.bniebes.util.ResponseUtil.onNoSuchElementException;
import static de.iu.bniebes.util.ResponseUtil.respondAccordingToResult;

import de.iu.bniebes.service.internal.InputSanitizationService;
import de.iu.bniebes.service.internal.ReleaseAccessService;
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
public class CurrentHttpServiceV1 implements HttpService {

    private final InputSanitizationService inputSanitizationService;
    private final ReleaseAccessService releaseAccessService;

    @Override
    public void routing(final HttpRules httpRules) {
        httpRules
                .get("/{app}", this::currentByApplication)
                .get("/{app}/{env}", this::currentByApplicationAndEnvironment);
    }

    private void currentByApplication(final ServerRequest request, final ServerResponse response) {
        try {
            final var parameters = request.path().pathParameters();
            final var maybeApp = inputSanitizationService.safeString(parameters.get("app"));

            if (maybeApp.isEmpty()) {
                response.status(Status.BAD_REQUEST_400).send();
                return;
            }

            final var maybeCurrentRelease = releaseAccessService.currentByApplication(maybeApp.get());
            respondAccordingToResult(maybeCurrentRelease, response, "Could not retrieve current release by app");
        } catch (NoSuchElementException nseEx) {
            onNoSuchElementException(nseEx, response);
        }
    }

    private void currentByApplicationAndEnvironment(final ServerRequest request, final ServerResponse response) {
        try {
            final var parameters = request.path().pathParameters();
            final var maybeApp = inputSanitizationService.safeString(parameters.get("app"));
            final var maybeEnv = inputSanitizationService.safeString(parameters.get("env"));
            if (maybeApp.isEmpty() || maybeEnv.isEmpty()) {
                response.status(Status.BAD_REQUEST_400).send();
                return;
            }
            final var maybeCurrentRelease =
                    releaseAccessService.currentByApplicationAndEnvironment(maybeApp.get(), maybeEnv.get());
            respondAccordingToResult(
                    maybeCurrentRelease, response, "Could not retrieve current release by app and env");
        } catch (NoSuchElementException nseEx) {
            onNoSuchElementException(nseEx, response);
        }
    }
}
