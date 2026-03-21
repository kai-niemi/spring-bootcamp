begin; --T1
begin; --T2
insert into locks values ('100', 'one') on conflict (name)
    do update set owner='one', updated_at=clock_timestamp() returning owner; -- T1
insert into locks values ('100', 'two') on conflict (name)
    do update set owner='two', updated_at=clock_timestamp() returning owner; -- T2, blocks on T1
commit; --T1, unlocks T2
commit; --T2
select * from locks where name='100'; --T2, two
