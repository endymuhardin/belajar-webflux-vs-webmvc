package com.muhardin.endy.belajar.bank.webflux.service;

import com.muhardin.endy.belajar.bank.webflux.entity.TransactionType;
import com.muhardin.endy.belajar.bank.webflux.entity.ActivityStatus;
import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Service @Slf4j
public class TransactionLogService {

    private static final String SQL_INSERT = "insert into transaction_log" +
            "(transaction_type, activity_status, activity_time, remarks) " +
            "values (:type, :status, :time, :remarks)";

    @Autowired
    private ConnectionFactory connectionFactory;
    private DatabaseClient databaseClient;
    private ReactiveTransactionManager transactionManager;

    @PostConstruct
    public void initialize() {
        transactionManager = new R2dbcTransactionManager(connectionFactory);
        databaseClient = DatabaseClient.create(connectionFactory);
    }

    public Mono<Void> log(TransactionType transactionType, ActivityStatus activityStatus, String remarks){
        DefaultTransactionDefinition tdf = new DefaultTransactionDefinition();
        tdf.setName(this.getClass().getName());
        tdf.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        TransactionalOperator rxtx = TransactionalOperator.create(transactionManager, tdf);
        log.debug("Transaction log running on thread {}", Thread.currentThread().getName());
        return databaseClient.sql(SQL_INSERT)
                .bind("type", transactionType.name())
                .bind("status", activityStatus.name())
                .bind("time", LocalDateTime.now())
                .bind("remarks", remarks)
                .then().as(rxtx::transactional);
    }
}
