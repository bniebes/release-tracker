package de.iu.bniebes.service.external.db;

import com.zaxxer.hikari.HikariDataSource;
import de.iu.bniebes.configuration.DBConfiguration;
import de.iu.bniebes.constant.GlobalConstants;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

@Slf4j
public class DBClientService implements AutoCloseable {

    private final HikariDataSource hikariDataSource;

    public final Jdbi jdbi;

    public DBClientService(final DBConfiguration dbConfiguration) {
        this.hikariDataSource = dataSource(dbConfiguration);
        this.jdbi = Jdbi.create(hikariDataSource);
    }

    private HikariDataSource dataSource(final DBConfiguration dbConfiguration) {
        final var hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(dbConfiguration.jdbcUrl());
        hikariDataSource.setUsername(dbConfiguration.user());
        hikariDataSource.setPassword(dbConfiguration.password());
        return hikariDataSource;
    }

    @Override
    public void close() throws Exception {
        log.atInfo()
                .addMarker(GlobalConstants.Markers.SERVICE)
                .setMessage("Closing hikari data source")
                .log();
        hikariDataSource.close();
    }
}
