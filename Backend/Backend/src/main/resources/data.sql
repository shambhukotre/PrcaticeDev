DROP TABLE IF EXISTS app_user;
-- Create table if not exists for app_user
CREATE TABLE app_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

-- Existing inserts
INSERT INTO app_user (name, email) VALUES ('Shambhu', 'shambhu@test.com');
INSERT INTO app_user (name, email) VALUES ('Rahul', 'rahul@gmail.com');
INSERT INTO app_user (name, email) VALUES ('Suresh', 'Suresh@gmail.com');
INSERT INTO app_user (name, email) VALUES ('Ramesh', 'Ramesh@gmail.com');
INSERT INTO app_user (name, email) VALUES ('Amit', 'amit@gmail.com');
INSERT INTO app_user (name, email) VALUES ('Priya', 'priya@gmail.com');
INSERT INTO app_user (name, email) VALUES ('Vikram', 'vikram@gmail.com');
INSERT INTO app_user (name, email) VALUES ('Neha', 'neha@gmail.com');
INSERT INTO app_user (name, email) VALUES ('Rajesh', 'rajesh@gmail.com');
INSERT INTO app_user (name, email) VALUES ('Anjali', 'anjali@gmail.com');
INSERT INTO app_user (name, email) VALUES ('Sunil', 'sunil@gmail.com');
INSERT INTO app_user (name, email) VALUES ('Pooja', 'pooja@test.com');
INSERT INTO app_user (name, email) VALUES ('Manish', 'manish@gmail.com');
INSERT INTO app_user (name, email) VALUES ('Kavita', 'kavita@gmail.com');
INSERT INTO app_user (name, email) VALUES ('Sanjay', 'sanjay@gmail.com');
INSERT INTO app_user (name, email) VALUES ('Ritu', 'Ritu@yaho.com');
