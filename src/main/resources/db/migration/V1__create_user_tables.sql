CREATE TABLE users
(
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    username                 VARCHAR(255) NOT NULL,
    name                     VARCHAR(255) NOT NULL,
    user_type                VARCHAR(50),
    password                 VARCHAR(255) NOT NULL,
    email                    VARCHAR(255) NOT NULL,
    last_login_at            DATETIME,
    created_at               DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at               DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at               DATETIME,
    last_password_updated_at DATETIME,
    trial_cnt                INT      DEFAULT 0,
    UNIQUE KEY unique_user_username (username)
);

CREATE TABLE roles
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE users_roles
(
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY unique_user_role (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (role_id) REFERENCES roles (id)
);
