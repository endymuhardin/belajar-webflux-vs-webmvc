package com.muhardin.endy.belajar.bank.webflux.dao;

import com.muhardin.endy.belajar.bank.webflux.entity.Account;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface AccountDao extends ReactiveCrudRepository<Account, String> {
    Mono<Account> findByAccountNumber(String accountNumber);
}
