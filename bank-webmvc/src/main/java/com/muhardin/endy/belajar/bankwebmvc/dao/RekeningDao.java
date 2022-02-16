package com.muhardin.endy.belajar.bankwebmvc.dao;

import com.muhardin.endy.belajar.bankwebmvc.entity.Rekening;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.persistence.LockModeType;

public interface RekeningDao extends PagingAndSortingRepository<Rekening, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Rekening findByNomor(String nomor);
}
