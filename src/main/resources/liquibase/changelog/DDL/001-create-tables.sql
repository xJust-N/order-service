--liquibase formatted sql

--changeset Just_N:001-create-tables
create table orders (
    id uuid primary key,
    customer_name varchar(200) not null,
    order_date timestamp not null,
    status varchar(20) not null
);

create table order_items (
    id bigserial primary key,
    order_id uuid not null,
    product_name varchar(200) not null,
    quantity integer not null,
    price numeric(10, 2) not null,
    constraint fk_order_items_order foreign key (order_id)
        references orders (id) on delete cascade
);

