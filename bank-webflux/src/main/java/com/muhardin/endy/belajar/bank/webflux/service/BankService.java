package com.muhardin.endy.belajar.bank.webflux.service;

import com.muhardin.endy.belajar.bank.webflux.dao.TransactionHistoryDao;
import com.muhardin.endy.belajar.bank.webflux.dao.AccountDao;
import com.muhardin.endy.belajar.bank.webflux.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.function.Function;

@Service @Transactional
public class BankService {

    @Autowired private AccountDao accountDao;
    @Autowired private TransactionHistoryDao transactionHistoryDao;
    @Autowired private RunningNumberService runningNumberService;
    @Autowired private TransactionLogService transactionLogService;

    public Mono<Void> transfer(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount){
        Mono<String> reference = transactionLogService.log(TransactionType.TRANSFER, ActivityStatus.START,
                        transferRemarks(sourceAccountNumber, destinationAccountNumber, amount))
                .then(runningNumberService.generateNumber(TransactionType.TRANSFER)
                .map(number -> TransactionType.TRANSFER.name() + "-" + String.format("%05d",number)));

        // https://stackoverflow.com/a/53596358 : validasi mono
        Mono<Account> sourceAccount = accountDao.findByAccountNumber(sourceAccountNumber)
                .flatMap(validateAccount(amount))
                .onErrorMap(logValidationError(sourceAccountNumber, destinationAccountNumber, amount));

        Mono<Account> destinationAccount = accountDao.findByAccountNumber(destinationAccountNumber)
                .flatMap(validateAccount(amount))
                .onErrorMap(logValidationError(sourceAccountNumber, destinationAccountNumber, amount));

        Mono<Void> successLog = transactionLogService.log(TransactionType.TRANSFER, ActivityStatus.SUCCESS,
                transferRemarks(sourceAccountNumber, destinationAccountNumber, amount));

        return
            Mono.zip(sourceAccount, destinationAccount, reference)
            .flatMapMany(tuple3 -> {
                Account src = tuple3.getT1();

                Account dst = tuple3.getT2();
                String ref = tuple3.getT3();
                String remarks = transferRemarks(sourceAccountNumber, destinationAccountNumber, amount);

                src.setBalance(src.getBalance().subtract(amount));
                dst.setBalance(dst.getBalance().add(amount));

                return
                    accountDao.save(src)
                    .then(accountDao.save(dst))
                    .thenMany(
                        Flux.concat(
                        saveTransactionHistory(src, remarks, amount.negate(), ref),
                        saveTransactionHistory(dst, remarks, amount, ref))
                    );
            })
            .flatMap(transactionHistoryDao::save).last().then(successLog).then();
    }

    private Function<Throwable, Throwable> logValidationError(String src, String dst, BigDecimal amount) {
        return e ->
            transactionLogService.log(TransactionType.TRANSFER, ActivityStatus.FAILED,
                            transferRemarks(src, dst, amount) +" - ["+e.getMessage()+"]")
                    .as(t -> e);
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
