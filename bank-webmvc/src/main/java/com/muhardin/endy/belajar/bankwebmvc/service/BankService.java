package com.muhardin.endy.belajar.bankwebmvc.service;

import com.muhardin.endy.belajar.bankwebmvc.dao.TransactionHistoryDao;
import com.muhardin.endy.belajar.bankwebmvc.dao.AccountDao;
import com.muhardin.endy.belajar.bankwebmvc.entity.Account;
import com.muhardin.endy.belajar.bankwebmvc.entity.TransactionType;
import com.muhardin.endy.belajar.bankwebmvc.entity.TransactionHistory;
import com.muhardin.endy.belajar.bankwebmvc.entity.ActivityStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service @Transactional
public class BankService {

    @Autowired private AccountDao accountDao;
    @Autowired private TransactionHistoryDao transactionHistoryDao;

    @Autowired private RunningNumberService runningNumberService;
    @Autowired private TransactionLogService transactionLogService;

    public void transfer(String src, String dst, BigDecimal amount){

        transactionLogService.log(TransactionType.TRANSFER, ActivityStatus.START,
                transactionLog(src, dst, amount));

        String reference = TransactionType.TRANSFER.name()
                + "-" + String.format("%05d",
                runningNumberService.generateNumber(TransactionType.TRANSFER));

        Account source = accountDao.findByAccountNumber(src);
        Account destination = accountDao.findByAccountNumber(dst);

        if(accountIsInactive(source, destination)) {
            transactionLogService.log(TransactionType.TRANSFER, ActivityStatus.FAILED,
                    transactionLog(src, dst, amount) +" - [INACTIVE ACCOUNT]");
            throw new IllegalArgumentException("Inactive account");
        }

        if (balanceIsNotSufficient(source, amount)) {
            transactionLogService.log(TransactionType.TRANSFER, ActivityStatus.FAILED,
                    transactionLog(src, dst, amount) +" - [INSUFFICIENT BALANCE]");
            throw new IllegalStateException("Insufficient balance");
        }

        updateAccountBalance(source, destination, amount);
        saveTransactionHistory(source, destination, amount, reference);

        transactionLogService.log(TransactionType.TRANSFER, ActivityStatus.SUCCESS,
                transactionLog(src, dst, amount));
    }

    private String transactionLog(String src, String dest, BigDecimal amount) {
        return "Transfer "+src+" -> "+dest+ " ["+amount+"]";
    }

    private void saveTransactionHistory(Account source, Account destination, BigDecimal amount, String reference) {
        String remarks = transferRemarks(amount, source, destination);
        saveTransactionHistory(source, remarks, amount.negate(), reference);
        saveTransactionHistory(destination, remarks, amount, reference);
    }

    private void updateAccountBalance(Account source, Account destination, BigDecimal amount) {
        source.setBalance(source.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));
        accountDao.save(source);
        accountDao.save(destination);
    }

    private void saveTransactionHistory(Account account, String remarks, BigDecimal amount, String reference) {
        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setAccount(account);
        transactionHistory.setTransactionType(TransactionType.TRANSFER);
        transactionHistory.setRemarks(remarks);
        transactionHistory.setAmount(amount);
        transactionHistory.setReference(reference+"-"+ account.getAccountNumber());
        transactionHistoryDao.save(transactionHistory);
    }

    private String transferRemarks(BigDecimal amount, Account source, Account destination) {
        return "Transfer from "+ source.getAccountNumber() + " to "+ destination.getAccountNumber()+" for "+ amount;
    }

    private boolean balanceIsNotSufficient(Account source, BigDecimal amount) {
        return amount.compareTo(source.getBalance()) > 0;
    }

    private boolean accountIsInactive(Account source, Account destination) {
        return !(source.getActive() && destination.getActive());
    }
}
