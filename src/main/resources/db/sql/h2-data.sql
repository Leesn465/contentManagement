-- TODO data
-- example
insert into users (id, username, password, role, created_date, last_modified_date)
values (1, 'admin', '$2a$10$3yLHXOXucI6d.7LHYDEl7OUFg5ubr0QTJHAY06hZPMChXZBx8QduC', 'ADMIN', now(), now());

-- contents 나중
insert into contents (title, description, view_count, created_date, created_by, last_modified_date, last_modified_by, author_id)
values ('테스트 콘텐츠', '설명입니다.', 0, now(), 'admin', now(), 'admin', 1);