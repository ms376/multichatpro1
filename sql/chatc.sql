alter session set "_ORACLE_SCRIPT"=true;

drop table chatuser;
drop table head;
create table blackword;
create table chatuser(
    id varchar2(50) not null primary key,
    pw varchar2(50) not null,
    utype varchar2(50),
    blackword varchar2(50),
    blocked_users varchar2(50)
    );
insert into chatuser (id,pw,utype,name)
    values ('gogo','123', '0','고고');

insert into chatuser (id,pw,utype,name)
    values ('gogo1','123', '1','고고1');

insert into chatuser (id,pw,utype,name)
    values ('gogo2','123', '1','gogo2');

insert into chatuser (id,pw,utype,name)
    values ('head','123', '0','헤드');

select count(*) from chatuser where id= 'gogo';


CREATE TABLE chatuser (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    pw VARCHAR(50) NOT NULL,
    utype VARCHAR(50),
    blackword VARCHAR(50)
);

INSERT INTO chatuser (id, pw, utype)
VALUES ('gogo', '123', '0');

INSERT INTO chatuser (id, pw, utype)
VALUES ('gogo1', '123', '1');

INSERT INTO chatuser (id, pw, utype)
VALUES ('gogo2', '123', '1');

INSERT INTO chatuser (id, pw, utype)
VALUES ('head', '123', '0');


