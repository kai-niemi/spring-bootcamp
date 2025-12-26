-- drop table if exists product_variation;

create table if not exists product_variation
(
    id          uuid           not null default gen_random_uuid(),
    product_id  uuid           null,
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
