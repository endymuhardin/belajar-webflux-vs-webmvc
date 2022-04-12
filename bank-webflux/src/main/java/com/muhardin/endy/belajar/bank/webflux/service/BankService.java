package com.muhardin.endy.belajar.bank.webflux.service;

import com.muhardin.endy.belajar.bank.webflux.dao.TransactionHistoryDao;
import com.muhardin.endy.belajar.bank.webflux.dao.AccountDao;
import com.muhardin.endy.belajar.bank.webflux.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.function.Function;

@Service @Slf4j
public class BankService {

    @Autowired private AccountDao accountDao;
    @Autowired private TransactionHistoryDao transactionHistoryDao;
    @Autowired private RunningNumberService runningNumberService;
    @Autowired private TransactionLogProgrammaticService transactionLogProgrammaticService;
    @Autowired private TransactionLogDeclarativeService transactionLogDeclarativeService;

    @Transactional
    public Mono<Void> transferProgrammatically(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount) {
        return transfer(sourceAccountNumber, destinationAccountNumber, amount, transactionLogProgrammaticService);
    }

    @Transactional
    public Mono<Void> transferDeclaratively(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount) {
        return transfer(sourceAccountNumber, destinationAccountNumber, amount, transactionLogDeclarativeService);
    }

    private Mono<Void> transfer(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount, TransactionLogService transactionLogService){
        Mono<Void> startLog = transactionLogService.log(TransactionType.TRANSFER, ActivityStatus.START,
                transferRemarks(sourceAccountNumber, destinationAccountNumber, amount));

        Mono<String> reference = runningNumberService.generateNumber(TransactionType.TRANSFER)
                .map(number -> TransactionType.TRANSFER.name() + "-" + String.format("%05d",number));

        // https://stackoverflow.com/a/53596358 : how to validate
        Mono<Account> sourceAccount = accountDao
                .findByAccountNumber(sourceAccountNumber)
                .flatMap(validateAccount(amount));

        Mono<Account> destinationAccount = accountDao
                .findByAccountNumber(destinationAccountNumber)
                .flatMap(validateAccount(amount));

        Mono<Void> processTransfer = Mono.zip(sourceAccount, destinationAccount, reference)
            .flatMapMany(tuple3 -> {
                Account src = tuple3.getT1();
                Account dst = tuple3.getT2();
                String ref = tuple3.getT3();
                String remarks = transferRemarks(sourceAccountNumber, destinationAccountNumber, amount);

                src.setBalance(src.getBalance().subtract(amount));
                dst.setBalance(dst.getBalance().add(amount));
                log.debug("Transfer running on thread {}", Thread.currentThread().getName());

                return
                    accountDao.save(src)
                    .then(accountDao.save(dst))
                    .thenMany(
                        Flux.concat(
                        saveTransactionHistory(src, remarks, amount.negate(), ref),
                        saveTransactionHistory(dst, remarks, amount, ref))
                    );
            })
            .flatMap(transactionHistoryDao::save).then();

        Mono<Void> successLog = transactionLogService.log(TransactionType.TRANSFER, ActivityStatus.SUCCESS,
                transferRemarks(sourceAccountNumber, destinationAccountNumber, amount));

        Mono<Void> transferHandleError = Mono.usingWhen(
                Mono.just("run"),
                x -> processTransfer,
                x -> successLog,
                (d,e) -> transactionLogService.log(TransactionType.TRANSFER, ActivityStatus.FAILED,
                        transferRemarks(sourceAccountNumber, destinationAccountNumber, amount) + " - [" + e.getMessage() + "]"),
                x -> Mono.error(new IllegalStateException("Transfer cancelled")));

        return startLog.then(transferHandleError);
    }

    private Function<Account, Mono<? extends Account>> validateAccount(BigDecimal amount) {
        return r -> {
            if(insufficientBalance(r, amount)) {
                return Mono.error(new IllegalStateException("Insufficient balance"));
            } else if(!r.getActive()) {
                return Mono.error(new IllegalStateException("Inactive account"));
            }
            return Mono.just(r);
        };
    }

    private String transferRemarks(String src, String dst, BigDecimal amount) {
        return "Transfer "+src+" -> "+dst+ " ["+amount+"]";
    }

    private boolean insufficientBalance(Account source, BigDecimal nilai) {
        return nilai.compareTo(source.getBalance()) > 0;
    }

    private Mono<TransactionHistory> saveTransactionHistory(Account account, String remarks, BigDecimal amount, String reference) {
        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setAccount(account);
        transactionHistory.setIdAccount(account.getId());
        transactionHistory.setTransactionType(TransactionType.TRANSFER);
        transactionHistory.setRemarks(remarks);
        transactionHistory.setAmount(amount);
        transactionHistory.setReference(reference+"-"+ account.getAccountNumber());
        return transactionHistoryDao.save(transactionHistory);
    }
}
