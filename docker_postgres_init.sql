create extension if not exists pgcrypto;

drop table if exists mutasi;
drop table if exists rekening;
drop table if exists running_number;

create table rekening (
    id varchar(36) DEFAULT gen_random_uuid(),
    nomor varchar(50) not null,
    nama varchar(100) not null,
    saldo decimal(19,2) not null,
    aktif boolean not null,
    primary key (id), 
    unique(nomor)
);

create table mutasi (
    id varchar(36) DEFAULT gen_random_uuid(),
    id_rekening varchar(36) not null,
    jenis_transaksi varchar(100) not null,
    keterangan varchar(255) not null,
    nilai decimal(19,2) not null,
    waktu_transaksi timestamp not null,
    referensi varchar(100) not null,
    primary key (id),
    foreign key (id_rekening) references rekening,
    unique(referensi)
);

create table running_number (
    id varchar(36) DEFAULT gen_random_uuid(),
    jenis_transaksi varchar(100) not null,
    reset_period date not null,
    angka_terakhir bigint not null,
    primary key (id)
);

create table log_transaksi (
    id varchar(36) DEFAULT gen_random_uuid(),   
    jenis_transaksi varchar(100) not null,
    status_aktivitas varchar(100) not null,
    waktu_aktivitas timestamp not null,
    keterangan varchar(255) not null,
    primary key (id)
);

insert into rekening (id, nomor, nama, saldo, aktif)
values ('n001', 'N-001', 'Nasabah 001', 1000000, true);

insert into rekening (id, nomor, nama, saldo, aktif)
values ('n002', 'N-002', 'Nasabah 002', 2000000, true);

insert into rekening (id, nomor, nama, saldo, aktif)
values ('n003', 'N-003', 'Nasabah 003', 3000000, false);