package com.muhardin.endy.belajar.bank.webflux.dao;

import com.muhardin.endy.belajar.bank.webflux.entity.LogTransaksi;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface LogTransaksiDao extends ReactiveCrudRepository<LogTransaksi, String> {
}
