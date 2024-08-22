package de.iu.bniebes.configuration;

import de.iu.bniebes.application.EnvironmentAccessor;
import de.iu.bniebes.constant.GlobalConstants;

public record DBConfiguration(String jdbcUrl, String user, String password) {

    private static final String COMMON_PREFIX = "db.jdbc.";

    public static final String KEY_DB_JDBC_URL = COMMON_PREFIX + "url";
    public static final String KEY_DB_JDBC_USER = COMMON_PREFIX + "url";
    public static final String KEY_DB_JDBC_PASSWORD_PATH = COMMON_PREFIX + "password";

    public static final String DEFAULT_DB_JDBC_URL = "jdbc:postgresql://pgsql:5432/release_tracker";
    public static final String DEFAULT_DB_JDBC_USER = "release-tracker";
    public static final String DEFAULT_DB_JDBC_PASSWORD_PATH = GlobalConstants.Directories.SECRET + "/db_jdbc_password";

    public static DBConfiguration from(final EnvironmentAccessor environmentAccessor) {
        return new DBConfiguration(
                environmentAccessor.getOrDefault(KEY_DB_JDBC_URL, DEFAULT_DB_JDBC_URL),
                environmentAccessor.getOrDefault(KEY_DB_JDBC_USER, DEFAULT_DB_JDBC_USER),
                environmentAccessor.loadSecret(KEY_DB_JDBC_PASSWORD_PATH, DEFAULT_DB_JDBC_PASSWORD_PATH));
    }
}
