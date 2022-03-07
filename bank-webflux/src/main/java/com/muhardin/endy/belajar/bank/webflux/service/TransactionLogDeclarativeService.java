package com.muhardin.endy.belajar.bank.webflux.service;

import com.muhardin.endy.belajar.bank.webflux.dao.TransactionLogDao;
import com.muhardin.endy.belajar.bank.webflux.entity.ActivityStatus;
import com.muhardin.endy.belajar.bank.webflux.entity.TransactionLog;
import com.muhardin.endy.belajar.bank.webflux.entity.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class TransactionLogDeclarativeService implements TransactionLogService {

    @Autowired private TransactionLogDao transactionLogDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Mono<Void> log(TransactionType transactionType, ActivityStatus activityStatus, String remarks){
        TransactionLog txLog = new TransactionLog();
        txLog.setTransactionType(transactionType);
        txLog.setActivityStatus(activityStatus);
        txLog.setRemarks(remarks);
        return transactionLogDao.save(txLog).then();
    }
}
