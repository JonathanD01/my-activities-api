-- Insert users

INSERT INTO users (id, firstname, lastname, email, password, account_locked, enabled, created_date, last_modified_date, role) VALUES (100, 'John', 'Doe', 'john.doe@example.com', 'password123', false, true, '2023-06-10T10:00:00', '2023-06-10T10:00:00', 'USER');

INSERT INTO users (id, firstname, lastname, email, password, account_locked, enabled, created_date, last_modified_date, role) VALUES (200, 'Jane', 'Smith', 'jane.smith@example.com', 'password456', true, false, '2023-06-11T11:00:00', '2023-06-11T11:00:00', 'ADMIN');

INSERT INTO users (id, firstname, lastname, email, password, account_locked, enabled, created_date, last_modified_date, role) VALUES (500, 'Ole', 'Doe', 'ole.doe@example.com', 'password123', false, false, '2023-06-10T10:00:00', '2023-06-10T10:00:00', 'USER');

INSERT INTO users (id, firstname, lastname, email, password, account_locked, enabled, created_date, last_modified_date, role) VALUES (600, 'Per', 'Doe', 'per.doe@example.com', 'password321', false, true, '2023-06-10T10:00:00', '2023-06-10T10:00:00', 'USER');

INSERT INTO users (id, firstname, lastname, email, password, account_locked, enabled, created_date, last_modified_date, role) VALUES (700, 'Guest', 'Doe', 'guest.user@example.com', 'password321', false, true, '2023-06-10T10:00:00', '2023-06-10T10:00:00', 'NONE');

INSERT INTO users (id, firstname, lastname, email, password, account_locked, enabled, created_date, last_modified_date, role) VALUES (800, 'Admin', 'Doe', 'admin.user@example.com', 'password321', false, true, '2023-06-10T10:00:00', '2023-06-10T10:00:00', 'ADMIN');


-- Insert activities

INSERT INTO activities (id, date, title, description, user_id, created_date, last_modified_date) VALUES (100, '2023-06-10', 'Activity 1', 'Description for Activity 1', 100, '2060-06-10T10:00:00', '2060-06-10T10:00:00');

INSERT INTO activities (id, date, title, description, user_id, created_date, last_modified_date) VALUES (200, '2023-06-10', 'Activity 2', 'Description for Activity 2', 100, '2060-06-10T10:00:00', '2060-06-10T10:00:00');

INSERT INTO activities (id, date, title, description, user_id, created_date, last_modified_date) VALUES (300, '2060-06-11', 'Activity 3', 'Description for Activity 3', 100, '2060-06-11T11:00:00', '2060-06-11T11:00:00');

INSERT INTO activities (id, date, title, description, user_id, created_date, last_modified_date) VALUES (400, '2060-06-11', 'Activity 4', 'Description for Activity 4', 100, '2060-06-12T11:00:00', '2060-06-11T11:00:00');

INSERT INTO activities (id, date, title, description, user_id, created_date, last_modified_date) VALUES (500, '2060-06-12', 'Activity 5', 'Description for Activity 5', 200, '2060-06-12T12:00:00', '2060-06-12T12:00:00');

INSERT INTO activities (id, date, title, description, user_id, created_date, last_modified_date) VALUES (600, '2060-06-12', 'Activity 5', 'Description for Activity 5', 800, '2060-06-12T12:00:00', '2060-06-12T12:00:00');

INSERT INTO activities (id, date, title, description, user_id, created_date, last_modified_date) VALUES (700, '2060-06-12', 'Activity 5', 'Description for Activity 5', 800, '2060-06-12T12:00:00', '2060-06-12T12:00:00');

INSERT INTO activities (id, date, title, description, user_id, created_date, last_modified_date) VALUES (800, '2060-06-12', 'Activity 5', 'Description for Activity 5', 800, '2060-06-12T12:00:00', '2060-06-12T12:00:00');

-- Insert token

INSERT INTO tokens (token, created_at, expires_at, user_id) VALUES ('my-super-super-secret-token', '2060-06-10T10:00:00', '2060-06-10T10:00:00', 500);

