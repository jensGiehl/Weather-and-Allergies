CREATE TABLE hourly_reading (
    id              VARCHAR(36)       PRIMARY KEY,
    daily_report_id VARCHAR(36)       NOT NULL,
    hour_of_day     INTEGER           NOT NULL,
    temperature     DOUBLE PRECISION,
    alder_pollen    DOUBLE PRECISION,
    birch_pollen    DOUBLE PRECISION,
    grass_pollen    DOUBLE PRECISION,
    mugwort_pollen  DOUBLE PRECISION,
    olive_pollen    DOUBLE PRECISION,
    ragweed_pollen  DOUBLE PRECISION,
    CONSTRAINT uq_daily_hour UNIQUE (daily_report_id, hour_of_day)
);
