package de.iu.bniebes.application;

import static de.iu.bniebes.constant.GlobalConstants.*;

import de.iu.bniebes.service.web.ReleaseHttpServiceV1;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpServices {

    public final ReleaseHttpServiceV1 releaseHttpServiceV1;

    public HttpServices(final Services services) {
        log.atInfo().addMarker(Markers.APPLICATION).setMessage("Set-Up").log();
        this.releaseHttpServiceV1 = new ReleaseHttpServiceV1(
                services.inputSanitizationService, services.releaseCreationService, services.releaseAccessService);
    }
}
