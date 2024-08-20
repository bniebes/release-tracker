package de.iu.bniebes.application;

import static de.iu.bniebes.constant.GlobalConstants.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Services implements AutoCloseable {

    public Services(final Configuration configuration) {
        log.atInfo().addMarker(Markers.APPLICATION).setMessage("Set-Up").log();
    }

    @Override
    public void close() throws Exception {}
}
