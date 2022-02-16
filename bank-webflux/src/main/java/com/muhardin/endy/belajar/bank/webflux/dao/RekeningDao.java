package com.muhardin.endy.belajar.bank.webflux.dao;

import com.muhardin.endy.belajar.bank.webflux.entity.Rekening;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RekeningDao extends ReactiveCrudRepository<Rekening, String> {
    Mono<Rekening> findByNomor(String nomor);
}
