drop table if exists locks;

create table if not exists locks
(
    resource_id string primary key not null,
    created_at timestamptz      not null default clock_timestamp(),
    owner      string                    default 'n/a'
);

-- begin; --T1
-- begin; --T2
-- insert into locks values ('100', clock_timestamp(), 'acme') on conflict do nothing;
-- select * from locks where resource_id='100' for update; --T1
-- select * from locks where resource_id='100' for update; --T2 blocks
-- commit; --T1, unlocks T2
-- commit; -- T2

