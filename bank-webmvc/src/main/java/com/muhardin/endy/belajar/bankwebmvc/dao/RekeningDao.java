package com.muhardin.endy.belajar.bankwebmvc.dao;

import com.muhardin.endy.belajar.bankwebmvc.entity.Rekening;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RekeningDao extends PagingAndSortingRepository<Rekening, String> {
}
