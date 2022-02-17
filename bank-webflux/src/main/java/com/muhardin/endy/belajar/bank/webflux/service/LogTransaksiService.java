package com.muhardin.endy.belajar.bank.webflux.service;

import com.muhardin.endy.belajar.bank.webflux.entity.JenisTransaksi;
import com.muhardin.endy.belajar.bank.webflux.entity.StatusAktivitas;
import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Service @Slf4j
public class LogTransaksiService {

    private static final String SQL_INSERT = "insert into log_transaksi" +
            "(jenis_transaksi, status_aktivitas, waktu_aktivitas, keterangan) " +
            "values (:jenis, :status, :waktu, :keterangan)";

    @Autowired
    private ConnectionFactory connectionFactory;
    private DatabaseClient databaseClient;
    private ReactiveTransactionManager transactionManager;

    @PostConstruct
    public void initialize() {
        transactionManager = new R2dbcTransactionManager(connectionFactory);
        databaseClient = DatabaseClient.create(connectionFactory);
    }

    public Mono<Void> catat(JenisTransaksi jenisTransaksi, StatusAktivitas statusAktivitas, String keterangan){
        TransactionalOperator rxtx = TransactionalOperator.create(transactionManager);
        return rxtx.execute(txStatus -> databaseClient.sql(SQL_INSERT)
                .bind("jenis", jenisTransaksi.name())
                .bind("status", statusAktivitas.name())
                .bind("waktu", LocalDateTime.now())
                .bind("keterangan", keterangan)
                .then()).then();
    }
}
