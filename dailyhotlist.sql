create database dailyhotlist;

use dailyhotlist;
show tables;

create table user
(
    id           varchar(100) primary key not null,
    username     varchar(100) unique      not null,
    password     varchar(100)             not null,
    account_type varchar(100)             not null
) character set utf8;

insert into user
values ('azRPNHVZMDA0dEphNTRu', 'YWRtaW4=', 'YWRtaW4=', '566h55CG5ZGY'),
       ('WWlKVDgwZFdqMFMwYnY4', '5byg5LiJ', 'MTIz', '5pmu6YCa55So5oi3');

select *
from user;

create table hotlist
(
    id           int(100) primary key auto_increment not null,
    hotlist_name varchar(100) unique                 not null,
    hotlist_url  varchar(3000)                       not null
) character set utf8;

select *
from hotlist;

create table hotlist_data
(
    id                     int(100) primary key auto_increment not null,
    hotlist_data_id        varchar(100)                        not null,
    hotlist_data_url       varchar(3000)                       not null,
    hotlist_data_title     varchar(5000)                       not null,
    hotlist_data_sub_title varchar(10000)                      not null default '',
    hotlist_data_heat      varchar(100)                        not null default '',
    hotlist_data_image_url varchar(3000)                       not null default '',
    hotlist_name           varchar(100)                        not null,
    constraint fk_hotlist_name foreign key (hotlist_name) references hotlist (hotlist_name)
) character set utf8;

select *
from hotlist_data;

create table subscribe
(
    id           int(100) primary key auto_increment not null,
    hotlist_name varchar(100)                        not null,
    user_id      varchar(100)                        not null,
    constraint fk_hn foreign key (hotlist_name) references hotlist (hotlist_name),
    constraint fk_user_id foreign key (user_id) references user (id)
) character set utf8;

select *
from subscribe;

create table feedback
(
    id               int(100) primary key auto_increment not null,
    feedback_title   varchar(1000)                       not null,
    feedback_content varchar(10000)                      not null,
    user_id          varchar(100)                        not null,
    constraint fk_ui foreign key (user_id) references user (id)
) character set utf8;

select *
from feedback;