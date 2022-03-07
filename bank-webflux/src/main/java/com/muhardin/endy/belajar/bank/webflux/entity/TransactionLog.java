package com.muhardin.endy.belajar.bank.webflux.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
public class TransactionLog {
    @Id private String id;
    private TransactionType transactionType;
    private ActivityStatus activityStatus;
    private LocalDateTime activityTime = LocalDateTime.now();
    private String remarks;
}
