create table if not exists product_variation
(
    id          uuid           not null default gen_random_uuid(),
    product_id  uuid           not null,
    inventory   int            not null,
    name        string         not null,
    description jsonb          null,
    price       numeric(19, 2) null,
    sku         string         not null,
    country     varchar(128)   not null,

    primary key (id)
);

create index on product_variation (country) storing (inventory,name,price);

alter table if exists product_variation
    add constraint if not exists fk_product_variation_ref_product
    foreign key (product_id)
    references product;

-- From a performance standpoint there should be one outbox table per aggregate that
-- will reduce the number of change feeds per range. In this demo however, there's
-- only one outbox table with a discriminator column for the aggregate type.

create table if not exists outbox
(
    aggregate_id   uuid as ((payload ->> 'id')::UUID) stored,
    aggregate_type varchar(32) not null,
    payload        jsonb       not null,

    primary key (aggregate_id)
);

alter table outbox set (ttl_expire_after = '1 hour');

create table if not exists inbox
(
    aggregate_id   uuid as ((payload ->> 'id')::UUID) stored,
    aggregate_type varchar(32) not null,
    payload        jsonb       not null,

    primary key (aggregate_id)
);

alter table inbox set (ttl_expire_after = '1 hour');

set cluster setting kv.rangefeed.enabled = true;

create changefeed into '${cdc-sink-url}?topic_name=orders-inbox'
with diff as
         select aggregate_id   as aggregate_id,
                aggregate_type as aggregate_type,
                event_op()     as event_type,
                payload
         from inbox
         where event_op() != 'delete'
           and aggregate_type = 'purchase_order';

create changefeed into '${cdc-sink-url}?topic_name=orders-outbox'
with diff as
         select aggregate_id   as aggregate_id,
                aggregate_type as aggregate_type,
                event_op()     as event_type,
                payload
         from outbox
         where event_op() != 'delete'
           and aggregate_type = 'purchase_order';
