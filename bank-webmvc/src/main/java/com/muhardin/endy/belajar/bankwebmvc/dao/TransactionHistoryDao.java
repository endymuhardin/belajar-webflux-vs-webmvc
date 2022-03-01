package com.muhardin.endy.belajar.bankwebmvc.dao;

import com.muhardin.endy.belajar.bankwebmvc.entity.TransactionHistory;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TransactionHistoryDao extends PagingAndSortingRepository<TransactionHistory, String> {
}
