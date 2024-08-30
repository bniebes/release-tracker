package de.iu.bniebes.model.parameter;

import static de.iu.bniebes.util.ResponseUtil.onNoSuchElementException;
import static de.iu.bniebes.util.ResponseUtil.respondBadRequest;

import de.iu.bniebes.service.internal.InputSanitizationService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import java.math.BigInteger;
import java.util.NoSuchElementException;
import java.util.Optional;

public record AllParameters(String app, String env, String ver, BigInteger zeu) {

    public static Optional<AllParameters> fromRequestResponding(
            final InputSanitizationService inputSanitizationService,
            final ServerRequest request,
            final ServerResponse response) {
        try {
            final var parameters = request.path().pathParameters();
            final var maybeApp = inputSanitizationService.safeString(parameters.get("app"));
            final var maybeEnv = inputSanitizationService.safeString(parameters.get("env"));
            final var maybeVer = inputSanitizationService.safeString(parameters.get("ver"));
            final var maybeZet = inputSanitizationService.bigInteger(parameters.get("zeu"));

            if (maybeApp.isEmpty() || maybeEnv.isEmpty() || maybeVer.isEmpty() || maybeZet.isEmpty()) {
                respondBadRequest(response);
                return Optional.empty();
            }

            return Optional.of(new AllParameters(maybeApp.get(), maybeEnv.get(), maybeVer.get(), maybeZet.get()));
        } catch (NoSuchElementException nseEx) {
            onNoSuchElementException(nseEx, response);
            return Optional.empty();
        }
    }
}
