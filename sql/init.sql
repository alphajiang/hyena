create database p_hyena;

CREATE USER 'hyena'@'%' IDENTIFIED BY 'hyenapass';
GRANT ALL PRIVILEGES ON p_hyena.* TO 'hyena'@'%' WITH GRANT OPTION;