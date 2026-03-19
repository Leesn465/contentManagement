-- TODO schema
-- example
drop table if exists contents;
drop table if exists users;

create table users
(
    id                 bigint primary key auto_increment,
    username           varchar(50)  not null unique,
    password           varchar(255) not null,
    role               varchar(20)  not null,
    created_date       timestamp    not null,
    last_modified_date timestamp
);

create table contents
(
    id                 bigint primary key auto_increment,
    title              varchar(100) not null,
    description        text,
    view_count         bigint       not null,
    created_date       timestamp    not null,
    created_by         varchar(50)  not null,
    last_modified_date timestamp,
    last_modified_by   varchar(50),
    author_id          bigint       not null,
    locked             boolean      not null default false,
    constraint fk_contents_author
        foreign key (author_id) references users(id)
);

create index idx_contents_created_id_desc
    on contents (created_date desc, id desc);