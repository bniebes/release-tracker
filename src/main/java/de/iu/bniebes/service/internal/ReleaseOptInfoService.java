package de.iu.bniebes.service.internal;

import de.iu.bniebes.model.OptInfo;
import de.iu.bniebes.model.parameter.AllParameters;
import de.iu.bniebes.model.result.Result;
import de.iu.bniebes.service.external.db.ReleaseDBService;
import de.iu.bniebes.service.external.db.ReleaseOptInfoDBService;
import de.iu.bniebes.util.TimestampUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ReleaseOptInfoService {

    private static final String JSON_FMT = """
            {
                "%s": "%s"
            }
            """;

    private final ReleaseDBService releaseDBService;
    private final ReleaseOptInfoDBService releaseOptInfoDBService;

    public Result<String> optInfo(final AllParameters parameters, final OptInfo optInfo) {
        final var maybeRelease = releaseDBService.release(
                parameters.app(), parameters.env(), parameters.ver(), TimestampUtils.instantOf(parameters.zeu()));
        if (maybeRelease.isError()) return Result.error();
        if (maybeRelease.isEmpty()) return Result.empty();
        final var releaseId = maybeRelease.get().id();

        final var maybeValue = releaseOptInfoDBService.stringValueById(releaseId, optInfo);
        if (maybeValue.notPresent()) return maybeValue;
        return Result.of(JSON_FMT.formatted(optInfo.label, maybeValue.get()));
    }

    public Result<Boolean> deleteOptInfo(final AllParameters parameters, final OptInfo optInfo) {
        final var maybeRelease = releaseDBService.release(
                parameters.app(), parameters.env(), parameters.ver(), TimestampUtils.instantOf(parameters.zeu()));
        if (maybeRelease.isError()) return Result.error();
        if (maybeRelease.isEmpty()) return Result.empty();
        final var releaseId = maybeRelease.get().id();

        return releaseOptInfoDBService.deleteValueById(releaseId, optInfo);
    }
}
