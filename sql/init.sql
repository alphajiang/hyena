create database score;

CREATE USER 'test'@'%' IDENTIFIED BY 'some_pass';
GRANT ALL PRIVILEGES ON score.* TO 'test'@'%' WITH GRANT OPTION;