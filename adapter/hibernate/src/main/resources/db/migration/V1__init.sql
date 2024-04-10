CREATE TABLE role
(
    id   BIGINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);
CREATE SEQUENCE sq_role_id START WITH 1 INCREMENT BY 1;

CREATE TABLE users
(
    id            BIGINT PRIMARY KEY,
    login         VARCHAR(255) NOT NULL UNIQUE,
    hash_password VARCHAR(255) NOT NULL,
    email         VARCHAR(255)  NOT NULL UNIQUE
);

CREATE SEQUENCE sq_user_id START WITH 1 INCREMENT BY 1;

CREATE TABLE user_role
(
    user_id INT NOT NULL,
    role_id INT NOT NULL
);

CREATE TABLE user_settings
(
    user_id       BIGINT PRIMARY KEY,
    host          VARCHAR(255) NOT NULL,
    port          INT NOT NULL,
    login         VARCHAR(255) NOT NULL,
    hash_password VARCHAR(255) NOT NULL,
    torrents_path VARCHAR(255)
);

CREATE TABLE user_media
(
    id      BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL
);

--CREATE TABLE user_media
--(
--    settings_id BIGINT NOT NULL,
--    path     VARCHAR(255) NOT NULL
--);

CREATE TABLE media
(
    id BIGINT PRIMARY KEY,
    runtime int NOT NULL,
    scan_check boolean NOT NULL,
    release_date timestamp(6) without time zone,
    category VARCHAR(255),
    db_link VARCHAR(255),
    description VARCHAR(255),
    name VARCHAR(255),
    path VARCHAR(255),
    torrent_hash VARCHAR(255)
);


INSERT INTO role VALUES (nextval('sq_role_id'), 'ROLE_ADMIN');
INSERT INTO role VALUES (nextval('sq_role_id'), 'ROLE_USER');

-- LOGIN:PASSWORD:
-- admin:admin
-- user:user

INSERT INTO users (id, login, hash_password, email)
VALUES (nextval('sq_user_id'), 'admin', '$2a$04$l6jf/IelD8EcKEx0z5LJFur01DtdBcTLUxfiq79X1GF2hgJdmIeEW', 'on-ant@inbox.ru'),
       (nextval('sq_user_id'), 'user', '$2a$04$y1oDipZIlwPEGBPtTrXT4.9enhB4zuQoQKstVueSSmK9qqdY.yU6y', 'user@milo.net');

INSERT INTO user_role
VALUES (1, 1), (2, 2);