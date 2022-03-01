package com.muhardin.endy.belajar.bankwebmvc.service;

import com.muhardin.endy.belajar.bankwebmvc.dao.RunningNumberDao;
import com.muhardin.endy.belajar.bankwebmvc.entity.TransactionType;
import com.muhardin.endy.belajar.bankwebmvc.entity.RunningNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service @Transactional
public class RunningNumberService {

    @Autowired private RunningNumberDao runningNumberDao;

    public Long generateNumber(TransactionType transactionType){
        LocalDate startOfNumber = LocalDate.now().withDayOfMonth(1);
        RunningNumber runningNumber = runningNumberDao.findByTransactionTypeAndResetPeriod(transactionType, startOfNumber);
        if (runningNumber == null) {
            runningNumber = new RunningNumber();
            runningNumber.setLastNumber(0L);
            runningNumber.setResetPeriod(startOfNumber);
            runningNumber.setTransactionType(transactionType);
        }

        runningNumber.setLastNumber(runningNumber.getLastNumber() + 1);
        runningNumberDao.save(runningNumber);
        return runningNumber.getLastNumber();
    }
}
