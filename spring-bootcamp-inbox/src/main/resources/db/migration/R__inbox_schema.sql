-- From a performance standpoint there should be one outbox table per aggregate that
-- will reduce the number of change feeds per range. In this demo however, there's
-- only one outbox table with a discriminator column for the aggregate type.

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

