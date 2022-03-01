package com.muhardin.endy.belajar.bank.webflux.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionHistory {
    @Id private String id;
    private TransactionType transactionType;

    @Transient
    private Account account;
    private String idAccount;
    private LocalDateTime transactionTime = LocalDateTime.now();
    private BigDecimal amount;
    private String remarks;
    private String reference;
}
