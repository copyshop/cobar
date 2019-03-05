#创建dbtest1
drop database if exists dbtest1;
create database dbtest1;
use dbtest1;

#在dbtest1上创建tb1
create table tb1(
  id int not null,
  gmt datetime
);

#创建dbtest2
drop database if exists dbtest2;
create database dbtest2;
use dbtest2;

#在dbtest2上创建tb2
create table tb2(
  id int not null,
  val varchar(256)
);

#创建dbtest3
drop database if exists dbtest3;
create database dbtest3;
use dbtest3;

#在dbtest3上创建tb2
create table tb2(
  id int not null,
  val varchar(256)
);