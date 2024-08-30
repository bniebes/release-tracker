package de.iu.bniebes.application;

import static de.iu.bniebes.constant.GlobalConstants.*;

import de.iu.bniebes.service.web.CurrentHttpServiceV1;
import de.iu.bniebes.service.web.ReleaseHttpServiceV1;
import de.iu.bniebes.service.web.ReleaseOptInfoHttpServiceV1;
import de.iu.bniebes.service.web.UtilHttpServiceV1;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpServices {

    public final CurrentHttpServiceV1 currentHttpServiceV1;
    public final ReleaseHttpServiceV1 releaseHttpServiceV1;
    public ReleaseOptInfoHttpServiceV1 releaseOptInfoHttpServiceV1;
    public final UtilHttpServiceV1 utilHttpServiceV1;

    public HttpServices(final Services services) {
        log.atInfo().addMarker(Markers.APPLICATION).setMessage("Set-Up").log();
        this.currentHttpServiceV1 =
                new CurrentHttpServiceV1(services.inputSanitizationService, services.releaseAccessService);
        this.releaseHttpServiceV1 = new ReleaseHttpServiceV1(
                services.inputSanitizationService, services.releaseCreationService, services.releaseAccessService);
        this.releaseOptInfoHttpServiceV1 =
                new ReleaseOptInfoHttpServiceV1(services.inputSanitizationService, services.releaseOptInfoService);
        this.utilHttpServiceV1 = new UtilHttpServiceV1(services.inputSanitizationService);
    }
}
