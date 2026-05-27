CREATE TABLE daily_report (
    id                        VARCHAR(36)       PRIMARY KEY,
    report_date               DATE              NOT NULL,
    weather_code              INTEGER,
    temperature_max           DOUBLE PRECISION,
    temperature_min           DOUBLE PRECISION,
    precipitation_sum         DOUBLE PRECISION,
    precipitation_probability INTEGER,
    windspeed_max             DOUBLE PRECISION,
    uv_index_max              DOUBLE PRECISION,
    sunrise                   VARCHAR(10),
    sunset                    VARCHAR(10),
    alder_pollen              DOUBLE PRECISION,
    birch_pollen              DOUBLE PRECISION,
    grass_pollen              DOUBLE PRECISION,
    mugwort_pollen            DOUBLE PRECISION,
    olive_pollen              DOUBLE PRECISION,
    ragweed_pollen            DOUBLE PRECISION,
    created_at                TIMESTAMP,
    CONSTRAINT uq_daily_date UNIQUE (report_date)
);

CREATE TABLE allergy_entry (
    id              VARCHAR(36)   PRIMARY KEY,
    daily_report_id VARCHAR(36)   NOT NULL,
    person_name     VARCHAR(100)  NOT NULL,
    symptoms        VARCHAR(1000),
    updated_at      TIMESTAMP,
    CONSTRAINT uq_person_per_day UNIQUE (daily_report_id, person_name)
);
