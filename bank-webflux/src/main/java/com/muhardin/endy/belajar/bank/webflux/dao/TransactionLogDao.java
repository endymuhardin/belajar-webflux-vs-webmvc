package com.muhardin.endy.belajar.bank.webflux.dao;

import com.muhardin.endy.belajar.bank.webflux.entity.TransactionLog;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TransactionLogDao extends ReactiveCrudRepository<TransactionLog, String> {
}
