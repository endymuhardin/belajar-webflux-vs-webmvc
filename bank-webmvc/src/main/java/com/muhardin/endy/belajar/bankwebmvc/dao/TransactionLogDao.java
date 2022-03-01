package com.muhardin.endy.belajar.bankwebmvc.dao;

import com.muhardin.endy.belajar.bankwebmvc.entity.TransactionLog;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TransactionLogDao extends PagingAndSortingRepository<TransactionLog, String> {
}
