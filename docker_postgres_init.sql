create extension if not exists pgcrypto;

drop table if exists transaction_history;
drop table if exists account;
drop table if exists running_number;

create table account (
    id varchar(36) DEFAULT gen_random_uuid(),
    account_number varchar(50) not null,
    name varchar(100) not null,
    balance decimal(19,2) not null,
    active boolean not null,
    primary key (id), 
    unique(account_number)
);

create table transaction_history (
    id varchar(36) DEFAULT gen_random_uuid(),
    id_account varchar(36) not null,
    transaction_type varchar(100) not null,
    remarks varchar(255) not null,
    amount decimal(19,2) not null,
    transaction_time timestamp not null,
    reference varchar(100) not null,
    primary key (id),
    foreign key (id_account) references account,
    unique(reference)
);

create table running_number (
    id varchar(36) DEFAULT gen_random_uuid(),
    transaction_type varchar(100) not null,
    reset_period date not null,
    last_number bigint not null,
    primary key (id)
);

create table transaction_log (
    id varchar(36) DEFAULT gen_random_uuid(),   
    transaction_type varchar(100) not null,
    activity_status varchar(100) not null,
    activity_time timestamp not null,
    remarks varchar(255) not null,
    primary key (id)
);

insert into account (id, account_number, name, balance, active)
values ('n001', 'C-001', 'Customer 001', 1000000, true);

insert into account (id, account_number, name, balance, active)
values ('n002', 'C-002', 'Customer 002', 2000000, true);

insert into account (id, account_number, name, balance, active)
values ('n003', 'C-003', 'Customer 003', 3000000, false);