package com.muhardin.endy.belajar.bankwebmvc.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Data
public class TransactionHistory {
    @Id
    @GeneratedValue(generator = "uuid2") @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @ManyToOne @JoinColumn(name = "id_account")
    private Account account;
    private LocalDateTime transactionTime = LocalDateTime.now();
    private BigDecimal amount;
    private String remarks;
    private String reference;
}
