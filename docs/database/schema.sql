DROP TABLE IF EXISTS activities;
DROP TABLE IF EXISTS user_characters;
DROP TABLE IF EXISTS character_spawns;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS submissions;
DROP TABLE IF EXISTS characters;
DROP TABLE IF EXISTS sensors;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL UNIQUE,
    college VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    exp INT NOT NULL DEFAULT 0,
    level INT NOT NULL DEFAULT 1,
    total_submission_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sensors (
    id BIGSERIAL PRIMARY KEY,
    sensor_name VARCHAR(100) NOT NULL UNIQUE,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL
);

CREATE TABLE submissions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    sensor_id BIGINT NOT NULL,

    temperature DECIMAL(5, 2),
    humidity DECIMAL(5, 2),
    eco2 INT,
    air_quality INT,
    rssi INT,

    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),

    measured_at TIMESTAMP NOT NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    time_slot TIMESTAMP NOT NULL,
    reward_exp INT NOT NULL DEFAULT 10,

    CONSTRAINT fk_submissions_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_submissions_sensor
        FOREIGN KEY (sensor_id) REFERENCES sensors(id),

    CONSTRAINT uq_user_sensor_time_slot
        UNIQUE (user_id, sensor_id, time_slot)
);

CREATE TABLE characters (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    rarity VARCHAR(20) NOT NULL,
    description TEXT,
    base_spawn_rate DECIMAL(5, 4) NOT NULL,
    bonus_exp INT NOT NULL DEFAULT 0
);

CREATE TABLE activities (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    exp_change INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_activities_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE user_characters (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    character_id BIGINT NOT NULL,
    sensor_id BIGINT,
    submission_id BIGINT,
    discovered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_characters_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_user_characters_character
        FOREIGN KEY (character_id) REFERENCES characters(id),

    CONSTRAINT fk_user_characters_sensor
        FOREIGN KEY (sensor_id) REFERENCES sensors(id),

    CONSTRAINT fk_user_characters_submission
        FOREIGN KEY (submission_id) REFERENCES submissions(id)
);

CREATE INDEX IF NOT EXISTS idx_user_characters_user_id
    ON user_characters(user_id);

CREATE INDEX IF NOT EXISTS idx_user_characters_character_id
    ON user_characters(character_id);

CREATE INDEX IF NOT EXISTS idx_submissions_user_id
    ON submissions(user_id);

CREATE INDEX IF NOT EXISTS idx_submissions_sensor_id
    ON submissions(sensor_id);

CREATE INDEX IF NOT EXISTS idx_submissions_time_slot
    ON submissions(time_slot);

CREATE INDEX IF NOT EXISTS idx_users_college_department
    ON users(college, department);

CREATE INDEX IF NOT EXISTS idx_activities_user_id
    ON activities(user_id);