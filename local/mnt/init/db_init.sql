-- releases table
CREATE TABLE IF NOT EXISTS releases
(
    application       VARCHAR,
    environment       VARCHAR,
    version           VARCHAR,
    release_timestamp TIMESTAMP,
    id                BIGINT GENERATED ALWAYS AS IDENTITY UNIQUE,

    PRIMARY KEY (application, environment, version, release_timestamp)
);
CREATE INDEX IF NOT EXISTS application_idx ON releases (application);
CREATE INDEX IF NOT EXISTS version_idx ON releases (version);

-- release names table
CREATE TABLE IF NOT EXISTS release_names
(
    release_id BIGINT references releases (id),
    name       VARCHAR
);

-- descriptions table
CREATE TABLE IF NOT EXISTS descriptions
(
    release_id  BIGINT references releases (id),
    description VARCHAR
);

-- changes table
CREATE TABLE IF NOT EXISTS changes
(
    release_id BIGINT references releases (id),
    changes    VARCHAR
);

-- responsibility table
CREATE TABLE IF NOT EXISTS responsibility
(
    release_id     BIGINT references releases (id),
    responsibility VARCHAR
);

-- build location table
CREATE TABLE IF NOT EXISTS build_location
(
    release_id     BIGINT references releases (id),
    build_location VARCHAR
);
