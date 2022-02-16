package com.muhardin.endy.belajar.bank.webflux.dao;

import com.muhardin.endy.belajar.bank.webflux.entity.Mutasi;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface MutasiDao extends ReactiveCrudRepository<Mutasi, String> {
}
