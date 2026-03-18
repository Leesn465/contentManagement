-- TODO data
-- example
insert into users (username, password, role, created_date, last_modified_date)
values ('admin', '$2a$10$replace-with-bcrypt-password', 'ADMIN', now(), now());

insert into contents (title, description, view_count, created_date, created_by, last_modified_date, last_modified_by, author_id)
values ('테스트 콘텐츠', '설명입니다.', 0, now(), 'admin', now(), 'admin', 1);