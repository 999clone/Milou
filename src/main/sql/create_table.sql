create table Users(
                      id INT primary key auto_increment,
                      name varchar(100) not null,
                      password varchar(100) not null,
                      email varchar(100) not null unique
);

create table Emails(
                       id INT primary key auto_increment,
                       code char(6) not null unique ,
                       sender_id int not null,
                       subject varchar(200) default 'no subject',
                       body TEXT,
                       date DATETIME not null,
                       foreign key (sender_id) references Users(id)
);

CREATE TABLE recipients (
                            id INT PRIMARY KEY AUTO_INCREMENT,
                            email_id INT NOT NULL,
                            recipients_id INT NOT NULL,
                            is_read BOOLEAN DEFAULT FALSE,
                            FOREIGN KEY (email_id) REFERENCES Emails(id),
                            FOREIGN KEY (recipients_id) REFERENCES Users(id)
);

ALTER TABLE recipients ADD COLUMN is_read BOOLEAN DEFAULT false;

select * from users;

DROP TABLE recipients;
DROP TABLE emails;
DROP TABLE users;



