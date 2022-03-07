package com.muhardin.endy.belajar.bank.webflux.service;

import com.muhardin.endy.belajar.bank.webflux.entity.ActivityStatus;
import com.muhardin.endy.belajar.bank.webflux.entity.TransactionType;
import reactor.core.publisher.Mono;

public interface TransactionLogService {
    Mono<Void> log(TransactionType transactionType, ActivityStatus activityStatus, String remarks);
}
