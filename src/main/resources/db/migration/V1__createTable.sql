DROP TABLE IF EXISTS link_to_be_process;
DROP TABLE IF EXISTS link_already_process;
DROP TABLE IF EXISTS news;

CREATE TABLE link_to_be_process(link VARCHAR(255));
CREATE TABLE link_already_process(link VARCHAR(255));
CREATE TABLE news(id int(11) PRIMARY KEY auto_increment,title text , content text , url VARCHAR(100) , create_time timestamp , update_time timestamp );
