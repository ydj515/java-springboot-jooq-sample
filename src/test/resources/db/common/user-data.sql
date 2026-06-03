INSERT INTO users (username, name, user_type, password, email, last_login_at, last_password_updated_at, trial_cnt)
VALUES ('user123', 'John Doe', 'USER', '{noop}password123', 'john.doe@example.com', '2024-06-01 10:30:00',
        '2024-05-01 09:00:00', 1);

INSERT INTO users (username, name, user_type, password, email, last_login_at, last_password_updated_at, trial_cnt)
VALUES ('user456', 'Jane Smith', 'MANAGER', '{noop}password456', 'jane.smith@example.com', '2024-06-10 14:45:00',
        '2024-05-15 11:15:00', 2);

INSERT INTO roles (name, description, created_at, updated_at)
VALUES ('ADMIN', 'admin', now(), now());

INSERT INTO roles (name, description, created_at, updated_at)
VALUES ('PLAIN', 'plain', now(), now());

INSERT INTO roles (name, description, created_at, updated_at)
VALUES ('AAA', 'aaa', now(), now());

INSERT INTO roles (name, description)
VALUES ('BBB', 'bbb');

INSERT INTO users_roles(user_id, role_id)
VALUES (1, 1);

INSERT INTO users_roles(user_id, role_id)
VALUES (1, 2);

INSERT INTO users_roles(user_id, role_id)
VALUES (2, 1);

INSERT INTO users_roles(user_id, role_id)
VALUES (2, 3);

INSERT INTO users_roles(user_id, role_id)
VALUES (2, 4);
