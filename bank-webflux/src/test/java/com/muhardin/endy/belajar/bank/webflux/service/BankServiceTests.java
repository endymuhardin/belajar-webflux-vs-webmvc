package com.muhardin.endy.belajar.bank.webflux.service;

import com.muhardin.endy.belajar.bank.webflux.dao.AccountDao;
import com.muhardin.endy.belajar.bank.webflux.dao.RunningNumberDao;
import com.muhardin.endy.belajar.bank.webflux.dao.TransactionHistoryDao;
import com.muhardin.endy.belajar.bank.webflux.dao.TransactionLogDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@SpringBootTest
public class BankServiceTests {

    @Autowired private RunningNumberDao runningNumberDao;
    @Autowired private TransactionHistoryDao transactionHistoryDao;
    @Autowired private TransactionLogDao transactionLogDao;
    @Autowired private AccountDao accountDao;

    @Autowired private BankService bankService;
    @Autowired private R2dbcEntityTemplate entityTemplate;
    @Value("classpath:/sql/reset-data.sql") private Resource resetDatabaseScript;

    @BeforeEach
    public void resetDatabase() throws Exception {
        this.entityTemplate
                .getDatabaseClient()
                .sql(StreamUtils.copyToString(
                        resetDatabaseScript.getInputStream(),
                        StandardCharsets.UTF_8))
                .then()
                .as(StepVerifier::create).verifyComplete();
    }

    @Test
    public void testTransferProgrammaticSuccess() {
        bankService.transferProgrammatically("C-001", "C-002", new BigDecimal(25000))
        .as(StepVerifier::create).verifyComplete();
        displayDatabaseContent();
    }

    @Test
    public void testTransferDeclarativeSuccess() {
        bankService.transferDeclaratively("C-001", "C-002", new BigDecimal(25000))
        .as(StepVerifier::create).verifyComplete();
        displayDatabaseContent();
    }

    // Exception cases expected result :
    // select * from running_number : should rollback
    // select * from transaction_history : should rollback
    // select * from account : should rollback
    // select * from transaction_log : *should not* rollback (error log inserted into database)
    // method outcome : throw Exception

    @Test
    public void testTransferInactiveAccount() {
        bankService.transferProgrammatically("C-001", "C-003", new BigDecimal(25000))
        .as(StepVerifier::create).verifyError();
        displayDatabaseContent();
    }

    @Test
    public void testTransferInsufficientBalance() {
        bankService.transferDeclaratively("C-001", "C-002", new BigDecimal(25000000))
        .as(StepVerifier::create).verifyError();
        displayDatabaseContent();
    }

    private void displayDatabaseContent() {
        displayAccount();
        displayTransactionHistory();
        displayRunningNumber();
        displayTransactionLog();
    }

    private void displayRunningNumber() {
        displayData("Running Number", runningNumberDao.findAll());
    }

    private void displayTransactionLog() {
        displayData("Transaction Log", transactionLogDao.findAll());
    }

    private void displayAccount() {
        displayData("Account", accountDao.findAll());
    }

    private void displayTransactionHistory() {
        displayData("Transaction History", transactionHistoryDao.findAll());
    }

    private void displayData(String dataName, Flux<? extends Object> data) {
        String startLog = "\r\n=== Start "+dataName+" ===";
        String endLog = "===  End "+dataName+"  ===\r\n";

        Mono.just(startLog)
            .doOnNext(System.out::println)
            .thenMany(data)
            .doOnNext(System.out::println)
            .doFinally(t -> System.out.println(endLog))
        .then().as(StepVerifier::create).verifyComplete();
    }
}
