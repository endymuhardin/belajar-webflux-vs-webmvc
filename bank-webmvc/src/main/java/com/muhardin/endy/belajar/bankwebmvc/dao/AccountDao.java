package com.muhardin.endy.belajar.bankwebmvc.dao;

import com.muhardin.endy.belajar.bankwebmvc.entity.Account;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.persistence.LockModeType;

public interface AccountDao extends PagingAndSortingRepository<Account, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Account findByAccountNumber(String nomor);
}
