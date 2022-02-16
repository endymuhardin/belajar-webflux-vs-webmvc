package com.muhardin.endy.belajar.bankwebmvc.dao;

import com.muhardin.endy.belajar.bankwebmvc.entity.Mutasi;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MutasiDao extends PagingAndSortingRepository<Mutasi, String> {
}
