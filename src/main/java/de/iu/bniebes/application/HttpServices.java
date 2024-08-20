package de.iu.bniebes.application;

import static de.iu.bniebes.constant.GlobalConstants.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpServices {

    public HttpServices(final Configuration configuration, final Services services) {
        log.atInfo().addMarker(Markers.APPLICATION).setMessage("Set-Up").log();
    }
}
