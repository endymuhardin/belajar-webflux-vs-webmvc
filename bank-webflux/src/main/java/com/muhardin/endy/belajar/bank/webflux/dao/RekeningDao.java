package com.muhardin.endy.belajar.bank.webflux.dao;

import com.muhardin.endy.belajar.bank.webflux.entity.Rekening;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RekeningDao extends ReactiveCrudRepository<Rekening, String> {
    Rekening findByNomor(String nomor);
}
