--liquibase formatted sql

--changeset Just_N:002-create-indexes

create index idx_orders_status on orders (status);
create index idx_orders_customer_name on orders (customer_name);
create index idx_order_items_order_id on order_items (order_id);
