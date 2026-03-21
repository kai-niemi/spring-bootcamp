--
-- Business txn
--

begin;
-- gen_random_uuid()
insert into transfer (id) values ( '00000000-0000-0000-0000-000000000000') returning id,booking_date;
insert into transfer_item (transfer_id, account_id, amount, running_balance)
values ('00000000-0000-0000-0000-000000000000', '10000000-0000-0000-0000-000000000000', 75.00,
        (select balance + 75.00 from account where id = '10000000-0000-0000-0000-000000000000'));
insert into transfer_item (transfer_id, account_id, amount, running_balance)
values ('00000000-0000-0000-0000-000000000000', '20000000-0000-0000-0000-000000000000', -75.00,
        (select balance - 75.00 from account where id = '20000000-0000-0000-0000-000000000000'));
update account set balance = balance + 75.00 where id = '10000000-0000-0000-0000-000000000000';
update account set balance = balance - 75.00 where id = '20000000-0000-0000-0000-000000000000';
commit;

-- select * from account;
-- select * from transfer;
-- select * from transfer_item;

-- truncate table transfer_item cascade ;
-- truncate table transfer cascade ;

--
-- Modifying CTE
--

with head as (
    insert into transfer (id) values (gen_random_uuid())
        returning id,booking_date),
     item1 as (
         insert into transfer_item (transfer_id, account_id, amount, running_balance)
             values ((select id from head),
                     '10000000-0000-0000-0000-000000000000',
                     75.00,
                     (select balance + 75.00 from account where id = '10000000-0000-0000-0000-000000000000'))
             returning transfer_id),
     item2 as (
         insert into transfer_item (transfer_id, account_id, amount, running_balance)
             values ((select id from head),
                     '20000000-0000-0000-0000-000000000000',
                     -75.00,
                     (select balance - 75.00 from account where id = '20000000-0000-0000-0000-000000000000'))
             returning transfer_id)
update account
set balance=account.balance + dt.balance
from (select unnest(array [75, -75])                                                                                   as balance,
             unnest(array ['10000000-0000-0000-0000-000000000000'::uuid,'20000000-0000-0000-0000-000000000000'::uuid]) as id) as dt
where account.id = dt.id
returning account.id, account.balance;