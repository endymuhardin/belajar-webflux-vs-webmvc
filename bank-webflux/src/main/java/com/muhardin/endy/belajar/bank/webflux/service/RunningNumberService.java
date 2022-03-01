package com.muhardin.endy.belajar.bank.webflux.service;

import com.muhardin.endy.belajar.bank.webflux.dao.RunningNumberDao;
import com.muhardin.endy.belajar.bank.webflux.entity.TransactionType;
import com.muhardin.endy.belajar.bank.webflux.entity.RunningNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service @Transactional
public class RunningNumberService {
    @Autowired
    private RunningNumberDao runningNumberDao;

    public Mono<Long> generateNumber(TransactionType transactionType){
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        return runningNumberDao.findByTransactionTypeAndResetPeriod(transactionType, startOfMonth)
                .defaultIfEmpty(createRunningNumber(transactionType, startOfMonth))
                .map(r -> {
                    r.setLastNumber(r.getLastNumber() + 1);
                    return r;
                })
                .flatMap(runningNumberDao::save)
                .map(r -> r.getLastNumber());
    }

    private RunningNumber createRunningNumber(TransactionType transactionType, LocalDate resetPeriod){
        RunningNumber runningNumber = new RunningNumber();
        runningNumber.setLastNumber(0L);
        runningNumber.setResetPeriod(resetPeriod);
        runningNumber.setTransactionType(transactionType);
        return runningNumber;
    }
}
