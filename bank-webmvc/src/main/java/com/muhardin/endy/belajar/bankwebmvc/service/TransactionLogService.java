package com.muhardin.endy.belajar.bankwebmvc.service;

import com.muhardin.endy.belajar.bankwebmvc.dao.TransactionLogDao;
import com.muhardin.endy.belajar.bankwebmvc.entity.TransactionType;
import com.muhardin.endy.belajar.bankwebmvc.entity.TransactionLog;
import com.muhardin.endy.belajar.bankwebmvc.entity.ActivityStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionLogService {

    @Autowired private TransactionLogDao transactionLogDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(TransactionType transactionType, ActivityStatus activityStatus, String keterangan){
        TransactionLog transactionLog = new TransactionLog();
        transactionLog.setTransactionType(transactionType);
        transactionLog.setActivityStatus(activityStatus);
        transactionLog.setRemarks(keterangan);
        transactionLogDao.save(transactionLog);
    }
}
