


SET sql_mode = STRICT_ALL_TABLES;

DROP TABLE IF EXISTS Hosted;
DROP TABLE IF EXISTS Present;
DROP TABLE IF EXISTS MultiOccurences;
DROP TABLE IF EXISTS OneTimeOcurrences;
DROP TABLE IF EXISTS OneTimeTypes;
DROP TABLE IF EXISTS MultiType;
DROP TABLE IF EXISTS PointValues;
DROP TABLE IF EXISTS Users;

CREATE TABLE Users (
    u_id INT,
    first_name VARCHAR(20),
    last_name VARCHAR(20),
    grad_year YEAR(4),
    account_hold BOOLEAN,
    active BOOLEAN,
    PRIMARY KEY (u_id)
);
CREATE TABLE PointValues(
    point_type VARCHAR(20),
    point_value DECIMAL(4,2), -- make this have only 2 decimal digits
    PRIMARY KEY (point_type)
);
CREATE TABLE OneTimeTypes(
    one_time_type_id INT,
    point_type VARCHAR(20),
    one_time_type_name VARCHAR(20),
    one_time_type_description VARCHAR(140),
    PRIMARY KEY (one_time_type_id),
    FOREIGN KEY (point_type) REFERENCES PointValues(point_type)
);
CREATE TABLE OneTimeOcurrences(
    one_time_id INT,
    one_time_date DATE,
    one_time_type_id INT,
    PRIMARY KEY (one_time_id),
    FOREIGN KEY (one_time_type_id) REFERENCES OneTimeTypes(one_time_type_id)
);
CREATE TABLE Present(
    one_time_id INT,
    u_id INT,
    PRIMARY KEY(one_time_id, u_id),
    FOREIGN KEY(one_time_id) REFERENCES OneTimeOcurrences(one_time_id),
    FOREIGN KEY(u_id) REFERENCES Users(u_id)
);

CREATE TABLE Hosted(
    one_time_id INT,
    u_id INT,
    point_type VARCHAR(20),
    PRIMARY KEY(one_time_id, u_id),
    FOREIGN KEY(one_time_id) REFERENCES OneTimeOcurrences(one_time_id),
    FOREIGN KEY(u_id) REFERENCES Users(u_id),
    FOREIGN KEY (point_type) REFERENCES PointValues(point_type)
);

CREATE TABLE MultiType(
    multi_type_name VARCHAR(20),
    point_type VARCHAR(20),
    max_points INT,
    PRIMARY KEY (multi_type_name),
    FOREIGN KEY (point_type) REFERENCES PointValues(point_type)
);

CREATE TABLE MultiOccurences(
    multi_id INT,
    multi_type_name VARCHAR(20),
    multi_date DATE,
    u_id INT,
    multi_description VARCHAR(140),
    multi_amount INT,
    PRIMARY KEY (multi_id),
    FOREIGN KEY(u_id) REFERENCES Users(u_id),
    FOREIGN KEY(multi_type_name) REFERENCES MultiType(multi_type_name)
);

