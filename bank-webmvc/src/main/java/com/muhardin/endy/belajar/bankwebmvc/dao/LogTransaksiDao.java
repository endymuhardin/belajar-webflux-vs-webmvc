package com.muhardin.endy.belajar.bankwebmvc.dao;

import com.muhardin.endy.belajar.bankwebmvc.entity.LogTransaksi;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface LogTransaksiDao extends PagingAndSortingRepository<LogTransaksi, String> {
}
