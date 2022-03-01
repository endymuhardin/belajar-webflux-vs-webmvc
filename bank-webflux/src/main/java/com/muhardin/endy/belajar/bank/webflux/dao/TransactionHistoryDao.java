package com.muhardin.endy.belajar.bank.webflux.dao;

import com.muhardin.endy.belajar.bank.webflux.entity.TransactionHistory;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TransactionHistoryDao extends ReactiveCrudRepository<TransactionHistory, String> {
}
