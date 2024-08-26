package de.iu.bniebes.service.web;

import de.iu.bniebes.constant.GlobalConstants;
import de.iu.bniebes.model.result.Result;
import de.iu.bniebes.service.internal.InputSanitizationService;
import de.iu.bniebes.service.internal.ReleaseAccessService;
import de.iu.bniebes.service.internal.ReleaseCreationService;
import io.helidon.http.HeaderNames;
import io.helidon.http.HeaderValues;
import io.helidon.http.Status;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import java.math.BigInteger;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ReleaseHttpServiceV1 implements HttpService {

    private static final String MEDIA_TYPE_JSON = HeaderValues.CONTENT_TYPE_JSON.values();

    private record AllParameters(String app, String env, String ver, BigInteger zeu) {}

    private final InputSanitizationService inputSanitizationService;
    private final ReleaseCreationService releaseCreationService;
    private final ReleaseAccessService releaseAccessService;

    @Override
    public void routing(final HttpRules httpRules) {
        httpRules
                .get("/", this::all)
                .get("/{app}", this::allByApplication)
                .get("/{app}/{env}", this::allByApplicationAndEnvironment)
                .post("/{app}/{env}/{ver}", this::create)
                .put("/{app}/{env}/{ver}/{zeu}", this::createOrUpdate)
                .get("/{app}/{env}/{ver}/{zeu}", this::get);
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
                return;
            }

            response.status(Status.CREATED_201).send(maybeCreateResult.get());
        } catch (NoSuchElementException nseEx) {
            onNoSuchElementException(nseEx, response);
        }
    }

    private void createOrUpdate(final ServerRequest request, final ServerResponse response) {
        final var maybeParameters = allParameters(request, response);
        if (maybeParameters.isEmpty()) return;
        final var parameters = maybeParameters.get();

        final var optionalInfoJson = request.content().asOptional(String.class).orElse("");

        final var maybeCreateOrUpdateResult = releaseCreationService.createOrUpdate(
                parameters.app, parameters.env, parameters.ver, parameters.zeu, optionalInfoJson);
        if (maybeCreateOrUpdateResult.notPresent()) {
            response.status(Status.INTERNAL_SERVER_ERROR_500).send();
            return;
        }

        final var createOrUpdateResult = maybeCreateOrUpdateResult.get();
        if (createOrUpdateResult.created()) {
            response.header(HeaderNames.CONTENT_TYPE, MEDIA_TYPE_JSON)
                    .status(Status.CREATED_201)
                    .send(createOrUpdateResult.jsonResponse());
            return;
        }

        response.send();
    }

    private void get(final ServerRequest request, final ServerResponse response) {
        final var maybeParameters = allParameters(request, response);
        if (maybeParameters.isEmpty()) return;
        final var parameters = maybeParameters.get();

        final var result = releaseAccessService.get(parameters.app, parameters.env, parameters.ver, parameters.zeu);
        respondAccordingToResult(result, response, "Could not get release");
    }

    private void all(final ServerRequest request, final ServerResponse response) {
        respondAccordingToResult(releaseAccessService.all(), response, "Could not retrieve releases");
    }

    private void allByApplication(final ServerRequest request, final ServerResponse response) {
        try {
            final var parameters = request.path().pathParameters();
            final var maybeApp = inputSanitizationService.safeString(parameters.get("app"));

            if (maybeApp.isEmpty()) {
                response.status(Status.BAD_REQUEST_400).send();
                return;
            }

            final var result = releaseAccessService.allByApplication(maybeApp.get());
            respondAccordingToResult(result, response, "Could not retrieve releases");
        } catch (NoSuchElementException nseEx) {
            onNoSuchElementException(nseEx, response);
        }
    }

    private void allByApplicationAndEnvironment(final ServerRequest request, final ServerResponse response) {
        try {
            final var parameters = request.path().pathParameters();
            final var maybeApp = inputSanitizationService.safeString(parameters.get("app"));
            final var maybeEnv = inputSanitizationService.safeString(parameters.get("env"));

            if (maybeApp.isEmpty() || maybeEnv.isEmpty()) {
                response.status(Status.BAD_REQUEST_400).send();
                return;
            }

            final var result = releaseAccessService.allByApplicationAndEnvironment(maybeApp.get(), maybeEnv.get());
            respondAccordingToResult(result, response, "Could not retrieve releases");
        } catch (NoSuchElementException nseEx) {
            onNoSuchElementException(nseEx, response);
        }
    }

    private Optional<AllParameters> allParameters(final ServerRequest request, final ServerResponse response) {
        try {
            final var parameters = request.path().pathParameters();
            final var maybeApp = inputSanitizationService.safeString(parameters.get("app"));
            final var maybeEnv = inputSanitizationService.safeString(parameters.get("env"));
            final var maybeVer = inputSanitizationService.safeString(parameters.get("ver"));
            final var maybeZet = inputSanitizationService.bigInteger(parameters.get("zeu"));

            if (maybeApp.isEmpty() || maybeEnv.isEmpty() || maybeVer.isEmpty() || maybeZet.isEmpty()) {
                response.status(Status.BAD_REQUEST_400).send();
                return Optional.empty();
            }

            return Optional.of(new AllParameters(maybeApp.get(), maybeEnv.get(), maybeVer.get(), maybeZet.get()));
        } catch (NoSuchElementException nseEx) {
            onNoSuchElementException(nseEx, response);
            return Optional.empty();
        }
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

    private void respondAccordingToResult(
            final Result<String> result, final ServerResponse response, final String errorMsg) {
        if (result.isEmpty()) {
            response.status(Status.NOT_FOUND_404).send();
            return;
        }
        if (result.isError()) {
            onErrorResult(errorMsg, response);
            return;
        }
        response.send(result.get());
    }
}
