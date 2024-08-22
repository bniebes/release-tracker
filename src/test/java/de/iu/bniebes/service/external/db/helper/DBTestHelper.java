package de.iu.bniebes.service.external.db.helper;

import com.zaxxer.hikari.HikariDataSource;
import de.iu.bniebes.configuration.DBConfiguration;
import de.iu.bniebes.constant.GlobalConstants;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

@Slf4j
public class DBTestHelper {

    private DBTestHelper() {}

    public static Jdbi createTestJdbi() {
        final var hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/release_tracker");
        hikariDataSource.setUsername(DBConfiguration.DEFAULT_DB_JDBC_USER);
        hikariDataSource.setPassword("dev");
        return Jdbi.create(hikariDataSource);
    }

    public static void infoLogResult(final Object result) {
        log.atInfo()
                .setMessage("Result: {}")
                .addMarker(GlobalConstants.Markers.TEST)
                .addArgument(result)
                .log();
    }
}
