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
import java.util.function.BiFunction;
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

        Mono<Void> processTransfer = Mono.usingWhen(
                reference,
                transfer(sourceAccount, destinationAccount, amount),
                successLog(sourceAccountNumber, destinationAccountNumber, amount, transactionLogService),
                errorLog(sourceAccountNumber, destinationAccountNumber, amount, transactionLogService),
                x -> Mono.error(new IllegalStateException("Transfer cancelled")));

        return startLog.then(processTransfer);
    }

    private Function<String, Mono<Void>> successLog(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount, TransactionLogService transactionLogService) {
        return ref -> transactionLogService.log(TransactionType.TRANSFER, ActivityStatus.SUCCESS,
                transferRemarks(sourceAccountNumber, destinationAccountNumber, amount) + " - ["+ref+"]");
    }

    private BiFunction<String, Throwable, Mono<Void>> errorLog(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount, TransactionLogService transactionLogService) {
        return (d,e) -> transactionLogService.log(TransactionType.TRANSFER, ActivityStatus.FAILED,
                transferRemarks(sourceAccountNumber, destinationAccountNumber, amount) + " - [" + e.getMessage() + "]");
    }

    private Function<String, Mono<Void>> transfer(Mono<Account> sourceAccount, Mono<Account> destinationAccount, BigDecimal amount) {
        return transactionReference -> Mono.zip(sourceAccount, destinationAccount)
                .flatMapMany(tuple2 -> {
                    Account src = tuple2.getT1();
                    Account dst = tuple2.getT2();
                    String remarks = transferRemarks(src.getAccountNumber(), dst.getAccountNumber(), amount);

                    src.setBalance(src.getBalance().subtract(amount));
                    dst.setBalance(dst.getBalance().add(amount));
                    log.debug("Transfer running on thread {}", Thread.currentThread().getName());

                    return accountDao.save(src)
                        .then(accountDao.save(dst))
                        .thenMany(
                            Flux.concat(
                                    saveTransactionHistory(src, remarks, amount.negate(), transactionReference),
                                    saveTransactionHistory(dst, remarks, amount, transactionReference))
                        );
                })
                .flatMap(transactionHistoryDao::save).then();
    }

    private Function<Account, Mono<Account>> validateAccount(BigDecimal amount) {
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
