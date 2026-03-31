drop table ledger;

create table if not exists ledger
(
    id               uuid primary key not null default gen_random_uuid(),
    owner_id string           not null,
    balance          decimal(18, 2)   not null,
    created_at       timestamptz      not null default clock_timestamp()
);

create index on ledger(owner_id) storing (balance);

insert into ledger
values (gen_random_uuid(),'a', 100),
       (gen_random_uuid(),'a', 50),
       (gen_random_uuid(),'a', 100),
       (gen_random_uuid(),'a', 50);

-- Retry on conflict
begin; --T1
SELECT sum(balance) as balance FROM ledger WHERE owner_id = 'a'; --T1
begin; --T2
SELECT sum(balance) as balance FROM ledger WHERE owner_id = 'a'; --T2
INSERT INTO ledger (owner_id, balance) VALUES ('a', -10); --T1
INSERT INTO ledger (owner_id, balance) VALUES ('a', -10); --T2
commit; --T1
commit; --T2, ERROR: restart transaction: TransactionRetryWithProtoRefreshError: TransactionRetryError:

-- No retry with SFU (flipside: cant use agg. func)
begin; --T1
SELECT balance as balance FROM ledger WHERE owner_id = 'a' for update; --T1
-- app sum()
begin; --T2
SELECT balance as balance FROM ledger WHERE owner_id = 'a' for update; --T2, blocks on T1
-- app sum()
INSERT INTO ledger (owner_id, balance) VALUES ('a', -10); --T1
commit; --T1, unblocks T2
INSERT INTO ledger (owner_id, balance) VALUES ('a', -10); --T2
commit; --T2

-- No retry with mutex
begin; --T1
insert into locks (name,owner) values ('a', 'T1') on conflict (name)
    do update set owner='T1', updated_at=clock_timestamp() returning owner; -- T1
SELECT sum(balance) as balance FROM ledger WHERE owner_id = 'a'; --T1
begin; --T2
insert into locks (name,owner) values ('a', 'T1') on conflict (name)
    do update set owner='T1', updated_at=clock_timestamp() returning owner; -- T2, blocks on T1
INSERT INTO ledger (owner_id, balance) VALUES ('a', -10); --T1
commit; --T1, unblocks T2
SELECT sum(balance) as balance FROM ledger WHERE owner_id = 'a'; --T2
INSERT INTO ledger (owner_id, balance) VALUES ('a', -10); --T2
commit; --T2
