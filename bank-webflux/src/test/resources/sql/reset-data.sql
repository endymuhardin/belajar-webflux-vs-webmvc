update rekening set saldo = 1000000 where id = 'n001';
update rekening set saldo = 2000000 where id = 'n002';
update rekening set saldo = 3000000 where id = 'n003';

delete from mutasi;
delete from running_number;
delete from log_transaksi;