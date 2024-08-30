package de.iu.bniebes.service.web;

import de.iu.bniebes.model.OptInfo;
import de.iu.bniebes.model.parameter.AllParameters;
import de.iu.bniebes.service.internal.InputSanitizationService;
import de.iu.bniebes.service.internal.ReleaseOptInfoService;
import de.iu.bniebes.util.ResponseUtil;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ReleaseOptInfoHttpServiceV1 implements HttpService {

    private static final String COMMON_PREFIX = "/{app}/{env}/{ver}/{zeu}";
    private static final String PATH_RELEASE_NAME = COMMON_PREFIX + "/release-name";
    private static final String PATH_DESCRIPTION = COMMON_PREFIX + "/description";
    private static final String PATH_CHANGES = COMMON_PREFIX + "/changes";
    private static final String PATH_RESPONSIBILITY = COMMON_PREFIX + "/responsibility";
    private static final String PATH_BUILD_LOCATION = COMMON_PREFIX + "/build-location";

    private final InputSanitizationService inputSanitizationService;
    private final ReleaseOptInfoService releaseOptInfoService;

    @Override
    public void routing(final HttpRules httpRules) {
        httpRules
                .get(PATH_RELEASE_NAME, (request, response) -> get(request, response, OptInfo.RELEASE_NAME))
                .delete(PATH_RELEASE_NAME, (request, response) -> delete(request, response, OptInfo.RELEASE_NAME))
                .get(PATH_DESCRIPTION, (request, response) -> get(request, response, OptInfo.DESCRIPTION))
                .delete(PATH_DESCRIPTION, (request, response) -> delete(request, response, OptInfo.DESCRIPTION))
                .get(PATH_CHANGES, (request, response) -> get(request, response, OptInfo.CHANGES))
                .delete(PATH_CHANGES, (request, response) -> delete(request, response, OptInfo.CHANGES))
                .get(PATH_RESPONSIBILITY, (request, response) -> get(request, response, OptInfo.RESPONSIBILITY))
                .delete(PATH_RESPONSIBILITY, (request, response) -> delete(request, response, OptInfo.RESPONSIBILITY))
                .get(PATH_BUILD_LOCATION, (request, response) -> get(request, response, OptInfo.BUILD_LOCATION))
                .delete(PATH_BUILD_LOCATION, (request, response) -> delete(request, response, OptInfo.BUILD_LOCATION));
    }

    private void get(final ServerRequest request, final ServerResponse response, final OptInfo optInfo) {
        final var maybeParameters = AllParameters.fromRequestResponding(inputSanitizationService, request, response);
        if (maybeParameters.isEmpty()) return;
        final var parameters = maybeParameters.get();

        final var result = releaseOptInfoService.optInfo(parameters, optInfo);
        if (result.isError()) {
            final var message = "Could not get optional release information: %s".formatted(optInfo.label);
            ResponseUtil.onErrorResult(message, response);
            return;
        }
        if (result.isEmpty()) {
            ResponseUtil.respondNotFound(response);
            return;
        }
        ResponseUtil.respondJsonOK(result.get(), response);
    }

    private void delete(final ServerRequest request, final ServerResponse response, final OptInfo optInfo) {
        final var maybeParameters = AllParameters.fromRequestResponding(inputSanitizationService, request, response);
        if (maybeParameters.isEmpty()) return;
        final var parameters = maybeParameters.get();

        final var result = releaseOptInfoService.deleteOptInfo(parameters, optInfo);
        if (result.isError()) {
            final var message = "Could not get optional release information: %s".formatted(optInfo.label);
            ResponseUtil.onErrorResult(message, response);
            return;
        }
        if (result.isEmpty()) {
            ResponseUtil.respondNotFound(response);
            return;
        }
        ResponseUtil.respondOK(response);
    }
}
