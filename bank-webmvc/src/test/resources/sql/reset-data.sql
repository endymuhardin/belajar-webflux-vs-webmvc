update account set balance = 1000000 where id = 'n001';
update account set balance = 2000000 where id = 'n002';
update account set balance = 3000000 where id = 'n003';

delete from transaction_history;
delete from running_number;
delete from transaction_log;