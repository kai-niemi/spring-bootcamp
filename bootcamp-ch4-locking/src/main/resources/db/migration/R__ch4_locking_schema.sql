-- drop table if exists locks;

create table if not exists locks
(
    name       string primary key not null,
    owner      string             not null,
    created_at timestamptz        not null default clock_timestamp(),
    updated_at timestamptz        not null default clock_timestamp()
);

