-- Schema
create table if not exists account
(
    id      uuid           not null default gen_random_uuid(),
    balance decimal(19, 2) not null,
    name    varchar(128)   not null,

    primary key (id)
);

create table if not exists transfer
(
    id           uuid not null default gen_random_uuid(),
    booking_date date not null default current_date(),

    primary key (id)
);

create table if not exists transfer_item
(
    transfer_id     uuid           not null,
    account_id      uuid           not null,
    amount          decimal(19, 2) not null,
    running_balance decimal(19, 2) not null,

    primary key (transfer_id, account_id)
);

alter table if exists transfer_item
    add constraint if not exists fk_region_ref_transfer
    foreign key (transfer_id) references transfer (id);

alter table if exists transfer_item
    add constraint if not exists fk_region_ref_account
    foreign key (account_id) references account (id);
