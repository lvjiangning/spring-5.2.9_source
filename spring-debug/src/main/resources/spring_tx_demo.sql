CREATE DATABASE spring_tx_demo
CHARACTER SET UTF8;

-- 账号表
create table account(
                        id int not null,
                        balance int  not null,
                        username  varchar(20)
);


create table book(

                     id int not null,
                     price int
);

create table book_stock(
                           id int not null,
                           stock int,
                           book_id int
);


insert into account  values(1,2000,'张三');
insert into account  values(2,2000,'李四');


insert into book  values(1,49);
insert into book  values(2,59);

insert into book_stock  values(1,100,1);
insert into book_stock  values(2,100,1);








