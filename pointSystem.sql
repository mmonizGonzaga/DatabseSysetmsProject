
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

INSERT INTO Users VALUES(0,'Thomas', 'McDonald', 2020, FALSE, FALSE);
INSERT INTO Users VALUES(1,'Shawn', 'Bowers', 1960, FALSE, FALSE);
INSERT INTO Users VALUES(2,'Susan', 'Boyle', 1950, FALSE, FALSE);
INSERT INTO Users VALUES(3,'Michael', 'Jackson', 1970, FALSE, FALSE);

INSERT INTO PointValues VALUES('Informal Meeting', 2.5);
INSERT INTO PointValues VALUES('Formal Meeting', 3.5);
INSERT INTO PointValues VALUES('Service Hour', .25);
INSERT INTO PointValues VALUES('Credits', .5);
INSERT INTO PointValues VALUES('Hosted Event', 2);


INSERT INTO OneTimeTypes VALUES(0, 'Informal Meeting' , 'Informal Meeting', 'We meet weekly');
INSERT INTO OneTimeTypes VALUES(1, 'Formal Meeting', 'Formal Meeting', "formal meeting occasionaly");

-- informal occurences
INSERT INTO OneTimeOcurrences VALUES(0, '2019-01-01', 0);
INSERT INTO OneTimeOcurrences VALUES(1, '2019-01-08', 0);
INSERT INTO OneTimeOcurrences VALUES(2, '2019-01-15', 0);
INSERT INTO OneTimeOcurrences VALUES(3, '2019-01-23', 0);
-- formal occurences
INSERT INTO OneTimeOcurrences VALUES(4, '2019-02-01', 1);
INSERT INTO OneTimeOcurrences VALUES(5, '2019-01-24', 1);

-- event_id, user_id
INSERT INTO Present VALUES (0,0);
INSERT INTO Present VALUES (0,2);
INSERT INTO Present VALUES (0,3);

INSERT INTO Present VALUES (1,0);
INSERT INTO Present VALUES (1,2);
INSERT INTO Present VALUES (1,1);

INSERT INTO Present VALUES (2,0);
INSERT INTO Present VALUES (2,1);
INSERT INTO Present VALUES (2,2);
INSERT INTO Present VALUES (2,3);

INSERT INTO Present VALUES (3,0);
INSERT INTO Present VALUES (3,1);

INSERT INTO Present VALUES (4,0);
INSERT INTO Present VALUES (4,1);
INSERT INTO Present VALUES (4,2);
INSERT INTO Present VALUES (4,3);

-- Hosted, lets get rid of it

INSERT INTO MultiType VALUES('Service', 'Service Hour', 12);
INSERT INTO MultiType VALUES('Class Credits', 'Credits', NULL);

INSERT INTO MultiOccurences VALUES(0, 'Service', '2019-01-23', 0, 'food kitchen', 3);
INSERT INTO MultiOccurences VALUES(1, 'Service', '2019-01-23', 2, 'food kitchen', 3);
INSERT INTO MultiOccurences VALUES(3, 'Service', '2019-01-17', 1, 'food kitchen', 4.5);
INSERT INTO MultiOccurences VALUES(4, 'Service', '2019-01-30', 0, 'food kitchen', 10);

INSERT INTO MultiOccurences VALUES(5, 'Class Credits', '2019-01-30', 0, 'class bb', 2);

-- Queries
-- Sum Event totals for all users
SELECT u.first_name, u.last_name, SUM(p.point_value)
FROM Users u  JOIN Present pr USING(u_id)
    JOIN OneTimeOcurrences oto USING(one_time_id)
    JOIN OneTimeTypes ott USING(one_time_type_id)
    JOIN PointValues p USING(point_type)
GROUP BY u.u_id;

-- Sum multi events for all users
-- need to implement max
SELECT u.first_name, u.last_name, SUM(p.point_value * mo.multi_amount)
FROM Users u LEFT OUTER JOIN MultiOccurences mo USING(u_id)
    LEFT OUTER JOIN MultiType mt USING(multi_type_name)
    LEFT OUTER JOIN PointValues p USING(point_type)
GROUP BY u.u_id;


SELECT u.first_name, u.last_name, u.grad_year
FROM Users u
WHERE u.grad_year > 1960;
